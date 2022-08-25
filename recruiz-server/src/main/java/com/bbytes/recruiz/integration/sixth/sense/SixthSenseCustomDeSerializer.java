package com.bbytes.recruiz.integration.sixth.sense;

import java.io.IOException;
import java.util.List;

import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseResultDTO;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SixthSenseCustomDeSerializer extends JsonDeserializer<Object> {
	@Override
	public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ObjectCodec oc = jp.getCodec();
		JsonNode node = oc.readTree(jp);
		try {

			List<SixthSenseResultDTO> resulList = mapper.readValue(node.toString(),
					new TypeReference<List<SixthSenseResultDTO>>() {
					});
			return resulList;
		} catch (JsonParseException | JsonMappingException e) {
			try {
				String resultMessage = mapper.readValue(node.toString(), String.class);
				return resultMessage;
			} catch (Exception ex) {
				return null;
			}
		}
	}
}