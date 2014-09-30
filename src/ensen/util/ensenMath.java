package ensen.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ejml.simple.SimpleMatrix;

import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

import com.google.common.collect.Sets;

public class ensenMath {
	static Logger log = Logger.getLogger(ensenMath.class.getName());

	public static SingularValueDecomposition SVD(Matrix data) {
		// perform SVD
		SingularValueDecomposition SVD = new SingularValueDecomposition(data);
		/*Matrix U = SVD.getU();
		Matrix V = SVD.getV();
		Matrix S = SVD.getS();
		Printer.setLogFile("SVD\\10-2-SVD_U" + System.currentTimeMillis());
		System.out.println("SVD U");
		Printer.printJamaMatrix(U);
		Printer.setLogFile("SVD\\10-2-SVD_S_" + System.currentTimeMillis());
		System.out.println("SVD Sigma ");
		Printer.printJamaMatrix(S);
		Printer.setLogFile("SVD\\10-2-SVD_V" + System.currentTimeMillis());
		System.out.println("SVD V");
		Printer.printJamaMatrix(V);*/
		return SVD;
	}

	public static ArrayList<Object> Xmeans(Matrix Z, String ClusterID, boolean visualize) {
		ArrayList<Object> res = new ArrayList<>();

		XMeans xmeans = new XMeans();
		int minCluster = 2;
		int maxCluster = Z.getColumnDimension();
		xmeans.setMaxNumClusters(maxCluster);
		xmeans.setMinNumClusters(minCluster);

		try {
			xmeans.setMaxIterations(50);
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		//System.out.println(Arrays.toString(xmeans.getOptions()));

		Instances instances = prepareKmeansData(Z);
		Printer.registerTime("prepare Xmeans Data " + Z.getRowDimension() + " X " + Z.getColumnDimension());
		//writeToarffFile(instances, "data_" + ClusterID);
		HashMap<Integer, ArrayList<Integer>> clusters = null;
		try {

			System.out.println("Applying Xmeans");
			xmeans.buildClusterer(instances);
			Printer.registerTime("xmeans.buildClusterer " + Z.getRowDimension() + " X " + Z.getColumnDimension());

			clusters = buildXClusters(xmeans, instances);
			double[] silhouetteIndex = new double[clusters.size()];

			silhouetteIndex = SilhouetteIndex(Z, clusters);
			//silhouetteIndex = MazenTestingSilhouetteIndex(Z, clusters);//temp mazen
			res.add(clusters);
			res.add(silhouetteIndex);
			//Printer.registerTime("Applying Xmeans over a matrix " + Z.getRowDimension() + " X " + Z.getColumnDimension());
			Printer.registerTime("silhouetteIndex " + Z.getRowDimension() + " X " + Z.getColumnDimension());

		} catch (Exception e) {
			
			e.printStackTrace();
		}

		return res;
	}

	private static double CHindex(Matrix Z, HashMap<Integer, ArrayList<Integer>> clusters) {
		int topics = Z.getColumnDimension();
		int items = Z.getRowDimension();
		int n = topics * items;
		//mean of all
		double Y = 0;
		for (int itemCounter = 0; itemCounter < items; itemCounter++) {
			for (int topicCounter = 0; topicCounter < topics; topicCounter++) {
				Y += Z.get(itemCounter, topicCounter);
			}
		}
		Y = Y * 1.0 / (items * topics);

		//between-groups: mean of clusters ==> Yi, and distance to the data center Y ==> Bi, and the sum of distances ==> B
		int clustersCounter = 0;
		double[] Yi = new double[clusters.size()];
		double[] Bi = new double[clusters.size()];
		double B = 0;
		double W = 0;

		for (ArrayList<Integer> c1 : clusters.values()) {
			int ni = c1.size();
			for (int itemCounter = 0; itemCounter < ni; itemCounter++) {
				for (int topinCounter = 0; topinCounter < topics; topinCounter++) {
					Yi[clustersCounter] += Z.get(c1.get(itemCounter), topinCounter);
				}
			}
			Yi[clustersCounter] = Yi[clustersCounter] * 1.0 / (c1.size() * topics);
			Bi[clustersCounter] = Math.pow(Yi[clustersCounter] - Y, 2);
			B += ni * Bi[clustersCounter];
			clustersCounter++;
		}

		clustersCounter = 0;
		//within-groups
		for (ArrayList<Integer> c1 : clusters.values()) {
			int ni = c1.size();
			for (int itemCounter = 0; itemCounter < ni; itemCounter++) {
				for (int topinCounter = 0; topinCounter < topics; topinCounter++) {
					W += Math.pow(Z.get(c1.get(itemCounter), topinCounter) - Yi[clustersCounter], 2);
				}
			}
			clustersCounter++;
		}

		double ch = (B / (clusters.size() - 1)) / (W / (n - clusters.size()));

		return ch;
	}

	private static double[] SilhouetteIndex(Matrix Z, HashMap<Integer, ArrayList<Integer>> clusters) {
		//System.out.println("============Silhouette==============");
		double[] sks = new double[clusters.size()];
		int topics = Z.getColumnDimension();
		int items = Z.getRowDimension();
		int n = topics * items;
		double sumOfSk = 0.0;
		int clusterID = 0;
		for (ArrayList<Integer> c1 : clusters.values()) {//for each cluster
			//System.out.println("Silhouette for cluster : " + clusterID);
			int ni = c1.size();
			if (ni > 1) {
				//within-cluster mean distance  a(i)
				double sumOfSi = 0.0;
				for (int itemCounter1 = 0; itemCounter1 < ni; itemCounter1++) {
					double currentAi = 0.0;
					double currentBi = 1000000.0;
					double currentSi = 0.0;
					for (int itemCounter2 = 0; itemCounter2 < ni; itemCounter2++) {

						if (itemCounter1 != itemCounter2) {
							currentAi = currentAi + distance(Z, c1.get(itemCounter1), c1.get(itemCounter2));//Z.get(itemCounter, topinCounter);
							//System.out.println("currentAi: " + currentAi);
						}
					}
					int temp = ni - 1;
					if (temp == 0)
						temp = 1;
					currentAi = currentAi / temp;

					//b(i) mean distance to	the points of each of the other clusters
					for (ArrayList<Integer> c2 : clusters.values()) {//for each other cluster
						if (!c1.equals(c2)) {
							double mi = 0.0;
							int nj = c2.size();
							for (int itemCounter3 = 0; itemCounter3 < nj; itemCounter3++) {//items of C2
								mi = mi + distance(Z, c1.get(itemCounter1), c2.get(itemCounter3));//Z.get(itemCounter, topinCounter);								
							}
							//System.out.println("mi: " + mi);
							temp = nj - 1;
							if (temp == 0)
								temp = 1;
							double delta = mi / temp;
							if (delta < currentBi)
								currentBi = delta;
						}
					}

					//calculate Si: silhouette width of the point
					//System.out.println("max between" + currentAi + " , " + currentBi);
					double max = Math.max(currentAi, currentBi);
					if (max != 0) {
						currentSi = (currentBi - currentAi) / max;
					} else
						currentSi = 0.0;
					//System.out.println("currentSi: " + currentSi);
					sumOfSi += currentSi;
				}

				//Sk the cluster mean silhouette
				//System.out.println("sumOfSi * ni" + sumOfSi + " * " + ni);

				double sk = (1.0 / ni) * sumOfSi;
				//System.out.println("sk " + sk);
				sks[clusterID] = sk;
				sumOfSk += sk;

			}
			//System.out.println("sumOfSk" + sumOfSk);
			clusterID++;
		}
		//C the global silhouette
		double C = (1.0 / clusters.size()) * sumOfSk;
		//System.out.println("(1 / clusters.size())" + (1.0 / clusters.size()));
		//System.out.println("C" + C);
		//System.out.println("============Silhouette end==============");

		return sks;
	}

	private static double[] MazenTestingSilhouetteIndex(Matrix Z, HashMap<Integer, ArrayList<Integer>> clusters) {
		//System.out.println("============Silhouette==============");
		double[] sks = new double[clusters.size()];
		int clusterID = 0;
		for (ArrayList<Integer> c1 : clusters.values()) {//for each cluster			
			sks[clusterID++] = 1;

		}

		return sks;
	}

	/*
	 * Euclidean distance
	 */

	private static double distance(Matrix Z, int itemIndex1, int itemIndex2) {
		int topics = Z.getColumnDimension();
		double Dist = 0;
		for (int topinCounter = 0; topinCounter < topics; topinCounter++) {
			Dist += Math.pow(Z.get(itemIndex1, topinCounter) - Z.get(itemIndex2, topinCounter), 2);
		}
		return Math.sqrt(Dist);
	}

	public static Instances prepareKmeansData(Matrix Z) {
		// 1. set up attributes
		FastVector atts = new FastVector();
		for (int i = 0; i < Z.getColumnDimension(); i++) {
			atts.addElement(new Attribute("Topic-" + i));
		}
		Instances data = new Instances("Z", atts, 0);

		// 2. fill with data
		for (int i = 0; i < Z.getRowDimension(); i++) {
			//System.out.println(i + "," + i + "," + 0 + "," + Z.getColumnDimension());
			double[] vals = Z.getMatrix(i, i, 0, Z.getColumnDimension() - 1).getArray()[0];
			//System.out.println(java.util.Arrays.toString(vals));

			data.add(new SparseInstance(1.0, vals));
		}

		//System.out.println(data);
		return data;
	}

	public static void writeToarffFile(Instances dataSet, String filename) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("arff/" + filename + ".arff"));
			writer.write(dataSet.toString());
			writer.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public static HashMap<Integer, ArrayList<Integer>> buildXClusters(XMeans xmeans, Instances instances) {
		HashMap<Integer, ArrayList<Integer>> clusters = new HashMap<Integer, ArrayList<Integer>>();
		int cluster_num = -1;
		int i = 0;
		for (Instance x : instances) {
			try {
				cluster_num = xmeans.clusterInstance(x);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			ArrayList<Integer> values = clusters.get(cluster_num);
			if (values == null) {
				values = new ArrayList<Integer>();
			}
			values.add(i);
			clusters.put(cluster_num, values);
			i++;
		}

		return clusters;
	}

	public static double cosine_similarity(Map<String, Double> v1, Map<String, Double> v2) {
		Set<String> both = Sets.newHashSet(v1.keySet());
		both.retainAll(v2.keySet());
		double sclar = 0, norm1 = 0, norm2 = 0;
		for (String k : both)
			sclar += v1.get(k) * v2.get(k);
		for (String k : v1.keySet())
			norm1 += v1.get(k) * v1.get(k);
		for (String k : v2.keySet())
			norm2 += v2.get(k) * v2.get(k);
		return sclar / Math.sqrt(norm1 * norm2);
	}

	/* IDF(t) = log_e(Total number of resources / Number of resources with term t in it).
	 * resources in rows
	 * terms in cols
	 * */
	public static double idfCalculator(Matrix data, int termIndex) {
		double count = 0;
		for (int k = 0; k < data.getRowDimension(); k++) {
			if (data.get(k, termIndex) > 0) {
				count++;
			}
		}
		if (count == 0)
			return 0;
		return Math.log(data.getRowDimension() / count);
	}

	/* TF(t) = 0.5+0.5*(occurrence: i.e.Number of times term t appears in a resources) / (max occurrence in the resources).
	 * resources in rows
	 * terms in cols
	 * */
	public static double tfCalculator(Matrix data, int i, int j) {
		double max = 0;
		for (int k = 0; k < data.getColumnDimension(); k++) {//for all terms
			if (max < data.get(i, k))
				max = data.get(i, k);
		}
		if (max == 0)
			return 0.5;
		return 0.5 + 0.5 * data.get(i, j) / max;
	}

	/*
	 * this method takes a matrix resources X terms
	 * and replace values (frequency) by the TFIDF value
	 */
	public static Matrix normalizeUsingTFIDF(Matrix data) {
		for (int i = 0; i < data.getRowDimension(); i++) {
			for (int j = 0; j < data.getColumnDimension(); j++) {
				data.set(i, j, tfCalculator(data, i, j) * idfCalculator(data, j));
			}
		}
		return data;
	}

	public static Matrix fromEJtoJama(SimpleMatrix m) {
		try {
			Matrix A = new Matrix(m.numRows(), m.numCols());
			for (int i = 0; i < m.numRows(); i++) {
				for (int j = 0; j < m.numCols(); j++) {
					A.set(i, j, m.get(i, j));
				}
			}

			return A;
		} catch (Exception e) {
			SystemCommandExecutor.RAMmonitoring();
			e.printStackTrace();
		}
		return null;
	}

	public static Matrix selectNcolumns(Matrix u, int n) {
		return u.getMatrix(0, u.getRowDimension() - 1, 0, n - 1);
	}

	public static Matrix selectNcolumnsMrows(Matrix u, int n, int m) {
		return u.getMatrix(0, m - 1, 0, n - 1);
	}

	public static int getTopNFromDiagonal(Matrix S, double minSvalue) {
		int n = S.getColumnDimension();
		for (int i = 0; i < S.getColumnDimension(); i++) {
			if (i < S.getRowDimension() && S.get(i, i) > minSvalue)
				n = i;
		}
		return n;
	}
}
