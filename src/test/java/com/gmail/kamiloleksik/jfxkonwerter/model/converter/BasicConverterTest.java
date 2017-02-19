package com.gmail.kamiloleksik.jfxkonwerter.model.converter;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;

public class BasicConverterTest
{
	private BasicConverter basicConverter = new BasicConverter(new BigDecimal("2"), new BigDecimal("1.245"));
	
	@Test(expected = InvalidNumberFormatException.class)
	public void preprocessingShouldThrowExceptionIfInputContainsInvalidCharacters() throws InvalidNumberFormatException
	{
		basicConverter.preprocessUserInput("!");
	}
	
	@Test
	public void conversionResultShouldBeValid() throws InvalidNumberFormatException
	{
		// Result is calculated upon the formula: value * firstUnitRatio / secondUnitRatio
		InputValue<?> value = basicConverter.preprocessUserInput("2");
		String conversionResult = basicConverter.doValueConversion(value, 4);
		
		assertEquals("3.2129", conversionResult);
		
		value = basicConverter.preprocessUserInput("27.432");
		conversionResult = basicConverter.doValueConversion(value, 30);
		
		assertEquals("44.067469879518072289156626506024", conversionResult);
	}
}
