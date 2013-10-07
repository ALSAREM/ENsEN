package ensen.control;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Pps {
	Properties getPropertiesFromClasspath() {
		Properties props = new Properties();
		InputStream stream = this.getClass().getResourceAsStream("/config/ensen.properties");
		try {
			if (stream == null) {
				throw new FileNotFoundException("property file 'ensen.properties' not found in the classpath");
			}
			props.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return props;
	}
}

public class PropertiesManager {

	public PropertiesManager() {

	}

	public static String getProperty(String name) {
		Pps pp = new Pps();
		Properties defaultProps = pp.getPropertiesFromClasspath();// new
																	// Properties();
		/*
		 * try { FileInputStream in = new FileInputStream(session
		 * .getServletContext().getRealPath("/") +
		 * "../WEB-INF/ensen.properties"); defaultProps.load(in); in.close(); }
		 * catch (Exception e) { e.printStackTrace(); }
		 */
		return defaultProps.getProperty(name);

	}

	public static void setProperty(String name, String value) {

		Pps pp = new Pps();

		FileOutputStream out;

		Properties defaultProps = pp.getPropertiesFromClasspath();// new
																	// Properties();
		/*
		 * try { FileInputStream in = new FileInputStream(session
		 * .getServletContext().getRealPath("/") +
		 * "../WEB-INF/ensen.properties"); defaultProps.load(in); in.close(); }
		 * catch (Exception e) { e.printStackTrace(); }
		 */
		defaultProps.setProperty(name, value);
		try {

			out = new FileOutputStream(pp.getClass().getResource("/config/ensen.properties").getPath());
			defaultProps.store(out, "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
