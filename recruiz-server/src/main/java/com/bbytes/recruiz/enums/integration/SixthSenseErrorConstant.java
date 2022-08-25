package com.bbytes.recruiz.enums.integration;

import com.bbytes.recruiz.utils.ErrorHandler;

public enum SixthSenseErrorConstant {

	Success(0, "Success", ErrorHandler.SIXTH_SENSE_SUCCESS),
	Success_Exist_User(123, "Success", ErrorHandler.SIXTH_SENSE_SUCCESS),
	Licence_Expired(1, "Sixth Sense License Expired", ErrorHandler.SIXTH_SENSE_LICENCE_EXPIRED),
	Max_concurrent_user(16, "All portal users are on-line. Please try after sometime!", ErrorHandler.SIXTH_SENSE_MAX_CONCURRENT_USER_REACHED),
	JWT_Failure(3,"JWT Verification Failure", ErrorHandler.SIXTH_SENSE_JWT_Failure),
	Content_Type_Missing(4,"Input ContentType is not a JSON - got --> PLACEHOLDER", ErrorHandler.SIXTH_SENSE_CONTENT_TYPE_MISSING),
	Input_Json_Inproper(5,"Input JSON construct not proper", ErrorHandler.SIXTH_SENSE_CONTENT_TYPE_MISSING),
	Session_Invalid(6,"Session Invalid / Expired", ErrorHandler.SIXTH_SENSE_SESSION_INVALID),
	Input_Json_Empty(7,"Empty Input JSON", ErrorHandler.SIXTH_SENSE_INPUT_JSON_EMPTY),
	Invalid_Client_Id(8, "Invalid Client Id", ErrorHandler.SIXTH_SENSE_INVALID_CLIENT_ID),
	Mysql_Connection_Err(11,"System MySql connection error - try logging again", ErrorHandler.SIXTH_SENSE_MYSQL_CONNECTION_ERROR),
	Credential_Case_Sensitive(12,"User Name and Password are Case Sensitive - Pls try again", ErrorHandler.SIXTH_SENSE_CREDENTIAL_CASE_SENSITIVE),
	Invalid_UserName(13,"Invalid User Name", ErrorHandler.SIXTH_SENSE_INVALID_USERNAME),
	User_UI_Access(14,"User --> < PLACEHOLDER > has UI based access. Contact administrator to enable interactive access", ErrorHandler.SIXTH_SENSE_USER_UI_ACCESS),
	FatalError(153,"FATAL: Error while profile view processing --> Invalid Operation. Please Conduct a Search before the View Operation.", ErrorHandler.SIXTH_SENSE_LOGGEDIN_ANOTHER_SYSTEM),
	LoggedIn_Another_System(15,"The same user --> < PLACEHOLDER > has logged in another system. Use 'renewSession()' Reset", ErrorHandler.SIXTH_SENSE_LOGGEDIN_ANOTHER_SYSTEM),
	API_User_Online(16,"All API based users are on-line. Please try after sometime!", ErrorHandler.SIXTH_SENSE_API_USER_ONLINE),
	Unauthoried_Access(17,"User Authorization Failed -> Contact administrator to check for authorised period of access!", ErrorHandler.SIXTH_SENSE_UNAUTHORISED_ACCESS),
	Unauthoried_Invalid_Credential(18,"User Authorization Failed -> User Name / Password invalid - Pls try again!", ErrorHandler.SIXTH_SENSE_UNAUTHORISED_INVALID_CREDENTIAL),
	Close_Session_Failure(19,"Closing Session Failure", ErrorHandler.SIXTH_SENSE_UNAUTHORISED_INVALID_CREDENTIAL),
	No_Search_Result(101,"No Search Results For The Query", ErrorHandler.SIXTH_SENSE_NO_SEARCH_RESULT),
	Search_Json_Attribute_Missing(102,"Mandatory search JSON attrbutes missing --> PLACEHOLDER", ErrorHandler.SIXTH_SENSE_SEARCH_JSON_ATTRIBUTE_MISSING),
	Search_Result_Error(103,"Search Results error --> PLACEHOLDER", ErrorHandler.SIXTH_SENSE_SEARCH_RESULT_ERROR),
	Candidate_Profile_Json_Attribute_Missing(104,"Mandatory getCandidateProfileAPI JSON attributes missing --> PLACEHOLDER", ErrorHandler.SIXTH_SENSE_CANDIDATE_PROFILE_JSON_ATTRIBUTE_MISSING),
	Captcha_Occurred(1000,"Captcha Occurred", ErrorHandler.SIXTH_SENSE_CANDIDATE_PROFILE_JSON_ATTRIBUTE_MISSING),
	Resolution_failure_Recaptcha_Process(1001,"Resolution failure - Recaptcha Process", ErrorHandler.SIXTH_SENSE_CANDIDATE_PROFILE_JSON_ATTRIBUTE_MISSING),
	Captcha_Search_Success(1002,"Success – Search Results", ErrorHandler.SIXTH_SENSE_CAPTCHA_SEARCH_SUCCESS),
	Captcha_Candidate_Profile_Success(1003,"Success – Candidate Profile", ErrorHandler.SIXTH_SENSE_CAPTCHA_CANDIDATE_PROFILE_SUCCESS),
	Captcha_Candidate_Document_Success(1004,"Success – Candidate Document", ErrorHandler.SIXTH_SENSE_CAPTCHA_CANDIDATE_DOCUMENT_SUCCESS),
	User_Creation_Error(201,"User creation error", ErrorHandler.SIXTH_SENSE_USER_CREATION_ERROR),
	User_Updation_Error(202,"User updation error", ErrorHandler.SIXTH_SENSE_USER_UPDATION_ERROR),
	User_Deletion_Error(203,"User deletion error", ErrorHandler.SIXTH_SENSE_USER_DELETION_ERROR),
	No_User_Found(204,"No users match the given search criteria", ErrorHandler.SIXTH_SENSE_NO_USER_FOUND),
	OTP_Occurred(1010,"OTP Occurred", ErrorHandler.SIXTH_SENSE_OTP_OCCURRED),
	OTP_Resolved_Success(1011,"Success – OTP resolved", ErrorHandler.SIXTH_SENSE_OTP_RESOLVED),
	OTP_Mobile_Input(1012,"OTP Mobile Input", ErrorHandler.SIXTH_SENSE_OTP_MOBILE_INPUT),
	OTP_Input_Screen_Error(308,"OTP Input screen generation error --> PLACEHOLDER", ErrorHandler.SIXTH_SENSE_OTP_MOBILE_INPUT);

	private final int code;
	private final String message;
	private final String reason;

	SixthSenseErrorConstant(int code, String message, String reason) {
		this.code = code;
		this.message = message;
		this.reason = reason;
	}

	public int getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	public String getReason() {
		return this.reason;
	}

}