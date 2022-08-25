package com.bbytes.recruiz.utils;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public final class VelocityTemplateUtils {

	/**
	 * to get template from HTML file
	 * 
	 * @param htmlTemplate
	 * @param emailVariableMap
	 * @return
	 */
	public static String getTemplateString(String htmlTemplate, Map<String, Object> emailVariableMap,String header,String footer) {

		emailVariableMap.put(GlobalConstants.TEMPLATE_NAME_HEADER, header);
		emailVariableMap.put(GlobalConstants.TEMPLATE_NAME_FOOTER, footer);
		
		VelocityContext context = new VelocityContext();
		for (Map.Entry<String, Object> entry : emailVariableMap.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}
		StringWriter stringWritter = new StringWriter();
		Velocity.evaluate(context, stringWritter, "template", htmlTemplate);
		return stringWritter.toString();
	}
	
	public static String getTemplateString(String htmlTemplate, Map<String, Object> emailVariableMap) {
		
		VelocityContext context = new VelocityContext();
		for (Map.Entry<String, Object> entry : emailVariableMap.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}
		StringWriter stringWritter = new StringWriter();
		Velocity.evaluate(context, stringWritter, "template", htmlTemplate);
		return stringWritter.toString();
	}
}
