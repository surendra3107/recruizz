package com.bbytes.recruiz.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

	private final PasswordEncoder passEncoder = new BCryptPasswordEncoder();

	public String encodePassword(String rawPassword) {
		return passEncoder.encode(rawPassword);
	}

	public boolean passwordMatches(String toBeVerifiedPassword, String hashPasswordFromDb) {
		return passEncoder.matches(toBeVerifiedPassword, hashPasswordFromDb);
	}

}
