package com.gmail.kamiloleksik.jfxkonwerter.model.converter;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberBaseException;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;

public class NumberBaseConverterTest
{
	NumberBaseConverter numberBaseConverter;

	@Test(expected = InvalidNumberFormatException.class)
	public void ifUserInputIsEmptyExceptionShouldBeThrown()
			throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		numberBaseConverter = new NumberBaseConverter(new BigInteger("4"), new BigInteger("16"));
		numberBaseConverter.preprocessUserInput("");
	}
	
	@Test(expected = InvalidNumberBaseException.class)
	public void ifUserInputContainsInvalidCharactersRegardingInputNumberBaseExceptionShouldBeThrown()
			throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		numberBaseConverter = new NumberBaseConverter(new BigInteger("2"), new BigInteger("10"));
		numberBaseConverter.preprocessUserInput("1002");
	}
	
	@Test
	public void numberBasesShouldBeConvertProperly()
			throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		numberBaseConverter = new NumberBaseConverter(new BigInteger("10"), new BigInteger("2"));
		InputValue<?> value = numberBaseConverter.preprocessUserInput("452");
		String conversionResult = numberBaseConverter.doValueConversion(value, 4);
		
		assertEquals("111000100", conversionResult);
		
		numberBaseConverter = new NumberBaseConverter(new BigInteger("8"), new BigInteger("16"));
		value = numberBaseConverter.preprocessUserInput("704");
		conversionResult = numberBaseConverter.doValueConversion(value, 4);
		
		assertEquals("1C4", conversionResult);
	}
}
