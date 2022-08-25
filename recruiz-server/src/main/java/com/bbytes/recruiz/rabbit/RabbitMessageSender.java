package com.bbytes.recruiz.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auklabs.recruiz.connect.core.dto.ConnectCandidateEventDTO;
import com.auklabs.recruiz.connect.core.dto.ConnectPositionEventDTO;
import com.auklabs.recruiz.connect.core.utils.RabbitMQConstants;

@Service
public class RabbitMessageSender implements MessageSender {

	private final RabbitTemplate rabbitTemplate;

	@Value("${recruiz.instance.identifier}")
	private String recruizInstanceId;

	@Autowired
	public RabbitMessageSender(final RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}
	// Commented out since connect feature is not used

	@Override
	public void publishPosition(ConnectPositionEventDTO connectPositionEventDTO) {
		// rabbitTemplate.convertAndSend(RabbitMQConstants.RECRUIZ_TOPIC_EXCHANGE,
		// connectPositionEventDTO.getConnectPositionDTO().getInstanceId() +
		// ".position.publish",
		// connectPositionEventDTO);
	}

	@Override
	public void changeVendorPositionStatus(ConnectPositionEventDTO connectPositionEventDTO) {
		// rabbitTemplate.convertAndSend(RabbitMQConstants.RECRUIZ_TOPIC_EXCHANGE,
		// connectPositionEventDTO.getConnectPositionDTO().getInstanceId() +
		// ".position.vendor.status",
		// connectPositionEventDTO);
	}

	@Override
	public void moveCandidateToRound(ConnectCandidateEventDTO connectCandidateEventDTO, String instanceId) {

		// rabbitTemplate.convertAndSend(RabbitMQConstants.RECRUIZ_TOPIC_EXCHANGE,
		// instanceId + ".candidate.move.round",
		// connectCandidateEventDTO);
	}

}
