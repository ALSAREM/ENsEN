package ensen.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class MathTools {
	public static double calculateStandardDeviation(Collection<Integer> data, double mean) {
		double SD = 0.0;
		double sum = 0.0;
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			Integer x = (Integer) iterator.next();
			sum += (x - mean) * (x - mean);
		}
		if (data.size() > 0)
			SD = Math.sqrt(sum / data.size());
		return SD;
	}

	public static double calculateStandardDeviationDouble(Collection<Double> data, double mean) {
		double SD = 0.0;
		double sum = 0.0;
		for (Iterator iterator = data.iterator(); iterator.hasNext();) {
			Double x = (Double) iterator.next();
			sum += (x - mean) * (x - mean);
		}
		if (data.size() > 0)
			SD = Math.sqrt(sum / data.size());
		return SD;
	}

	public static double calculateZScore(double SD, double value, double mean) {
		double ZScore = 0.0;
		if (SD > 0)
			ZScore = (value - mean) / SD;
		return ZScore;
	}

	public static double calculateZScoreDouble(double SD, double value, double mean) {
		double ZScore = 0.0;
		if (SD > 0)
			ZScore = (value - mean) / SD;
		return ZScore;
	}

	public static double calculateMean(Map<String, Integer> data) {
		double Mean = 0.0;
		int i = 0;
		for (Map.Entry<String, Integer> entry : data.entrySet()) {
			Mean += entry.getValue();
			i++;
		}
		if (i > 0)
			Mean = Mean / i;

		return Mean;
	}

}
