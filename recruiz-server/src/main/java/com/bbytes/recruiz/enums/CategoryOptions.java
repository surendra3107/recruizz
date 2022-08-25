package com.bbytes.recruiz.enums;

public enum CategoryOptions {

//	Sales("Sales"), Software("Software"), Marketing("Marketing"),HR("HR"), Finance("Finance");
	
	Sales_Retail_Business_Development("Sales, Retail, Business Development"),
	IT_Software_Application_Programming("IT Software - Application Programming"),
	Maintenance("IT Software - Application Programming, Maintenance"),
	ITES_BPO_KPO_LPO_Customer_Service_Operations("ITES, BPO, KPO, LPO, Customer Service, Operations"),
	Production_Manufacturing_Maintenance("Production, Manufacturing, Maintenance"),
	Accounts_Finance_Tax_Company_Secretary_Audit("Accounts, Finance, Tax, Company, Secretary, Audit"),
	Analytics_Business_Intelligence("Analytics & Business Intelligence"),
	Architecture_InteriorDesign("Architecture, Interior Design"),
	Beauty_Fitness_Spa_Services("Beauty / Fitness/ Spa Services"),
	CSR_Sustainability("CSR & Sustainability"),
	Defence_Forces_Security_Services("Defence Forces, Security Services"),
	Design_Creative_User_Experience("Design, Creative, User Experience"),
	Executive_Assistant_Front_office_Data_Entry("Executive Assistant, Front office, Data Entry"),
	Export_Import_Merchandising("Export, Import, Merchandising"),
	Fashion_Design_Merchandising("Fashion Design, Merchandising"),
	Hotels_Restaurants("Hotels, Restaurants"),
	HR_Recruitment_Administration_IR("HR_Recruitment, Administration, IR"),
	Financial_Services_Banking_Investments_Insurance("Financial Services, Banking, Investments, Insurance"),
	IT_Hardware_Technical_Support("IT Hardware, Technical Support"),
	Telecom_Engineering("Telecom Engineering"),
	IT_Software_Client_Server_Programming("IT Software - Client/ Server Programming"),
	IT_Software_System_Programming("IT Software - System Programming"),
	IT_Software_eCommerce_Internet_Technologies("IT Software - e-commerce, Internet Technologies"),
	IT_Software_Mainframe("IT Software - Mainframe"),
	IT_Software_Middleware("IT Software - Middleware"),
	IT_Software_Mobile("IT Software - Mobile"),
	IT_Software_Others("IT Software - Others"),
	IT_Software_Telecom_software("IT Software - Telecom software"),
	IT_Software_DBA_Data_warehousing("IT Software - DBA, Data warehousing"),
	IT_Software_Embedded_EDA_VLSI_ASIC_ChipDesign("IT Software - Embedded, EDA, VLSI, ASIC, Chip Design"),
	IT_Software_ERP_CRM("IT Software -  ERP, CRM"),
	IT_Software_Network_Administrator_Security("IT Software - Network Administrator, Security"),
	IT_Software_QA_Testing("IT Software - QA & Testing"),
	IT_Software_Systems_EDP_MIS("IT Software - Systems, EDP & MIS"),
	Journalism_Editing_Content("Journalism, Editing, Content"),
	Legal_Regulatory_Intellectual_Property("Legal, Regulatory, Intellectual Property"),
	Marketing_Advertising_MR_PR_Media_Planning("Marketing, Advertising, MR, PR, Media Planning"),
	Medical_Healthcare_RD_Pharmaceuticals_Biotechnology("Medical, Healthcare, 	R&D, Pharmaceuticals, Biotechnology"),
	Packaging("Packaging"),
	Self_Employed_Entrepreneur_Independent_Consultant("Self Employed, Entrepreneur, Independent Consultant"),
	Shipping("Shipping"),
	Site_Engineering_Project_Management("Site Engineering, Project Management"),
	Strategy_Management_Consultant_Corporate_Planning("Strategy Management Consultant, Corporate Planning"),
	Supply_Chain_Logistics_Purchase_Materials("Supply Chain, Logistics, Purchase, Materials"),
	Teaching_Education_Training_Counselling("Teaching, Education, Training, Counselling"),
	Top_Management("Top Management"),
	Travel_Tours_Ticketing_Airlines("Travel, Tours, Ticketing, Airlines"),
	TV_Films_Production_Broadcasting("TV, Films, Production, Broadcasting"),
	Others("Others");


	String displayName;

	private CategoryOptions(String displayName) {
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
