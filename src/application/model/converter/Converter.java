package application.model.converter;

import application.model.converter.exception.InvalidNumberBaseException;
import application.model.converter.exception.InvalidNumberFormatException;

public interface Converter
{
	public Object preprocessUserInput() throws InvalidNumberFormatException;

	public String doValueConversion(String userInput, int numberOfDecimalPlaces)
			throws InvalidNumberFormatException, InvalidNumberBaseException;

	public String formatResult();
}