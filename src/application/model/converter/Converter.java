package application.model.converter;

import application.model.converter.exception.InvalidNumberBaseException;
import application.model.converter.exception.InvalidNumberFormatException;

public interface Converter
{
	public InputValue preprocessUserInput(String userInput)
			throws InvalidNumberFormatException, InvalidNumberBaseException;

	public String doValueConversion(InputValue value, int numberOfDecimalPlaces);
}