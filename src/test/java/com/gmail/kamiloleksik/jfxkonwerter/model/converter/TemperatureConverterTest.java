package com.gmail.kamiloleksik.jfxkonwerter.model.converter;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;

public class TemperatureConverterTest
{
	private TemperatureConverter temperatureConverter;
	private String conversionResult;
	private InputValue<?> value;

	@Test(expected = InvalidNumberFormatException.class)
	public void preprocessingShouldThrowExceptionIfInputContainsInvalidCharacters() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		temperatureConverter.preprocessUserInput("34!");
	}

	@Test
	public void newtonShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-4.5455", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("61.5909", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("82.4242", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("677.1245", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("376.1803", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("217.4545", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.NEWTON_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("103.0303", conversionResult);
	}
	
	@Test
	public void delisleShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("25.52", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("48.1", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("61.8667", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("630.87", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("350.4833", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("171.2", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.DELISLE_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("77.3333", conversionResult);
	}
	
	@Test
	public void romerShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("16.6571", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("74.2857", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("40.381", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("582.5271", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("323.6262", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("122.8571", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.ROMER_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("50.4762", conversionResult);
	}
	
	@Test
	public void reaumurShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("14.025", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("86.25", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("29.8125", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("568.17", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("315.65", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("108.5", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.REAUMUR_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("42.5", conversionResult);
	}
	
	@Test
	public void rankineShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-83.9062", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("531.3917", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-125.9871", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-203.4089", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("18.8889", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-425.67", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.RANKINE_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-254.2611", conversionResult);
	}
	
	@Test
	public void kelvinShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-78.9195", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("508.725", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-118.0537", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-191.32", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("61.2", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-398.47", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.KELVIN_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("-239.15", conversionResult);
	}
	
	@Test
	public void farenheitShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("0.3667", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("148.3333", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("8.0833", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("0.8889", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("493.67", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("274.2611", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.FARENHEIT_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("1.1111", conversionResult);
	}
	
	@Test
	public void celsiusShouldBeConvertProperly() throws InvalidNumberFormatException
	{
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.NEWTON_SCALE_CODE);
		value = temperatureConverter.preprocessUserInput("34");
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("11.22", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.DELISLE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("99", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.ROMER_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("25.35", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.REAUMUR_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("27.2", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.RANKINE_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("552.87", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.KELVIN_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("307.15", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.FARENHEIT_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("93.2", conversionResult);
		
		temperatureConverter = new TemperatureConverter(TemperatureConverter.CELSIUS_SCALE_CODE,
				TemperatureConverter.CELSIUS_SCALE_CODE);
		conversionResult = temperatureConverter.doValueConversion(value, 4);
		
		assertEquals("34", conversionResult);
	}
}
