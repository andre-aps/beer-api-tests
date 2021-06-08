package one.digitalinnovation.beerstock.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.exception.BeerStockLessThanZeroException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;

@ExtendWith(MockitoExtension.class)
class BeerServiceTest {

	private static final Long INVALID_ID = 1L;
	
	@Mock
	private BeerRepository beerRepository;
	
	private BeerMapper beerMapper = BeerMapper.INSTANCE;
	
	@InjectMocks
	private BeerService beerService;
	
	@Test
	void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

		//when
		when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
		when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);
		
		//then
		BeerDTO createBeerDTO = beerService.createBeer(expectedBeerDTO);
		
		assertThat(createBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
		assertThat(createBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
		assertThat(createBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
	}
	
	@Test
	void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrow() throws BeerAlreadyRegisteredException {
		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);
		
		//when
		when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));		
		
		//then
		assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
	}
	
	@Test
	void whenValidBeerNameIsGivenThenReturnAbeer() throws BeerNotFoundException {
		//given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
		
		//when
		when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));
		
		//then
		BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
		
		assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
	}

	@Test
	void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
		//given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		
		//when
		when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());
		
		//then
		assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
	}
	
	@Test
	void whenListBeerIsCalledThenReturnAlistOfBeers() {
		//given
		BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);
		
		//when
		when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));
		
		//then
		List<BeerDTO> foundListBeersDTO = beerService.listAll();
		
		assertThat(foundListBeersDTO, is(not(empty())));
		assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
	}
	
	@Test
	void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
		//when
		when(beerRepository.findAll()).thenReturn(Collections.emptyList());
		
		//then
		List<BeerDTO> foundListBeersDTO = beerService.listAll();
		
		assertThat(foundListBeersDTO, is(empty()));
	}
	
	@Test
	void whenExclusionIsCalledWithValidIdThenAbeerShouldBeDeleted() throws BeerNotFoundException {
		//given
		BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);
		
		//when
		when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
		doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());
		
		//then
		beerService.deleteById(expectedDeletedBeerDTO.getId());
		
		verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
		verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
	}
	
	@Test
	void whenExclusionIsCalledWithInValidIdThenThrowAnException() throws BeerNotFoundException {
		//when
		when(beerRepository.findById(INVALID_ID)).thenReturn(Optional.empty());
		
		//then
		assertThrows(BeerNotFoundException.class, () -> beerService.deleteById(INVALID_ID));
	}
	
	@Test
	void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
		
		//when
		when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
		when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
		
		int quantityToIncrement = 5;
		int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;
		
		//then
		BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);
		
		assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
		assertThat(expectedQuantityAfterIncrement, lessThan(incrementedBeerDTO.getMax()));
	}
	
	@Test
	void whenIncrementIsGreaterThanMaxThenThrowException() {
		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
		
		//when
		when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
		
		int quantityToIncrement = 55;
		
		//then
		assertThrows(BeerStockExceededException.class, 
				() -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
	}
	
	@Test
	void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockLessThanZeroException {
		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
		
		//when
		when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
		when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);
		
		int quantityToDecrement = 10;
		int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
		
		//then
		BeerDTO decrementedBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);
		
		assertThat(decrementedBeerDTO.getQuantity(), is(equalTo(expectedQuantityAfterDecrement)));
		assertThat(expectedQuantityAfterDecrement, is(greaterThanOrEqualTo((0))));
	}
	
	@Test
	void whenBeerStockQuantityIsLessThanZeroThenThrowException() throws BeerNotFoundException, BeerStockLessThanZeroException {
		//given
		BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
		Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);
		
		//when
		when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
		
		int quantityToDecrement = 11;
		
		//then
		assertThrows(BeerStockLessThanZeroException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
	}

}
