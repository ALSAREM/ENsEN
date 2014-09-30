package ensen.util.comparators;

import java.util.Comparator;

import ensen.entities.EnsenDBpediaResource;

public class EnsenDBpediaResourceComparator implements Comparator<EnsenDBpediaResource> {
	public int compare(EnsenDBpediaResource object1, EnsenDBpediaResource object2) {
		return (int) (object2.similarityScore - object1.similarityScore);
	}
}
