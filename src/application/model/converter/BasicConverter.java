package application.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import application.model.converter.exception.InvalidNumberFormatException;

public class BasicConverter implements Converter
{
	String userInput;
	private BigDecimal value;
	private BigDecimal result;
	private BigDecimal firstUnitRatio;
	private BigDecimal secondUnitRatio;
	private int numberOfDecimalPlaces;

	public BasicConverter(String firstUnitRatio, String secondUnitRatio) throws InvalidNumberFormatException
	{
		try
		{
			this.firstUnitRatio = new BigDecimal(firstUnitRatio);
			this.secondUnitRatio = new BigDecimal(secondUnitRatio);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidNumberFormatException();
		}
	}

	@Override
	public String doValueConversion(String userInput, int numberOfDecimalPlaces) throws InvalidNumberFormatException
	{
		this.userInput = userInput;
		this.numberOfDecimalPlaces = numberOfDecimalPlaces;
		value = preprocessUserInput();
		result = value.multiply(firstUnitRatio.divide(secondUnitRatio, 100, RoundingMode.HALF_UP));

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

	@Override
	public String formatResult()
	{
		result = result.setScale(numberOfDecimalPlaces, RoundingMode.HALF_UP);

		return result.stripTrailingZeros().toPlainString();
	}
}
