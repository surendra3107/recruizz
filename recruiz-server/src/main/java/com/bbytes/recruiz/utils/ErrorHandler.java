package com.bbytes.recruiz.utils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bbytes.recruiz.exception.RecruizAuthException;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

	public static final String AUTH_FAILURE = "authentication_failure";

	public static final String ACCOUNT_INACTIVE = "account_inactive";

	public static final String ACCOUNT_DISABLED = "account_disabled";

	public static final String ACTIVATION_FAILED = "Activation failed";

	public static final String USER_NOT_FOUND = "user_not_found";

	public static final String CONNECT_DUPLICATE_CANDIDATE = "connect_duplicate_candidate";

	public static final String BAD_CREDENTIALS = "bad_credentials";

	public static final String PASSWORD_INCORRECT = "Login failed. Please verify correct username / password";

	public static final String ROLE_NOT_EXIST = "Role does not exist";

	public static final String SIGN_UP_FAILED = "sign_up_failed";

	public static final String LOGIN_FAILED = "login failed";

	public static final String ORG_NOT_UNIQUE = "Organization already exist";

	public static final String ORG_ALREADY_CREATED = "Organization already created";

	public static final String FREELANCER_EXISTS = "Name already taken";

	public static final String EMAIL_NOT_UNIQUE = "email_not_unique";

	public static final String MULTIPLE_ORG_ERROR = "multiple_org_error";

	public static final String SERVER_ERROR = "server_error";

	public static final String OPEN_OFFICE_SERVICE_FAILURE = "open_office_service_start_failed";

	public static final String ORG_USER_EXISTS = "Email is already registered";

	public static final String ORG_REGISTRATION_FAILED = "Organization registration failed";

	public static final String INVITATION_FAILED = "Email already in use";

	public static final String PASSWORD_MISMATCH = "wrong_password";

	public static final String CURRENT_PASSWORD_INCORRECT = "current_password_incorrect";

	public static final String USER_NAME_NOT_VALID = "Name can not be blank";

	public static final String INVALID_NAME = "invalid_name";

	public static final String CLIENT_EXISTS = "Client with same name exist";

	public static final String DUPLICATE_CLIENT = "client_exist";

	public static final String ELASTICSEARCH_ERROR = "elasticsearch_error";

	public static final String CLIENT_NOT_EXISTS = "Client does not exist";

	public static final String CLIENT_NOT_SET = "Clinet is not set for decision maker";

	public static final String CLIENT_MISSING = "Client Missing";

	public static final String DECISION_MAKER_MISSING = "Decison maker error";

	public static final String DECISON_MAKER_NOT_FOUND = "Descison maker not found";

	public static final String INVLID_DATA = "Invalid data";

	public static final String INTERVIEW_NULL = "Invalid data for interviewer";

	public static final String EMAIL_EXISTS = "email_exist";

	public static final String DECISION_MAKER_EXIST = "Decision maker with email ";

	public static final String INTERVIEW_PANEL_EXIST = "Interview panel with email ";

	public static final String USER_NOT_PART_OF_ORG = "no_org_for_user";

	public static final String PERMISSION_DENIED = "Permission Denied";

	public static final String PERMISSION_DENIED_NON_OWNER = "Permission Denied, Only owner is allowed to perform this operation";

	public static final String NO_GLOBAL_EDIT_PERMISSION = "Looks like you don't have permission to edit other's data.";

	public static final String INVALID_ROLE = "user_role_invalid";

	public static final String CANDIDATE_EXISTS = "Candidate is already added";

	public static final String DUPLICATE_CANDIDATE = "candidate_exist";

	public static final String INVALID_CLIENT = "invalid_client";

	public static final String CANDIDATE_NOT_FOUND = "candidate_not_found";

	public static final String POSITION_NOT_FOUND = "position_not_found";

	public static final String CLIENT_NOT_FOUND = "client_not_found";

	public static final String DUPLICATE_POSITION = "Position already exists with this position code ";

	public static final String POSITON_CODE_EXIST = "positon_code_exist";

	public static final String POSITION_NOT_EXIST = "Position does not exist";

	public static final String ROUND_EXIST = "Round already exist with same name";

	public static final String ROLE_NOT_DELETABLE = "This role can not be deleted";

	public static final String CAN_NOT_DELETE_ROLE = "can_not_delete_role";

	public static final String DISPOSABLE_EMAIL_DOMAIN = "Email domain is not supported";

	public static final String INVALID_DOMAIN = "invalid_domain";

	public static final String INSUFFICIENT_PRIVILEGE = "insufficient_privilege";

	public static final String ORGANIZATION_NOT_FOUND = "organization_not_found";

	public static final String NO_OWNERSHIP = "no_ownership";

	public static final String EXPORT_JOB_BUSY = "export_job_busy";

	public static final String NOT_ALLOWED = "User don't have privilege";

	public static final String CANNOT_DELETE_JOINED_USER = "Deletion of active user is not allowed";

	public static final String DELETE_OPERATION_FAILED = "Delete operation failed";

	public static final String ROLE_NOT_EDITABLE = "Role_Not_Deleteable";

	public static final String CANNOT_RENAME_ROLE = "This role can not be renamed";

	public static final String RENAMING_FAILED = "renaming_rolename_failed";

	public static final String ROLE_NAME_EXISTS = "Role_name_already_exists";

	public static final String ALREADY_SIGNED_UP = "User already signed up";

	public static final String TENANT_NOT_PRESENT = "Tenant not registered";

	public static final String INVALID_REQUEST = "invalid_request";

	public static final String FILE_DOES_NOT_EXIST = "file_not_exist";

	public static final String CORPORATE_NOT_CONNECT = "corporate_not_connect";

	public static final String CORPORATE_NOT_REGISTERED = "corporate is not register to connect";

	public static final String ROUND_CANDIDATE_NOT_FOUND = "round_candidate_not_found";

	public static final String NOT_JOINED = "Account has not yet been activated, Please contact admin or support";

	public static final String USER_DISABLED = "Account has been disabled, Please contact admin or support";

	public static final String FAILED_TO_UPLOAD_RESUME = "Failed to upload resume";

	public static final String FILE_UPLOAD_FAILED = "File upload failed";

	public static final String FILE_EXISTS = "file_exists";

	public static final String INVALID_REQUEST_TO_SERVER = "Invalid request to server";

	public static final String INDIVIDUAL_SIGN_UP_FAILED = "Individual sign up failed";

	public static final String ROUND_NOT_DELETABLE = "Round not deleteable";

	public static final String ROUND_NOT_PRESENT = "Round data not available";

	public static final String ROUND_NOT_EXIST = "round_not_exist";

	public static final String INVALID_MOVE = "Invalid candidate move";

	public static final String NO_FILE = "File does not exist";

	public static final String USER_EXISTS = "User already exist with this email";

	public static final String ONLY_ONE_ACCN_ALLOWED = "Only one organization account allowed for this server instance";

	public static final String USER_IS_UNIQUE = "User is unique";

	public static final String ROUNDID_STATUS_MISSING = "Round Id or status value is missing";

	public static final String CANDIDATE_NOT_EXISTS = "Candidate does not exist in application";

	public static final String POSITION_NOT_EXISTS = "Position does not exist in application";

	public static final String ROUND_CANDIDATE_NOT_EXISTS = "Candidate moved or not exist in round";

	public static final String NO_EVENT_FOUND = "No event found";

	public static final String NO_EVENT = "no_event";

	public static final String SCHEDULE_TIME_NULL = "Either start or end time is null";

	public static final String RESUME_PARSER_NULL = "resume_file_null";

	public static final String FAILED_TO_READ_FILE = "failed_to_read_file";

	public static final String RESUME_PARSER_ERROR = "error_while_parsing_resume";

	public static final String JD_PARSER_ERROR = "error_while_parsing_job_description";

	public static final String CLIENT_STATUS_OPERATION = "Client is On-Hold or Closed";

	public static final String POSITION_STATUS_OPERATION = "Position is On-Hold or Closed";

	public static final String CLIENT_ONHOLD_CLOSED = "client_onhold_closed";

	public static final String POSITION_ONHOLD_CLOSED = "position_onhold_closed";

	public static final String FAILED_TO_UPLOAD_FILE = "File exist";

	public static final String EXTERNAL_USER_URL_ACCESS_DENIED = "external_user_url_access_denied";

	public static final String DUPLICATE_SESSION_NOT_SUPPORTED = "duplicate_session_not_supported";

	public static final String NO_FEEDBACK = "No feedback found";

	public static final String Feedback_InActive = "Feedback link expired or not active";

	public static final String NO_FEEDBACK_FOUND = "no_feedback";

	public static final String BACK_DATE_NOT_ALLOWED = "Back date not allowed";

	public static final String BACK_DATE_SELECTED = "back_date";

	public static final String START_TIME_GREATER_THAN_END_TIME = "start_time_greater_than_end_time";

	public static final String NO_POSITION_EXISTS = "No position found for you";

	public static final String NO_REQUESTED_POSITION_EXISTS = "No position found for you";

	public static final String NO_POSITION = "no_position";

	public static final String NO_REQUEST_POSITION = "no_request_position";

	public static final String NO_CLIENT = "no_client";

	public static final String NO_CLIENT_FOUND = "No client found";

	public static final String DELETION_NOT_ALLOWED = "deletion_not_allwed";

	public static final String POSITION_EXITS_FOR_CLIENT = "Delete positions for client and then try again";

	public static final String MAX_LIMIT = "max_limit_reached";

	public static final String MAX_LIMIT_REACHED = "Max limit reached";

	public static final String INVALID_SOURCE_MODE = "invalid source mode";

	public static final String INVALID_MODE = "invalid_source_mode";

	public static final String NOT_AUTHORISIED_TO_BOARD = "not_authorisied_to_this_board";

	public static final String DOES_NOT_HAVE_PERMISSION = "Either you don't have permission to view pipeline or you are not working on this pipeline";

	public static final String USER_ALREADY_REGISTERED = "User already registered";

	public static final String ALREADY_REGISTERED = "already_registered";

	public static final String NORMAL_USER = "normal_user";

	public static final String NORMAL_USER_NOT_ALLOWED = "Normal user not allowed for this operation";

	public static final String FEEDBACK_CLOSED = "feedback_closed";

	public static final String ONE_ORG_ADMIN_REQUIRED = "One org admin is required, can not change role";

	public static final String NO_ORG_ADMIN = "no_org_admin";

	public static final String ALREADY_APPLIED_FOR_POSITION = "Already applied to this Position";

	public static final String ALREADY_APPLIED = "already_applied";

	public static final String SIGNUP_INVITE_EMAIL_MISMATCH = "invite_email_mismatch_with_signup_email";

	public static final String VENDOR_EXISTS = "vendor_exists";

	public static final String USER_IS_NOT_VENDOR = "User is not a vendor";

	public static final String NOT_A_VENDOR = "not_a_vendor";

	public static final String POSITION_NOT_SHARED = "position_not_shared";

	public static final String VENDOR_NOT_AUTHORISIED = "Vendor not authorisied to work on this position";

	public static final String TASK_ITEM_SAVE_UPDATE_FAILED = "task_item_save_or_update_failed";

	public static final String TASK_ITEM_DELETE_FAILED = "task_item_delete_failed";

	public static final String TASK_FOLDER_SAVE_UPDATE_FAILED = "task_folder_save_or_update_failed";

	public static final String TASK_FOLDER_DELETE_FAILED = "task_folder_delete_failed";

	public static final String INVALID_VENDOR = "invalid_vendor";

	public static final String VENDOR_NOT_FOUND = "Vendor Not Found";

	public static final String VENDOR_NOT_EXISTS = "no_vendor_exists";

	public static final String EMAIL_MODIFICATION_NOT_ALLOWED = "Email modofication not allowed";

	public static final String EMAIL_CHANGE_NOT_ALLLOWED = "email_change_not_allowed";

	public static final String VENDOR_DISABLED = "vendor_disabled";

	public static final String EMAIL_IN_USE = "Email is in use";

	public static final String CAN_NOT_DELETE_LOOGEDIN_USER = "Can not delete logged in user";

	public static final String CAN_NOT_DELETE_USER = "Can not delete user";

	public static final String USER_DELETE_FAILED = "user_delete_failed";

	public static final String NO_FILES_UPLOADED = "No files uploaded";

	public static final String NO_RESUME = "no_resume";

	public static final String BULD_UPLAOD_FAILED = "buld_uplaod_failed";

	public static final String IMPORT_DATA_FAILED = "import_data_failed";

	public static final String INVALID_FILE_FORMAT = "invalid_file_format";

	public static final String REQUEST_STATUS_CHANGED = "Only pending requests are allowed to delete";

	public static final String CANNOT_DELETE_INPROCESS = "In process requested position can not be deleted";

	public static final String ONLY_PENDING_ALLOWED = "only_pending_status_allowed";

	public static final String INPROCESS_REQUEST_CANNOT_BE_DELETED = "in_process_can_not_be_deleted";

	public static final String ORGANIZATION_DOES_NOT_EXISTS = "Organization does not exist";

	public static final String NO_ORG_REGISTERED = "no_org_registered";

	public static final String ORG_API_TOKEN_CREATE_FAILED = "org_api_token_create_failed";

	public static final String USER_LIMIT_REACHED = "user_limit_reached";

	public static final String USER_LIMIT_EXCEEDED = "User limit reached, contact admin";

	public static final String VENDOR_FEATURE_NOT_ALLOWED = "Vendor feature is not allowed";

	public static final String VENDOR_LIMIT_EXCEEDED = "Vendor limit exceeded, contact admin";

	public static final String FEATURE_NOT_ALLOWED = "feature_not_allowed";

	public static final String FEATURE_NOT_ENABLED = "This feature is not enabled, contact admin";

	public static final String PARSER_LIMIT = "parser_limit";

	public static final String PARSER_LIMIT_EXCEEDED = "Parser limit exceeded, contact admin";

	public static final String LICENCE_EXPIRED = "licence_expired";

	public static final String TENANT_NOT_VALID = "tenant_not_valid";

	public static final String RENEW_LICENCE = "License expired , please contact support";

	public static final String TENANT_INVALID = "Given tenant is not valid";

	public static final String SUSPENDED_ORGANIZATION_MSG = "Account is temporary disabled , please contact support";

	public static final String NO_EMAIL = "no_email";

	public static final String Email_NOT_PRESENT = "Email not present";

	public static final String NO_TEMPLATE = "no_template";

	public static final String TEMPLATE_EXISTS = "template_exits";

	public static final String TEMPLATE_EXISTS_WITH_NAME = "Template exist with same name, try other name.";

	public static final String FIELD_REQUIRED = "field_required";

	public static final String REQUIRED_FIELD_MISSING = "Mandatory field missing";

	public static final String NO_ADD_UPDATE = "add_hr_category_not_allowed";

	public static final String NOT_ALLOWED_FOR_HR_CATEGORY = "Adding template for HR category is not allowed.";

	public static final String NOT_ALLOWED_FOR_HR_CATEGORY_UPDATE = "Category change for HR template not allowed.";

	public static final String FEEDBACK_CREATE_ERROR = "feedback_create_error";

	public static final String SCHEDULE_FAILED = "Scheduling interview failed due to error in creating feedback";

	public static final String MESSAGE_NOT_FOUND = "Message not found";

	public static final String NO_MSG = "no_msg";

	public static final String RESCHEDULE_INTERVIEW_FAILED = "Rescheduling interview failed";

	public static final String RESCHEDULE_INTERVIEW_FAILED_BY_EVENT_ID = "Rescheduling interview failed due to invalid schedule selection";

	public static final String SCHEDULE_NOT_FOUND = "Schedule not found";

	public static final String SCHEDULE_DATA_CORRUPTED = "Looks like schedule data is corrupted";

	public static final String RESCHEDULE_FAILED = "rescheduled_failed";

	public static final String REQUIRED_PARAM_MISSING = "Required param missing";

	public static final String INVALID_SERVER_REQUEST = "invalid_server_request";

	public static final String ACTIVATION_ERROR = "We are not able to activate your account.";

	public static final String ELASTICSEARCH_REINDEX_ERROR = "elastic search reindex failed";

	public static final String ELASTICSEARCH_REINDEX_FALIED = "elastic_search_reindex_failed";

	public static final String org_update_failed = "org_update_failed";

	public static final String NO_RECORD_FOUND = "No record found";

	public static final String NO_RECORD = "no_record";

	public static final String RESUME_MISSING = "Resume is required";

	public static final String APPLICATION_FAILED = "application_failed";

	public static final String REQUEST_PARAMETER_MISSING = "request_parameter_missing";

	public static final String PARAMETER_MISSING = "Something is wrong when requesting server";

	public static final String APPLY_FAILED = "Something went wrong, try again later with new link";

	public static final String CAN_NOT_APPY = "can_not_apply";

	public static final String TEMP_FILE_CREATION_FAILED = "Failed to create temp file";

	public static final String TEMP_FILE_ERROR = "temp_file_error";

	public static final String MOVING_CANDIDATE_FAILED = "moving_candidate_failed";

	public static final String CANNOT_MOVE_CANDIDATE = "Moving candidate failed";

	public static final String INTERVIEW_IS_ACTIVE = "Can not schedule interview for a active one, check calender and edit the interview";

	public static final String INTERVIEW_SCHEDULE_FAILED = "schedule_interview_failed";

	public static final String NOTES_UPDATE_FAILED = "Failed to update candidate notes";

	public static final String NOTES_DELETE_FAILED = "Failed to delete candidate notes";

	public static final String NOTE_NOT_ADDED_BY_YOU = "Notes added by you are only allowed to edit/delete.";

	public static final String MAX_CANDIDATE_LIMIT_REACHED = "Maximum candidate added, renew your plan.";

	public static final String MAX_CANDIDATE_REACHED = "max_candidate_reached";

	public static final String NO_PLANS_FOUND = "No plan found";

	public static final String NO_PLANS = "no_plans";

	public static final String NOT_FOUND = "not_found";

	public static final String NO_DEATILS_FOUND = "No details found";

	public static final String ORG_NOT_FOUND = "org_not_found";

	public static final String NO_INVOICE = "no_invoice";

	public static final String FAILED_TO_GET_INVOICE = "Failed to fetch invoice";

	public static final String NO_ORG_ID = "no_org_id";

	public static final String ORG_ID_NULL = "Organozaion ID not available in request";

	public static final String INVALID_PLAN_INFO = "invalid_plan_info";

	public static final String INVALID_PLAN_DETAILS = "Invalid plan details passed, either plan id or plan name or feature map is not present";

	public static final String PLUGIN_ADD_CANDIDATE_FAILED = "plugin_add_candidate_failed";

	public static final String EMAIL_TEMPLATE_RESTORE_FAILED = "Something went wrong while restoring template, try again";

	public static final String TEMPLATE_RESTORE_FAILED = "template_restore_failed";

	public static final String PROSPECT_NOT_EXIST = "prospect_not_exist";

	public static final String PROSPECT_NOT_FOUND = "prospect not found";

	public static final String PROSPECT_ALREADY_CONVERTED_TO_CLIENT = "prospect_already_converted_to_client";

	public static final String PROSPECT_CAN_NOT_EMPTY = "prospect_can_not_empty";

	public static final String PROSPECT_CONTACT_CAN_NOT_EMPTY = "prospect_contact_can_not_empty";

	public static final String PROSPECT_COMPANY_NAME_CAN_NOT_EMPTY = "prospect_company_name_can_not_empty";

	public static final String PROSPECT_NAME_CAN_NOT_EMPTY = "prospect_name_can_not_empty";

	public static final String PROSPECT_MOBILE_CAN_NOT_EMPTY = "prospect_mobile_can_not_empty";

	public static final String PROSPECT_EMAIL_CAN_NOT_EMPTY = "prospect_email_can_not_empty";

	public static final String PROSPECT_LOCATION_CAN_NOT_EMPTY = "prospect_location_can_not_empty";

	public static final String PROSPECT_DESIGNATION_CAN_NOT_EMPTY = "prospect_designation_can_not_empty";

	public static final String PROSPECT_INDUSTRY_CAN_NOT_EMPTY = "prospect industry can not empty";

	public static final String PROSPECT_CATEGORY_CAN_NOT_EMPTY = "prospect category can not empty";

	public static final String PROSPECT_RATING_CAN_NOT_EMPTY = "prospect rating can not empty";

	public static final String PROSPECT_COMPANY_ALREADY_EXIST = "prospect_company_already_exist";

	public static final String PROSPECT_NAME_ALREADY_EXIST = "prospect_name_already_exist";

	public static final String PROSPECT_EMAIL_ALREADY_EXIST = "prospect_email_already_exist";

	public static final String PROSPECT_MOBILE_ALREADY_EXIST = "prospect_mobile_already_exist";

	public static final String PROSPECT_CREATION_FAILED = "prospect_creation_failed";

	public static final String PROSPECT_CONTACT_INFO_CREATION_FAILED = "prospect_contact_info_creation_failed";

	public static final String PROSPECT_CONTACT_INFO_EMAIL_CAN_NOT_EMPTY = "prospect_contact_info_email_can_not_empty";

	public static final String PROSPECT_CONTACT_INFO_MOBILE_CAN_NOT_EMPTY = "prospect_contact_info_mobile_can_not_empty";

	public static final String PROSPECT_CONTACT_INFO_NAME_CAN_NOT_EMPTY = "prospect_contact_info_name_can_not_empty";

	public static final String PROSPECT_CONTACT_INFO_NOT_EXIST = "prospect_contact_info_not_exist";

	public static final String UPDATE_FAILED = "update_failed";

	public static final String PROSPECT_UPDATE_FAILED = "prospect update failed";

	public static final String GET_FAILED = "get_failed";

	public static final String PROSPECT_CLIENT_CONVERTION_FAILED = "prospect_clent_convertion_failed";

	public static final String PROSPECT_CLIENT_NAME_DUPLICATE = "prospect company name is already exist in client. please change the prospect company name";

	public static final String PROSPECT_CLIENT_NAME_REDUNDECY = "prospect_company_name_exist_in_client";

	public static final String PROSPECT_STATUS_NOT_FOUND = "prospect_status_not_found";

	public static final String ID_NOT_EXIST = "id_not_exist";

	public static final String DELETE_FAILED = "delete_failed";

	public static final String PROSPECT_STATUS_CHANGED_FAILED = "prospect_status_changed_failed";

	public static final String COMPANY_NOT_EXIST = "company_not_exist";

	public static final String MOBILE_NOT_EXIST = "mobile_not_exist";

	public static final String EMAIL_NOT_EXIST = "email_not_exist";

	public static final String COMPANY_EXIST_IN_SAME_ID = "company_exist_same_id";

	public static final String MOBILE_EXIST_IN_SAME_ID = "mobile_exist_same_id";

	public static final String EMAIL_EXIST_IN_SAME_ID = "email_exist_same_id";

	public static final String MOBILE_NUMBER_EXIST = "mobile number exist";

	public static final String EMAIL_EXIST = "email exist";

	public static final String NO_ACTIVITY = "No Activity";

	public static final String NO_ACTIVITY_FOUND = "no_activity";

	public static final String INVALID_STATUS_SELECTED = "Invalid status selected";

	public static final String INVALID_STATUS = "invalid_status";

	public static final String CHANGE_STATUS_FAILED = "change status failed";

	public static final String REASON_SAVE_FAILED = "reason_save_failed";

	public static final String STATUS_CHANGE_FAILED = " status change failed";

	public static final String REASON_UPDATE_FAILED = " reason_update_failed";

	public static final String NO_ROLE_CHANGE_FOR_INACTIVE_USER = "no_role_change_for_inactive_user";

	public static final String CAN_NOT_CHANGE_ROLE_FOR_INACTIVE_USER = "Can not change role for inactive user";

	public static final String CAN_NOT_ASSIGN_SUPER_ADMIN_ROLE_FOR_INACTIVE_USER = "Can not assign Super admin role to pending or deactivated user.";

	public static final String MANDATORY_FIELD = "mandatory field";

	public static final String AGENCY_INVOICE_ID_NOT_EXIST = "agency invoice id not exist";

	public static final String HISTORY_NOT_EXIST = "history not exist";

	public static final String NOT_VALID_ID = "not_valid_id";

	public static final String CLIENT_NAME_MANDATORY = "client_name_mandatory";

	public static final String POSITION_NAME_MANDATORY = "position_name_mandatory";

	public static final String POSITION_NAME_MISSING = "Postion Name Missing";

	public static final String CANDIDATE_NAME_MANDATORY = "candidate_name_mandatory";

	public static final String CANDIDATE_NAME_MISSING = "Candidate Name Missing";

	public static final String OFFERED_DATE_MANDATORY = "offered_date_mandatory";

	public static final String OFFERED_DATE_MISSING = "Offered Date Missing";

	public static final String INVOICE_STATUS_MANDATORY = "invoice_status_mandatory";

	public static final String INVOICE_STATUS_MISSING = "Invoice status missing";

	public static final String DUE_DATE_MANDATORY = "due_date_mandatory";

	public static final String DUE_DATE_MISSING = "Due date missing";

	public static final String AMOUNT_MISSING = "Amount missing";

	public static final String INVOICE_ALREADY_GENERATED = "Invoice already generated";

	public static final String INVOICE_GENERATED = "Invoice_already_generated";

	public static final String INVOICE_NOT_GENERATED = "Invoice_not_generated";

	public static final String GENERATING_OF_INVOICE_FAILED = "Generating of Invoice Failed";

	public static final String AMOUNT_MANDATORY = "amount_mandatory";

	public static final String PAYMENT_RECEIVED_DATE_MISSING = "Payment received date missing";

	public static final String PAYMENT_RECEIVED_DATE_MANDATORY = "payment_received_date_mandatory";

	public static final String PAYMENT_RECEIVED_AMOUNT_MISSING = "Payment Received Amount Missing";

	public static final String PAYMENT_RECEIVED_AMOUNT_MANDATORY = "payment_received_amount_mandatory";

	public static final String FEW_STATUS_CHANGE_FAILED = "few_status_change_failed";

	public static final String NO_TOKEN_FOUND_IN_BODY = "No token found in request";

	public static final String NO_TOKEN_FOUND = "no_token_found";

	public static final String PASSED_TOKEN_NOT_VALID = "Passed token is not valid";

	public static final String INVALID_INTEGRATION_TOKEN = "invalid_integration_token";

	public static final String FAILED_ADDING_CAMPAIGN = "failed_adding_campaign";

	public static final String FAILED_TO_CREATE_CAMPAIGN = "Something went wrong while creating campaign. Try again !!!";

	public static final String FAILED_GETTING_CAMPAIGN = "no_compaign_found";

	public static final String FAILED_TO_GET_CAMPAIGN = "No campaign found, Try again !!!";

	public static final String CAN_NOT_RUN_CAMPAIGN_WITHOUT_MEMBER = "Can not run a campaign with no members, Add member and try again";

	public static final String NO_CAMPAIGN_MEMBER = "no_campaign_member";

	public static final String CAMPAIGN_TYPE_NOT_FOUND = "Campaign type not found";

	public static final String NO_CAMPAIGN_TYPE_FOUND = "no_campaign_type_found";

	public static final String SOMETHING_WENT_WRONG_WHILE_RUNNING = "Something went wrong while running campaign";

	public static final String FAILED_TO_RUN_CAMPAIGN = "failed_to_run _campaign";

	public static final String EMAIL_USAGE_EXCEEDED_BUY_MORE = "Email usage exceeded, Contact Admin";

	public static final String EMAIL_USAGE_EXCEEDED = "email_usage_exceeded";

	public static final String FAILED_TO_GET_CAMPAIGN_STAT = "Failed to get campaign stat";

	public static final String FAILED_GETTING_CAMPAIGN_STAT = "failed_to_get_campaign_stat";

	public static final String FAILED_TO_CHANGE_CAMPAIGN_STATUS = "Failed to change campaign status";

	public static final String FAILED_CHANGING_CAMPAIGN_STATUS = "failed_to_change_campaign_status";

	public static final String CAN_NOT_RUN_CAMPAIGN_WITHOUT_CANDIDATE = "Can not run a campaign with no candidate, Add candidate and try again";

	public static final String NO_CAMPAIGN_CANDIDATE = "no_campaign_candidate";

	public static final String ERROR_DELETING_CAMPAIGN = "Something went wrong while deleting campaign !";

	public static final String FAILED_TO_DELETE_CAMPAIGN = "failed_to_delete_campaign";;

	public static final String CANDIDATE_STATUS_MISSING = "candidate status missing";

	public static final String CANDIDATE_STATUS_NOT_FOUND = "candidate_status_not_found";

	public static final String CAN_NOT_UPDATE_ARCHIVE_INVOICE = "can't update archive invoice";

	public static final String UPDATE_ARCHIVE_INVOICE = "can't_update_archive_invoice";

	public static final String CAN_NOT_PAY_FOR_ARCHIVE_INVOICE = "can't pay for archive invoice";

	public static final String PAYMENT_NOT_ALLOWED_ARCHIVE_INVOICE = "payment_not_allowed_for_archive_invoice";

	public static final String INVOICE_EXISTS_FOR_CANDIDAT_TO_DELETE = "There is an invoice generated for this candidate. Delete the invoice and then delete candidate";

	public static final String INVOICE_EXISTS_FOR_CANDIDATE = "invoice_exists_for_given_candidate";

	public static final String POSITION_CODE_MANDATORY = "position_code_mandatory";

	public static final String INVOICE_DTO_EMPTY = "invoice dto empty";

	public static final String INVOICE_DATA_EMPTY = "invoice data empty";

	public static final String INVOICE_DTO_CANT_NOT_EMPTY = "invoice_dto_can't_empty";

	public static final String CLIENT_ID_MISSING = "client id missing";

	public static final String POSTION_CODE_MISSING = "postion code missing";

	public static final String CANDIDATE_EMAIL_MISSING = "candidate email missing";

	public static final String ENTER_VALID_VALUE = "enter valid value";

	public static final String VALID_VALUE = "not_valid_value";

	public static final String CANDIDATE_ID_MISSING = "candidate id missing";

	public static final String CANDIDATE_ID_MANDATORY = "candidate_id_mandatory";

	public static final String CLIENT_ID_MANDATORY = "client_id_mandatory";

	public static final String CANDIDATE_EMAIL_MANDATORY = "candidate_email_mandatory";

	public static final String CAN_NOT_CREATE_INVOICE_FOR_MORE_THAN_ONE_CLIENT_FOR_SAME_CANDIDATE = "can not create invoice more than one client for same candidate";

	public static final String CAN_NOT_CREATE_INVOICE_FOR_MORE_THAN_ONE_CLIENT_FOR_SME_CANDIDATE = "can_not_create_invoice_more_than_one_client_for_same_candidate";

	public static final String CANDIDATE_INVOICE_NOT_EXIST = "candidate invoice not exist";

	public static final String ENTER_VALUE_OR_PERCENT = "enter value or percent";

	public static final String NOT_VALID_VALUE = "not_valid_value";

	public static final String RESUME_MASKING_ERROR = "error_while_masking_resume";

	public static final String RESUME_MASKED_FAILED = "Resume Making Failed, Try Again";

	public static final String ALREADY_ACTIVATED = "account_activated";

	public static final String ACCOUNT_IS_ACTIVATED = "Account is already activated";

	public static final String NO_CAMPAIGN_PERMISSION = "no_campaign_permission";

	public static final String USER_DONT_HAVE_CAMPAIGN_PERMISSION = "You don't have permission for campaign service, ask you admin to grant permission";

	public static final String ACTIAVTED_FAILED_TO_UPDATE_TEMPLATE = "Account activated but failed to updated templates from file.";

	public static final String FAILED_UPDATING_TEMPLATES = "failed_updating_templates";

	public static final String NEW_OWNER_NOT_EXISTS_IN_SYSTEM = "New owner does not exists in the system";

	public static final String FAILED_TO_FETCH_EMAIL_INBOX = "failed_to_get_inbox";

	public static final String USER_INFORMATION_MISSING = "User email information is not updated.";

	public static final String Connection_Failed = "mail_server_connection_failed";

	public static final String FAILED_TO_ADD_EMAIL_CLIENT_DETAILS = "Something went wrong while adding new email client details, Try Again";

	public static final String FAILED_TO_ADD_EMAIL_CLIENT = "failed_to_add_email_client";

	public static final String EMAIL_CLIENT_DETAILS_NOT_FOUND = "Email client details not found.";

	public static final String FAILED_TO_UPDATE_EMAIL_CLIENT = "failed_to_update_email_client";

	public static final String FAILED_TO_UPDATE_EMAIL_CLIENT_DETAILS = "Failed to update existing email client details. Try Again";

	public static final String FAILED_TO_DELETE_EMAIL_CLIENT = "failed_to_delete_email_client_details";

	public static final String FAILED_TO_DELETE_EMAIL_CLIENT_DETAILS = "Failed to delete email client details,Try Again";

	public static final String FAILED_TO_GET_EMAIL_CLIENT_DETAILS = "Failed to get email client details";

	public static final String FAILED_TO_GET_EMAIL_CLIENT = "failed_to_get_email_client_details";

	public static final String PROSPECT_POSITION_CAN_NOT_EMPTY = "prospect position can not be empty";

	public static final String PROSPECT_POSITION_CAN_NOT_NULL = "prospect_position_can_not_be_empty";

	public static final String PROSPECT_POSITION_NAME_CAN_NOT_EMPTY = "prospect position name can not be empty";

	public static final String PROSPECT_POSITION_NAME_CAN_NOT_NULL = "prospect_position_name_can_not_be_empty";

	public static final String PROSPECT_POSITION_CLOSURE_DATE_CAN_NOT_EMPTY = "prospect position closure date can not be empty";

	public static final String PROSPECT_POSITION_CLOSURE_DATE_CAN_NOT_NULL = "prospect_position_closure_date_can_not_be_empty";

	public static final String PROSPECT_POSITION_OPENING_CAN_NOT_EMPTY = "prospect position opening can not be empty";

	public static final String PROSPECT_POSITION_OPENING_CAN_NOT_NULL = "prospect_position_opening_can_not_be_empty";

	public static final String PROSPECT_POSITION_MIN_EXP_CAN_NOT_EMPTY = "prospect position min exp can not be empty";

	public static final String PROSPECT_POSITION_MIN_EXP_CAN_NOT_NULL = "prospect_position_min_exp_can_not_be_empty";

	public static final String PROSPECT_POSITION_MAX_EXP_CAN_NOT_EMPTY = "prospect position max exp can not be empty";

	public static final String PROSPECT_POSITION_MAX_EXP_CAN_NOT_NULL = "prospect_position_max_exp_can_not_be_empty";

	public static final String PROSPECT_POSITION_SKILLS_CAN_NOT_EMPTY = "prospect position skills can not be empty";

	public static final String PROSPECT_POSITION_SKILLS_CAN_NOT_NULL = "prospect_position_skills_can_not_be_empty";

	public static final String PROSPECT_POSITION_LOCATION_CAN_NOT_EMPTY = "prospect position location can not be empty";

	public static final String PROSPECT_POSITION_LOCATION_CAN_NOT_NULL = "prospect_position_location_can_not_be_empty";

	public static final String PROSPECT_POSITION_EDUCATION_QUALIFICATION_CAN_NOT_EMPTY = "prospect position education qualification can not be empty";

	public static final String PROSPECT_POSITION_EDUCATION_QUALIFICATION_CAN_NOT_NULL = "prospect_position_education_qualification_can_not_be_empty";

	public static final String PROSPECT_POSITION_TYPE_CAN_NOT_EMPTY = "prospect position type can not be empty";

	public static final String PROSPECT_POSITION_TYPE_CAN_NOT_NULL = "prospect_position_type_can_not_be_empty";

	public static final String PROSPECT_POSITION_INDUSTRY_CAN_NOT_EMPTY = "prospect position industry can not be empty";

	public static final String PROSPECT_POSITION_INDUSTRY_CAN_NOT_NULL = "prospect_position_industry_can_not_be_empty";

	public static final String PROSPECT_POSITION_FUNCTIONAL_AREA_CAN_NOT_EMPTY = "prospect position industry can not be empty";

	public static final String PROSPECT_POSITION_FUNCTIONAL_AREA_CAN_NOT_NULL = "prospect_position_industry_can_not_be_empty";

	public static final String POSITION_ADDED_FAILED = " position added failed";

	public static final String POSITION_ADDED_FAILURE = " position_added_failed";

	public static final String PROSPECT_POSITION_NOT_EXIST = "prospect position not exist";

	public static final String PROSPECT_POSITION_NOT_FOUND = "prospect_position_not_found";

	public static final String PROSPECT_POSITION_DELETE_FAILED = "prospect position delete failure";

	public static final String PROSPECT_POSITION_DELETE_FAILURE = "prospect_position_delete_failure";

	public static final String PROSPECT_POSITION_UPDATE_FAILED = "prospect position update failure";

	public static final String PROSPECT_POSITION_UPDATE_FAILURE = "prospect_position_update_failure";

	public static final String NO_EMAIL_CLIENT = "no_email_client_found";

	public static final String No_Email_Client_Configured = "No email clinet found, add a client and try again.";

	public static final String FAILED_TO_SEND_EMAIL = "failed_to_send_email";

	public static final String FAILED_TO_DELETE_EMAIL = "failed_to_delete_email";

	public static final String MESSAGE_ID_EMPTY = "Message ids are empty";

	public static final String FAILED_TO_MARK_DEFAULT_EMAIL_CLIENT_DETAILS = "Failed to mark as default";

	public static final String FAILED_TO_MARK_DEFAULT_EMAIL_CLIENT = "failed_to_mark_default";

	public static final String NO_MASKED_RESUME = "no_masked_resume";

	public static final String REASON_IS_MANDATORY = "Reason is mandatory for this operation";

	public static final String REASON_MANDATORY = "reason_is_mandatory";

	public static final String UPDATE_PAYMENT_HISTORY_FAILED = "update payment history failed";

	public static final String FAILED_TO_CONNECT_TO_MAIL_SERVER = "Failed to connect to email server, check credentials and try again";

	public static final String MAIL_SERVER_CONNECTION_ERROR = "mail_server_coonection_error";

	public static final String EMAIL_ACCOUNT_NOT_FOUND = "Email Account Not Found";

	public static final String EMAIL_ACCOUNT_MISSING = "email_acccount_missing";

	public static final String NOT_AGENCY_TYPE = "organization is not agency type";

	public static final String NOT_AGENCY_TYPE_ = "organization_not_agency_type";

	public static final String BANK_DETAILS_NOT_FOUND = "bank details not found";

	public static final String BANK_DETAILS_NOT_EXIST = "bank_details_not_exist";

	public static final String BANK_DETAILS_CAN_NOT_EMPTY = "bank details can not be empty";

	public static final String BANK_DETAILS_CAN_NOT_NULL = "bank_details_can_not_be_empty";

	public static final String BANK_NAME_CAN_NOT_EMPTY = "bank name can not be empty";

	public static final String BANK_NAME_CAN_NOT_NULL = "bank_name_can_not_be_empty";

	public static final String BANK_ACCOUNT_NUMBER_CAN_NOT_EMPTY = "bank account number can not be empty";

	public static final String BANK_ACCOUNT_NUMBER_CAN_NOT_NULL = "bank_account_number_can_not_be_empty";

	public static final String BANK_ACCOUNT_NUMBER_ALREADY_EXIST = "bank account number already exist";

	public static final String BANK_ACCOUNT_NUMBER_EXIST = "bank_account_number_already_exist";

	public static final String BANK_DETAILS_UPDATE_FAILED = "bank details update failed";

	public static final String BANK_DETAILS_UPDATE_FAILURE = "bank_details_update_failed";

	public static final String ADD_BANK_DETAILS_FAILED = "bank details not added";

	public static final String ADD_BANK_DETAILS_FAILURE = "bank_details_not_added";

	public static final String BANK_DETAILS_DELETE_FAILED = "Bank Details delete failed";

	public static final String TAX_DETAILS_DELETE_FAILED = "Tax Details delete failed";

	public static final String TAX_DETAILS_DELETE__FAILED = "Tax_Details_delete_failed";

	public static final String SIXTH_SENSE_SUCCESS = "ss_success";

	public static final String SIXTH_SENSE_CAPTCHA_SEARCH_SUCCESS = "ss_captcha_search_success";

	public static final String SIXTH_SENSE_CAPTCHA_CANDIDATE_PROFILE_SUCCESS = "ss_captcha_candidate_profile_success";

	public static final String SIXTH_SENSE_CAPTCHA_CANDIDATE_DOCUMENT_SUCCESS = "ss_captcha_candidate_document_success";

	public static final String SIXTH_SENSE_LICENCE_EXPIRED = "ss_licence_expired";
	
	public static final String SIXTH_SENSE_MAX_CONCURRENT_USER_REACHED = "ss_max_concurrent_user_reached";

	public static final String SIXTH_SENSE_INVALID_CLIENT_ID = "ss_client_id";

	public static final String SIXTH_SENSE_JWT_Failure = "ss_jwt_verification_failure";

	public static final String SIXTH_SENSE_CONTENT_TYPE_MISSING = "ss_content_type_missing";

	public static final String SIXTH_SENSE_INPUT_JSON_INPROPER = "ss_input_json_inproper";

	public static final String SIXTH_SENSE_SESSION_INVALID = "ss_session_invalid";

	public static final String SIXTH_SENSE_INPUT_JSON_EMPTY = "ss_input_json_empty";

	public static final String SIXTH_SENSE_MYSQL_CONNECTION_ERROR = "ss_mysql_connection_error";

	public static final String SIXTH_SENSE_CREDENTIAL_CASE_SENSITIVE = "ss_credential_case_sensitive";

	public static final String SIXTH_SENSE_INVALID_USERNAME = "ss_invalid_username";

	public static final String SIXTH_SENSE_USER_UI_ACCESS = "ss_user_ui_access";

	public static final String SIXTH_SENSE_LOGGEDIN_ANOTHER_SYSTEM = "ss_loggedin_another_system";

	public static final String SIXTH_SENSE_API_USER_ONLINE = "ss_api_user_online";

	public static final String SIXTH_SENSE_UNAUTHORISED_ACCESS = "ss_unauthorised_access";

	public static final String SIXTH_SENSE_UNAUTHORISED_INVALID_CREDENTIAL = "ss_unauthorised_invalid_credential";

	public static final String SIXTH_SENSE_CLOSE_SESSION_FAILURE = "ss_close_session_failure";

	public static final String SIXTH_SENSE_NO_SEARCH_RESULT = "ss_no_search_result";

	public static final String SIXTH_SENSE_SEARCH_JSON_ATTRIBUTE_MISSING = "ss_search_json_attribute_missing";

	public static final String SIXTH_SENSE_SEARCH_RESULT_ERROR = "ss_search_result_error";

	public static final String SIXTH_SENSE_CANDIDATE_PROFILE_JSON_ATTRIBUTE_MISSING = "ss_candidate_profile_json_attribute_missing";

	public static final String SIXTH_SENSE_CAPTCHA_OCCURRED = "ss_captcha_occurred";

	public static final String SIXTH_SENSE_RESOLUTION_FAILURE = "ss_resolution_failure";

	public static final String SIXTH_SENSE_USER_CREATION_ERROR = "ss_user_creation_error";

	public static final String SIXTH_SENSE_USER_UPDATION_ERROR = "ss_user_updation_error";

	public static final String SIXTH_SENSE_USER_DELETION_ERROR = "ss_user_deletion_error";

	public static final String SIXTH_SENSE_NO_USER_FOUND = "ss_no_user_found";

	public static final String SIXTH_SENSE_OTP_OCCURRED = "ss_otp_occurred";

	public static final String SIXTH_SENSE_OTP_RESOLVED = "ss_otp_resolved";

	public static final String SIXTH_SENSE_OTP_MOBILE_INPUT = "ss_otp_mobile_input";

	public static final String SIXTH_SENSE_OTP_SCREEN_ERROR = "ss_otp_screen_error";

	public static final String SIXTH_SENSE_NOT_CONFIGURED = "ss_job_not_configured";

	public static final String SIXTH_SENSE_NOT_CONFIGURED_MSG = "Job Portal is not configured";

	public static final String SIXTH_SENSE_INVALID_URL_MSG = "job portal url is invalid";

	public static final String SIXTH_SENSE_INVALID_URL = "invalid_job_portal_url";

	public static final String SIXTH_SENSE_SERVER_ERROR = "Sixth Sense Server error";

	public static final String SIXTH_SENSE_SERVER_DOWN = "Looks like the search portal is down or not reachable. Please check back after some time.";

	public static final String SIXTH_SENSE_SERVER_DOWN_MSG = "Looks like the search portal is down or not reachable. Please check back after some time.";

	public static final String SIXTH_SENSE_SOURCE_MISSING = "Job Portal search source not configured";

	public static final String SIXTH_SENSE_JOB_PORTAL_DISABLED = "ss_job_portal_disabled";

	public static final String SIXTH_SENSE_JOB_PORTAL_DISABLED_MSG = "Job Portal not enabled";

	public static final String FAILED_ADDING_GENERIC_INTERVIEWER = "failed_to_add_generic_interviewer";

	public static final String FAILED_ADDING_GENERIC_DM = "failed_to_add_generic_decision_maker";

	public static final String FAILED_UPDATING_GENERIC_DM = "failed_to_update_generic_dm";

	public static final String FAILED_TO_DELETE_GENERIC_INTERVIEWER = "failed_to_delete_generic_interviewer";

	public static final String FAILED_TO_DELETE_GENERIC_DM = "failed_to_delete_generic_dm";

	public static final String FAILED_DELETING_INTERVIEWER = "failed_to_delete_interviewer";

	public static final String FAILED_TO_DELETE_INTERVIEWER = "Failed to delete interviewer";

	public static final String FAILED_TO_ADD_HR_TO_POSITION = "Failed to add hr to position";

	public static final String FAILED_ADDING_HR = "failed_to_add_hr";

	public static final String FAILED_TO_DELETE_HR = "Failed to remove HR from position";

	public static final String FAILED_DELETING_HR = "failed_deleting_hr";

	public static final String FAILED_TO_ADD_VENDOR_TO_POSITION = "Failed to add vendor to a position";

	public static final String FAILED_ADDING_VENDOR = "failed_to_add_vendor";

	public static final String FAILED_DELETING_VENDOR = "failed_deleting_vendor";

	public static final String FAILED_TO_DELETE_VENDOR = "Failed to delete a vendor from position";

	public static final String INTERVIEWER_NOT_FOUND = "Interviewer not found.";

	public static final String INTERVIEWER_MISSING = "interviewer_missing";

	public static final String FAILED_TO_SEND_FEEDBACK_REMINDER = "Failed to send feedback reminder";

	public static final String FAILED_SENDING_FEEDBACK_REMINDER = "failed_sending_feedback_reminder";

	public static final String FEEDBACK_REMINDER_FAILED = "feedback_reminder_failed";

	public static final String CAN_NOT_ASSIGN_NON_HR_TO_POSITION = "Can not assign a non HR user to any position.";

	public static final String HR_ASSIGNMENT_FAILED = "hr_assignment_failed";

	public static final String FAILED_TO_ASSIGN_HR_TO_POSITION = "failed_to_assign_hr_to_positions";

	public static final String FAILED_TO_CHECK_EMAIL = "failed_to_check_email";

	public static final String FAILED_TO_CHECK_MOBILE = "failed_to_check_mobile";

	public static final String INVOICE_NOT_EXIST_FOR_CANDIATE = "invoice_not_exit_for_candidate";

	public static final String INVALID_TAX_NAME = "Fill proper name for tax";

	public static final String INCORRECT_TAX_NAME = "tax_can_not_be_empty_or_null";

	public static final String INVALID_TAX_DTO = "Tax dto can not be null or empty";

	public static final String INCORRECT_TAX_DTO = "tax_dto_can_not_be_empty_or_null";

	public static final String TAX_NAME_ALREADY_EXIST = "tax name already exist";

	public static final String TAX_NAME_EXIST = "tax_name_already_exist";

	public static final String TAX_NAME_MISSING = "tax name not valid";

	public static final String TAX_NAME_NOT_FOUND = "tax_name_not_found";

	public static final String TAX_VALUE_MISSING = "tax value not valid";

	public static final String TAX_VALUE_NOT_FOUND = "tax_value_not_found";

	public static final String TAX_ADD_FAILED = "tax adding failed";

	public static final String TAX_ADD_FAILURE = "tax_adding_failed";

	public static final String TAX_UPDATE_FAILED = "tax update failed";

	public static final String TAX_UPDATE_FAILURE = "tax_update_failed";

	public static final String TAX_DELETE_FAILED = "tax delete failed";

	public static final String TAX_DELETE_FAILURE = "tax_delete_failed";

	public static final String TAX_DETAILS_DTO_NULL = "tax details dto can not be empty or null";

	public static final String TAX_DETAILS_DTO__NULL = "tax_details_dto_can_not_be_empty_or_null";

	public static final String INVALID_TAX = "Tax Not Found";

	public static final String TAX_NOT_FOUND = "tax_not_found";

	public static final String TAX_NUMBER_INVALID = "tax number is empty";

	public static final String TAX_NUMBER_NOT_VALID = "not_valid_tax_number";

	public static final String SETTING_CAN_NOT_BE_NULL = "invoice setting is empty or null";

	public static final String SETTING_CAN_NOT_BE_EMPTY = "invoice_setting_is_empty_or_null";

	public static final String INVOICE_SETTING_ADDED_FAILED = "invoice setting added failed";

	public static final String INVOICE_SETTING_ADDED_FAILURE = "invoice_setting_added_failure";

	public static final String ERROR_SUBMITTING_EMAIL_RESPONSE = "error_submitting-email_response";

	public static final String INVOICE_NUMBER_ALREADY_EXIST = "invoice number already exist";

	public static final String INVOICE_NUMBER_ALREADY__EXIST = "invoice_number_already_exist";

	public static final String INVOICE_NUMBER_NOT_EXIST = "invoice number not exist";

	public static final String NO_CANDIDATE_FOUND = "No candidate found";

	public static final String N0_CANDIDATE = "no_candidate";

	public static final String DATE_NOT_PRESENT = "Query date is not selected, please select the date and try again.";

	public static final String NO_DATE = "no_date";

	public static final String RECRUITEMT_PROFILE_ERROR_OCCURED = "Something went wrong while generating this report, please try again.";

	public static final String RECRUITMENT_PROFILE_ERROR = "recruitement_profile_error";

	public static final String NOT_PERMITTED = "not_permitted";

	public static final String FAILED_TO_GET_EMAIL = "Couldn't fetch any email for your account, please try again.";

	public static final String FAILED_GETTING_EMAIL = "failed_getting_emails";

	public static final String NO_DATA_FOUND = "no_data_found";

	public static final String NO_CATEGORY_SENT = "No sub-categories sent to add, try again !!!";

	public static final String DELETING_SUB_CATEGORY_FAILED = "Deleting of sub category failed, try again !!!";

	public static final String NO_ON_BOARD_DETAILS_SENT = "No on board details found";

	public static final String DELETING_ON_BOARD_DETAILS_FAILED = "Deleting onboard details failed, try again !!!";

	public static final String STATUS_CHANGE_ON_BOARD_DETAILS_FAILED = "Couldn't change status of this activity, try again !!!";

	public static final String DELETING_EMPLOYEE_FAILED = "Deleting of employee failed, try again";

	public static final String ADDING_COMMENT_FAILED = "Couldn't publish your comment, try again !!!";

	public static final String ADDING_COMMENT_ERROR = "failed_to_add_comment";

	public static final String EDITING_COMMENT_FAILED = "Editing comment failed, try again";

	public static final String EDITING_COMMENT_ERROR = "failed_to_edit_comment";

	public static final String DELETING_COMMENT_ERROR = "failed_to_delete_comment";

	public static final String DELETING_COMMENT_FAILED = "Failed to delete comment, try again !!!";

	public static final String FETCHING_ACTIVITY_FAILED = "Couldn't fetch your activity, try again !!!";

	public static final String FAILED_TO_FETCH_CATEGORY = "failed_to_fetch_category";

	public static final String FEW_STATUS_CHANGE_FAILED_CANDIDATE_IN_EMPLOYEE_STATUS = "candidate_exists_with_employee_status";

	public static final String FAILED_TO_ADD_TEMPLATE = "Oops, Couldn't add template, try again !!!";

	public static final String FAILED_ADDING_TEMPLATES = "FAILED_TO_ADD_TEMPLATE";

	public static final String FAILED_TO_EDIT_TEMPLATE = "Failed to edit template, try again !!!";

	public static final String FAILED_EDITING_TEMPLATES = "failed_editing_templates";

	public static final String FAILED_DELETEING_TEMPLATES = "failed_deleting_templates";

	public static final String FAILED_TO_DELETE_TEMPLATE = "Failed to delete template, try again !!!";

	public static final String DUMMY_DATA_ALREADY_ADDED = "Dummy data already added";

	public static final String DUMMY_DATA_EXISTS = "dummy_data_exists";

	public static final String FAILED_TASK_EDITING = "failed_to_edit_task";

	public static final String FAILED_EDITING_TASK = "Failed to edit task, try again later !!!";

	public static final String EMPLOYEE_NOT_AVAILABLE = "Employee not available";

	public static final String EMPLOYEE_NOT_FOUND = "employee_not_found";

	public static final String AUTHENTICATION_FAILED = "email_authentication_failed";

	public static final String FAILED_TO_CONFIGURE_SYNC_INFO = "Failed to configure email sync info";

	public static final String FAILED_TO_SYNC_UPDATE = "failed_sync_config";

	public static final String FAILED_TO_STOP_SYNC = "Failed to stop sync operation";

	public static final String FAILED_STOP_SYNC = "stopping_sync_failed";

	public static final String SYNC_STOPPED = "Sync stopped";

	public static final String EMPLOYEE_ID_EXISTS = "Employee Id Exists";

	public static final String DUPLICATE_EMPLOYEE_ID = "employee_id_exists";

	public static final String SIXTH_SENSE_SOURCE_NOT_CONFIGURED = "source_not_configured";

	public static final String STATUS_CHANGE_NOT_ALLOWED = "status_change_not_allowed";

	public static final String CAN_NOT_JOIN_MORE_CANDIDATE = "Number of candidates in joined status exceed the number of openings. Please check and try again.";

	public static final String NO_TEAM_ADDED = "No team added";

	public static final String TEAM_NOT_AVAILABLE = "Team not available";

	public static String NO_TEAM_FOUND = "no_team_found";

	public static String resolveAuthError(AuthenticationException authEx) throws IOException {
		try {
			RestResponse errorResponse;
			if (authEx instanceof UsernameNotFoundException) {
				errorResponse = new RestResponse(false, authEx.getMessage(), USER_NOT_FOUND);
			} else if (authEx instanceof BadCredentialsException) {
				errorResponse = new RestResponse(false, PASSWORD_INCORRECT, BAD_CREDENTIALS);
			} else if (authEx instanceof RecruizAuthException) {
				errorResponse = new RestResponse(false, authEx.getMessage(), ((RecruizAuthException) authEx).getErrConstant());
			} else {
				errorResponse = new RestResponse(false, authEx.getMessage(), AUTH_FAILURE);
			}

			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(errorResponse);
		} catch (JsonProcessingException jsonEx) {
			logger.error(jsonEx.getMessage(), jsonEx);
		}
		return "Server Failure";
	}
}
