package com.bbytes.recruiz.enums;

public enum IndustryOptions {

	// IT("IT"), Biotech("Biotech"), FMCG("FMCG"),Digital_Marketing("Digital
	// marketing"), Advertising("Advertising");
	IT_SW("IT-Software / Software Services"), ITES("BPO / Call Centre / ITES"), Automotive(
			"Automotive / Auto Ancillary / Auto Components"), Accounting_Finance(
					"Accounting / Finance"), Advertising_PR_MR_EventManagement(
							"Advertising / PR / MR / Event Management"), Agriculture_Diary(
									"Agriculture / Diary"), Airlines("Airlines"), Animation_Gaming(
											"Animation / Gaming"), Architecture_Interior_Design(
													"Architecture / Interior Design"), Aviation_Aerospace(
															"Aviation / Aerospace"), Banking_Financial_Services_Broking(
																	"Banking / Financial Services / Broking"), Brewery_Distillery(
																			"Brewery / Distillery"), Broadcasting(
																					"Broadcasting"), Ceramics_Sanitary_Ware(
																							"Ceramics / Sanitary ware"), Chemicals_PetroChemicals_Plastics_Rubber(
																									"Chemicals / Petro Chemicals / Plastics / Rubber"), Construction_Engineering_CementMetals(
																											"Construction / Engineering / Cement / Metals"), ConsumerElectronics_Appliances_Durables(
																													"Consumer Electronics / Appliances / Durables"), Courier_Transportation_Freight_Warehousing(
																															"Courier / Transportation / Freight / Warehousing"), Education_Teaching_Training(
																																	"Education / Teaching / Training"), Electricals_Switchgears(
																																			"Electricals /  Switchgears"), Export_Import(
																																					"Export / Import"), Glass_Glassware(
																																							"Glass/ Glassware"), Facility_Management(
																																									"Facility Management"), Fertilizers_Pesticides(
																																											"Fertilizers / Pesticides"), FMCG_Food_Beverages(
																																													"FMCG / Food / Beverages"), Food_Processing(
																																															"Food Processing"), Gems_Jewelry(
																																																	"Gems / Jewelry"), Government_Defence(
																																																			"Government / Defence"), HeatVentilation_AirConditioning(
																																																					"Heat Ventilation / Air Conditioning"), IndustrialProducts_HeavyMachinery(
																																																							"Industrial Products / Heavy Machinery"), Insurance(
																																																									"Insurance"), Iron_Steel(
																																																											"Iron & Steel"), IT_Hardware_Networking(
																																																													"IT - Hardware & Networking"), KPO_ResearchAnalysis(
																																																															"KPO / Research Analysis"), Legal(
																																																																	"Legal"), Media_Entertainment_Internet(
																																																																			"Media / Entertainment / Internet"), Internet_Ecommerce(
																																																																					"Internet / E-commerce"), Leather_Medical_Hospitals(
																																																																							"Leather / Medical / Hospitals"), Medical_Devices_Equipment(
																																																																									"Medical Devices / Equipment"), Mining_Quarrying(
																																																																											"Mining / Quarrying"), NGO_Social_Service_Regulators_IndustryAssociations(
																																																																													"NGO / Social Service / Regulators / Industry Associations"), Office_Equipment_Automation(
																																																																															"Office Equipment / Automation"), Oil_Gas_Energy_PowerInfrastructure(
																																																																																	"Oil & Gas / Energy / Power Infrastructure"), Pulp_Paper(
																																																																																			"Pulp & Paper"), Pharma_Biotech_Clinical_Research(
																																																																																					"Pharma / Biotech / Clinical Research"), Printing_Packaging(
																																																																																							"Printing / Packaging"), Publishing(
																																																																																									"Publishing"), RealEstate_Property(
																																																																																											"Real Estate / Property"), Recruitment_Staffing(
																																																																																													"Recruitment / Staffing"), Retail_Wholesale(
																																																																																															"Retail/Wholesale"), Security_LawEnforcement(
																																																																																																	"Security /	Law Enforcement"), Semiconductors_Electronics(
																																																																																																			"Semiconductors/Electronics"), Shipping_Marine(
																																																																																																					"Shipping/Marine"), StrategyManagementConsulting(
																																																																																																							"Strategy/	Management Consulting"), Sugar(
																																																																																																									"Sugar"), Telecom_ISP(
																																																																																																											"Telecom/ISP"), Textiles_Garments_Accessories(
																																																																																																													"Textiles/Garments/Accessories"), Travel_Hotels_Restaurants(
																																																																																																															"Travel/Hotels/Restaurants"), Tyres(
																																																																																																																	"Tyres"), WaterTreatment_WasteManagement(
																																																																																																																			"Water Treatment / Waste Management"), Wellness_Fitness_Sports_Beauty(
																																																																																																																					"Wellness/Fitness/Sports/Beauty"), Others(
																																																																																																																							"Others");
	String displayName;

	private IndustryOptions(String displayName) {
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
