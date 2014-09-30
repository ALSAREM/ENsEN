package ensen.entities.tensortxt;

import java.math.BigInteger;

import ensen.entities.EnsenDBpediaResource;

public class Path {
	public EnsenDBpediaResource Resource1;
	public EnsenDBpediaResource Resource2;
	public Fragment F1;
	public Fragment F2;
	public BigInteger F1Cluster;
	public BigInteger F2Cluster;
	public String Term;
	public BigInteger TermCluster;

	public boolean isGood() {
		//System.out.println(F1Cluster + " VS " + F2Cluster);
		if (F1Cluster != null && F2Cluster != null)
			return F1Cluster.equals(F2Cluster);
		else
			return false;
	}

	public String toString() {
		return Resource1 + " ==> " + F1.id + "(Cluster-" + F1Cluster + ") ==> " + Term + "(Cluster-" + TermCluster + ") ==> " + F2.id + "(Cluster-" + F2Cluster + ") ==> " + Resource2;
	}
}
