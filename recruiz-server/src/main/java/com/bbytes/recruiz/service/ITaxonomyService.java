package com.bbytes.recruiz.service;

public interface ITaxonomyService {

	/**
	 * Skill alias method can be used to search alias name for a Skill.
	 * 
	 * @param keyword
	 *            (e.g. Java)
	 * @return
	 * @throws Exception
	 */
	String skillAlias(final String skillKeyword) throws Exception;

	/**
	 * Similar job profile method can be used for suggestion and auto complete.
	 * We use Like % keyword% for searching similar job profile and return max
	 * 100 matching records
	 * 
	 * @param skillKeyword
	 *            (e.g. Java)
	 * @return
	 * @throws Exception
	 */
	String similarSkills(final String skillKeyword) throws Exception;

	/**
	 * Similar job profile method can be used for suggestion and auto complete.
	 * We use Like % keyword% for searching similar job profile and return max
	 * 100 matching records
	 * 
	 * @param jobProfileKeyword
	 * 
	 * @return
	 * @throws Exception
	 */
	String similarJobProfiles(final String jobProfileKeyword) throws Exception;

	/**
	 * Job Skills method can be used to get skills name related to particular
	 * job.
	 * 
	 * @param JobProfileKeyword
	 *            (e.g. Java Developer)
	 * @return
	 * @throws Exception
	 */
	String jobSkillRelation(final String JobProfileKeyword) throws Exception;

	/**
	 * Job Skills method can be used to get skills name related to particular
	 * job.
	 * 
	 * @param JobProfileKeyword
	 *            (e.g. Java Developer)
	 * @return
	 * @throws Exception
	 */
	String jobDomain(final String JobProfileKeyword) throws Exception;

	/**
	 * Job Skills method can be used to get skills name related to particular
	 * job.
	 * 
	 * @param skillKeyword
	 *            (e.g.Quality Control)
	 * @return
	 * @throws Exception
	 */
	String skillDomain(final String skillKeyword) throws Exception;

}
