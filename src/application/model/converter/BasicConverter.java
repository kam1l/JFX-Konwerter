package application.model.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import application.model.converter.exception.InvalidNumberFormatException;

public class BasicConverter implements Converter
{
	public static final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
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
	public String doValueConversion(InputValue<?> inputValue, int numberOfDecimalPlaces)
	{
		BigDecimal result = ((BigDecimal) inputValue.get())
				.multiply(firstUnitRatio.divide(secondUnitRatio, numberOfDecimalPlaces + 1, roundingMode));
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
}
