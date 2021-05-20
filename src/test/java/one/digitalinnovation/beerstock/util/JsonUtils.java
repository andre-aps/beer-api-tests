package one.digitalinnovation.beerstock.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import one.digitalinnovation.beerstock.dto.BeerDTO;

public class JsonUtils {

	public static String asJsonString(BeerDTO beerDTO) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			return objectMapper.writeValueAsString(beerDTO);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
