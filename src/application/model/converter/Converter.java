package application.model.converter;

public abstract class Converter
{
	public abstract String doValueConversion(String userInput, int numberOfDecimalPlaces)
			throws InvalidNumberFormatException, InvalidNumberBaseException;

	public String removeNegativeAndTrailingZeros(String value)
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
