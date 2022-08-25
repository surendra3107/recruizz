package com.bbytes.recruiz.exception;

public class RecruizCandidateExistException extends Exception {

	private static final long serialVersionUID = -2317792934996425645L;

	private String candidateEmail;

	public RecruizCandidateExistException(String candidateEmail) {
		super("Candidate already exists with email : " + candidateEmail);
		this.setCandidateEmail(candidateEmail);
	}

	/**
	 * @return the candidateEmail
	 */
	public String getCandidateEmail() {
		return candidateEmail;
	}

	/**
	 * @param candidateEmail the candidateEmail to set
	 */
	public void setCandidateEmail(String candidateEmail) {
		this.candidateEmail = candidateEmail;
	}

}
