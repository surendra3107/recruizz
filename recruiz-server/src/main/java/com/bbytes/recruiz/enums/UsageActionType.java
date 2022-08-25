package com.bbytes.recruiz.enums;

import java.util.HashMap;
import java.util.Map;

public enum UsageActionType {

    LoggedIn("Log In"), LoggedOut("Log Out"), CancelEmailSync("Cancel Email Sync"), SyncEmailAccount(
	    "Email Sync"), SyncEmailResult("Email Sync Result"), EmailAccountMarkedDeafult(
		    "Email Account Marked Default"), GetAllEmailClient("Get All Email Client"), DeleteEmailClient(
			    "Delete Email Client"), EditEmailClient("Edit Email Client"), AddEmailClient(
				    "Add Email Client"), DeleteDeptHead("Delete Dept Head"), GetDeptHead(
					    "Get Dept Head"), PopulateDummyData("Populate Dummy Data"), DeleteDummyData(
						    "Delete Dummy Data"), UserExistsCheck(
							    "User Exists Check"), MarkForDeleteUser(
								    "User Mark For Delete"), UserUpdate(
									    "User Update"), UserPasswordChange(
										    "User Password Change)"), ResetPassword(
											    "Reset Password"), ForgetPassword(
												    "Forget Password"), GetExternalUser(
													    "Get External User"), GetCurrentUser(
														    "Get Current User"), GetAllTenantList(
															    "Get All Tenant List"), UpdateToken(
																    "Update Token"), GetAPIToken(
																	    "Get API token"), GenerateApiToken(
																		    "Generate API Token"), GetAllUser(
																			    "Get All User"), GetUserRole(
																				    "Get User Role"), DeleteUser(
																					    "Delete User"), GetAllHr(
																						    "Get All Hr"), GetAllJoinedUser(
																							    "Get All Joined User"), ChangeUserRole(
																								    "Change User Role"), UpdateUserAccountStatus(
																									    "Update User Account Status"), UpdateJoinedStatus(
																										    "Update joined Status"), UserBulkUpload(
																											    "Upload Bulk User"), UserInvite(
																												    "Invite User"), UserReInvite(
																													    "Reinvite User"), SendBoardReportToClient(
																														    "Send Board Report To Client"), SendFeedbackReminder(
																															    "Send Feedback Reminder"), GetCandidateFeedback(
																																    "Get Candidate Feedback"), GetCandidateToSourceBoard(
																																	    "Get Candidate to Source In Board"), GetMonthlyCalenderByPosition(
																																		    "Get Position Monthly Calender"), GetCalenderForPosition(
																																			    "Get Position Calender"), GetMonthlyCalender(
																																				    "Get Monthly Calender"), GetCompleteCalender(
																																					    "Get Complete Calender"), CandidatePluginUpload(
																																						    "CandidatePluginUpload"), StopBulkUpload(
																																							    "Stop Bulk Upload"), AddCandidate(
																																								    "Add Candidate"), UploadCandidateResume(
																																									    "Upload Candidate Resume"), GetAllCandidate(
																																										    "Get All Candidate"), BulkOperationOnCandidate(
																																											    "Bulk Action On Candidate"), GetPositionMatchCandidate(
																																												    "Get Position Match Candidate"), GetRoundCandidate(
																																													    "Get Round Candidate"), ParseAndGetCandidate(
																																														    "Get Candidate From Parser"), GetCandidateDetails(
																																															    "Get Candidate Details"), ChangeCandidateStatus(
																																																    "Change Candidate Status"), UpdateCandidate(
																																																	    "Update Candidate"), DeleteCandidate(
																																																		    "Delete Candidate"), IsCandidateExists(
																																																			    "Check Candidate Exists"), UploadCandidateFile(
																																																				    "Upload Candidate File"), DeleteCandidateFile(
																																																					    "Delete Candidate File"), IsCandidateNumberExists(
																																																						    "Check Candidate Number Exists"), CandidateBulkUpload(
																																																							    "Candidate Bulk Upload"), QuickAddBulkCandidateUpload(
																																																								    "Quick Add Bulk Upload"), CandidatePluginAdd(
																																																									    "Add Candidate From Plugin"), CandidateQuickAdd(
																																																										    "Candidate Quick Add"), GetCandidateForExternalUser(
																																																											    "Get Candidate For External User"), AddCandidateNotes(
																																																												    "Add Candidate Notes"), UpdateCandidateNotes(
																																																													    "Update Candidate Notes"), DeleteCandidateNotes(
																																																														    "Delete Candidate Notes"), GetCandidateAssesment(
																																																															    "Get Candidate Assesment"), GetPositionCandidate(
																																																																    "Get Position Candidate"), RateCandidate(
																																																																	    "Rate Candidate"), GetAllFolderUser(
																																																																		    "Get All Folder User"), CreateCandidateFolder(
																																																																			    "Create Candidate Folder"), UpdateCandidateFolder(
																																																																				    "Update Candidate Folder"), AddCandidateToFolder(
																																																																					    "Add Candidate To Folder"), RemoveCandidateFromFolder(
																																																																						    "Remove Candidate From Folder"), ShareFolderWithUser(
																																																																							    "Share Folder With User"), UnshareFolderWithUser(
																																																																								    "Unshare Folder With User"), DeleteCandidateFolder(
																																																																									    "Delete Candidate Folder"), CheckFolderExists(
																																																																										    "Check IF Folder Exists"), ListCandidateFolderForCurrentUser(
																																																																											    "List Candidate Folder User"), GetFolderCandidate(
																																																																												    "Get Folder Candidate"), ListUsersInFolder(
																																																																													    "List User In Folder"), GetAllFolderCandidate(
																																																																														    "Get All Folder Candidate"), CareerSiteGetAllPosition(
																																																																															    "Career Site Get All Position"), CareerSitePositionDetails(
																																																																																    "Career Site Position Details"), AddCandidateToPositionCareerSite(
																																																																																	    "Career Site Add Canddiate To Position"), CareerSiteSerachActivePosition(
																																																																																		    "Career Site Search Active Position"), AddClient(
																																																																																			    "Add Client"), UpdateClient(
																																																																																				    "Update Client"), GetClientDetails(
																																																																																					    "Get Client Details"), ChangeClientStatus(
																																																																																						    "Change Client Status"), GetAllClient(
																																																																																							    "Get All Client"), DeleteClient(
																																																																																								    "Delete Client"), AddClientNotes(
																																																																																									    "Add Client Notes"), UpdateClientNotes(
																																																																																										    "Update Client Notes"), DeleteClientNotes(
																																																																																											    "Delete Client Notes"), GetClientNotes(
																																																																																												    "Get Client Notes"), DeleteClientInterviewer(
																																																																																													    "Delete Client Interviewer"), DeleteClientDM(
																																																																																														    "Delete Client Decison Maker"), AddInvoiceInfoForClient(
																																																																																															    "Add Invoice Info For Client"), AddClientFiles(
																																																																																																    "Add Client File"), GetClientFiles(
																																																																																																	    "Get Client File"), DeleteClientFile(
																																																																																																		    "Delete Client File"), GetHRDashBoard(
																																																																																																			    "Get DashBoard"), GetBulkUploadStat(
																																																																																																				    "Get Bulk Upload Stat"), SendEmail(
																																																																																																					    "Send Email"), GetAllSentEmail(
																																																																																																						    "Get All Sent Email"), EmployeeAddUpdate(
																																																																																																							    "Add Update Employee"), DeleteEmployee(
																																																																																																								    "Delete Employee"), AddEmployeeFiles(
																																																																																																									    "Add Employee File"), GetEmployeeFiles(
																																																																																																										    "Get Employee File"), GetEmployeeDashboard(
																																																																																																											    "Get Employee Dashboard"), GetAllEmployee(
																																																																																																												    "Get All Employee"), GetAllYetToOnBoardCandidate(
																																																																																																													    "Get All Yet To Onboard Candidate"), GetEmployeeDetails(
																																																																																																														    "Get Employee Details"), ProvideFeedback(
																																																																																																															    "Provide Feedback"), AddCandidateFromExternalSource(
																																																																																																																    "Add Candidate From External Source"), ForwardCandidateProfile(
																																																																																																																	    "Forward Candidate Profile"), AddGenericDM(
																																																																																																																		    "Add Generic Decision Maker"), AddGenericDMFromClient(
																																																																																																																			    "Add Generic Decision Maker From Client"), UpdateGenericDM(
																																																																																																																				    "Update Generic Decision Maker"), GetAllDM(
																																																																																																																					    "Get All Decision Maker"), DeleteGenericDM(
																																																																																																																						    "Delete Generic Decision Maker"), AddGerericInterviewer(
																																																																																																																							    "Add Generic Interviewer"), UpdateGenericInterviewer(
																																																																																																																								    "Update Generic Interviewer"), GetAllGenericInterviewer(
																																																																																																																									    "Get All Generic Interviewer"), AddGenericInterviewerToPosition(
																																																																																																																										    "Add Generic Interviewer To Position"), DeleteGenericInterviewerFromClient(
																																																																																																																											    "Remove Generic Interviewer Form Client"), DeleteGenericInterviewer(
																																																																																																																												    "Delete Generic Interviewer"), UploadImportFile(
																																																																																																																													    "Upload Import File"), ExportRecruizData(
																																																																																																																														    "Export Recruiz Data"), ImportData(
																																																																																																																															    "Import Data"), DownloadExcelImportItemsReport(
																																																																																																																																    "Download Excel Import Items Report"), GetSampleBulkUploadFile(
																																																																																																																																	    "Get Sample Bulk Upload File"), DownloadExportedData(
																																																																																																																																		    "Download Exported Data"), GetImportDataStat(
																																																																																																																																			    "Get Import Data Stat"), GetImportUploadItemsStat(
																																																																																																																																				    "Get Import Upload Item Stat"), ScheduleInterview(
																																																																																																																																					    "Schedule Interview"), CancelInterview(
																																																																																																																																						    "Cancel Interview"), RescheduleInterview(
																																																																																																																																							    "Reschedule Interview"), GetInterviewSchedule(
																																																																																																																																								    "Get Interview Schedule"), GetOpenPositionStats(
																																																																																																																																									    "Get Open Position Stats"), GetOverallOpenPositionStats(
																																																																																																																																										    "Get Overall Open Position Stats"), GetClientwiseOpenPositionStats(
																																																																																																																																											    "Get Client's Open Position Stat"), GetClosePositionStats(
																																																																																																																																												    "Get Closed Position Stats"), GetOverallClosePositionStats(
																																																																																																																																													    "Get Overall Closed Position Stats"), GetClientwiseClosePositionStats(
																																																																																																																																														    "Get Client's Close Position Stats"), GetOverallPositionSourcingChannelStats(
																																																																																																																																															    "Get Overall Position Soourcing Channel Stats"), GetPerPositionSourcingChannelStats(
																																																																																																																																																    "Get Position Sourcing Channel Stats"), GetClientwiseSourcingChannelStats(
																																																																																																																																																	    "Get Client's Sourcing Channel Stats"), GetPerPositionGenderMixStats(
																																																																																																																																																		    "Get Position's Gender Mix Stats"), GetClientwiseGenderMixStats(
																																																																																																																																																			    "Get Client's Gender Mix Stat"), GetOverallPositionGenderMixStats(
																																																																																																																																																				    "Get Overall Position Gender Mix Stat"), GetClientwiseInterviewScheduleStats(
																																																																																																																																																					    "Get Client's Interview Schedule Stats"), GetOverallPositionInterviewScheduleStats(
																																																																																																																																																						    "Get Overall Position Interview Schedule Stat"), GetPerPositionInterviewScheduleStats(
																																																																																																																																																							    "Get Position's Interview Schedule Stat"), GetClientwiseNewSourcedCandidateStats(
																																																																																																																																																								    "Get Client's New Sourced Candidate Stats"), GetOverallPositionNewSourcedCandidateStats(
																																																																																																																																																									    "Get Overall Position New Sourced Candidate Stats"), GetPerPositionNewSourcedCandidateStats(
																																																																																																																																																										    "Get Position's New Sourced Candidate Stats"), GetClientwiseCandidateRejectionMixStats(
																																																																																																																																																											    "Get Client's Candidate Rejection Mix Stats"), GetOverallPositionCandidateRejectionMixStats(
																																																																																																																																																												    "Get Overall Position Candidate Rejection Mix Stats"), GetClientwiseSourcedbyRecruiterStats(
																																																																																																																																																													    "Get Client's Sourced By Recuriter Stats"), GetOverallPositionSourcedbyRecruiterStats(
																																																																																																																																																														    "Get Over All Position Sourced By Recruiter Stats"), GetPerPositionSourcedbyRecruiterStats(
																																																																																																																																																															    "Get Position's Sourced By Recruiter Stats"), GetCandidatesListInEachRecruiter(
																																																																																																																																																																  "Get All Candidates Sourced By Each Recruiter Stats"), DeleteSubCategory(
																																																																																																																																																																    "Delete OnBoarding Subcategory"), AddSubCategory(
																																																																																																																																																																	    "Add Onboarding Subcategory"), AddUpdateOnBoardingDetails(
																																																																																																																																																																		    "Add Update OnBoarding Details"), AddUpdateAdminOnBoardDetails(
																																																																																																																																																																			    "Add Update Admin Onboarding Details"), AddOnBoardingTemplate(
																																																																																																																																																																				    "Add OnBoarding Templates"), EditOnBoardingTemplate(
																																																																																																																																																																					    "Edit Onboarding Template"), DeleteOnBoardingTemplate(
																																																																																																																																																																						    "Delete Onboarding Details"), EditEmployeeOnBoardingDetails(
																																																																																																																																																																							    "Edit EmployeeOnboarding Details"), UpdateOrganizationSetting(
																																																																																																																																																																								    "Update Organization Setting"), GetOrganizationDetails(
																																																																																																																																																																									    "Get Organization Details"), GetOrgLevelAuthToken(
																																																																																																																																																																										    "Get Organization Security Token"), MarkForDeleteOrganization(
																																																																																																																																																																											    "Organization Marked For Delete"), AddTaxDetails(
																																																																																																																																																																												    "Add Tax Details"), UpdateTaxDetails(
																																																																																																																																																																													    "Update Tax Details"), DeleteTaxDetails(
																																																																																																																																																																														    "Delete Tax Details"), SendJdInEmail(
																																																																																																																																																																															    "Send JD Email"), RemoveVendorFromPosition(
																																																																																																																																																																																    "Remove Vendor From Position"), AddVendorToPosition(
																																																																																																																																																																																	    "Add Vendor To Position"), DeleteHRFromPosition(
																																																																																																																																																																																		    "Delete HR From Position"), AddHRToPosition(
																																																																																																																																																																																			    "Add HR To Position"), DeleteInterviewerFromPosition(
																																																																																																																																																																																				    "Remove Interviewer From Position"), DeletePositionNote(
																																																																																																																																																																																					    "Delete Position Note"), UpdatePositionNotes(
																																																																																																																																																																																						    "Update Position Note"), AddPositionNotes(
																																																																																																																																																																																							    "Add Position Note"), GetPositionDetails(
																																																																																																																																																																																								    "Get Position Details"), GetBoardForDeptHead(
																																																																																																																																																																																									    "Get Board For Dept. Head"), GetPositionURL(
																																																																																																																																																																																										    "Get Position URL"), GetPositionEmail(
																																																																																																																																																																																											    "Get Position Email"), GetBoard(
																																																																																																																																																																																												    "Get Pipeline"), DeletePosition(
																																																																																																																																																																																													    "Delete Position"), GetAllPositionByClient(
																																																																																																																																																																																														    "Get Client's Position"), GetAllPosition(
																																																																																																																																																																																															    "Get All Position"), PublishRecruizConnectPosition(
																																																																																																																																																																																																    "Publish Position To Recruiz Connect"), PublishToCareerSite(
																																																																																																																																																																																																	    "Publish To Career Site"), ChangePositionStatus(
																																																																																																																																																																																																		    "Change Position Status"), UpdatePosition(
																																																																																																																																																																																																			    "Update Position"), AddPosition(
																																																																																																																																																																																																				    "Add Position"), GetAllPositionFolder(
																																																																																																																																																																																																					    "Get All Position Folder"), GetUserPositionFolders(
																																																																																																																																																																																																						    "Get User's Position Folder"), RemovePositionFromFolder(
																																																																																																																																																																																																							    "Remove Position From Folder"), AddPositionToFolder(
																																																																																																																																																																																																								    "Add Position To Folder"), DeleteRequestedPosition(
																																																																																																																																																																																																									    "Delete Requested Position"), ChangeRequestedPositionStatus(
																																																																																																																																																																																																										    "Change Requested Position Status"), EditRequestedPosition(
																																																																																																																																																																																																											    "Edit Requested Position"), GetAllNewRequestedPosition(
																																																																																																																																																																																																												    "Get All New Requested Position"), GetRequestedPositionDetails(
																																																																																																																																																																																																													    "Get Requested Position Details"), GetAllInProcessRequestedPosition(
																																																																																																																																																																																																														    "Get All Inprocess Requested Position"), GetAllRequestedPosition(
																																																																																																																																																																																																															    "Get All Requested Position"), RaisePositionRequest(
																																																																																																																																																																																																																    "Create Position Request"), AddVendorCandidateToPosition(
																																																																																																																																																																																																																	    "Add Vendor Candidate To Position"), DeleteVendor(
																																																																																																																																																																																																																		    "Delete Vendor"), GetVendorDetails(
																																																																																																																																																																																																																			    "Get Vendor Details"), UpdateVendor(
																																																																																																																																																																																																																				    "Update Vendor"), ChangeVendorStatus(
																																																																																																																																																																																																																					    "Change Vendor Status"), SourceCandidateByVendor(
																																																																																																																																																																																																																						    "Source Candidate By Vendor"), GetVendorsPositionDetails(
																																																																																																																																																																																																																							    "Get Vendor's Position Details"), GetVendorUserList(
																																																																																																																																																																																																																								    "Get Vendor's User"), InviteVendorUser(
																																																																																																																																																																																																																									    "Invite Vendor User"), UpdateVendorCandidate(
																																																																																																																																																																																																																										    "Update Vendor Candidate"), GetVendorBoard(
																																																																																																																																																																																																																											    "Get Vendor Board"), GetVendorCandidateDetails(
																																																																																																																																																																																																																												    "Get Vendor's Candidate Details"), GetVendorsCandidate(
																																																																																																																																																																																																																													    "Get Vendor's Candidate List"), AddCandidateByVendor(
																																																																																																																																																																																																																														    "Add Candidate By Vendor"), GetPositionForVendor(
																																																																																																																																																																																																																															    "Get Vendor's Position"), GetAllVendor(
																																																																																																																																																																																																																																    "Get All Vendor"), AddVendor(
																																																																																																																																																																																																																																	    "Add Vendor"), AddRole(
																																																																																																																																																																																																																																		    "Add Role"), ChangeRole(
																																																																																																																																																																																																																																			    "Change Role"), GetAllRoles(
																																																																																																																																																																																																																																				    "Get All Roles"), DeleteRole(
																																																																																																																																																																																																																																					    "Delete Role"), GetRoleByName(
																																																																																																																																																																																																																																						    "Get Role By Name"), GetAllPermission(
																																																																																																																																																																																																																																							    "Get All Permission"), ChangeRoleName(
																																																																																																																																																																																																																																								    "Change Role Name"), ChangeRolePermission(
																																																																																																																																																																																																																																									    "Change Role Permission"), GetAllRoleAndPermission(
																																																																																																																																																																																																																																										    "Get All Role And Permission"), GetAllUserForTeam(
																																																																																																																																																																																																																																											    "Get Team Users"), GetAllTeam(
																																																																																																																																																																																																																																												    "Get All Team"), GetTeamForCurrentUser(
																																																																																																																																																																																																																																													    "Get Team For User"), FetchFullTeam(
																																																																																																																																																																																																																																														    "Fetch Full Team"), DeleteTeam(
																																																																																																																																																																																																																																															    "Delete Team"), RemoveTeamMember(
																																																																																																																																																																																																																																																    "Remove Team Member"), AddTeamMember(
																																																																																																																																																																																																																																																	    "Add Team Member"), UpdateTeamMember(
																																																																																																																																																																																																																																																		    "Update Team Member"), UpdateTeam(
																																																																																																																																																																																																																																																			    "Update Team"), CreateTeam(
																																																																																																																																																																																																																																																				    "Create Team"), DeleteCandidateFromRound(
																																																																																																																																																																																																																																																					    "Delete Candidate From Round"), SaveRound(
																																																																																																																																																																																																																																																						    "Save Round"), DeleteRound(
																																																																																																																																																																																																																																																							    "Delete Round"), MoveCandidate(
																																																																																																																																																																																																																																																								    "Move Candidate"), SourceCandidateToRound(
																																																																																																																																																																																																																																																									    "Source Candidate To Round"), ChangeStatusOfProspectPosition(
																																																																																																																																																																																																																																																										    "Change Status Of Prospect Position"), GetClientsProspectPosition(
																																																																																																																																																																																																																																																											    "Get Client Prospect Position"), GetProspectPositionDetails(
																																																																																																																																																																																																																																																												    "Get Prospect"), AddProspect(
																																																																																																																																																																																																																																																													    "Add Prospect"), GetAllProspect(
																																																																																																																																																																																																																																																														    "Get All Prospect"), GetProspectDetails(
																																																																																																																																																																																																																																																															    "Get Prospect Details"), AddProspectContactInfo(
																																																																																																																																																																																																																																																																    "Add Prospect Contact Info"), UpdateProspectContactInfo(
																																																																																																																																																																																																																																																																	    "Update Prospect Contact Info"), UpdateProspect(
																																																																																																																																																																																																																																																																		    "Update Prospect"), DeleteProspect(
																																																																																																																																																																																																																																																																			    "Delete Prospect"), DeleteProspectPosition(
																																																																																																																																																																																																																																																																				    "Delete Prospect Position"), GetProspectContactInfo(
																																																																																																																																																																																																																																																																					    "Get Prospect Contact Info"), DeleteProspectContactInfo(
																																																																																																																																																																																																																																																																						    "Delete Prospect Contact Info"), AddProspectNotes(
																																																																																																																																																																																																																																																																							    "Add Prospect Notes"), UpdateProspectNotes(
																																																																																																																																																																																																																																																																								    "Update Prospect Notes"), DeleteProspectNotes(
																																																																																																																																																																																																																																																																									    "Delete Prospect Notes"), GetProspectNotes(
																																																																																																																																																																																																																																																																										    "Get Prospect Notes"), ConvertProspectToClient(
																																																																																																																																																																																																																																																																											    "Convert Prospect To Client"), ChangeProspectStatus(
																																																																																																																																																																																																																																																																												    "Change Prospect Status"), AddTaskReminder(
																																																																																																																																																																																																																																																																													    "Add Task Reminder"), GetProspectActivity(
																																																																																																																																																																																																																																																																														    "Get Prospect Activity"), UpdateProspectReason(
																																																																																																																																																																																																																																																																															    "Update Prospect Reason"), AddProspectPosition(
																																																																																																																																																																																																																																																																																    "Add Prospect Position"), GetAllProspectPosition(
																																																																																																																																																																																																																																																																																	    "Get Prospect's Position"), UpdateProspectPosition(
																																																																																																																																																																																																																																																																																		    "Update Prospects Position"), GetGlobalSearchResultForClient(
																																																																																																																																																																																																																																																																																			    "Candidate Global Search"), GetGlobalSearchResultForProspect(
																																																																																																																																																																																																																																																																																				    "Prospect Global Search"), GetGlobalSearchResultForPosition(
																																																																																																																																																																																																																																																																																					    "Position Global Search"), GetGlobalSearchResultForCandidate(
																																																																																																																																																																																																																																																																																						    "Candidate Global Search"), GetAllQueryActionForCandidateGlobalSearch(
																																																																																																																																																																																																																																																																																							    "Get All Query Action For Candidate Global Search"), GetGlobalSearchResult(
																																																																																																																																																																																																																																																																																								    "Get Global Search Result"), GetSuggestedPositionLocation(
																																																																																																																																																																																																																																																																																									    "Position Location Suggest Search"), GetSuggestedPositionTitle(
																																																																																																																																																																																																																																																																																										    "Position Name Suggest Search"), GetSuggestedPositionSkill(
																																																																																																																																																																																																																																																																																											    "Position Skill Set Suggest Search"), GetSuggestedUserName(
																																																																																																																																																																																																																																																																																												    "User Name Suggest Search"), GetSuggestedUserEmail(
																																																																																																																																																																																																																																																																																													    "User Email Suggest Search"), GetSuggestedCandidateSkillSet(
																																																																																																																																																																																																																																																																																														    "Candidate Skill Set Suggest Search"), GetSuggestedCandidateName(
																																																																																																																																																																																																																																																																																															    "Candidate Name Suggest Search"), GetSuggestedCandidateEmailAddress(
																																																																																																																																																																																																																																																																																																    "Candidate Email Suggest Search"), GetSuggestedCandidatesSkillSet(
																																																																																																																																																																																																																																																																																																	    "Candidate Skill Suggest Search"), GetSuggestedCandidatePreferedLocation(
																																																																																																																																																																																																																																																																																																		    "Candidate Prefered Location Suggest Search"), GetSuggestedCandidateCurrentLocation(
																																																																																																																																																																																																																																																																																																			    "Candidate Current Location Suggest Search"), GetSuggestedCandidateCurrentCompany(
																																																																																																																																																																																																																																																																																																				    "Candidate Current Company Suggest Search"), GetSuggestedClientLocation(
																																																																																																																																																																																																																																																																																																					    "Client Location Suggest Search"), GetSuggestedProspectLocation(
																																																																																																																																																																																																																																																																																																						    "Prospect Location Suggest Search"), GetSuggestedClientName(
																																																																																																																																																																																																																																																																																																							    "Client Name Suggest Search"), GetSuggestedProspectCompany(
																																																																																																																																																																																																																																																																																																								    "Prospect Company Suggest Search"), GetSuggestedEducationQualification(
																																																																																																																																																																																																																																																																																																									    "Candidate Educational Qualification Suggest Search"), GetSuggestedInstitute(
																																																																																																																																																																																																																																																																																																										    "Institiue Suggest Search"), GetClientSearch(
																																																																																																																																																																																																																																																																																																											    "Client Search"), GetProspectSearch(
																																																																																																																																																																																																																																																																																																												    "Prospect Search"), GetPositionSearch(
																																																																																																																																																																																																																																																																																																													    "Position Search"), GetPositionRequestSearch(
																																																																																																																																																																																																																																																																																																														    "Position Request Search"), GetCandidateSearch(
																																																																																																																																																																																																																																																																																																															    "Candidate Search"), AdvanceSearchCandidate(
																																																																																																																																																																																																																																																																																																																    "Advance Search Candidate "), GetAllQueryActionForAdvanceSearch(
																																																																																																																																																																																																																																																																																																																	    "All Query Action For Advance Search"), GetSavedSearchedQuery(
																																																																																																																																																																																																																																																																																																																		    "Get Saved Search Query"), SaveAdvanceSerachQuery(
																																																																																																																																																																																																																																																																																																																			    "Save Advance Search Query"), DeleteSearchQuery(
																																																																																																																																																																																																																																																																																																																				    "Delete Search Query"), UploadCandidateOfferLetter(
																																																																																																																																																																																																																																																																																																																				    	"Upload Candidate Offer Letter"), saveOfferLetterTemplateFormula(
																																																																																																																																																																																																																																																																																																																						    	"Save formula's of template"), generateOfferLetterForPreview(
																																																																																																																																																																																																																																																																																																																								    	"Generate offerletter for preview"), saveFinalOfferLetterForCandidate(
																																																																																																																																																																																																																																																																																																																										    	"Save Final Offer Letter For Candidate");

    String displayName;

    private static Map<Object, Object> map = new HashMap<Object, Object>();

    static {
	for (UsageActionType status : UsageActionType.values()) {
	    map.put(status.displayName, status);
	}
    }

    public static UsageActionType getValueByDisplayName(String displayName) {
	Object obj = map.get(displayName);
	if (obj != null)
	    return (UsageActionType) map.get(displayName);
	return null;
    }

    private UsageActionType(String displayName) {
	this.displayName = displayName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
	return displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

}
