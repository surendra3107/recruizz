package com.bbytes.recruiz.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RchilliXML

{
	RchilliMapFields map;
	boolean exp = false;
	boolean edu = false;
	boolean skill = false;
	boolean linkedIn = false;

	ArrayList<HashMap<String, String>> EducationSplit = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> ExperienceSplit = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> SkillSplit = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> LinkedinSplit = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> projectSplit = new ArrayList<HashMap<String, String>>();
	ArrayList<HashMap<String, String>> webSites = new ArrayList<HashMap<String, String>>();

	HashMap<String, String> educations = new HashMap<>();
	HashMap<String, String> experiences = new HashMap<>();
	HashMap<String, String> skills = new HashMap<>();
	HashMap<String, String> linkedin = new HashMap<>();
	HashMap<String, String> projects = new HashMap<>();
	HashMap<String, String> website = new HashMap<>();

	int expCount = 0;
	String value = "";
	boolean socialRecruit = false;

	public RchilliXML(RchilliMapFields map) {
		this.map = map;

	}

	/*
	 * ---------------------------------------Others
	 * ends--------------------------------------------
	 */
	public void readXML(String xmlString) throws SAXException, IOException, ParserConfigurationException {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		DefaultHandler handler = new DefaultHandler() {
			boolean bfname = false;
			String nodeName = "";
			String attribute = "";
			boolean updated = false;
			boolean currentJobProfile = false;
			String formattedName = "";
			String alias = "";

			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				nodeName = qName;
				int length = attributes.getLength();
				updated = false;
				formattedName = "";
				alias = "";
				if (length > 0) {
					attribute = attributes.getValue(0);
					try {
						if (attributes.getValue("updated").equalsIgnoreCase("true")) {
							updated = true;
						}
					} catch (Exception ex) {
						updated = false;
					}
				}
				if (nodeName.equalsIgnoreCase("Skill") || nodeName.equalsIgnoreCase("JobProfile")) {
					try {
						formattedName = attributes.getValue("formattedName");
						if (formattedName == null)
							formattedName = "";
						alias = attributes.getValue("alias");
						if (alias == null)
							alias = "";
					} catch (Exception ex) {

					}
				}
				if (nodeName.equalsIgnoreCase("EducationSplit")) {
					educations = new HashMap<String, String>();
				}
				if (nodeName.equalsIgnoreCase("WorkHistory")) {
					experiences = new HashMap<String, String>();
					experiences.put("ExperienceId", Integer.toString(expCount));
				}
				if (nodeName.equalsIgnoreCase("SkillSet")) {
					skills = new HashMap<String, String>();

				}
				if (nodeName.equalsIgnoreCase("Recomendation")) {
					linkedin = new HashMap<String, String>();
				}
				if (nodeName.equalsIgnoreCase("Projects")) {
					projects = new HashMap<String, String>();
					projects.put("ExperienceId", Integer.toString(expCount));
				}

				bfname = true;
			}

			public void endElement(String uri, String localName, String qName) throws SAXException {

				nodeName = qName;
				if (nodeName.equals("EducationSplit")) {
					EducationSplit.add(educations);
				}
				if (nodeName.equals("WorkHistory")) {
					ExperienceSplit.add(experiences);
					expCount++;
				}
				if (nodeName.equalsIgnoreCase("SkillSet")) {
					SkillSplit.add(skills);
				}
				if (nodeName.equalsIgnoreCase("Recomendation")) {
					LinkedinSplit.add(linkedin);
				}
				if (nodeName.equalsIgnoreCase("Projects")) {
					projectSplit.add(projects);
				}
				if (!socialRecruit) {
					if (nodeName.equalsIgnoreCase("WebSite")) {
						webSites.add(website);
					}

				}
				if (!socialRecruit) {
					if (nodeName.equalsIgnoreCase("WebSites")) {
						map.setWebsites(webSites);
					}
				}
				if (nodeName.equalsIgnoreCase("SegregatedQualification")) {
					map.setQualificationSegrigation(EducationSplit);
				}
				if (nodeName.equalsIgnoreCase("SegregatedExperience")) {
					map.setExperienceSegrigation(ExperienceSplit);
					currentJobProfile = true;
				}
				if (nodeName.equalsIgnoreCase("OperationalSkills")) {
					map.setSkillSegrigation(SkillSplit);
					SkillSplit = new ArrayList<HashMap<String, String>>();
				}
				if (nodeName.equalsIgnoreCase("SoftSkills")) {
					map.setSoftSkillSegrigation(SkillSplit);
					SkillSplit = new ArrayList<HashMap<String, String>>();
				}
				if (nodeName.equalsIgnoreCase("BehaviorSkills")) {
					map.setBehaviourSkillSegrigation(SkillSplit);
					SkillSplit = new ArrayList<HashMap<String, String>>();
				}

				if (nodeName.equalsIgnoreCase("Recommendations")) {
					map.setLinkedInRecommendation(LinkedinSplit);
				}
				if (nodeName.equalsIgnoreCase("Projects")) {
					map.setProjects(projectSplit);
				}

			}

			public void characters(char ch[], int start, int length) throws SAXException {

				if (bfname) {
					value = new String(ch, start, length);

					if (value == null)
						value = "";
					if (value.equalsIgnoreCase("\n") || value.equalsIgnoreCase("\r")) {
						value = "";
					}
					value = value.trim();
					if (!socialRecruit) {
						if (nodeName.equalsIgnoreCase("ResumeFileName")) {
							map.setResumeFileName(value);
						} else if (nodeName.equalsIgnoreCase("ParsingDate")) {
							map.setParsingDate(value);
						} else if (nodeName.equalsIgnoreCase("TitleName")) {
							map.setTitleName(value);
						} else if (nodeName.equalsIgnoreCase("FirstName")) {
							map.setFirstName(value);
						} else if (nodeName.equalsIgnoreCase("Middlename")) {
							map.setMiddlename(value);
						} else if (nodeName.equalsIgnoreCase("LastName")) {
							map.setLastName(value);
						} else if (nodeName.equalsIgnoreCase("DateOfBirth")) {
							map.setDateOfBirth(value);
						} else if (nodeName.equalsIgnoreCase("Gender")) {
							map.setGender(value);
						} else if (nodeName.equalsIgnoreCase("FatherName")) {
							map.setFatherName(value);
						} else if (nodeName.equalsIgnoreCase("MotherName")) {
							map.setMotherName(value);
						} else if (nodeName.equalsIgnoreCase("MaritalStatus")) {
							map.setMaritalStatus(value);
						} else if (nodeName.equalsIgnoreCase("Nationality")) {
							map.setNationality(value);
						} else if (nodeName.equalsIgnoreCase("LanguageKnown")) {
							map.setLanguageKnown(value);
						} else if (nodeName.equalsIgnoreCase("UniqueID")) {
							map.setUniqueID(value);
						} else if (nodeName.equalsIgnoreCase("LicenseNo")) {
							map.setLicenseNo(value);
						} else if (nodeName.equalsIgnoreCase("PassportNo")) {
							map.setPassportNo(value);
						} else if (nodeName.equalsIgnoreCase("PanNo")) {
							map.setPanNo(value);
						} else if (nodeName.equalsIgnoreCase("VisaStatus")) {
							map.setVisaStatus(value);
						} else if (nodeName.equalsIgnoreCase("Email")) {
							map.setEmail(value);
						} else if (nodeName.equalsIgnoreCase("AlternateEmail")) {
							map.setAlternateEmail(value);
						} else if (nodeName.equalsIgnoreCase("Phone")) {
							map.setPhone(value);
						} else if (nodeName.equalsIgnoreCase("Mobile")) {
							map.setMobile(value);
						} else if (nodeName.equalsIgnoreCase("FaxNo")) {
							map.setFaxNo(value);
						} else if (nodeName.equalsIgnoreCase("Address")) {
							map.setAddress(value);
						} else if (nodeName.equalsIgnoreCase("City")) {
							map.setCity(value);
						} else if (nodeName.equalsIgnoreCase("State")) {
							map.setState(value);
						} else if (nodeName.equalsIgnoreCase("Country")) {
							map.setCountry(value);
						} else if (nodeName.equalsIgnoreCase("ZipCode")) {
							map.setZipCode(value);
						} else if (nodeName.equalsIgnoreCase("PermanentAddress")) {
							map.setPermanentAddress(value);
						} else if (nodeName.equalsIgnoreCase("PermanentCity")) {
							map.setPermanentCity(value);
						} else if (nodeName.equalsIgnoreCase("PermanentState")) {
							map.setPermanentState(value);
						} else if (nodeName.equalsIgnoreCase("PermanentCountry")) {
							map.setPermanentCountry(value);
						} else if (nodeName.equalsIgnoreCase("PermanentZipCode")) {
							map.setPermanentZipCode(value);
						} else if (nodeName.equalsIgnoreCase("Category")) {
							map.setCategory(value);
						} else if (nodeName.equalsIgnoreCase("SubCategory")) {
							map.setSubCategory(value);
						} else if (nodeName.equalsIgnoreCase("CurrentSalary")) {
							map.setCurrentSalary(value);
						} else if (nodeName.equalsIgnoreCase("ExpectedSalary")) {
							map.setExpectedSalary(value);
						} else if (nodeName.equalsIgnoreCase("Qualification")) {
							map.setQualification(value);
						} else if (nodeName.equalsIgnoreCase("Skills")) {
							map.setSkills(value);
						} else if (nodeName.equalsIgnoreCase("Experience")) {
							map.setExperience(value);
						} else if (nodeName.equalsIgnoreCase("CurrentEmployer")) {
							map.setCurrentEmployer(value);
						} else if (nodeName.equalsIgnoreCase("TotalExperienceInYear")) {
							map.setTotalExperienceInYear(value);
						} else if (nodeName.equalsIgnoreCase("TotalExperienceInMonths")) {
							map.setTotalExperienceInMonths(value);
						} else if (nodeName.equalsIgnoreCase("TotalExperienceRange")) {
							map.setTotalExperienceRange(value);
						} else if (nodeName.equalsIgnoreCase("GapPeriod")) {
							map.setGapPeriod(value);
						} else if (nodeName.equalsIgnoreCase("NumberofJobChanged")) {
							map.setNumberofJobChanged(value);
						} else if (nodeName.equalsIgnoreCase("AverageStay")) {
							map.setAverageStay(value);
						} else if (nodeName.equalsIgnoreCase("Availability")) {
							map.setAvailability(value);
						} else if (nodeName.equalsIgnoreCase("Hobbies")) {
							map.setHobbies(value);
						} else if (nodeName.equalsIgnoreCase("Objectives")) {
							map.setObjectives(value);
						} else if (nodeName.equalsIgnoreCase("Achievements")) {
							map.setAchievements(value);
						} else if (nodeName.equalsIgnoreCase("References")) {
							map.setReferences(value);
						} else if (nodeName.equalsIgnoreCase("PreferredLocation")) {
							map.setPreferredLocation(value);
						} else if (nodeName.equalsIgnoreCase("Certification")) {
							map.setCertification(value);
						} else if (nodeName.equalsIgnoreCase("CustomFields")) {
							map.setCustomFields(value);
						} else if (nodeName.equalsIgnoreCase("EmailFrom")) {
							map.setEmailFrom(value);
						} else if (nodeName.equalsIgnoreCase("EmailTo")) {
							map.setEmailTo(value);
						} else if (nodeName.equalsIgnoreCase("EmailSubject")) {
							map.setEmailSubject(value);
						} else if (nodeName.equalsIgnoreCase("EmailBody")) {
							map.setEmailBody(value);
						} else if (nodeName.equalsIgnoreCase("EmailCC")) {
							map.setEmailCC(value);
						} else if (nodeName.equalsIgnoreCase("EmailReplyTo")) {
							map.setEmailReplyTo(value);
						} else if (nodeName.equalsIgnoreCase("EmailSignature")) {
							map.setEmailSignature(value);
						} else if (nodeName.equalsIgnoreCase("DetailResume")) {
							map.setDetailResume(value);
						} else if (nodeName.equalsIgnoreCase("htmlresume")) {
							map.sethtmlresume(value);
						} else if (nodeName.equalsIgnoreCase("CandidateImageFormat")) {
							map.setCandidateImageFormat(value);
						} else if (nodeName.equalsIgnoreCase("CandidateImageData")) {
							map.setCandidateImageData(value);
						} else if (nodeName.equalsIgnoreCase("FormattedPhone")) {
							map.setFormattedPhoneNo(value);
						} else if (nodeName.equalsIgnoreCase("FormattedMobile")) {
							map.setFormattedMobileNo(value);
						} else if (nodeName.equalsIgnoreCase("FormattedAddress")) {
							map.setFormattedAddress(value);
						} else if (nodeName.equalsIgnoreCase("LongestStay")) {
							map.setLongestStay(value);
						} else if (nodeName.equalsIgnoreCase("CurrentLocation")) {
							map.setCurrentLocation(value);
						} else if (nodeName.equalsIgnoreCase("Coverletter")) {
							map.setCoverletter(value);
						} else if (nodeName.equalsIgnoreCase("Publication")) {
							map.setPublication(value);
						} else if (nodeName.equalsIgnoreCase("TemplateOutputData")) {
							map.setTemplateData(value);
						} else if (nodeName.equalsIgnoreCase("TemplateOutputFileName")) {
							map.setTemplateFileName(value);
						} else if (nodeName.equalsIgnoreCase("Availabilty")) {
							map.setAvailabilty(value);
						} else if (nodeName.equalsIgnoreCase("Summery")) {
							map.setSummery(value);
						} else if (nodeName.equalsIgnoreCase("BehaviorSkills")) {
							map.setBehaviorSkills(value);
						} else if (nodeName.equalsIgnoreCase("SoftSkills")) {
							map.setSoftSkills(value);
						} else if (currentJobProfile && nodeName.equalsIgnoreCase("JobProfile")) {
							map.setJobProfile(value);
							currentJobProfile = false;
						} else if (nodeName.equalsIgnoreCase("Website")) {
							website.put("WebSite", value);
							website.put("Type", attribute);
						} else if (nodeName.equalsIgnoreCase("Employer") || nodeName.equalsIgnoreCase("JobProfile")
								|| nodeName.equalsIgnoreCase("JobLocation") || nodeName.equalsIgnoreCase("JobPeriod")
								|| nodeName.equalsIgnoreCase("StartDate") || nodeName.equalsIgnoreCase("EndDate")
								|| nodeName.equalsIgnoreCase("JobDescription")) {

							if (!experiences.containsKey("Updated")) {
								experiences.put("Updated", updated + "");
							}
							if (nodeName.equalsIgnoreCase("JobProfile")) {
								if (!experiences.containsKey("FormattedName")) {
									experiences.put("FormattedName", formattedName + "");
								}
								if (!experiences.containsKey("AliasName")) {
									experiences.put("AliasName", alias + "");
								}
							}

							experiences.put(nodeName, value);

						} else if (nodeName.equalsIgnoreCase("University") || nodeName.equalsIgnoreCase("UniversityName")
								|| nodeName.equalsIgnoreCase("UniversityCity") || nodeName.equalsIgnoreCase("UniversityState")
								|| nodeName.equalsIgnoreCase("UniversityCountry") || nodeName.equalsIgnoreCase("InstituteName")
								|| nodeName.equalsIgnoreCase("InstituteCity") || nodeName.equalsIgnoreCase("InstituteState")
								|| nodeName.equalsIgnoreCase("InstituteCountry") || nodeName.equalsIgnoreCase("Aggregate")
								|| nodeName.equalsIgnoreCase("Degree") || nodeName.equalsIgnoreCase("Year")) {
							if (!educations.containsKey("Updated")) {

								educations.put("Updated", updated + "");

							}
							educations.put(nodeName, value);
						} else if (nodeName.equalsIgnoreCase("PersonName") || nodeName.equalsIgnoreCase("PositionTitle")
								|| nodeName.equalsIgnoreCase("CompanyName") || nodeName.equalsIgnoreCase("Relation")
								|| nodeName.equalsIgnoreCase("Description")) {

							linkedin.put(nodeName, value);

						} else if (nodeName.equalsIgnoreCase("Skill") || nodeName.equalsIgnoreCase("ExperienceInMonths")) {
							if (!skills.containsKey("Updated")) {
								skills.put("Updated", updated + "");
							}
							if (nodeName.equalsIgnoreCase("Skill")) {
								if (!skills.containsKey("FormattedName")) {
									skills.put("FormattedName", formattedName + "");
								}
								if (!skills.containsKey("AliasName")) {
									skills.put("AliasName", alias + "");
								}
							}
							skills.put(nodeName, value);
						} else if (nodeName.equalsIgnoreCase("ProjectName") || nodeName.equalsIgnoreCase("UsedSkills")
								|| nodeName.equalsIgnoreCase("TeamSize")) {

							projects.put(nodeName, value);
						}

					}
					bfname = false;
				}
			}
		};

		ByteArrayInputStream in = new ByteArrayInputStream(xmlString.getBytes());
		InputSource is = new InputSource();
		is.setEncoding("UTF-8");
		is.setByteStream(in);
		saxParser.parse(is, handler);

		/*--------------------  Personal Info ends--------------------------------------------------------------	 */

		/*
		 * ---------------------------------------Skills
		 * begins---------------------------------------------
		 */

	}

}