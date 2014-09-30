package ensen.util.comparators;

import java.util.Comparator;
import java.util.Map;

import ensen.entities.Concept;

public class ConceptValueComparator implements Comparator<String> {

	Map<String, Concept> base;

	public ConceptValueComparator(Map<String, Concept> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.    
	public int compare(String a, String b) {
		try{
		if (base.get(a).score >= base.get(b).score) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
		}
		catch(Exception e){//comparing without score, 
			if (a.equals(b))
				return 0;
			else
				return -1;
		}
	}
}