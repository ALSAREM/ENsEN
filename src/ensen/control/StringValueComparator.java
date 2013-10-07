package ensen.control;

import java.util.Comparator;
import java.util.Map;

public class StringValueComparator implements Comparator<String> {

	Map<String, String> base;

	public StringValueComparator(Map<String, String> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b) {
		if (base.get(a).compareTo(base.get(b)) > 0) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}