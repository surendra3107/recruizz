SET FOREIGN_KEY_CHECKS=0;

delete from template_variable where templateId != ''; 

-- template varriables category -> interview
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Telephonic Screening' and category = 'interview'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Telephonic Screening' and category = 'interview'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Telephonic Screening' and category = 'interview'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Telephonic Screening' and category = 'interview'),'0')),'${emailSignature}');

INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Face-to-Face Meetings' and category = 'interview'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Face-to-Face Meetings' and category = 'interview'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Face-to-Face Meetings' and category = 'interview'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Face-to-Face Meetings' and category = 'interview'),'0')),'${emailSignature}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Face-to-Face Meetings' and category = 'interview'),'0')),'${scheduleDate}');

INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reach Candidate' and category = 'interview'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reach Candidate' and category = 'interview'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reach Candidate' and category = 'interview'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reach Candidate' and category = 'interview'),'0')),'${emailSignature}');


INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate offer letter' and category = 'interview'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate offer letter' and category = 'interview'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate offer letter' and category = 'interview'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate offer letter' and category = 'interview'),'0')),'${emailSignature}');


INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Online Video Call Online Assignment Test' and category = 'interview'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Online Video Call Online Assignment Test' and category = 'interview'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Online Video Call Online Assignment Test' and category = 'interview'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Online Video Call Online Assignment Test' and category = 'interview'),'0')),'${emailSignature}');


-- template varriables category -> email
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Offer Letter' and category = 'email'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Offer Letter' and category = 'email'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Offer Letter' and category = 'email'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Offer Letter' and category = 'email'),'0')),'${emailSignature}');

INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reject Mailer' and category = 'email'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reject Mailer' and category = 'email'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reject Mailer' and category = 'email'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Reject Mailer' and category = 'email'),'0')),'${emailSignature}');

INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Docs Submission' and category = 'email'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Docs Submission' and category = 'email'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Docs Submission' and category = 'email'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Docs Submission' and category = 'email'),'0')),'${emailSignature}');


INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out' and category = 'email'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out' and category = 'email'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out' and category = 'email'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out' and category = 'email'),'0')),'${emailSignature}');


INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out 2' and category = 'email'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out 2' and category = 'email'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out 2' and category = 'email'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Candidate Reach Out 2' and category = 'email'),'0')),'${emailSignature}');



-- template varriables category -> Interviewer Template
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${emailSignature}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${candidateEmail}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${candidateMobile}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${candidateTotalExperience}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${keySkills}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Template - Schedule Interview' and category = 'interviewer'),'0')),'${roundName}');

INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Reschedule' and category = 'interviewer'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Reschedule' and category = 'interviewer'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Reschedule' and category = 'interviewer'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Reschedule' and category = 'interviewer'),'0')),'${emailSignature}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interviewer Reschedule' and category = 'interviewer'),'0')),'${interviewerList}');



-- template varriables category -> Forward Profile
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.fullName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${emailSignature}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.mobile}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.email}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.currentCompany}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.currentTitle}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.currentLocation}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.totalExp}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.currentCtc}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile' and category = 'forward'),'0')),'${candidate.expectedCtc}');


INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile Masked' and category = 'forward'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile Masked' and category = 'forward'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Forward Profile Masked' and category = 'forward'),'0')),'${emailSignature}');



-- template varriables category -> Interview Schedule HR
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interview Schedule HR' and category = 'interview_schedule_hr'),'0')),'${clientName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interview Schedule HR' and category = 'interview_schedule_hr'),'0')),'${positionName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interview Schedule HR' and category = 'interview_schedule_hr'),'0')),'${candidateName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Interview Schedule HR' and category = 'interview_schedule_hr'),'0')),'${interviewerList}');


-- template varriables category -> Bulk Email
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Bulk Email' and category = 'email_candidate'),'0')),'${orgName}');
INSERT INTO `template_variable` (`templateId`,`templateVariable`) VALUES ((SELECT IFNULL((select id from email_template_data where name='Bulk Email' and category = 'email_candidate'),'0')),'${emailSignature}');


SET FOREIGN_KEY_CHECKS=0;

