package com.bbytes.recruiz.enums;

public enum RolePermission {

    SuperAdmin("Super Admin", "Permission to perform super admin actions within Recruiz"), NormalPermission("Normal", "Normal"), ITAdmin("IT Admin",
	    "IT Admin"), AECandidate("Add Edit Candidate", "Permission to add & edit candidates into the database"), DeleteCandidate("Delete Candidate",
		    "Delete Candidate"), AEPosition("Add Edit Position", "Permission to add & edit all positions"), DeletePosition(
			    "Delete Position",
			    "Delete Position"), AEClient("Add Edit Client", "Permission to add & edit all clients"), DeleteClient(
				    "Delete Client",
				    "Delete Client"), VEBoard("View Edit Board", "Permission to view & edit various stages of position pipeline"), AEUSER(
					    "Add Edit USER",
					    "Add Edit USER"), DeleteUser("Delete User", "Permission to delete users within your account"), AERoles(
						    "Add Edit Roles", "Permission to add and edit user roles"), DeleteRoles("Delete Roles",
							    "Permission to delete user roles"), ManagerSetting("Manager Setting",
								    "Persmission to view manager dashboard"), GlobalEdit("Global Edit",
									    "Permission to edit data of other users within your organisation"), GlobalDelete(
										    "Global Delete",
										    "Permission to delete data of other users within your organisation"), AdminSetting(
											    "Admin Setting",
											    "Permission to view and change admin related organisation settings"), EmailClient(
												    "Email Client",
												    "View email inbox module"), CampaignFunction(
													    "Campaign Function",
													    "Permission to view campaign module"), ProspectsViewAll(
														    "Prospects - View All",
														    "Permission to view prospects/leads added by other users within your organisation"), CandidatesViewAll(
															    "Candidates - View All",
															    "Permission to view all candidates within your database"), CareerSite(
																    "Career Site",
																    "Permission to view & edit organisation career site"), GenerateInvoice(
																	    "Generate Invoice",
																	    "Permission to view & generate invoices"), ViewReports(
																		    "View Reports",
																		    "Permission to view organisation reports"), AEProspects(
																			    "Add Edit Prospects",
																			    "Permission to view prospects module"), DisableResumeDownload(
																				    "Disable Resume Download",
																				    "Disable user from downloading resumes"),TeamViewAll("Team - View All","Team - View all teams and team data"),TeamViewOwn("Team - View Own","View only teams and team data that logged in user is part of"),OfferLetter("OfferLetter - Generation","Permission to Generate OfferLetter For Candidate"),
    OfferLetterRollout("OfferLetterRollout - Generation","Permission for offer letter rollout");

    String displayName;

    private String permissionName;

    private String displayText;

    private RolePermission(String permissionName, String displayText) {
	this.setPermissionName(permissionName);
	this.setDisplayText(displayText);
    }

    public String getPermissionName() {
	return permissionName;
    }

    public void setPermissionName(String permissionName) {
	this.permissionName = permissionName;
    }

    public String getDisplayText() {
	return displayText;
    }

    public void setDisplayText(String displayText) {
	this.displayText = displayText;
    }

}
