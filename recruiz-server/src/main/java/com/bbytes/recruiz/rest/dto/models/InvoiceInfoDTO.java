package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InvoiceInfoDTO implements Serializable{

	private static final long serialVersionUID = -7535699225543784890L;

	private List<InvoiceInfoRateDTO> info = new ArrayList<>();
    
    private List<String> removedKey = new ArrayList<>();

}
