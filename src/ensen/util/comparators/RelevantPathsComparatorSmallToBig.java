package ensen.util.comparators;

import java.util.Comparator;

import ensen.entities.tensortxt.RelevantPath;

public class RelevantPathsComparatorSmallToBig implements Comparator<RelevantPath> {

	@Override
	public int compare(RelevantPath arg0, RelevantPath arg1) {

		return arg0.paths.size() - arg1.paths.size();
	}
}
