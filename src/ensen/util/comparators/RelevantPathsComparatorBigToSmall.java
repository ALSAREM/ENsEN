package ensen.util.comparators;

import java.util.Comparator;

import ensen.entities.tensortxt.RelevantPath;

public class RelevantPathsComparatorBigToSmall implements Comparator<RelevantPath> {

	@Override
	public int compare(RelevantPath arg0, RelevantPath arg1) {

		return arg1.paths.size() - arg0.paths.size();
	}
}