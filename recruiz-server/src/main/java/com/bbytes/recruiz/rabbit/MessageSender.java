package com.bbytes.recruiz.rabbit;

import com.auklabs.recruiz.connect.core.dto.ConnectCandidateEventDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionEventDTO;

public interface MessageSender {

	void publishPosition(ConnectPositionEventDTO connectPositionEventDTO);

	void changeVendorPositionStatus(ConnectPositionEventDTO connectPositionEventDTO);

	void moveCandidateToRound(ConnectCandidateEventDTO connectCandidateEventDTO, String instanceId);

}
