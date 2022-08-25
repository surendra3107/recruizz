package com.bbytes.recruiz.rest.dto.models;

import com.bbytes.recruiz.utils.ArrayUtil;
import com.fdsapi.ResultSetConverter;

import lombok.Data;

@Data
public class ResultSetData {

	private String[] columns;

	private Object[][] data;

	private Object[][] columnToRowData;

	private int columnCount;

	private int rowCount;

	public ResultSetData(ResultSetConverter rsc) {
		columns = rsc.getMetaData();
		data = rsc.getResultSet();
		if (data != null) {
			columnToRowData = ArrayUtil.trasposeMatrix(data);
			columnCount = rsc.getColumnCount();
			rowCount = rsc.getRowCount();
		}

	}

	public Object[] getColumnData(int columnIndex) {
		return columnToRowData[columnIndex];
	}

	public Object getColumnRowData(int columnIndex, int rowIndex) {
		return columnToRowData[columnIndex][rowIndex];
	}

	public int getColumnIndex(String columnName) {
		String[] columnNames = this.getColumns();
		int columnIndex = -1;
		for (int i = 0; i < columnNames.length; i++) {
			if (columnName.equalsIgnoreCase(columnNames[i])) {
				columnIndex = i;
				break;
			}
		}

		return columnIndex;
	}

}
