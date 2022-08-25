package com.bbytes.recruiz.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.bbytes.recruiz.domain.Candidate;

public class SearchUtil {

	/*
	 * Clean up query text ..issue like "Java dev" will create issue as the
	 * double quotes is not escaped
	 * 
	 */
	@Deprecated // elastic search takes care of it 
	public static String cleanQueryText(String queryText) {
		if (queryText == null || queryText.isEmpty())
			return queryText;

//		return QueryParser.escape(queryText).toString();
		return queryText;
	}

	public static String cleanBoolQuery(String booleanQuery) {
		if (!StringUtils.isValid(booleanQuery))
			return booleanQuery;

		booleanQuery = StringUtils.findReplace("or", "OR", booleanQuery);
		booleanQuery = StringUtils.findReplace("and", "AND", booleanQuery);
		booleanQuery = StringUtils.findReplace("not", "NOT", booleanQuery);

//		booleanQuery = cleanQueryText(booleanQuery);
		return booleanQuery;

	}

	public static String candidateHash(Candidate candidate) {
		return Base64.encodeBase64String(DigestUtils.sha1Hex(candidate.getFullName() + candidate.getCurrentCompany()).getBytes());
	}

	public static String candidateHash(String fullName, String currentCompany) {
		return Base64.encodeBase64String(DigestUtils.sha1Hex(fullName + currentCompany).getBytes());
	}

}