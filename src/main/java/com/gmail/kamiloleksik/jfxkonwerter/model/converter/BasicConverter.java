package com.gmail.kamiloleksik.jfxkonwerter.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;

public class BasicConverter implements Converter
{
	public static final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
	private BigDecimal firstUnitRatio;
	private BigDecimal secondUnitRatio;

	public BasicConverter(BigDecimal firstUnitRatio, BigDecimal secondUnitRatio)
	{
		this.firstUnitRatio = firstUnitRatio;
		this.secondUnitRatio = secondUnitRatio;
	}

	@Override
	public String doValueConversion(InputValue<?> inputValue, int numberOfDecimalPlaces)
	{
		BigDecimal result = ((BigDecimal) inputValue.get()[0])
				.multiply(firstUnitRatio.divide(secondUnitRatio, 300, roundingMode));
		result = result.setScale(numberOfDecimalPlaces, roundingMode);

		return result.stripTrailingZeros().toPlainString();
	}

	@Override
	public InputValue<?> preprocessUserInput(String userInput) throws InvalidNumberFormatException
	{
		BigDecimal bdValue;

		try
		{
			bdValue = new BigDecimal(userInput);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidNumberFormatException();
		}

		return new InputValue<BigDecimal>(new BigDecimal[] { bdValue });
	}
}
