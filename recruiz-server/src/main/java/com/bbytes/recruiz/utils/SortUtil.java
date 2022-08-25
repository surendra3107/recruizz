package com.bbytes.recruiz.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.bbytes.recruiz.domain.Candidate;

public class SortUtil {

	public static void sortCandidateModificationDateDesc(List<Candidate> toBeSorted) {
		Collections.sort(toBeSorted, new Comparator<Candidate>() {
			public int compare(Candidate o1, Candidate o2) {
				return o2.getModificationDate().compareTo(o1.getModificationDate());
			}
		});
	}
}
