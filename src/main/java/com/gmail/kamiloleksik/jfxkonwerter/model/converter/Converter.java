package com.gmail.kamiloleksik.jfxkonwerter.model.converter;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberBaseException;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;

public interface Converter
{
	public InputValue<?> preprocessUserInput(String userInput)
			throws InvalidNumberFormatException, InvalidNumberBaseException;

	public String doValueConversion(InputValue<?> value, int numberOfDecimalPlaces);
}