package ensen.threads;

import java.net.URI;

import ensen.entities.Document;
import ensen.util.Calculate;
import ensen.util.MathTools;

public class ResourceScoreThread /*extends Thread*/{
	String entry;
	Document D;
	public Double score;
	public String URI;

	public ResourceScoreThread(String entry, Document D) {
		//super(entry); // Store the thread name
		this.entry = entry;
		this.D = D;
		this.score = null;
		//run();
	}

	public void run() {
		int inValue = 0;
		int outValue = 0;
		int freqValue = 1;
		int qValue = 0;
		double SimilarityScore = 0.0;

		try {
			URI uri = new URI(entry);
			URI = uri.toASCIIString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			URI = entry;
		}
		System.out.println("----------------Calculate Score for (" + URI + ")--------------------");
		System.out.println("----------------Calculate values--------------------");
		if (D.topicInRelatedResources.get(URI) != null)
			inValue = D.topicInRelatedResources.get(URI) + 0;
		if (D.topicOutRelatedResources.get(URI) != null)
			outValue = D.topicOutRelatedResources.get(URI) + 0;
		if (D.topicRelatedFreqResources.get(URI) != null)
			freqValue = D.topicRelatedFreqResources.get(URI) + 0;
		if (freqValue != 0)
			SimilarityScore = Calculate.getSimilarityScore(D.triplets, D.maxSimilarity, URI) / freqValue;
		if (D.queryRelatedResources.contains(URI))
			qValue = 1;

		/* 2 types of isolated resources:
		 * - found in text one time (freq==1) but still with no links ((inValue == 0) && (outValue == 0))
		 * - not found in the text(freq==0), i.e. from the expansion ==> (in==1), and no out 		 * 
		 */
		if (((inValue == 1) && (outValue == 0) && (freqValue == 0)) || ((inValue == 0) && (outValue == 0) && (freqValue == 1))) {
			System.out.println("isolated resource: " + URI);
			//D.fullGraph = RDFManager.removeResourceWithAllStatements(D.fullGraph, URI);
		} else {
			System.out.println(URI + ": Values:	in " + (inValue * 1.0) + ", out " + (outValue * 1.0) + " ,SimilarityScore	" + (SimilarityScore) + " ,freqValue	" + (freqValue * 1.0) + " ,qValue	" + qValue);

			double inZ = MathTools.calculateZScore(D.inSD, inValue, D.inMean);
			System.out.println("inZ: " + inZ);
			double outZ = MathTools.calculateZScore(D.outSD, outValue, D.outMean);
			System.out.println("outZ: " + outZ);
			double freqZ = MathTools.calculateZScore(D.freqSD, freqValue, D.freqMean);
			System.out.println("freqZ: " + freqZ);
			double simZ = MathTools.calculateZScoreDouble(D.simSD, SimilarityScore, D.simMean);
			System.out.println("simZ: " + simZ);
			double qZ = MathTools.calculateZScoreDouble(D.qSD, qValue, D.qMean);
			System.out.println("qZ: " + qZ);

			score = (D.inImportance * inZ) + (D.outImportance * outZ) + (D.SimilarityImportance * simZ) + (D.freqImportance * freqZ) + (D.qImportance * qZ);
			System.out.println(URI + ": Score: " + score);

			D.ResourceScoreCounter++;
		}
	}

}