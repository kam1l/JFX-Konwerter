package application.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import application.model.converter.exception.InvalidNumberFormatException;

public class TemperatureConverter implements Converter
{
	String userInput;
	private BigDecimal value;
	private BigDecimal result;
	private String firstScaleAbbreviation;
	private String secondScaleAbbreviation;
	private int numberOfDecimalPlaces;

	public TemperatureConverter(String firstScaleAbbreviation, String secondScaleAbbreviation)
	{
		this.firstScaleAbbreviation = firstScaleAbbreviation;
		this.secondScaleAbbreviation = secondScaleAbbreviation;
	}

	@Override
	public String doValueConversion(String userInput, int numberOfDecimalPlaces) throws InvalidNumberFormatException
	{
		this.userInput = userInput;
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
		value = preprocessUserInput();

		if (firstScaleAbbreviation.equals("°C"))
		{
			result = convertFromCelsiusToOther();
		}
		else
		{
			result = convertFromOtherToOther();
		}

		String formattedResult = formatResult();

		return formattedResult;
	}

	@Override
	public BigDecimal preprocessUserInput() throws InvalidNumberFormatException
	{
		BigDecimal bigDecimal;

		try
		{
			bigDecimal = new BigDecimal(userInput);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidNumberFormatException();
		}

		return bigDecimal;
	}

	private BigDecimal convertFromOtherToOther()
	{
		if (firstScaleAbbreviation.equals(secondScaleAbbreviation))
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

		if (secondScaleAbbreviation.equals("°C"))
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

	@Override
	public String formatResult()
	{
		result = result.setScale(numberOfDecimalPlaces, RoundingMode.HALF_UP);

		return result.stripTrailingZeros().toPlainString();
	}
}
