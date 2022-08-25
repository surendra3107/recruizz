package com.bbytes.recruiz.utils;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;

import com.bbytes.recruiz.rest.dto.models.OfferLetterFormulaDTO;

public class FormulaCalculation {




	public static void main(String... h){

		int finalVaraibleValue = 0;

		List<OfferLetterFormulaDTO> values = new ArrayList<>();
		OfferLetterFormulaDTO dto = new OfferLetterFormulaDTO();
		dto.setId("BASIC");
		dto.setValue("120000");
		values.add(dto);
		dto = new OfferLetterFormulaDTO();
		dto.setId("HRA");
		dto.setValue("50000");
		values.add(dto);

		dto = new OfferLetterFormulaDTO();
		dto.setId("CA");
		dto.setValue("1200");
		values.add(dto);

		dto = new OfferLetterFormulaDTO();
		dto.setId("FOOD");
		dto.setValue("2200");
		values.add(dto);

		dto = new OfferLetterFormulaDTO();
		dto.setId("MEDICAL");
		dto.setValue("2000");
		values.add(dto);

		dto = new OfferLetterFormulaDTO();
		dto.setId("BONUS");
		dto.setValue("1500");
		values.add(dto);


		String formula = "BASIC+200-500*2/10/2/2+HRA-CA*2-FOOD/10+MEDICAL+BONUS";

		String splitValue ="";

		List<String> valueArray = new ArrayList<>();
		List<String> opratorArray = new ArrayList<>();

		try{ 
			char[] formulaArray = formula.toCharArray(); 

			for (char c : formulaArray) {

				if(c=='*'){
					if(splitValue.equals("")){
						String[] first = formula.split("\\*",2);
						valueArray.add(first[0]);
						opratorArray.add("*");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("\\*",2);
						valueArray.add(first[0]);
						opratorArray.add("*");
						splitValue = first[1];
					}

				} else if(c=='/'){

					if(splitValue.equals("")){
						String[] first = formula.split("/",2);
						valueArray.add(first[0]);
						opratorArray.add("/");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("/",2);
						valueArray.add(first[0]);
						opratorArray.add("/");
						splitValue = first[1];
					}

				} else if(c=='+'){

					if(splitValue.equals("")){
						String[] first = formula.split("\\+",2);
						valueArray.add(first[0]);
						opratorArray.add("+");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("\\+",2);
						valueArray.add(first[0]);
						opratorArray.add("+");
						splitValue = first[1];
					}

				} else if(c=='-'){

					if(splitValue.equals("")){
						String[] first = formula.split("-",2);
						valueArray.add(first[0]);
						opratorArray.add("-");
						splitValue = first[1];
					}else{
						String[] first = splitValue.split("-",2);
						valueArray.add(first[0]);
						opratorArray.add("-");
						splitValue = first[1];
					}
				}


			}						

			valueArray.add(splitValue);

			System.out.println(valueArray);
			System.out.println(opratorArray);


			for(int i=0;i<valueArray.size();i++){


				int varaibleValue = getVaraibleValue(valueArray.get(i), values);

				if(varaibleValue == -1){
					System.out.println("Getting error  ="+valueArray.get(i));
					break;
				}


				if(i==0){
					finalVaraibleValue = finalVaraibleValue + varaibleValue;
				}else{

					String operatorValue = opratorArray.get(i-1);
					
					if(operatorValue.equalsIgnoreCase("*")){

						finalVaraibleValue = finalVaraibleValue * varaibleValue;

					} else if(operatorValue.equalsIgnoreCase("/")){
						
						finalVaraibleValue = finalVaraibleValue / varaibleValue;

					} else if(operatorValue.equalsIgnoreCase("+")){
						
						finalVaraibleValue = finalVaraibleValue + varaibleValue;

					} else if(operatorValue.equalsIgnoreCase("-")){
						
						finalVaraibleValue = finalVaraibleValue - varaibleValue;

					}

				}


			}

			System.out.println("finalVaraibleValue  === "+finalVaraibleValue);


		}catch(Exception e){
			e.printStackTrace();
		}

	}


	public static int getVaraibleValue(String valueArray, List<OfferLetterFormulaDTO> values){

		try{
			int intValue = 0;
			int k =0;int p = 0; 
			if(StringUtils.isNumeric(valueArray)){

				intValue = Integer.parseInt(valueArray);
				k =1; p=1;
			}else{

				for (OfferLetterFormulaDTO value : values) {		
					if(value.getId().equalsIgnoreCase(valueArray)){
						k=1;
						if(value.getValue()!=null && !value.getValue().equals("") && !value.getValue().isEmpty()){
							p=1;
							intValue = Integer.parseInt(value.getValue());
						}

					}		
				}

			}


			if(k==0 || p==0){
				System.out.println("Not getting the value of variable !!");
				return -1;
			}

			return intValue;

		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}

	}


}
