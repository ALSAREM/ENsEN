package ensen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class permute {

	static String permute(int level, String permuted, boolean used[], String[] original) {
		int length = original.length;
		if (level == length) {
			return (permuted + "#");
		} else {
			String temp = "";
			String temp1 = "";
			for (int i = 0; i < length; i++) {
				if (!used[i]) {
					used[i] = true;
					temp += permute(level + 1, permuted + " " + original[i], used, original);
					used[i] = false;
					temp1 += permuted + "#";
					// System.out.println("(" + permuted + ")");
				}
			}
			return temp1 + temp;
		}
	}

	static private ArrayList<String> getSubCominations(int level, String temp) {
		//System.err.println(temp);
		String[] inArray = temp.trim().split(" ");
		ArrayList<String> resWords = new ArrayList<String>();
		if (inArray.length == 1) {
			resWords.add(temp.trim());
			return resWords;
		} else {
			String w1 = inArray[0];
			resWords.add(w1);
			if (w1.split(" ").length < level) {
				ArrayList<String> rec = getSubCominations(level - 1, temp.replace(w1, ""));
				for (String w2 : rec) {
					if (w2 != null) {
						resWords.add(w2);
						resWords.add(w1.trim() + " " + w2.trim());
						resWords.add(w2.trim() + " " + w1.trim());
					}
				}
			}
			return resWords;
		}
	}

	static public String[] getAllCominations(int level, String in) {
		return getSubCominations(level, in).toArray(new String[5]);
	}

	static public String[] permuteAll(String in) {
		String[] inWords = in.split(" ");
		boolean used[] = new boolean[inWords.length];
		for (int i = 0; i < inWords.length; i++) {
			used[i] = false;
		}
		String res = permute(0, "", used, inWords);
		String[] resWords = res.split("#");
		Set<String> set = new HashSet<String>();
		Collections.addAll(set, resWords);
		String[] outWords = new String[set.size()];
		outWords = set.toArray(outWords);
		sortArray(outWords);
		return outWords;
	}

	public static void sortArray(String[] strArray) {
		String tmp;
		if (strArray.length == 1)
			return;
		for (int i = 0; i < strArray.length; i++) {
			for (int j = i + 1; j < strArray.length; j++) {
				if (strArray[i].length() < strArray[j].length()) {
					tmp = strArray[i];
					strArray[i] = strArray[j];
					strArray[j] = tmp;
				}
			}
		}
	}

	public static void main(String[] args) {
		//System.out.print(Arrays.toString(permuteAll("Information Retrieval private")));
		System.out.print(Arrays.toString(getAllCominations(3, "Information Retrieval private syria lyon villeurbanne")));
	}
}