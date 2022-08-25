package com.bbytes.recruiz.utils;

import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLUtil {

//	private static final Logger logger = LoggerFactory.getLogger(HTMLUtil.class);

	public static String cleanupInlineCssHTMLEmail(String htmlInput) {
		try {
			
			Document doc = Jsoup.parse(htmlInput);
			final String style = "style";
			
			Elements els = doc.select(style);// to get all the style elements
			for (Element e : els) {
				String styleRules = e.getAllElements().get(0).data().replaceAll("\n", "").trim(), delims = "{}";
				StringTokenizer st = new StringTokenizer(styleRules, delims);
				while (st.countTokens() > 1) {
					String selector = st.nextToken(), properties = st.nextToken();
					Elements selectedElements = doc.select(selector);
					for (Element selElem : selectedElements) {
						String oldProperties = selElem.attr(style);
						selElem.attr(style, oldProperties.length() > 0 ? concatenateProperties(oldProperties, properties) : properties);
					}
				}
				e.remove();
			}
			
			return doc.html();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String concatenateProperties(String oldProp, String newProp) {
		oldProp = oldProp.trim();
		if (!newProp.endsWith(";"))
			newProp += ";";
		return newProp + oldProp; // The existing (old) properties should take
									// precedence.
	}

}