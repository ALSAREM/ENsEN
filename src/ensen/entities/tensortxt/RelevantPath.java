package ensen.entities.tensortxt;

import java.math.BigInteger;
import java.util.ArrayList;

import ensen.entities.EnsenDBpediaResource;

public class RelevantPath {
	public EnsenDBpediaResource Resource1;
	public EnsenDBpediaResource Resource2;
	public ArrayList<Path> paths;
	public BigInteger FCluster;
	public ArrayList<String> Terms;
	public ArrayList<Integer> TermCounter;
	public ArrayList<BigInteger> TermClusters;

	public RelevantPath() {
		paths = new ArrayList<Path>();
		Terms = new ArrayList<String>();
		TermClusters = new ArrayList<BigInteger>();
		TermCounter = new ArrayList<Integer>();

	}

	public String toString() {
		//path with N of pathes
		String out = "==================== \nRelevantPath: \n" + Resource1 + " ==> " + Resource2 + " , Cluster(" + FCluster + ") , size:" + paths.size() + "\n";
		out += "-----------------\n";
		out += "Terms' Clusters: \n";
		//pathes clusters (by term cluster)
		ArrayList<Integer> TempTermCounter = new ArrayList<Integer>();
		ArrayList<BigInteger> TempTermClusters = new ArrayList<BigInteger>();
		ArrayList<String> TempTerms = new ArrayList<String>();

		for (int i = 0; i < TermClusters.size(); i++) {
			BigInteger c = TermClusters.get(i);
			int index = TempTermClusters.indexOf(c);
			if (index == -1) {
				TempTermClusters.add(c);
				TempTermCounter.add(TermCounter.get(i));
				TempTerms.add(Terms.get(i));

			} else {
				TempTermCounter.set(index, TempTermCounter.get(index) + TermCounter.get(i));
				TempTerms.set(index, Terms.get(index) + " , ");
			}
		}

		for (int i = 0; i < TempTermClusters.size(); i++) {
			out += "Cluster- " + TempTermClusters.get(i);
			out += " Size: " + TempTermCounter.get(i);
			out += " Terms: " + TempTerms.get(i);
			out += "\n";

		}
		out += "-----------------\n";
		out += "Terms: ";

		for (int i = 0; i < Terms.size(); i++) {
			out += Terms.get(i) + /* "(Cluster-" + TermClusters.get(i) + */"(Freq: " + TermCounter.get(i) + ") , ";
		}
		out += "\n";
		return out;//Resource1 + " ==> " + F1.id + "(" + F1Cluster + ") ==> " + Term + "(" + TermCluster + ") ==> " + F2.id + "(" + F2Cluster + ") ==> " + Resource2;
	}

	public boolean isHere(Path p) {
		/*System.out.println(p.Resource1.getFullUri() + " == " + Resource1.getFullUri() + " : " + (p.Resource1.getFullUri() == Resource1.getFullUri()));
		System.out.println(p.Resource2.getFullUri() + " == " + Resource2.getFullUri() + " : " + (p.Resource2.getFullUri() == Resource2.getFullUri()));
		System.out.println(p.F1Cluster + " == " + FCluster + " : " + (p.F1Cluster == FCluster));
		*/
		return p.Resource1.getFullUri().contains(Resource1.getFullUri()) && p.Resource2.getFullUri().contains(Resource2.getFullUri()) && p.F1Cluster.equals(FCluster);

	}
}
