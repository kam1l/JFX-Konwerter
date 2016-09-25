package application.service;

public class TemperatureConverter extends Converter
{
	private double value;
	private String firstScaleAbbreviation;
	private String secondScaleAbbreviation;
	private boolean isValidDouble;

	{
		isValidDouble = true;
	}

	public TemperatureConverter(String userInput, String firstScaleAbbreviation, String secondScaleAbbreviation)
	{
		try
		{
			value = Double.parseDouble(userInput);
		}
		catch (Exception e)
		{
			isValidDouble = false;
		}

		this.firstScaleAbbreviation = firstScaleAbbreviation;
		this.secondScaleAbbreviation = secondScaleAbbreviation;
	}

	@Override
	public String doValueConvesion()
	{
		if (isValidDouble)
		{
			double result;
			String formattedResult;

			if (firstScaleAbbreviation.matches("°C"))
			{
				result = convertFromCelsiusToOther(value, secondScaleAbbreviation);
			}
			else
			{
				result = convertFromOtherToOther(value, firstScaleAbbreviation, secondScaleAbbreviation);
			}

			formattedResult = getFormattedResult(result);

			return formattedResult;
		}
		else
		{
			return Message.INVALID_NUMBER_FORMAT_MESSAGE;
		}
	}

	private double convertFromOtherToOther(double value, String firstScaleAbbreviation, String secondScaleAbbreviation)
	{
		if (firstScaleAbbreviation.matches(secondScaleAbbreviation))
		{
			return value;
		}

		double result;

		switch (firstScaleAbbreviation)
		{
		case "°F":
			result = (value - 32) / 1.8;
			break;

		case "°K":
			result = value - 273.15;
			break;

		case "°R":
			result = value / 1.8 - 273.15;
			break;

		case "°Ré":
			result = 1.25 * value;
			break;

		case "°Rø":
			result = (value - 7.5) * 40 / 21;
			break;

		case "°D":
			result = 100 - value * 2 / 3;
			break;

		case "°N":
			result = value / 0.33;
			break;

		default:
			result = 0;
		}

		if (secondScaleAbbreviation.matches("°C"))
		{
			return result;
		}
		else
		{
			return convertFromCelsiusToOther(result, secondScaleAbbreviation);
		}
	}

	private double convertFromCelsiusToOther(double value, String secondScaleAbbreviation)
	{
		switch (secondScaleAbbreviation)
		{
		case "°C":
			return value;

		case "°F":
			return value * 1.8 + 32;

		case "°K":
			return value + 273.15;

		case "°R":
			return (value + 273.15) * 1.8;

		case "°Ré":
			return value / 0.8;

		case "°Rø":
			return value * 21 / 40 + 7.5;

		case "°D":
			return (100 - value) * 3 / 2;

		case "°N":
			return value * 0.33;

		default:
			return 0;
		}
	}
}
