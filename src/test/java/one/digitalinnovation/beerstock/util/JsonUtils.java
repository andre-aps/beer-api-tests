package one.digitalinnovation.beerstock.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	public static String asJsonString(Object dto) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			return objectMapper.writeValueAsString(dto);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
