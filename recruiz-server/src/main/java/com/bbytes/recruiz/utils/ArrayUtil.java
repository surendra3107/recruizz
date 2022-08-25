package com.bbytes.recruiz.utils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ArrayUtil {

	public static Object[][] trasposeMatrix(Object[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;

		Object[][] trasposedMatrix = new Object[n][m];

		for (int x = 0; x < n; x++) {
			for (int y = 0; y < m; y++) {
				trasposedMatrix[x][y] = matrix[y][x];
			}
		}

		return trasposedMatrix;
	}
	
	public static Object[] removeDuplicate(Object[] data) {
		Set<Object> dataSet = new LinkedHashSet<Object>(Arrays.asList(data));
		Object[] duplicateRemoved = dataSet.toArray(new Object[dataSet.size()]);
		return duplicateRemoved;
	}
}
