package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockLessThanZeroException extends Exception {

	private static final long serialVersionUID = 1L;

	public BeerStockLessThanZeroException(Long id, int quantityToDecrement) {
		super(String.format("Beer with ID %s has quantity less than zero. Quantity to decrement %s", id, quantityToDecrement));
	}
}
