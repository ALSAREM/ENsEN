package ensen.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.umbc.cs.maple.utils.JamaUtils;
import ensen.util.Printer;
import ensen.util.PropertiesManager;

public class Cluster {
	static Logger log = Logger.getLogger(Cluster.class.getName());
	public String name;

	public Matrix Data;
	public ArrayList<String> resources;
	public ArrayList<String> terms;
	public double silhouetteIndex;
	public double fatherGlobalSilhouetteIndex;
	public double fatherGlobalSD;
	public String father;
	public String path;

	public Cluster(String f) {
		father = f;
		path = father + "/";
	}

	public Cluster(ArrayList<Integer> value, ArrayList<String> allResources, ArrayList<String> AllTerms, String f, int indexInFather) {
		terms = new ArrayList<>();
		resources = new ArrayList<>();
		father = f;
		path = father + "/" + indexInFather;
		for (int i = 0; i < value.size(); i++) {
			int index = value.get(i);
			if (index < allResources.size())
				resources.add(allResources.get(index));
			else
				terms.add(AllTerms.get(index - allResources.size()));
		}
	}

	public boolean termsMoreThanResources() {
		return (resources.size() <= terms.size());
	}

	public void buildClusterDataMatrix(Cluster father) {
		//System.out.println("buildClusterDataMatrix of: " + name);
		Data = father.Data.copy();
		/*	System.out.println("cluster items: " + indices.size());
			System.out.println("father cluster items: " + father.indices.size());*/

		if (resources.size() > 0) {
			int[] colunms = new int[resources.size()];
			int i = 0;
			for (String s : father.resources) {
				if (resources.contains(s)) {
					colunms[i++] = father.resources.indexOf(s);
				}
			}
			Data = JamaUtils.getcolumns(Data, colunms);
		}

		if (terms.size() > 0) {
			int[] rows = new int[terms.size()];
			int j = 0;
			for (String s : father.terms) {
				if (terms.contains(s)) {
					rows[j++] = father.terms.indexOf(s);
				}
			}
			Data = JamaUtils.getrows(Data, rows);
		}

		//System.out.println("result cluster data: " + Data.getRowDimension() + " X " + Data.getColumnDimension());
	}

	public void printClusterToFile(int currIndex, int iteration) {
		Date date = new Date();
		String physicalFolder = PropertiesManager.getProperty("webRootPath");
		String mainFolder = "/log/clusters/" + date.getHours() + "_" + date.getMinutes() / 3;
		String[] names = path.split("/");
		String currPath = physicalFolder + mainFolder;
		for (int i = 0; i <= names.length; i++) {
			File dir = new File(currPath);
			if (!dir.exists()) {
				boolean result = dir.mkdir();
			}
			if (i < names.length)
				currPath += "/" + names[i];
		}

		String filePath = mainFolder + "/" + path + "_It" + iteration + name + ".txt";
		ArrayList<String> clusterItems = new ArrayList<String>();
		if (resources != null)
			clusterItems.addAll(resources);
		if (terms != null)
			clusterItems.addAll(terms);
		Printer.printToFile(filePath, "Father silhouette: " + fatherGlobalSilhouetteIndex + " _ silhouette: " + silhouetteIndex + "_ items: " + clusterItems.toString());
	}

	private boolean inCluster(Entry<Integer, ArrayList<Integer>> cluster, int item) {

		for (int j = 0; j < cluster.getValue().size(); j++) {
			if (cluster.getValue().get(j) == item)
				return true;
		}

		return false;
	}

	public Cluster copy() {
		Cluster c = new Cluster(father);
		c.Data = Data.copy();
		c.name = name;
		c.resources = new ArrayList<>(resources);
		c.terms = new ArrayList<>(terms);

		return c;
	}

	public Matrix buildClustersData(Matrix A, ArrayList<String> fatherResources, ArrayList<String> fatherTerms) {
		Matrix B = null;
		//copy resources
		int[] rows = new int[resources.size()];
		int i = 0;
		for (String item : resources) {
			int index = fatherResources.indexOf(item);
			rows[i++] = index;
		}
		B = JamaUtils.getrows(A, rows);
		//copy terms	
		int[] cols = new int[terms.size()];
		int j = 0;
		for (String item : terms) {
			int index = fatherTerms.indexOf(item);
			cols[j++] = index;
		}
		B = JamaUtils.getcolumns(B, cols);
		return B;
	}

}
