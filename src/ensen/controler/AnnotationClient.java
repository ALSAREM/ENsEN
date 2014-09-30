/**
 * Copyright 2011 Pablo Mendes, Max Jakob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ensen.controler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;

import ensen.entities.EnsenDBpediaResource;


/**
 * @author pablomendes
 */
public abstract class AnnotationClient {

	public Logger LOG = Logger.getLogger(this.getClass());

	// Create an instance of HttpClient.
	private static HttpClient client = new HttpClient();

	public String request(HttpMethod method) throws AnnotationException {

		String response = null;

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
			// Execute the method.
			client.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				System.out.println("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			InputStream responseBodyStream = method.getResponseBodyAsStream(); //Going to buffer response body of large or unknown size. Using getResponseBodyAsStream instead is recommended.

			int b = responseBodyStream.read();
			ArrayList<Integer> bytes = new ArrayList<Integer>();
			while (b != -1) {
				bytes.add(b);
				b = responseBodyStream.read();
			}
			byte[] responseBody = new byte[bytes.size()];

			for (int i = 0; i < bytes.size(); i++) {
				responseBody[i] = bytes.get(i).byteValue();
			}
			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary data
			response = new String(responseBody);

		} catch (HttpException e) {
			System.out.println("Fatal protocol violation: " + e.getMessage());
			try {
				System.err.println(method.getURI());
			} catch (URIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new AnnotationException("Protocol error executing HTTP request.", e);
		} catch (IOException e) {
			System.out.println("Fatal transport error: " + e.getMessage());
			System.out.println(method.getQueryString());
			throw new AnnotationException("Transport error executing HTTP request.", e);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		return response;

	}

	protected static String readFileAsString(String filePath) throws java.io.IOException {
		return readFileAsString(new File(filePath));
	}

	protected static String readFileAsString(File file) throws IOException {
		byte[] buffer = new byte[(int) file.length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
		f.read(buffer);
		return new String(buffer);
	}

	static abstract class LineParser {

		public abstract String parse(String s) throws ParseException;

		static class ManualDatasetLineParser extends LineParser {
			public String parse(String s) throws ParseException {
				return s.trim();
			}
		}

		static class OccTSVLineParser extends LineParser {
			public String parse(String s) throws ParseException {
				String result = s;
				try {
					result = s.trim().split("\t")[3];
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new ParseException(e.getMessage(), 3);
				}
				return result;
			}
		}
	}

	public void saveExtractedEntitiesSet(File inputFile, File outputFile, LineParser parser, int restartFrom) throws Exception {
		PrintWriter out = new PrintWriter(outputFile);
		System.out.println("Opening input file " + inputFile.getAbsolutePath());
		String text = readFileAsString(inputFile);
		int i = 0;
		int correct = 0;
		int error = 0;
		int sum = 0;
		for (String snippet : text.split("\n")) {
			String s = parser.parse(snippet);
			if (s != null && !s.equals("")) {
				i++;

				if (i < restartFrom)
					continue;

				List<EnsenDBpediaResource> entities = new ArrayList<EnsenDBpediaResource>();
				try {
					final long startTime = System.nanoTime();
					entities = ensenExtract(new Text(snippet.replaceAll("\\s+", " ")));
					final long endTime = System.nanoTime();
					sum += endTime - startTime;
					System.out.println(String.format("(%s) Extraction ran in %s ns.", i, endTime - startTime));
					correct++;
				} catch (AnnotationException e) {
					error++;
					System.out.println(e);
					e.printStackTrace();
				}
				for (EnsenDBpediaResource e : entities) {
					out.println(e.uri());
				}
				out.println();
				out.flush();
			}
		}
		out.close();
		System.out.println(String.format("Extracted entities from %s text items, with %s successes and %s errors.", i, correct, error));
		System.out.println("Results saved to: " + outputFile.getAbsolutePath());
		double avg = (new Double(sum) / i);
		System.out.println(String.format("Average extraction time: %s ms", avg * 1000000));
	}

	public void evaluate(File inputFile, File outputFile) throws Exception {
		evaluateManual(inputFile, outputFile, 0);
	}

	public void evaluateManual(File inputFile, File outputFile, int restartFrom) throws Exception {
		saveExtractedEntitiesSet(inputFile, outputFile, new LineParser.ManualDatasetLineParser(), restartFrom);
	}

	//    public void evaluateCurcerzan(File inputFile, File outputFile) throws Exception {
	//         saveExtractedEntitiesSet(inputFile, outputFile, new LineParser.OccTSVLineParser());
	//    }

	/**
	 * Entity extraction code.
	 * @param text
	 * @return
	 */
	public abstract List<DBpediaResource> extract(Text text) throws AnnotationException;

	public abstract List<EnsenDBpediaResource> ensenExtract(Text text) throws AnnotationException;
}
