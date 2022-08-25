package com.bbytes.recruiz.integration.servetel;

import java.io.Serializable;

import lombok.Data;

@Data
public class ServetelCallDetailReponseDto implements Serializable {

	private static final long serialVersionUID = -4586151472664264837L;
	
	private String uuid;

	private String call_to_number;

	private String caller_id_number;

	private String start_stamp;

	private String answer_stamp;

	private String end_stamp;

	private String hangup_cause;

	private String billsec;

	private Object digits_dialed;
	
	private String direction;

	private String duration;

	private Object answered_agent;

	private Object missed_agent;

	private Object call_flow;

	private Object broadcast_lead_fields;

	private String recording_url;

	private String call_status;

	private String call_id;

	private String outbound_sec;

	private String agent_ring_time;
}
