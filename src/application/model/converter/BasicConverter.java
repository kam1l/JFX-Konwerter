package application.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BasicConverter extends Converter
{
	private BigDecimal value;
	private BigDecimal firstUnitRatio;
	private BigDecimal secondUnitRatio;

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
		try
		{
			value = new BigDecimal(userInput);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidNumberFormatException();
		}

		BigDecimal result = value.multiply(firstUnitRatio.divide(secondUnitRatio, 100, RoundingMode.HALF_UP));
		result = result.setScale(numberOfDecimalPlaces, RoundingMode.HALF_UP);
		String formattedResult = result.stripTrailingZeros().toPlainString();

		return formattedResult;
	}
}
