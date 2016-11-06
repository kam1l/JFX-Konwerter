package application.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import application.model.converter.exception.InvalidNumberFormatException;

public class TemperatureConverter implements Converter
{
	public static final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
	private String firstScaleAbbreviation;
	private String secondScaleAbbreviation;

	public TemperatureConverter(String firstScaleAbbreviation, String secondScaleAbbreviation)
	{
		this.firstScaleAbbreviation = firstScaleAbbreviation;
		this.secondScaleAbbreviation = secondScaleAbbreviation;
	}

	@Override
	public String doValueConversion(InputValue<?> inputValue, int numberOfDecimalPlaces)
	{
		BigDecimal result;

		if (firstScaleAbbreviation.equals("°C"))
		{
			result = convertFromCelsiusToOther((BigDecimal) inputValue.get(), numberOfDecimalPlaces);
		}
		else
		{
			result = convertFromOtherToOther((BigDecimal) inputValue.get(), numberOfDecimalPlaces);
		}

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

		return new InputValue<BigDecimal>(bdValue);
	}

	private BigDecimal convertFromOtherToOther(BigDecimal value, int numberOfDecimalPlaces)
	{
		if (firstScaleAbbreviation.equals(secondScaleAbbreviation))
		{
			return value;
		}

		BigDecimal result = BigDecimal.ZERO;

		switch (firstScaleAbbreviation)
		{
		case "°F":
			result = (value.subtract(new BigDecimal(32))).divide(new BigDecimal(1.8), numberOfDecimalPlaces + 1,
					roundingMode);
			break;

		case "°K":
			result = value.subtract(new BigDecimal(273.15));
			break;

		case "°R":
			result = value.divide(new BigDecimal(1.8), numberOfDecimalPlaces + 1, roundingMode)
					.subtract(new BigDecimal(273.15));
			break;

		case "°Ré":
			result = value.divide(new BigDecimal(0.8), numberOfDecimalPlaces + 1, roundingMode);
			break;

		case "°Rø":
			result = (value.subtract(new BigDecimal(7.5))).multiply(new BigDecimal(40)).divide(new BigDecimal(21),
					numberOfDecimalPlaces + 1, roundingMode);
			break;

		case "°D":
			result = new BigDecimal(100).subtract((value.multiply(new BigDecimal(2)).divide(new BigDecimal(3),
					numberOfDecimalPlaces + 1, roundingMode)));
			break;

		case "°N":
			result = value.divide(new BigDecimal(0.33), numberOfDecimalPlaces + 1, roundingMode);
			break;
		}

		if (secondScaleAbbreviation.equals("°C"))
		{
			return result;
		}
		else
		{
			value = result;
			return convertFromCelsiusToOther(value, numberOfDecimalPlaces);
		}
	}

	private BigDecimal convertFromCelsiusToOther(BigDecimal value, int numberOfDecimalPlaces)
	{
		switch (secondScaleAbbreviation)
		{
		case "°C":
			return value;

		case "°F":
			return value.multiply(new BigDecimal(1.8)).add(new BigDecimal(32));

		case "°K":
			return value.add(new BigDecimal(273.15));

		case "°R":
			return (value.add(new BigDecimal(273.15))).multiply(new BigDecimal(1.8));

		case "°Ré":
			return value.multiply(new BigDecimal(0.8));

		case "°Rø":
			return value.multiply(new BigDecimal(21))
					.divide(new BigDecimal(40), numberOfDecimalPlaces + 1, roundingMode).add(new BigDecimal(7.5));

		case "°D":
			return (new BigDecimal(100).subtract(value)).multiply(new BigDecimal(3)).divide(new BigDecimal(2),
					numberOfDecimalPlaces + 1, roundingMode);

		case "°N":
			return value.multiply(new BigDecimal(0.33));

		default:
			return BigDecimal.ZERO;
		}
	}
}
