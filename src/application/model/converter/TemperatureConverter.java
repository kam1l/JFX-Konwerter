package application.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TemperatureConverter extends Converter
{
	private BigDecimal value;
	private String firstScaleAbbreviation;
	private String secondScaleAbbreviation;

	public TemperatureConverter(String firstScaleAbbreviation, String secondScaleAbbreviation)
	{
		this.firstScaleAbbreviation = firstScaleAbbreviation;
		this.secondScaleAbbreviation = secondScaleAbbreviation;
	}

	@Override
	public String doValueConversion(String userInput, int numberOfDecimalPlaces) throws InvalidNumberFormatException
	{
		try
		{
			value = new BigDecimal(userInput);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidNumberFormatException();
		}

		BigDecimal result;

		if (firstScaleAbbreviation.matches("°C"))
		{
			result = convertFromCelsiusToOther();
		}
		else
		{
			result = convertFromOtherToOther();
		}

		result = result.setScale(numberOfDecimalPlaces, RoundingMode.HALF_UP);
		String formattedResult = result.stripTrailingZeros().toPlainString();

		return formattedResult;
	}

	private BigDecimal convertFromOtherToOther()
	{
		if (firstScaleAbbreviation.matches(secondScaleAbbreviation))
		{
			return value;
		}

		BigDecimal result = BigDecimal.ZERO;

		switch (firstScaleAbbreviation)
		{
		case "°F":
			result = (value.subtract(new BigDecimal(32))).divide(new BigDecimal(1.8), 100, RoundingMode.HALF_UP);
			break;

		case "°K":
			result = value.subtract(new BigDecimal(273.15));
			break;

		case "°R":
			result = value.divide(new BigDecimal(1.8), 100, RoundingMode.HALF_UP).subtract(new BigDecimal(273.15));
			break;

		case "°Ré":
			result = value.divide(new BigDecimal(0.8), 100, RoundingMode.HALF_UP);
			break;

		case "°Rø":
			result = (value.subtract(new BigDecimal(7.5))).multiply(new BigDecimal(40)).divide(new BigDecimal(21), 100,
					RoundingMode.HALF_UP);
			break;

		case "°D":
			result = new BigDecimal(100)
					.subtract((value.multiply(new BigDecimal(2)).divide(new BigDecimal(3), 100, RoundingMode.HALF_UP)));
			break;

		case "°N":
			result = value.divide(new BigDecimal(0.33), 100, RoundingMode.HALF_UP);
			break;
		}

		if (secondScaleAbbreviation.matches("°C"))
		{
			return result;
		}
		else
		{
			value = result;
			return convertFromCelsiusToOther();
		}
	}

	private BigDecimal convertFromCelsiusToOther()
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
			return value.multiply(new BigDecimal(21)).divide(new BigDecimal(40), 100, RoundingMode.HALF_UP)
					.add(new BigDecimal(7.5));

		case "°D":
			return (new BigDecimal(100).subtract(value)).multiply(new BigDecimal(3)).divide(new BigDecimal(2), 100,
					RoundingMode.HALF_UP);

		case "°N":
			return value.multiply(new BigDecimal(0.33));

		default:
			return BigDecimal.ZERO;
		}
	}
}
