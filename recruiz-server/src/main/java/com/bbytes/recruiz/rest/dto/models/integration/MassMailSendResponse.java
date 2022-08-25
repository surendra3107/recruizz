
package com.bbytes.recruiz.rest.dto.models.integration;

import java.io.Serializable;

import lombok.Data;

@Data
public class MassMailSendResponse implements Serializable {

	private final static long serialVersionUID = 2048592560889704603L;

	private SixthSenseMessageObject messageObject;

	public String emailMsgCode;

	public String emailMessage;

	public String source;

}
