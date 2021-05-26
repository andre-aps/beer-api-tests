package one.digitalinnovation.beerstock.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.service.BeerService;
import one.digitalinnovation.beerstock.util.JsonUtils;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

	private static final String BEER_API_URL_PATH = "/api/v1/beers";
	private static final Long VALID_BEER_ID = 1L;
	private static final Long INVALID_BEER_ID = 2L;
	private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
	private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

	private MockMvc mockMvc;

	@Mock
	private BeerService beerService;

	@InjectMocks
	private BeerController beerController;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(beerController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.setViewResolvers((s, locale) -> new MappingJackson2JsonView())
				.build();
	}

	@Test
	void whenPostIsCalledThenAbeerIsCreated() throws Exception {
		//given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

		//when
		when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

		//then
		mockMvc.perform(post(BEER_API_URL_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonUtils.asJsonString(beerDTO)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name", is(beerDTO.getName())))
			.andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
			.andExpect(jsonPath("$.type", is(beerDTO.getType().name())));
	}
	
	@Test
	void whenPostIsCalledWithoutRequiredFieldThenAnErrorIsReturn() throws Exception {
		//given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		beerDTO.setBrand(null);

		//then
		mockMvc.perform(post(BEER_API_URL_PATH)
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonUtils.asJsonString(beerDTO)))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	void whenGetIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
		//given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		
		//when
		when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);
		
		//then
		mockMvc.perform(get(BEER_API_URL_PATH.concat("/").concat(beerDTO.getName()))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is(beerDTO.getName())))
				.andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
				.andExpect(jsonPath("$.type", is(beerDTO.getType().name())));
	}
	
	@Test
	void whenGetIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {
		//given
		BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		
		//when
		when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);
		
		//then
		mockMvc.perform(get(BEER_API_URL_PATH.concat("/").concat(beerDTO.getName()))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}
	
}
