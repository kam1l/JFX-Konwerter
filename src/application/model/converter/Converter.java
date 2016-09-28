package application.model.converter;

import java.util.Locale;

public abstract class Converter
{
	public abstract String doValueConversion(String userInput, int numberOfDecimalPlaces)
			throws InvalidNumberFormatException, InvalidNumberBaseException;

	public String getFormattedResult(double result, int numberOfDecimalPlaces)
	{
		String formattingString = getFormattingString(numberOfDecimalPlaces);
		String formattedResult = removeNegativeAndTrailingZeros(String.format(Locale.ROOT, formattingString, result));

		return formattedResult;
	}

	private String getFormattingString(int numberOfDecimalPlaces)
	{
		return "%1$." + numberOfDecimalPlaces + "f";
	}

	protected String removeNegativeAndTrailingZeros(String value)
	{
		return removeNegativeZero(removeTrailingZeros(value));
	}

	protected String removeTrailingZeros(String value)
	{
		return (value.indexOf(".") == -1) ? value : value.replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	private String removeNegativeZero(String value)
	{
		return value.matches("-|-0") ? "0" : value;
	}
}
