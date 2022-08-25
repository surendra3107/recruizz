package com.bbytes.recruiz.utils;

public class PlutusURLConstants {

	public static final String API_CONTEXT_URL = "/recruiz/api/v1";

	public static final String REGISTER = API_CONTEXT_URL + "/customer/register";

	public static final String UPDATE_STRIPE_ID = API_CONTEXT_URL + "/update/stripe";

	public static final String ALL_PLANS = API_CONTEXT_URL + "/pricingPlans";

	public static final String PLAN_ID = API_CONTEXT_URL + "/pricingPlans/";

	public static final String PUSH_STAT = API_CONTEXT_URL + "/accountStatses/stats";

	public static final String INVOICE_URL = API_CONTEXT_URL + "/customer/invoice/";

	public static final String CUSTOMER_INFO = API_CONTEXT_URL + "/get/customer/";

	public static final String INVOICE_DETAILS = API_CONTEXT_URL + "/customer/invoice/details/";

	public static final String MARK_FOR_DELETE_ORG = API_CONTEXT_URL + "/customer/markdelete/";

	public static final String REMOVE_PLUTUS_CUSTOMER = API_CONTEXT_URL + "/removedCustomers/";

	public static final String UPDATE_PLAN_ON_PLUTUS = API_CONTEXT_URL + "/change/pricingPlan";

}
