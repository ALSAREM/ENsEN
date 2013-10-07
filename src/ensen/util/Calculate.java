package ensen.util;

import java.util.List;

import ensen.entities.EnsenDBpediaResource;

public class Calculate {
	public static Double getSimilarityScore(List<EnsenDBpediaResource> triplets, double maxSimilarity, String uri) {
		double res = 0.0;
		for (EnsenDBpediaResource r : triplets) {
			if (r.getFullUri().trim().toLowerCase().equals(uri.trim().toLowerCase()))
				res = res + r.similarityScore;
		}
		//normalize values: make all values between 0 and 1(make max = 1)
		/*if (maxSimilarity > 0)
			res = res / maxSimilarity;*/
		return res;
	}
}
