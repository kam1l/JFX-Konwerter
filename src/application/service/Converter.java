package application.service;

import java.util.Locale;

public abstract class Converter
{
	public abstract String doValueConvesion();

	public String getFormattedResult(double result)
	{
		String formattingString = getFormattingString();
		String formattedResult = removeNegativeAndTrailingZeros(String.format(Locale.ROOT, formattingString, result));

		return formattedResult;
	}

	private String getFormattingString()
	{
		int numOfDecimalPlaces = Model.getNumberOfDecimalPlaces();

		return "%1$." + numOfDecimalPlaces + "f";
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
