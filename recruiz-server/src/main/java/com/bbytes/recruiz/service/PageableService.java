package com.bbytes.recruiz.service;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
public class PageableService {

	public static int defaultPageSize = 10;
	public static String defaultSortingField = "modificationDate";
	private static String defaultSearchSortingField = "_score";

	/**
	 * this page return a pageable object with default value as page size of
	 * 'defaultPageSize' and sorted by 'defaultSortingField' in descending order
	 * 
	 * @return
	 */
	public Pageable defaultPageRequest() {
		return new PageRequest(0, defaultPageSize, new Sort(Sort.Direction.DESC, defaultSortingField));
	}

	public Pageable defaultPageRequest(int pageNo) {
		return new PageRequest(pageNo, defaultPageSize, new Sort(Sort.Direction.DESC, defaultSortingField));
	}

	public Pageable defaultPageRequest(int pageNo, int pageSize) {
		return new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, defaultSortingField));
	}

	public Pageable defaultPageRequest(String sortingField) {
		return new PageRequest(0, defaultPageSize, new Sort(Sort.Direction.DESC, defaultSortingField));
	}

	public Pageable defaultSearchPageRequest() {
		return new PageRequest(0, defaultPageSize, new Sort(Sort.Direction.DESC, defaultSearchSortingField));
	}

	public Pageable searchPageRequest(int pageSize) {
		return new PageRequest(0, pageSize, new Sort(Sort.Direction.DESC, defaultSearchSortingField));
	}

	public Pageable searchPageRequest(int pageNo, int pageSize) {
		return new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, defaultSearchSortingField));
	}

	/**
	 * this will the pageable request based on input, if page no is present then
	 * with page no, if sorting field is present then with sort if both are present
	 * then with both otherwise it will return a default page object with size 10
	 * sorted on created_date field in descending order
	 * 
	 * @param pageNo
	 * @param sortField
	 * @return
	 */
	public Pageable getPageRequestObject(String pageNo, String sortField) {
		if ((pageNo != null && !pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty()))
			return pageRequestByPageAndSortField(Integer.valueOf(pageNo), sortField);
		else if ((pageNo != null && !pageNo.isEmpty()) && (sortField == null || sortField.isEmpty()))
			return pageRequestByPageNo(Integer.valueOf(pageNo));
		else if ((pageNo == null || pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty()))
			return pageRequestBySortField(sortField);

		return defaultPageRequest();
	}

	public Pageable getPageRequestObject(String pageNo) {
		return pageRequestByPageNo(Integer.valueOf(pageNo));
	}
	
	public Pageable pageRequestByPageNoAndSize(String pageNo,int pagesize) {
		return new PageRequest(Integer.valueOf(pageNo), pagesize, new Sort(Sort.Direction.DESC, defaultSortingField));
	}

	/**
	 * this will the pageable request based on input, if page no is present then
	 * with page no, if sorting field is present then with sort, if sort direction
	 * present then direction if all are present then with all otherwise it will
	 * return a default page object with size 10 sorted on created_date field in
	 * descending order
	 * 
	 * @param pageNo
	 * @param sortField
	 * @param sortOrder
	 * @return
	 */
	public Pageable getPageRequestObject(String pageNo, String sortField, Direction sortOrder) {
		if ((pageNo != null && !pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty()) && sortOrder != null)
			return pageRequestByPageAndSortFieldAndSortOrder(Integer.valueOf(pageNo), sortField, sortOrder);
		else if ((pageNo != null && !pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty())
				&& (sortOrder == null))
			return pageRequestByPageAndSortField(Integer.valueOf(pageNo), sortField);
		else if ((pageNo == null || pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty())
				&& (sortOrder != null))
			return pageRequestBySortFieldAndSortOrder(sortField, sortOrder);
		else if ((pageNo != null && !pageNo.isEmpty()) && (sortField == null || sortField.isEmpty())
				&& (sortOrder != null))
			return pageRequestByPageAndSortOrder(Integer.valueOf(pageNo), sortOrder);
		else if ((pageNo != null && !pageNo.isEmpty()) && (sortField == null || sortField.isEmpty())
				&& (sortOrder == null))
			return pageRequestByPageNo(Integer.valueOf(pageNo));
		else if ((pageNo == null || pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty())
				&& (sortOrder == null))
			return pageRequestBySortField(sortField);
		else if ((pageNo == null || pageNo.isEmpty()) && (sortField == null || sortField.isEmpty())
				&& (sortOrder != null))
			return pageRequestBySortOrder(sortOrder);

		return defaultPageRequest();
	}

	public Pageable getSearchPageRequestObject(String pageNo, String sortField) {
		if ((pageNo != null && !pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty()))
			return pageRequestByPageAndSortField(Integer.valueOf(pageNo), sortField);
		else if ((pageNo != null && !pageNo.isEmpty())
				&& (sortField == null || sortField.trim().equals("null") || sortField.isEmpty()))
			return pageSearchRequestByPageNo(Integer.valueOf(pageNo));
		else if ((pageNo == null || pageNo.isEmpty()) && (sortField != null && !sortField.isEmpty()))
			return pageRequestBySortField(sortField);

		return defaultSearchPageRequest();
	}

	/**
	 * pageable object for page no
	 * 
	 * @param pageNo
	 * @return
	 */
	private Pageable pageRequestByPageNo(int pageNo) {
		return new PageRequest(pageNo, defaultPageSize, new Sort(Sort.Direction.DESC, defaultSortingField));
	}

	/**
	 * pageable object for page no
	 * 
	 * @param pageNo
	 * @return
	 */
	private Pageable pageSearchRequestByPageNo(int pageNo) {
		return new PageRequest(pageNo, defaultPageSize, new Sort(Sort.Direction.DESC, defaultSearchSortingField));
	}

	/**
	 * pageable object for sort order
	 * 
	 * @author Akshay
	 * @param sortOder
	 * @return
	 */
	private Pageable pageRequestBySortOrder(Direction sortOder) {
		return new PageRequest(0, defaultPageSize, new Sort(sortOder, defaultSortingField));
	}

	/**
	 * pageable object for sort field
	 * 
	 * @param sortField
	 * @return
	 */
	private Pageable pageRequestBySortField(String sortField) {
		return new PageRequest(0, defaultPageSize, new Sort(Sort.Direction.DESC, sortField));
	}

	/**
	 * pageable object for sort field and sort order
	 * 
	 * @author Akshay
	 * @param sortField
	 * @param sortOder
	 * @return
	 */
	private Pageable pageRequestBySortFieldAndSortOrder(String sortField, Direction sortOder) {
		return new PageRequest(0, defaultPageSize, new Sort(sortOder, sortField));
	}

	/**
	 * pageable object for page no and sort order
	 * 
	 * @param pageNo
	 * @param sortOder
	 * @return
	 */
	private Pageable pageRequestByPageAndSortOrder(int pageNo, Direction sortOder) {
		return new PageRequest(pageNo, defaultPageSize, new Sort(sortOder, defaultSortingField));
	}

	/**
	 * pageable object for page no and sort field
	 * 
	 * @param pageNo
	 * @param sortField
	 * @return
	 */
	private Pageable pageRequestByPageAndSortField(int pageNo, String sortField) {
		return new PageRequest(pageNo, defaultPageSize, new Sort(Sort.Direction.DESC, sortField));
	}

	/**
	 * pageable object for page no, sort field and sort order
	 * 
	 * @author Akshay
	 * @param pageNo
	 * @param sortField
	 * @param sortOder
	 * @return
	 */
	private Pageable pageRequestByPageAndSortFieldAndSortOrder(int pageNo, String sortField, Direction sortOder) {
		return new PageRequest(pageNo, defaultPageSize, new Sort(sortOder, sortField));
	}

	/**
	 * Check sort order from UI and matches with enum then gives appropriate sort
	 * direction
	 * 
	 * @author Akshay
	 * @param sortOrder
	 * @return
	 */
	public Direction getSortDirection(String sortOrder) {
		if (sortOrder != null && !sortOrder.isEmpty()) {
			if (SortOrder.DESC.toString().equals(sortOrder))
				return Direction.DESC;
			else if (SortOrder.ASC.toString().equals(sortOrder))
				return Direction.ASC;
		}
		return null;
	}

}
