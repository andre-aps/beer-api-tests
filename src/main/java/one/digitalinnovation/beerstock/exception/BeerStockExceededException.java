package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockExceededException extends Exception {

	private static final long serialVersionUID = 1L;

	public BeerStockExceededException(Long id, int quantityToIncrement) {
        super(String.format("Beer with ID %s has quantity greater than stock capacity. Quantity to increment %s", id, quantityToIncrement));
    }
}
