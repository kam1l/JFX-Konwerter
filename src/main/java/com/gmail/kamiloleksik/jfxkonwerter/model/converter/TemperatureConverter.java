package com.gmail.kamiloleksik.jfxkonwerter.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;

public class TemperatureConverter implements Converter
{
	public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
	public static final int NEWTON_SCALE_CODE = 1;
	public static final int DELISLE_SCALE_CODE = 2;
	public static final int ROMER_SCALE_CODE = 3;
	public static final int REAUMUR_SCALE_CODE = 4;
	public static final int RANKINE_SCALE_CODE = 5;
	public static final int KELVIN_SCALE_CODE = 6;
	public static final int FARENHEIT_SCALE_CODE = 7;
	public static final int CELSIUS_SCALE_CODE = 8;

	private int firstScaleCode;
	private int secondScaleCode;

	public TemperatureConverter(int firstScaleCode, int secondScaleCode)
	{
		this.firstScaleCode = firstScaleCode;
		this.secondScaleCode = secondScaleCode;
	}

	@Override
	public String doValueConversion(InputValue<?> inputValue, int numberOfDecimalPlaces)
	{
		BigDecimal result;

		if (firstScaleCode == CELSIUS_SCALE_CODE)
		{
			result = convertFromCelsiusToOther((BigDecimal) inputValue.get()[0]);
		}
		else
		{
			result = convertFromOtherToOther((BigDecimal) inputValue.get()[0], numberOfDecimalPlaces);
		}

		result = result.setScale(numberOfDecimalPlaces, ROUNDING_MODE);

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

	private BigDecimal convertFromOtherToOther(BigDecimal value, int numberOfDecimalPlaces)
	{
		if (firstScaleCode == secondScaleCode)
		{
			return value;
		}

		BigDecimal result = BigDecimal.ZERO;

		switch (firstScaleCode)
		{
		case FARENHEIT_SCALE_CODE:
			result = (value.subtract(new BigDecimal(32))).divide(new BigDecimal(1.8), 300, ROUNDING_MODE);
			break;

		case KELVIN_SCALE_CODE:
			result = value.subtract(new BigDecimal(273.15));
			break;

		case RANKINE_SCALE_CODE:
			result = value.divide(new BigDecimal(1.8), 300, ROUNDING_MODE).subtract(new BigDecimal(273.15));
			break;

		case REAUMUR_SCALE_CODE:
			result = value.multiply(new BigDecimal(1.25));
			break;

		case ROMER_SCALE_CODE:
			result = (value.subtract(new BigDecimal(7.5))).multiply(new BigDecimal(40)).divide(new BigDecimal(21), 300,
					ROUNDING_MODE);
			break;

		case DELISLE_SCALE_CODE:
			result = new BigDecimal(100)
					.subtract((value.multiply(new BigDecimal(2)).divide(new BigDecimal(3), 300, ROUNDING_MODE)));
			break;

		case NEWTON_SCALE_CODE:
			result = value.divide(new BigDecimal(0.33), 300, ROUNDING_MODE);
			break;
		}

		if (secondScaleCode == CELSIUS_SCALE_CODE)
		{
			return result;
		}
		else
		{
			return convertFromCelsiusToOther(result);
		}
	}

	private BigDecimal convertFromCelsiusToOther(BigDecimal value)
	{
		switch (secondScaleCode)
		{
		case CELSIUS_SCALE_CODE:
			return value;

		case FARENHEIT_SCALE_CODE:
			return value.multiply(new BigDecimal(1.8)).add(new BigDecimal(32));

		case KELVIN_SCALE_CODE:
			return value.add(new BigDecimal(273.15));

		case RANKINE_SCALE_CODE:
			return (value.add(new BigDecimal(273.15))).multiply(new BigDecimal(1.8));

		case REAUMUR_SCALE_CODE:
			return value.multiply(new BigDecimal(0.8));

		case ROMER_SCALE_CODE:
			return value.multiply(new BigDecimal(0.525)).add(new BigDecimal(7.5));

		case DELISLE_SCALE_CODE:
			return (new BigDecimal(100).subtract(value)).multiply(new BigDecimal(1.5));

		case NEWTON_SCALE_CODE:
			return value.multiply(new BigDecimal(0.33));

		default:
			return BigDecimal.ZERO;
		}
	}
}
