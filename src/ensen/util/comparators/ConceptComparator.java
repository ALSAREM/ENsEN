package ensen.util.comparators;

import java.util.Comparator;

import ensen.entities.Concept;

public class ConceptComparator implements Comparator<Concept> {
	public int compare(Concept object1, Concept object2) {

		return Double.compare(object2.generalScore, object1.generalScore);
	}
}
