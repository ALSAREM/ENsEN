package ensen.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Model;

public class GraphMatching {

	public static TreeMap<String, Double> Match(Model Ain, Model Bin) {
		Model A = RDFManager.createRDFModel();
		Model B = RDFManager.createRDFModel();

		if (Ain != null) {
			A.add(Ain);
			A = RDFManager.removeLiteral(A);
		}
		if (Bin != null) {

			B.add(Bin);
			B = RDFManager.removeLiteral(B);
		}

		// get intersection
		ArrayList<String> resIntersect = RDFManager.intersection(A, B);
		System.out.println("intersecting (A,B) - intersection Size=" + resIntersect.size());
		// System.out.println(resIntersect);
		Map<String, Double> res = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(res);
		TreeMap<String, Double> Res = new TreeMap<String, Double>(bvc);

		// for each res in intersection
		Bipartite biA = new Bipartite(A);
		Bipartite biB = new Bipartite(B);
		for (String uri : resIntersect) {
			if (uri != null) {
				System.out.println("get similar resources to (" + uri + ")in A ");
				TreeMap<String, Double> resA = biA.applyRS_Approx(uri, A);// get
																			// similar
																			// resources
																			// in
																			// A
				System.out.println("similar resources (" + uri + ") in A: " + resA.size());
				System.out.println("get similar resources (" + uri + ") in B ");
				TreeMap<String, Double> resB = biB.applyRS_Approx(uri, B); // get
																			// similar
																			// resources
																			// in
																			// B
				System.out.println("similar resources (" + uri + ") in B: " + resB.size());
				res.put(uri, 1.0);

				System.out.println("Merge and rank results to (" + uri + ")");
				for (Entry<String, Double> entry : resA.entrySet()) {
					Double value = entry.getValue();
					String key = entry.getKey();
					if (res.containsKey(key))
						res.put(key, res.get(key) + value);
					else
						res.put(key, value);
				}

				for (Entry<String, Double> entry : resB.entrySet()) {
					Double value = entry.getValue();
					String key = entry.getKey();
					if (res.containsKey(key))
						res.put(key, res.get(key) + value);
					else
						res.put(key, value);
				}
			}
		}
		Res.putAll(res);

		return Res;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
