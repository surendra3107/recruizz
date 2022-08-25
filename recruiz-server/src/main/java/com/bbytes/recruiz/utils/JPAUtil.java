package com.bbytes.recruiz.utils;

import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.User;

public class JPAUtil {

	private static Specification<User> userContainsTextInAttributes(String text, List<String> attributes) {
		if (!text.contains("%")) {
			text =  text + "%";
		}
		String finalText = text;
		return (root, query,
				builder) -> builder.or(root.getModel().getDeclaredSingularAttributes().stream()
						.filter(a -> attributes.contains(a.getName()))
						.map(a -> builder.like(root.get(a.getName()), finalText)).toArray(Predicate[]::new));
	}

	private static Specification<Candidate> candidateContainsTextInAttributes(String text, List<String> attributes) {
		if (!text.contains("%")) {
			text = text + "%";
		}
		String finalText = text;
		return (root, query,
				builder) -> builder.or(root.getModel().getDeclaredSingularAttributes().stream()
						.filter(a -> attributes.contains(a.getName()))
						.map(a -> builder.like(root.get(a.getName()), finalText)).toArray(Predicate[]::new));
	}

	public static Specification<User> userContainsTextInNameOrEmailOrMobile(String text) {
		return userContainsTextInAttributes(text, Arrays.asList("name", "email"));
	}

	public static Specification<Candidate> candidateContainsTextInName(String text) {
		return candidateContainsTextInAttributes(text, Arrays.asList("fullName", "email"));
	}

}