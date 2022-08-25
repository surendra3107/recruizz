-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: tenant_mgmt
-- ------------------------------------------------------
-- Server version	5.7.13-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_activity`
--

DROP TABLE IF EXISTS `account_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activityTitle` varchar(255) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `details` varchar(255) DEFAULT NULL,
  `entityID` varchar(255) DEFAULT NULL,
  `userEmail` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_activity`
--

/*!40000 ALTER TABLE `account_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_activity` ENABLE KEYS */;

--
-- Table structure for table `advanced_search_query`
--

DROP TABLE IF EXISTS `advanced_search_query`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `advanced_search_query` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `advanced_search_and_keys` varchar(255) DEFAULT NULL,
  `advanced_search_boolean_query` varchar(255) DEFAULT NULL,
  `advanced_search_ctc` varchar(255) DEFAULT NULL,
  `advanced_search_curr_location` varchar(255) DEFAULT NULL,
  `advanced_search_employement_type` varchar(255) DEFAULT NULL,
  `advanced_search_experience` varchar(255) DEFAULT NULL,
  `advanced_search_not_keys` varchar(255) DEFAULT NULL,
  `advanced_search_notice_period` varchar(255) DEFAULT NULL,
  `advanced_search_or_keys` varchar(255) DEFAULT NULL,
  `advanced_search_pref_location` varchar(255) DEFAULT NULL,
  `advanced_search_name` varchar(255) DEFAULT NULL,
  `advanced_search_field` varchar(255) DEFAULT NULL,
  `advanced_search_skillset` varchar(255) DEFAULT NULL,
  `advanced_search_sourced` varchar(255) DEFAULT NULL,
  `advanced_search_status` varchar(255) DEFAULT NULL,
  `advanced_search_tab` varchar(255) DEFAULT NULL,
  `owner_user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_f8yvt5fqkttlob196vg4ymg5o` (`owner_user_id`),
  CONSTRAINT `FK_f8yvt5fqkttlob196vg4ymg5o` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `advanced_search_query`
--

/*!40000 ALTER TABLE `advanced_search_query` DISABLE KEYS */;
/*!40000 ALTER TABLE `advanced_search_query` ENABLE KEYS */;

--
-- Table structure for table `auditentity`
--

DROP TABLE IF EXISTS `auditentity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auditentity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp` bigint(20) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditentity`
--

/*!40000 ALTER TABLE `auditentity` DISABLE KEYS */;
/*!40000 ALTER TABLE `auditentity` ENABLE KEYS */;

--
-- Table structure for table `board`
--

DROP TABLE IF EXISTS `board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `clientStatus` varchar(255) DEFAULT NULL,
  `positionStatus` varchar(255) DEFAULT NULL,
  `status` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board`
--

/*!40000 ALTER TABLE `board` DISABLE KEYS */;
/*!40000 ALTER TABLE `board` ENABLE KEYS */;

--
-- Table structure for table `board_audit`
--

DROP TABLE IF EXISTS `board_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `board_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `clientStatus` varchar(255) DEFAULT NULL,
  `positionStatus` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_4km78rypa8nmmcfbajq9x3y6b` (`REV`),
  CONSTRAINT `FK_4km78rypa8nmmcfbajq9x3y6b` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `board_audit`
--

/*!40000 ALTER TABLE `board_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `board_audit` ENABLE KEYS */;

--
-- Table structure for table `candidate`
--

DROP TABLE IF EXISTS `candidate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `candidate` (
  `cid` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `alternateEmail` varchar(255) DEFAULT NULL,
  `alternateMobile` varchar(255) DEFAULT NULL,
  `comments` text,
  `communication` varchar(255) DEFAULT NULL,
  `ctcUnit` varchar(255) DEFAULT NULL,
  `currentCompany` varchar(255) DEFAULT NULL,
  `currentCtc` double NOT NULL,
  `currentLocation` varchar(255) DEFAULT NULL,
  `currentTitle` varchar(255) DEFAULT NULL,
  `dob` datetime DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `employmentType` varchar(255) DEFAULT NULL,
  `expectedCtc` double NOT NULL,
  `facebookProf` varchar(255) DEFAULT NULL,
  `fullName` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `githubProf` varchar(255) DEFAULT NULL,
  `highestQual` varchar(255) DEFAULT NULL,
  `lastWorkingDay` datetime DEFAULT NULL,
  `linkedinProf` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `noticePeriod` int(11) NOT NULL,
  `noticeStatus` bit(1) NOT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `preferredLocation` varchar(255) DEFAULT NULL,
  `profile_url` varchar(255) DEFAULT NULL,
  `resumeLink` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `sourceDetails` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `totalExp` double NOT NULL,
  `twitterProf` varchar(255) DEFAULT NULL,
  `sourceEmail` varchar(255) DEFAULT NULL,
  `sourceMobile` varchar(255) DEFAULT NULL,
  `sourceName` varchar(255) DEFAULT NULL,
  `sourced_date` datetime DEFAULT NULL,
  PRIMARY KEY (`cid`),
  UNIQUE KEY `UK_qfut8ruekode092nlkipgl09g` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `candidate`
--

/*!40000 ALTER TABLE `candidate` DISABLE KEYS */;
/*!40000 ALTER TABLE `candidate` ENABLE KEYS */;

--
-- Table structure for table `candidate_activity`
--

DROP TABLE IF EXISTS `candidate_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `candidate_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `candidateId` varchar(255) DEFAULT NULL,
  `what` varchar(255) DEFAULT NULL,
  `whatTime` datetime DEFAULT NULL,
  `who` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `candidate_activity`
--

/*!40000 ALTER TABLE `candidate_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `candidate_activity` ENABLE KEYS */;

--
-- Table structure for table `candidate_candidate_file`
--

DROP TABLE IF EXISTS `candidate_candidate_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `candidate_candidate_file` (
  `candidate_cid` bigint(20) NOT NULL,
  `files_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_sitq3o2vvdcf6i8n4dhaqq6gh` (`files_id`),
  KEY `FK_exqiltuv2y2bfsbteiqry9cgo` (`candidate_cid`),
  CONSTRAINT `FK_exqiltuv2y2bfsbteiqry9cgo` FOREIGN KEY (`candidate_cid`) REFERENCES `candidate` (`cid`),
  CONSTRAINT `FK_sitq3o2vvdcf6i8n4dhaqq6gh` FOREIGN KEY (`files_id`) REFERENCES `candidate_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `candidate_candidate_file`
--

/*!40000 ALTER TABLE `candidate_candidate_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `candidate_candidate_file` ENABLE KEYS */;

--
-- Table structure for table `candidate_file`
--

DROP TABLE IF EXISTS `candidate_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `candidate_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `companyType` varchar(255) DEFAULT NULL,
  `fileName` varchar(255) DEFAULT NULL,
  `filePath` varchar(255) DEFAULT NULL,
  `fileType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `candidate_file`
--

/*!40000 ALTER TABLE `candidate_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `candidate_file` ENABLE KEYS */;

--
-- Table structure for table `candidate_file_audit`
--

DROP TABLE IF EXISTS `candidate_file_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `candidate_file_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `companyType` varchar(255) DEFAULT NULL,
  `fileName` varchar(255) DEFAULT NULL,
  `filePath` varchar(255) DEFAULT NULL,
  `fileType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_dgr52idjutkx2brnwt7iqj9wm` (`REV`),
  CONSTRAINT `FK_dgr52idjutkx2brnwt7iqj9wm` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `candidate_file_audit`
--

/*!40000 ALTER TABLE `candidate_file_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `candidate_file_audit` ENABLE KEYS */;

--
-- Table structure for table `candidate_key_skills`
--

DROP TABLE IF EXISTS `candidate_key_skills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `candidate_key_skills` (
  `Candidate_cid` bigint(20) NOT NULL,
  `keySkills` varchar(255) DEFAULT NULL,
  KEY `FK_lud6lfcan3pyf8bnfdpd3pfal` (`Candidate_cid`),
  CONSTRAINT `FK_lud6lfcan3pyf8bnfdpd3pfal` FOREIGN KEY (`Candidate_cid`) REFERENCES `candidate` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `candidate_key_skills`
--

/*!40000 ALTER TABLE `candidate_key_skills` DISABLE KEYS */;
/*!40000 ALTER TABLE `candidate_key_skills` ENABLE KEYS */;

--
-- Table structure for table `client`
--

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  `clientLocation` varchar(255) NOT NULL,
  `clientName` varchar(255) NOT NULL,
  `empSize` varchar(255) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `turnOvr` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_5x0a3tfwne9nywhdpoj5q0tl4` (`clientName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client`
--

/*!40000 ALTER TABLE `client` DISABLE KEYS */;
/*!40000 ALTER TABLE `client` ENABLE KEYS */;

--
-- Table structure for table `client_audit`
--

DROP TABLE IF EXISTS `client_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `clientLocation` varchar(255) DEFAULT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  `empSize` varchar(255) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `turnOvr` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_b8oju35xhcb10q81m7dx716u8` (`REV`),
  CONSTRAINT `FK_b8oju35xhcb10q81m7dx716u8` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client_audit`
--

/*!40000 ALTER TABLE `client_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `client_audit` ENABLE KEYS */;

--
-- Table structure for table `client_position_audit`
--

DROP TABLE IF EXISTS `client_position_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client_position_audit` (
  `REV` int(11) NOT NULL,
  `client_id` bigint(20) NOT NULL,
  `id` bigint(20) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`client_id`,`id`),
  CONSTRAINT `FK_b8118bx4m986fld6rtmekc48o` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client_position_audit`
--

/*!40000 ALTER TABLE `client_position_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `client_position_audit` ENABLE KEYS */;

--
-- Table structure for table `decision_maker`
--

DROP TABLE IF EXISTS `decision_maker`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `decision_maker` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `position_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kul96mv8jqs7d8ua6oo8ncf8s` (`email`,`client_id`,`position_id`),
  KEY `FK_70kem3glk9xkcmxlrnrry9kn2` (`client_id`),
  KEY `FK_lps2dtkrkcaeal1uxl9anau5n` (`position_id`),
  CONSTRAINT `FK_70kem3glk9xkcmxlrnrry9kn2` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`),
  CONSTRAINT `FK_lps2dtkrkcaeal1uxl9anau5n` FOREIGN KEY (`position_id`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `decision_maker`
--

/*!40000 ALTER TABLE `decision_maker` DISABLE KEYS */;
/*!40000 ALTER TABLE `decision_maker` ENABLE KEYS */;

--
-- Table structure for table `email_activity`
--

DROP TABLE IF EXISTS `email_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` varchar(255) DEFAULT NULL,
  `details` varchar(255) DEFAULT NULL,
  `emailFrom` varchar(255) DEFAULT NULL,
  `emailTo` varchar(255) DEFAULT NULL,
  `positionName` varchar(255) DEFAULT NULL,
  `attachmentLink` varchar(255) DEFAULT NULL,
  `body` text,
  `subject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_activity`
--

/*!40000 ALTER TABLE `email_activity` DISABLE KEYS */;
/*!40000 ALTER TABLE `email_activity` ENABLE KEYS */;

--
-- Table structure for table `email_template_data`
--

DROP TABLE IF EXISTS `email_template_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_template_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `body` text,
  `name` varchar(255) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `category` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_template_data`
--

/*!40000 ALTER TABLE `email_template_data` DISABLE KEYS */;
INSERT INTO `email_template_data` VALUES (1,NULL,NULL,'<p>Hi ${candidateName}, </p><p><br/></p><p><br/></p><p><br/></p><p>Thank you for applying with ${clientName}.  We are currently reviewing your experience and qualifications.  Should we determine that your profile meets our requirements, we will contact you with further inputs.</p><p><br/></p><p><br/></p><p><br/></p><p>We invite you to visit our website to learn more about our selection process, our people, our culture and how to connect with our social networks and talent communities.</p><p><br/></p> <p><br/></p><p>Thank you for your interest in career opportunities with ${clientName}.</p><p><br/></p><p><br/></p><p><br/></p><p>Regards, </p><p><br/></p><p>${hrName}</p><p><br/></p><p>${hrMobile}</p><p><br/></p>\n\n','Application Responses','Auto response','apply'),(2,NULL,NULL,'<p>Hi ${candidateName}, <br/><br/>Hope you\'re doing good ! <br/>I came across your Profile and found it to be quite interesting. We understand that you are looking for new opportunities and would like to connect with you to discuss an opportunity into ${clientName} as ${positionName}. <br/><br/>Please do let me know a suitable time along with your contact details so as to discuss and network further. Feel free to reach out to me for any concerns. <br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>','Candidate Reach-out','Profile Shortlisted','interview'),(3,NULL,NULL,'<p>Hi ${candidateName}, <br/><br/><br/>It was great talking to you about current opportunities with ${clientName}. <br/>As part of the Interview cycle we’d like to inform you, that your Profile has been shortlisted for further evaluation. We are scheduling  your Telephonic Interview schedule details along with this. <br/>Kindly confirm your availability for the same. <br/><br/>    In case of any queries, feel free to reach out to us.<br/>POC Name  : <br/>Contact#     : <br/>E-mail             :<br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>','Telephonic Screening','Telephonic Screening','interview'),(4,NULL,NULL,'<p>Hi ${candidateName}, <br/><br/><br/>As part of the Interview cycle, your Profile is being assessed and moved to the next (Technical) Interview round - Online Coding / Video Call / Online Assignment Test session and is scheduled as mentioned below.  <br/><br/><br/>To Connect to Online Meeting : <br/>From your Computer, please join online meeting using ____Browser. <br/>Go to :    [ additional info ] <br/>----------OR--------<br/>From your Computer, please join online meeting using Skype / Google Hangout / others. <br/>Add User :  <br/>Use your microphone and speakers (VoIP) for audio. You\'ll sound best with a headset. <br/><br/><br/>Please ensure you are having uninterrupted Internet connectivity throughout the session and we request you to be available about 5 minutes before the call. <br/>In case of any concern, feel free to reach out to us. <br/>POC Name  : <br/>Contact#     : <br/>E-mail             : <br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>','Online: Video Call / Online Assignment Test','Online: Video Call / Online Assignment Test','interview'),(5,NULL,NULL,'<p>Hi ${candidateName}, <br/><br/><br/>Your Face-to-Face meeting has been scheduled / rescheduled. Please find the details mentioned here. <br/><br/><br/>Office location: <br/>Organization Name:  <br/>Complete address:<br/>City:<br/>(Landmark: ) <br/>Google Map Location: <br/>Kindly try and reach about 5-10 minutes before scheduled time. <br/>Feel free to connect with us if you\'re unable to locate the Office. <br/>POC Name  : <br/>Contact#     : <br/>E-mail             : <br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>','Face-to-Face Meetings','Face-to-Face Meetings','interview'),(6,NULL,NULL,'<p>Hi ${candidateName}, <br/>Congratulations on your Offer !<br/>Further to your discussions / meetings with us, we are pleased to Offer you the position of ${positionName} at ${clientName}.<br/><br/><br/>Your Offer Letter is attached with this e-mail and your tentative Date of Joining is being considered as DD-MMM-YYYY. <br/><br/><br/>Please revert to this mail by tomorrow with your confirmation regarding the acceptance of Offer. <br/>Feel free to get in touch with us for any further clarifications. <br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>','Offer Letter','Offer Letter','email'),(7,NULL,NULL,'<p>Hi ${candidateName}, <br/><br/><br/>This is with regards to your Interview rounds with ${clientName} and we appreciate you taking the time to Interview with us.  After careful consideration, we regret to inform you that your Profile has not been selected for this position.<br/> <br/>However your information will remain in our system for any other job opportunities in future and we hope our paths cross again. <br/><br/><br/>Thank you for your interest in career opportunities with ${clientName}. <br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}</p>','Reject Mailer','Reject Mailer','email'),(8,NULL,NULL,'<p>Hi ${candidateName}, <br/><br/><br/>As part of Joining formalities, you are requested you to submit a Scan Copy of the following documents.<br/><br/><br/>1. Relieving Letter / Resignation acceptance letter from your current Employer<br/>2. Previous Employment letters<br/>3. Education Certificates<br/>4. PAN Card<br/>5. Aadhaar Card / Govt. issued Photo ID Card  <br/>6. Date of Birth details <br/><br/><br/><br/><br/>Regards, <br/>${hrName}<br/>${hrMobile}<br/><br/></p>','Docs Submission','Docs Submission Required','email'),(9,NULL,NULL,'<p dir=\"ltr\" style=\"text-align: justify;\" id=\"docs-internal-guid-a3a32881-5955-048e-37cb-e3b9eb1bd92a\"><span style=\"font-size: 13.333333333333332px;color: #222222;background-color: transparent;vertical-align: baseline;\">Hi, </span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #000000;background-color: transparent;vertical-align: baseline;\">Few candidate profiles for open position - </span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">${positionName}</span><span style=\"font-size: 13.333333333333332px;color: #000000;background-color: transparent;vertical-align: baseline;\"> have been forwarded to you for Review and Feedback.</span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #000000;background-color: transparent;vertical-align: baseline;\">Please click the link below to view and give your feedback.</span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #333333;background-color: transparent;vertical-align: baseline;\">Regards, </span><span style=\"font-size: 13.333333333333332px;color: #333333;background-color: transparent;vertical-align: baseline;\"><br class=\"kix-line-break\"/></span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">${hrName}</span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\"><br/></span></p><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">${hrMobile}</span></p>','Forward Profile','Forward Profile','forward'),(10,NULL,NULL,'<p dir=\"ltr\" style=\"text-align: justify;\" id=\"docs-internal-guid-a3a32881-5956-efdc-5f75-4f3061806278\"><span style=\"font-size: 13.333333333333332px;color: #222222;background-color: transparent;vertical-align: baseline;\">Hi </span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">${candidateName}</span><span style=\"font-size: 13.333333333333332px;color: #222222;background-color: transparent;vertical-align: baseline;\">, </span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #222222;background-color: #ffffff;vertical-align: baseline;\">We regret to inform you that your Interview/Meeting with </span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">${clientName} </span><span style=\"font-size: 13.333333333333332px;color: #222222;background-color: #ffffff;vertical-align: baseline;\">has been cancelled. We will keep you posted regarding the new schedule.</span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><p dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #000000;background-color: #ffffff;vertical-align: baseline;\">In case of any concern, feel free to reach out to us. </span></p><div dir=\"ltr\" style=\"text-align: justify;\"><br/></div><h4 dir=\"ltr\" style=\"text-align: justify;\"><span style=\"font-size: 13.333333333333332px;color: #333333;background-color: transparent;vertical-align: baseline;\">Regards, </span><span style=\"font-size: 13.333333333333332px;color: #333333;background-color: transparent;vertical-align: baseline;\"><br class=\"kix-line-break\"/></span><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">${hrName}</span></h4><p><span style=\"font-size: 13.333333333333332px;color: #4a86e8;background-color: transparent;vertical-align: baseline;\">${hrMobile}</span></p>','Interview Cancel','Interview Cancelled','interview_cancel');
/*!40000 ALTER TABLE `email_template_data` ENABLE KEYS */;

--
-- Table structure for table `event_attendee`
--

DROP TABLE IF EXISTS `event_attendee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_attendee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_attendee`
--

/*!40000 ALTER TABLE `event_attendee` DISABLE KEYS */;
/*!40000 ALTER TABLE `event_attendee` ENABLE KEYS */;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `feedback` varchar(255) DEFAULT NULL,
  `feedbackBy` varchar(255) DEFAULT NULL,
  `feedbackByMobile` varchar(255) DEFAULT NULL,
  `feedbackByName` varchar(255) DEFAULT NULL,
  `roundCandidateId` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `round_candidate` bigint(20) DEFAULT NULL,
  `candidateId` varchar(255) DEFAULT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  `positionName` varchar(255) DEFAULT NULL,
  `roundName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ocpnckctvg607at3jwghln9oo` (`round_candidate`),
  CONSTRAINT `FK_ocpnckctvg607at3jwghln9oo` FOREIGN KEY (`round_candidate`) REFERENCES `round_candidate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;

--
-- Table structure for table `feedback_audit`
--

DROP TABLE IF EXISTS `feedback_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feedback_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `feedback` varchar(255) DEFAULT NULL,
  `feedbackBy` varchar(255) DEFAULT NULL,
  `feedbackByMobile` varchar(255) DEFAULT NULL,
  `feedbackByName` varchar(255) DEFAULT NULL,
  `roundCandidateId` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `round_candidate` bigint(20) DEFAULT NULL,
  `candidateId` varchar(255) DEFAULT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  `positionName` varchar(255) DEFAULT NULL,
  `roundName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_hkwnphbuy30x3yh6svsu8tnd9` (`REV`),
  CONSTRAINT `FK_hkwnphbuy30x3yh6svsu8tnd9` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback_audit`
--

/*!40000 ALTER TABLE `feedback_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedback_audit` ENABLE KEYS */;

--
-- Table structure for table `forward_profile`
--

DROP TABLE IF EXISTS `forward_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forward_profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attachmentLink` varchar(255) DEFAULT NULL,
  `body` text,
  `date` datetime DEFAULT NULL,
  `emailFrom` varchar(255) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forward_profile`
--

/*!40000 ALTER TABLE `forward_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `forward_profile` ENABLE KEYS */;

--
-- Table structure for table `forwarded_candidate`
--

DROP TABLE IF EXISTS `forwarded_candidate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forwarded_candidate` (
  `id` bigint(20) NOT NULL,
  `roundCandidateId` varchar(255) DEFAULT NULL,
  KEY `FK_9ssi9hv0xrd3khnbcd0je88tt` (`id`),
  CONSTRAINT `FK_9ssi9hv0xrd3khnbcd0je88tt` FOREIGN KEY (`id`) REFERENCES `forward_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forwarded_candidate`
--

/*!40000 ALTER TABLE `forwarded_candidate` DISABLE KEYS */;
/*!40000 ALTER TABLE `forwarded_candidate` ENABLE KEYS */;

--
-- Table structure for table `interview_file`
--

DROP TABLE IF EXISTS `interview_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interview_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `file` longblob,
  `fileType` varchar(255) DEFAULT NULL,
  `schedule_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_5cd8tc78nf7s26v33aro9chmh` (`schedule_id`),
  CONSTRAINT `FK_5cd8tc78nf7s26v33aro9chmh` FOREIGN KEY (`schedule_id`) REFERENCES `interview_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_file`
--

/*!40000 ALTER TABLE `interview_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_file` ENABLE KEYS */;

--
-- Table structure for table `interview_hr`
--

DROP TABLE IF EXISTS `interview_hr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interview_hr` (
  `Schedule_ID` bigint(20) NOT NULL,
  `HR_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Schedule_ID`,`HR_ID`),
  KEY `FK_n2e3865m7dqo9ha31ayjbpn4y` (`HR_ID`),
  CONSTRAINT `FK_n2e3865m7dqo9ha31ayjbpn4y` FOREIGN KEY (`HR_ID`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FK_qimi1ywt4jnp6aqft8qobd3h1` FOREIGN KEY (`Schedule_ID`) REFERENCES `interview_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_hr`
--

/*!40000 ALTER TABLE `interview_hr` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_hr` ENABLE KEYS */;

--
-- Table structure for table `interview_panel`
--

DROP TABLE IF EXISTS `interview_panel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interview_panel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fhtefi4umd3mmacutko48r647` (`email`,`client_id`),
  KEY `FK_r74vfpmpd03iyk2uw02pxcvnw` (`client_id`),
  CONSTRAINT `FK_r74vfpmpd03iyk2uw02pxcvnw` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_panel`
--

/*!40000 ALTER TABLE `interview_panel` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_panel` ENABLE KEYS */;

--
-- Table structure for table `interview_schedule`
--

DROP TABLE IF EXISTS `interview_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interview_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `candidateAccepted` varchar(255) DEFAULT NULL,
  `candidateEmail` varchar(255) DEFAULT NULL,
  `candidateEventId` varchar(255) DEFAULT NULL,
  `candidateName` varchar(255) DEFAULT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  `endsAt` datetime DEFAULT NULL,
  `interviewSchedulerEmail` varchar(255) DEFAULT NULL,
  `interviewSchedulerName` varchar(255) DEFAULT NULL,
  `interviewerEventId` varchar(255) DEFAULT NULL,
  `notes` longtext,
  `positionCode` varchar(255) DEFAULT NULL,
  `positionName` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `roundName` varchar(255) DEFAULT NULL,
  `roundType` varchar(255) DEFAULT NULL,
  `startsAt` datetime DEFAULT NULL,
  `templateName` varchar(255) DEFAULT NULL,
  `templateSubject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_schedule`
--

/*!40000 ALTER TABLE `interview_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_schedule` ENABLE KEYS */;

--
-- Table structure for table `interview_schedule_audit`
--

DROP TABLE IF EXISTS `interview_schedule_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interview_schedule_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `candidateAccepted` varchar(255) DEFAULT NULL,
  `candidateEmail` varchar(255) DEFAULT NULL,
  `candidateEventId` varchar(255) DEFAULT NULL,
  `candidateName` varchar(255) DEFAULT NULL,
  `clientName` varchar(255) DEFAULT NULL,
  `endsAt` datetime DEFAULT NULL,
  `interviewSchedulerEmail` varchar(255) DEFAULT NULL,
  `interviewSchedulerName` varchar(255) DEFAULT NULL,
  `interviewerEventId` varchar(255) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `positionName` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `roundName` varchar(255) DEFAULT NULL,
  `roundType` varchar(255) DEFAULT NULL,
  `startsAt` datetime DEFAULT NULL,
  `templateName` varchar(255) DEFAULT NULL,
  `templateSubject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_5haso36799v9dbpbybr0b6khw` (`REV`),
  CONSTRAINT `FK_5haso36799v9dbpbybr0b6khw` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_schedule_audit`
--

/*!40000 ALTER TABLE `interview_schedule_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_schedule_audit` ENABLE KEYS */;

--
-- Table structure for table `interview_schedule_event_attendee`
--

DROP TABLE IF EXISTS `interview_schedule_event_attendee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interview_schedule_event_attendee` (
  `interview_schedule_id` bigint(20) NOT NULL,
  `attendee_id` bigint(20) NOT NULL,
  PRIMARY KEY (`interview_schedule_id`,`attendee_id`),
  UNIQUE KEY `UK_422knaejm4w9jsuf7cuostfsr` (`attendee_id`),
  CONSTRAINT `FK_422knaejm4w9jsuf7cuostfsr` FOREIGN KEY (`attendee_id`) REFERENCES `event_attendee` (`id`),
  CONSTRAINT `FK_sh2puo7hcqi7k9398qr5momjl` FOREIGN KEY (`interview_schedule_id`) REFERENCES `interview_schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_schedule_event_attendee`
--

/*!40000 ALTER TABLE `interview_schedule_event_attendee` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_schedule_event_attendee` ENABLE KEYS */;

--
-- Table structure for table `interview_schedule_event_attendee_audit`
--

DROP TABLE IF EXISTS `interview_schedule_event_attendee_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interview_schedule_event_attendee_audit` (
  `REV` int(11) NOT NULL,
  `interview_schedule_id` bigint(20) NOT NULL,
  `attendee_id` bigint(20) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`interview_schedule_id`,`attendee_id`),
  CONSTRAINT `FK_lnnfo32mx4x7u8stk5q6thyna` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_schedule_event_attendee_audit`
--

/*!40000 ALTER TABLE `interview_schedule_event_attendee_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_schedule_event_attendee_audit` ENABLE KEYS */;

--
-- Table structure for table `jv_commit`
--

DROP TABLE IF EXISTS `jv_commit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jv_commit` (
  `commit_pk` bigint(20) NOT NULL AUTO_INCREMENT,
  `author` varchar(200) DEFAULT NULL,
  `commit_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `commit_id` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`commit_pk`),
  KEY `jv_commit_commit_id_idx` (`commit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jv_commit`
--

/*!40000 ALTER TABLE `jv_commit` DISABLE KEYS */;
/*!40000 ALTER TABLE `jv_commit` ENABLE KEYS */;

--
-- Table structure for table `jv_commit_property`
--

DROP TABLE IF EXISTS `jv_commit_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jv_commit_property` (
  `property_name` varchar(200) NOT NULL DEFAULT '',
  `property_value` varchar(200) DEFAULT NULL,
  `commit_fk` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`commit_fk`,`property_name`),
  KEY `jv_commit_property_commit_fk_idx` (`commit_fk`),
  KEY `jv_commit_property_property_name_property_value_idx` (`property_name`,`property_value`),
  CONSTRAINT `jv_commit_property_commit_fk` FOREIGN KEY (`commit_fk`) REFERENCES `jv_commit` (`commit_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jv_commit_property`
--

/*!40000 ALTER TABLE `jv_commit_property` DISABLE KEYS */;
/*!40000 ALTER TABLE `jv_commit_property` ENABLE KEYS */;

--
-- Table structure for table `jv_global_id`
--

DROP TABLE IF EXISTS `jv_global_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jv_global_id` (
  `global_id_pk` bigint(20) NOT NULL AUTO_INCREMENT,
  `local_id` varchar(200) DEFAULT NULL,
  `fragment` varchar(200) DEFAULT NULL,
  `type_name` varchar(200) DEFAULT NULL,
  `owner_id_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`global_id_pk`),
  KEY `jv_global_id_owner_id_fk` (`owner_id_fk`),
  KEY `jv_global_id_local_id_idx` (`local_id`),
  CONSTRAINT `jv_global_id_owner_id_fk` FOREIGN KEY (`owner_id_fk`) REFERENCES `jv_global_id` (`global_id_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jv_global_id`
--

/*!40000 ALTER TABLE `jv_global_id` DISABLE KEYS */;
/*!40000 ALTER TABLE `jv_global_id` ENABLE KEYS */;

--
-- Table structure for table `jv_snapshot`
--

DROP TABLE IF EXISTS `jv_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jv_snapshot` (
  `snapshot_pk` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(200) DEFAULT NULL,
  `version` bigint(20) DEFAULT NULL,
  `state` text,
  `changed_properties` text,
  `managed_type` varchar(200) DEFAULT NULL,
  `global_id_fk` bigint(20) DEFAULT NULL,
  `commit_fk` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`snapshot_pk`),
  KEY `jv_snapshot_global_id_fk_idx` (`global_id_fk`),
  KEY `jv_snapshot_commit_fk_idx` (`commit_fk`),
  CONSTRAINT `jv_snapshot_commit_fk` FOREIGN KEY (`commit_fk`) REFERENCES `jv_commit` (`commit_pk`),
  CONSTRAINT `jv_snapshot_global_id_fk` FOREIGN KEY (`global_id_fk`) REFERENCES `jv_global_id` (`global_id_pk`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jv_snapshot`
--

/*!40000 ALTER TABLE `jv_snapshot` DISABLE KEYS */;
/*!40000 ALTER TABLE `jv_snapshot` ENABLE KEYS */;

--
-- Table structure for table `organization`
--

DROP TABLE IF EXISTS `organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organization` (
  `org_id` varchar(255) NOT NULL,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `org_name` varchar(255) DEFAULT NULL,
  `orgType` varchar(255) DEFAULT NULL,
  `time_preference` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`org_id`),
  UNIQUE KEY `UK_8xwh6htjvm2c39c2se8hbptj9` (`org_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organization`
--

/*!40000 ALTER TABLE `organization` DISABLE KEYS */;
/*!40000 ALTER TABLE `organization` ENABLE KEYS */;

--
-- Table structure for table `organization_audit`
--

DROP TABLE IF EXISTS `organization_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organization_audit` (
  `org_id` varchar(255) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `org_name` varchar(255) DEFAULT NULL,
  `orgType` varchar(255) DEFAULT NULL,
  `time_preference` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`org_id`,`REV`),
  KEY `FK_pn66jjapn7mtkhcbvhbxa9go9` (`REV`),
  CONSTRAINT `FK_pn66jjapn7mtkhcbvhbxa9go9` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `organization_audit`
--

/*!40000 ALTER TABLE `organization_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `organization_audit` ENABLE KEYS */;

--
-- Table structure for table `parser_count`
--

DROP TABLE IF EXISTS `parser_count`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `parser_count` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `usedBy` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parser_count`
--

/*!40000 ALTER TABLE `parser_count` DISABLE KEYS */;
/*!40000 ALTER TABLE `parser_count` ENABLE KEYS */;

--
-- Table structure for table `parser_count_audit`
--

DROP TABLE IF EXISTS `parser_count_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `parser_count_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `usedBy` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_p2h0j6xwrayaoip5f8c6i4rqe` (`REV`),
  CONSTRAINT `FK_p2h0j6xwrayaoip5f8c6i4rqe` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `parser_count_audit`
--

/*!40000 ALTER TABLE `parser_count_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `parser_count_audit` ENABLE KEYS */;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `permission` (
  `role_name` bigint(20) NOT NULL,
  `permissionName` varchar(255) DEFAULT NULL,
  KEY `FK_c8i4qxf7i5nl5648ywlepuoad` (`role_name`),
  CONSTRAINT `FK_c8i4qxf7i5nl5648ywlepuoad` FOREIGN KEY (`role_name`) REFERENCES `user_roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;

--
-- Table structure for table `points`
--

DROP TABLE IF EXISTS `points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `points` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `points` double NOT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `roundCandidate_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_qtf7rm40p4cq7jdss79th3sad` (`roundCandidate_id`),
  CONSTRAINT `FK_qtf7rm40p4cq7jdss79th3sad` FOREIGN KEY (`roundCandidate_id`) REFERENCES `round_candidate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points`
--

/*!40000 ALTER TABLE `points` DISABLE KEYS */;
/*!40000 ALTER TABLE `points` ENABLE KEYS */;

--
-- Table structure for table `points_audit`
--

DROP TABLE IF EXISTS `points_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `points_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `points` double DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `roundCandidate_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_qd3n2aoh4ubfal0ylmjc5bo8n` (`REV`),
  CONSTRAINT `FK_qd3n2aoh4ubfal0ylmjc5bo8n` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_audit`
--

/*!40000 ALTER TABLE `points_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `points_audit` ENABLE KEYS */;

--
-- Table structure for table `position`
--

DROP TABLE IF EXISTS `position`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `clientStatus` varchar(255) DEFAULT NULL,
  `closeByDate` datetime DEFAULT NULL,
  `description` text,
  `jdPath` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `maxSal` double NOT NULL,
  `notes` text,
  `openedDate` datetime DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `positionUrl` varchar(255) DEFAULT NULL,
  `remoteWork` bit(1) NOT NULL,
  `salUnit` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `totalPosition` int(11) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `board_id` bigint(20) DEFAULT NULL,
  `client_id` bigint(20) DEFAULT NULL,
  `experienceRange` varchar(255) NOT NULL,
  `functionalArea` varchar(255) NOT NULL,
  `industry` varchar(255) NOT NULL,
  `minSal` double NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_87dvpe8f5ujlixh0gbxnjnyy0` (`positionCode`),
  KEY `FK_a6v3vnh3yrxeaph5qdehb9x92` (`board_id`),
  KEY `FK_jourrs42ujx0dkg3fijchjq30` (`client_id`),
  CONSTRAINT `FK_a6v3vnh3yrxeaph5qdehb9x92` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`),
  CONSTRAINT `FK_jourrs42ujx0dkg3fijchjq30` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position`
--

/*!40000 ALTER TABLE `position` DISABLE KEYS */;
/*!40000 ALTER TABLE `position` ENABLE KEYS */;

--
-- Table structure for table `position_audit`
--

DROP TABLE IF EXISTS `position_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `clientStatus` varchar(255) DEFAULT NULL,
  `closeByDate` datetime DEFAULT NULL,
  `description` text,
  `jdPath` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `maxSal` double DEFAULT NULL,
  `notes` text,
  `openedDate` datetime DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `positionUrl` varchar(255) DEFAULT NULL,
  `remoteWork` bit(1) DEFAULT NULL,
  `salUnit` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `totalPosition` int(11) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `board_id` bigint(20) DEFAULT NULL,
  `experienceRange` varchar(255) DEFAULT NULL,
  `functionalArea` varchar(255) DEFAULT NULL,
  `industry` varchar(255) DEFAULT NULL,
  `minSal` double DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_f9yk2m8pdr63wlyfqeifapiix` (`REV`),
  CONSTRAINT `FK_f9yk2m8pdr63wlyfqeifapiix` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_audit`
--

/*!40000 ALTER TABLE `position_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_audit` ENABLE KEYS */;

--
-- Table structure for table `position_educationa_qualification`
--

DROP TABLE IF EXISTS `position_educationa_qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_educationa_qualification` (
  `Position_id` bigint(20) NOT NULL,
  `educationalQualification` varchar(255) DEFAULT NULL,
  KEY `FK_8tev3upy55xinpgff2cx1rbpl` (`Position_id`),
  CONSTRAINT `FK_8tev3upy55xinpgff2cx1rbpl` FOREIGN KEY (`Position_id`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_educationa_qualification`
--

/*!40000 ALTER TABLE `position_educationa_qualification` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_educationa_qualification` ENABLE KEYS */;

--
-- Table structure for table `position_educationa_qualification_audit`
--

DROP TABLE IF EXISTS `position_educationa_qualification_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_educationa_qualification_audit` (
  `REV` int(11) NOT NULL,
  `Position_id` bigint(20) NOT NULL,
  `educationalQualification` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`Position_id`,`educationalQualification`),
  CONSTRAINT `FK_5spyhqhppn38miog62esmv46c` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_educationa_qualification_audit`
--

/*!40000 ALTER TABLE `position_educationa_qualification_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_educationa_qualification_audit` ENABLE KEYS */;

--
-- Table structure for table `position_good_skill_set`
--

DROP TABLE IF EXISTS `position_good_skill_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_good_skill_set` (
  `Position_id` bigint(20) NOT NULL,
  `goodSkillSet` varchar(255) DEFAULT NULL,
  KEY `FK_cyolk9qoyd3skxynhg8r1725u` (`Position_id`),
  CONSTRAINT `FK_cyolk9qoyd3skxynhg8r1725u` FOREIGN KEY (`Position_id`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_good_skill_set`
--

/*!40000 ALTER TABLE `position_good_skill_set` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_good_skill_set` ENABLE KEYS */;

--
-- Table structure for table `position_good_skill_set_audit`
--

DROP TABLE IF EXISTS `position_good_skill_set_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_good_skill_set_audit` (
  `REV` int(11) NOT NULL,
  `Position_id` bigint(20) NOT NULL,
  `goodSkillSet` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`Position_id`,`goodSkillSet`),
  CONSTRAINT `FK_2kxexstiaji34aldjjkrlvsy6` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_good_skill_set_audit`
--

/*!40000 ALTER TABLE `position_good_skill_set_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_good_skill_set_audit` ENABLE KEYS */;

--
-- Table structure for table `position_hr`
--

DROP TABLE IF EXISTS `position_hr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_hr` (
  `Position_ID` bigint(20) NOT NULL,
  `HR_ID` bigint(20) NOT NULL,
  PRIMARY KEY (`Position_ID`,`HR_ID`),
  KEY `FK_lvg0tt37ya0o3nfcikppg8sf7` (`HR_ID`),
  CONSTRAINT `FK_4h382skr9c0mtm9sa6qvlke8p` FOREIGN KEY (`Position_ID`) REFERENCES `position` (`id`),
  CONSTRAINT `FK_lvg0tt37ya0o3nfcikppg8sf7` FOREIGN KEY (`HR_ID`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_hr`
--

/*!40000 ALTER TABLE `position_hr` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_hr` ENABLE KEYS */;

--
-- Table structure for table `position_interviewer`
--

DROP TABLE IF EXISTS `position_interviewer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_interviewer` (
  `Position_ID` bigint(20) NOT NULL,
  `interviewer_Id` bigint(20) NOT NULL,
  PRIMARY KEY (`Position_ID`,`interviewer_Id`),
  KEY `FK_g3w64c28g8nkcud0nucgsta9v` (`interviewer_Id`),
  CONSTRAINT `FK_g3w64c28g8nkcud0nucgsta9v` FOREIGN KEY (`interviewer_Id`) REFERENCES `interview_panel` (`id`),
  CONSTRAINT `FK_jg44b4ltq2bydnblmmcvd2ydf` FOREIGN KEY (`Position_ID`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_interviewer`
--

/*!40000 ALTER TABLE `position_interviewer` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_interviewer` ENABLE KEYS */;

--
-- Table structure for table `position_req_skill_set`
--

DROP TABLE IF EXISTS `position_req_skill_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_req_skill_set` (
  `Position_id` bigint(20) NOT NULL,
  `reqSkillSet` varchar(255) DEFAULT NULL,
  KEY `FK_qn5t74ngtuyg69w4pheos2bux` (`Position_id`),
  CONSTRAINT `FK_qn5t74ngtuyg69w4pheos2bux` FOREIGN KEY (`Position_id`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_req_skill_set`
--

/*!40000 ALTER TABLE `position_req_skill_set` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_req_skill_set` ENABLE KEYS */;

--
-- Table structure for table `position_req_skill_set_audit`
--

DROP TABLE IF EXISTS `position_req_skill_set_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_req_skill_set_audit` (
  `REV` int(11) NOT NULL,
  `Position_id` bigint(20) NOT NULL,
  `reqSkillSet` varchar(255) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`REV`,`Position_id`,`reqSkillSet`),
  CONSTRAINT `FK_2fel445xj7o4g9gwd1idhgqjv` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_req_skill_set_audit`
--

/*!40000 ALTER TABLE `position_req_skill_set_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_req_skill_set_audit` ENABLE KEYS */;

--
-- Table structure for table `position_vendors`
--

DROP TABLE IF EXISTS `position_vendors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `position_vendors` (
  `Position_ID` bigint(20) NOT NULL,
  `vendor` bigint(20) NOT NULL,
  PRIMARY KEY (`Position_ID`,`vendor`),
  KEY `FK_lbywy8it3c5j6xfjnlkgs21t` (`vendor`),
  CONSTRAINT `FK_lbywy8it3c5j6xfjnlkgs21t` FOREIGN KEY (`vendor`) REFERENCES `vendor` (`id`),
  CONSTRAINT `FK_m0qugiyvblrnlkvo6fwakv976` FOREIGN KEY (`Position_ID`) REFERENCES `position` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `position_vendors`
--

/*!40000 ALTER TABLE `position_vendors` DISABLE KEYS */;
/*!40000 ALTER TABLE `position_vendors` ENABLE KEYS */;

--
-- Table structure for table `presignup`
--

DROP TABLE IF EXISTS `presignup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `presignup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_k84s4pecwjkcgd6xmnhayfbvi` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presignup`
--

/*!40000 ALTER TABLE `presignup` DISABLE KEYS */;
/*!40000 ALTER TABLE `presignup` ENABLE KEYS */;

--
-- Table structure for table `profile_reciever`
--

DROP TABLE IF EXISTS `profile_reciever`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile_reciever` (
  `id` bigint(20) NOT NULL,
  `profileReciever` varchar(255) DEFAULT NULL,
  KEY `FK_qo82xfg791c4ot1idbxmjv9mw` (`id`),
  CONSTRAINT `FK_qo82xfg791c4ot1idbxmjv9mw` FOREIGN KEY (`id`) REFERENCES `forward_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_reciever`
--

/*!40000 ALTER TABLE `profile_reciever` DISABLE KEYS */;
/*!40000 ALTER TABLE `profile_reciever` ENABLE KEYS */;

--
-- Table structure for table `round_candidate`
--

DROP TABLE IF EXISTS `round_candidate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `round_candidate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `candidate_cid` bigint(20) DEFAULT NULL,
  `round_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ilguuofiykesloawwtrb4aytd` (`candidate_cid`),
  KEY `FK_2eauol6lcs98tkcgai5fc3kw2` (`round_id`),
  CONSTRAINT `FK_2eauol6lcs98tkcgai5fc3kw2` FOREIGN KEY (`round_id`) REFERENCES `rounds` (`id`),
  CONSTRAINT `FK_ilguuofiykesloawwtrb4aytd` FOREIGN KEY (`candidate_cid`) REFERENCES `candidate` (`cid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `round_candidate`
--

/*!40000 ALTER TABLE `round_candidate` DISABLE KEYS */;
/*!40000 ALTER TABLE `round_candidate` ENABLE KEYS */;

--
-- Table structure for table `round_candidate_audit`
--

DROP TABLE IF EXISTS `round_candidate_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `round_candidate_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `positionCode` varchar(255) DEFAULT NULL,
  `roundId` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `candidate_cid` bigint(20) DEFAULT NULL,
  `round_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_1xjjdyslw794phj28g8qghl41` (`REV`),
  CONSTRAINT `FK_1xjjdyslw794phj28g8qghl41` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `round_candidate_audit`
--

/*!40000 ALTER TABLE `round_candidate_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `round_candidate_audit` ENABLE KEYS */;

--
-- Table structure for table `rounds`
--

DROP TABLE IF EXISTS `rounds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rounds` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `orderNo` int(11) NOT NULL,
  `roundName` varchar(255) NOT NULL,
  `roundType` varchar(255) DEFAULT NULL,
  `board_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_kjngqxy5xmv5k7bv35hmbrut2` (`board_id`),
  CONSTRAINT `FK_kjngqxy5xmv5k7bv35hmbrut2` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rounds`
--

/*!40000 ALTER TABLE `rounds` DISABLE KEYS */;
/*!40000 ALTER TABLE `rounds` ENABLE KEYS */;

--
-- Table structure for table `rounds_audit`
--

DROP TABLE IF EXISTS `rounds_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rounds_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `orderNo` int(11) DEFAULT NULL,
  `roundName` varchar(255) DEFAULT NULL,
  `roundType` varchar(255) DEFAULT NULL,
  `board_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_p22nqhs5c118kev9cyona3o6u` (`REV`),
  CONSTRAINT `FK_p22nqhs5c118kev9cyona3o6u` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rounds_audit`
--

/*!40000 ALTER TABLE `rounds_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `rounds_audit` ENABLE KEYS */;

--
-- Table structure for table `task_folder`
--

DROP TABLE IF EXISTS `task_folder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_folder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `owner_user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_c3uf4eitgysua7kob3tw9u87t` (`owner_user_id`),
  CONSTRAINT `FK_c3uf4eitgysua7kob3tw9u87t` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_folder`
--

/*!40000 ALTER TABLE `task_folder` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_folder` ENABLE KEYS */;

--
-- Table structure for table `task_item`
--

DROP TABLE IF EXISTS `task_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `due_date_time` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `notes` varchar(10000) DEFAULT NULL,
  `reminder_date_time` datetime DEFAULT NULL,
  `reminderPeriod` int(11) DEFAULT NULL,
  `reminderPeriodType` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `owner_user_id` bigint(20) DEFAULT NULL,
  `taskFolder_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_70rqo2fk2ydj547r62l8bydi` (`owner_user_id`),
  KEY `FK_en5ed6jqksukr5x9a4wtp03j4` (`taskFolder_id`),
  CONSTRAINT `FK_70rqo2fk2ydj547r62l8bydi` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FK_en5ed6jqksukr5x9a4wtp03j4` FOREIGN KEY (`taskFolder_id`) REFERENCES `task_folder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_item`
--

/*!40000 ALTER TABLE `task_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_item` ENABLE KEYS */;

--
-- Table structure for table `task_item_user`
--

DROP TABLE IF EXISTS `task_item_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_item_user` (
  `task_item_id` bigint(20) NOT NULL,
  `users_user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`task_item_id`,`users_user_id`),
  KEY `FK_c2e51yqqk0jurqr5016donn3y` (`users_user_id`),
  CONSTRAINT `FK_3s8as7pxgudb5xlroig51efkj` FOREIGN KEY (`task_item_id`) REFERENCES `task_item` (`id`),
  CONSTRAINT `FK_c2e51yqqk0jurqr5016donn3y` FOREIGN KEY (`users_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_item_user`
--

/*!40000 ALTER TABLE `task_item_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `task_item_user` ENABLE KEYS */;

--
-- Table structure for table `template_variable`
--

DROP TABLE IF EXISTS `template_variable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `template_variable` (
  `templateId` bigint(20) NOT NULL,
  `templateVariable` varchar(255) DEFAULT NULL,
  KEY `FK_2p09m0eat9howxqh6yg6gsif2` (`templateId`),
  CONSTRAINT `FK_2p09m0eat9howxqh6yg6gsif2` FOREIGN KEY (`templateId`) REFERENCES `email_template_data` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `template_variable`
--

/*!40000 ALTER TABLE `template_variable` DISABLE KEYS */;
/*!40000 ALTER TABLE `template_variable` ENABLE KEYS */;

--
-- Table structure for table `tenant_resolver`
--

DROP TABLE IF EXISTS `tenant_resolver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tenant_resolver` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `org_id` varchar(255) NOT NULL,
  `organization_name` varchar(255) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenant_resolver`
--


--
-- Table structure for table `time_slot`
--

DROP TABLE IF EXISTS `time_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `time_slot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  `startTime` datetime DEFAULT NULL,
  `clientInterviewerPanel_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ltdy69wqn4w9wexhpk7tbe1e5` (`clientInterviewerPanel_id`),
  CONSTRAINT `FK_ltdy69wqn4w9wexhpk7tbe1e5` FOREIGN KEY (`clientInterviewerPanel_id`) REFERENCES `interview_panel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `time_slot`
--

/*!40000 ALTER TABLE `time_slot` DISABLE KEYS */;
/*!40000 ALTER TABLE `time_slot` ENABLE KEYS */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `account_status` bit(1) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `joinedDate_date` datetime DEFAULT NULL,
  `joined_status` bit(1) DEFAULT NULL,
  `locale` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `profile_url` varchar(255) DEFAULT NULL,
  `timezone` varchar(255) DEFAULT NULL,
  `organization_org_id` varchar(255) DEFAULT NULL,
  `role` bigint(20) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `marked_delete` bit(1) DEFAULT NULL,
  `user_type` varchar(255) DEFAULT NULL,
  `vendor_id` varchar(255) DEFAULT NULL,
  `profile_signature` LONGTEXT DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`),
  KEY `FK_lnqes3rmmq5ud6ximh1eua7jt` (`organization_org_id`),
  KEY `FK_dl7g53f7lpmorjc24kx74apx8` (`role`),
  CONSTRAINT `FK_dl7g53f7lpmorjc24kx74apx8` FOREIGN KEY (`role`) REFERENCES `user_roles` (`id`),
  CONSTRAINT `FK_lnqes3rmmq5ud6ximh1eua7jt` FOREIGN KEY (`organization_org_id`) REFERENCES `organization` (`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--


--
-- Table structure for table `user_audit`
--

DROP TABLE IF EXISTS `user_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_audit` (
  `user_id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `account_status` bit(1) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `joinedDate_date` datetime DEFAULT NULL,
  `joined_status` bit(1) DEFAULT NULL,
  `locale` varchar(255) DEFAULT NULL,
  `mobile` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `profile_url` varchar(255) DEFAULT NULL,
  `timezone` varchar(255) DEFAULT NULL,
  `organization_org_id` varchar(255) DEFAULT NULL,
  `role` bigint(20) DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `marked_delete` bit(1) DEFAULT NULL,
  `user_type` varchar(255) DEFAULT NULL,
  `vendor_id` varchar(255) DEFAULT NULL,
  `profile_signature` LONGTEXT DEFAULT NULL,
  PRIMARY KEY (`user_id`,`REV`),
  KEY `FK_d8801nxqtgkt1miibqmq9w1ng` (`REV`),
  CONSTRAINT `FK_d8801nxqtgkt1miibqmq9w1ng` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_audit`
--

/*!40000 ALTER TABLE `user_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_audit` ENABLE KEYS */;

--
-- Table structure for table `user_notification`
--

DROP TABLE IF EXISTS `user_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `details` varchar(255) DEFAULT NULL,
  `forDate` varchar(255) DEFAULT NULL,
  `notification_title` varchar(255) DEFAULT NULL,
  `userBy` varchar(255) DEFAULT NULL,
  `userFor` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_notification`
--

/*!40000 ALTER TABLE `user_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_notification` ENABLE KEYS */;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `modification_date` datetime DEFAULT NULL,
  `role_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_40fvvy071dnqy9tywk6ei7f5r` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;

--
-- Table structure for table `user_social_connection`
--

DROP TABLE IF EXISTS `user_social_connection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_social_connection` (
  `providerId` varchar(255) NOT NULL,
  `providerUserId` varchar(255) NOT NULL,
  `userId` varchar(255) NOT NULL,
  `accessToken` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `expireTime` bigint(20) DEFAULT NULL,
  `imageUrl` varchar(255) DEFAULT NULL,
  `profileUrl` varchar(255) DEFAULT NULL,
  `rank` int(11) NOT NULL,
  `refreshToken` varchar(255) DEFAULT NULL,
  `secret` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`providerId`,`providerUserId`,`userId`),
  UNIQUE KEY `UK_96ikam5ushi9pt7lrp2j21fuo` (`userId`,`providerId`,`rank`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_social_connection`
--

/*!40000 ALTER TABLE `user_social_connection` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_social_connection` ENABLE KEYS */;

--
-- Table structure for table `vendor`
--

DROP TABLE IF EXISTS `vendor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vendor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `organization_org_id` varchar(255) DEFAULT NULL,
  `address` text,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_oo17anr6ieuks6rp0rf6yy6ed` (`organization_org_id`),
  CONSTRAINT `FK_oo17anr6ieuks6rp0rf6yy6ed` FOREIGN KEY (`organization_org_id`) REFERENCES `organization` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor`
--

/*!40000 ALTER TABLE `vendor` DISABLE KEYS */;
/*!40000 ALTER TABLE `vendor` ENABLE KEYS */;

--
-- Table structure for table `vendor_audit`
--

DROP TABLE IF EXISTS `vendor_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vendor_audit` (
  `id` bigint(20) NOT NULL,
  `REV` int(11) NOT NULL,
  `REVTYPE` tinyint(4) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `organization_org_id` varchar(255) DEFAULT NULL,
  `address` text,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`,`REV`),
  KEY `FK_q46hc6wxk0sp3dkmwj1ljgiw6` (`REV`),
  CONSTRAINT `FK_q46hc6wxk0sp3dkmwj1ljgiw6` FOREIGN KEY (`REV`) REFERENCES `auditentity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_audit`
--

/*!40000 ALTER TABLE `vendor_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `vendor_audit` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-12-07 20:30:39