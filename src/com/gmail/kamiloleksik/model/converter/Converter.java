package com.gmail.kamiloleksik.model.converter;

import com.gmail.kamiloleksik.model.converter.exception.InvalidNumberBaseException;
import com.gmail.kamiloleksik.model.converter.exception.InvalidNumberFormatException;

public interface Converter
{
	public InputValue<?> preprocessUserInput(String userInput)
			throws InvalidNumberFormatException, InvalidNumberBaseException;

	public String doValueConversion(InputValue<?> value, int numberOfDecimalPlaces);
}