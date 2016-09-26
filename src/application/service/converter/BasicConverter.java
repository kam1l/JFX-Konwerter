package application.service.converter;

public class BasicConverter extends Converter
{
	private double value;
	private double firstUnitRatio;
	private double secondUnitRatio;
	private boolean isValidDouble;

	{
		isValidDouble = true;
	}

	public BasicConverter(double firstUnitRatio, double secondUnitRatio)
	{
		this.firstUnitRatio = firstUnitRatio;
		this.secondUnitRatio = secondUnitRatio;
	}

	@Override
	public String doValueConversion(String userInput, int numberOfDecimalPlaces) throws InvalidNumberFormatException
	{
		try
		{
			value = Double.parseDouble(userInput);
		}
		catch (Exception e)
		{
			isValidDouble = false;
		}

		if (isValidDouble)
		{
			double result = value * firstUnitRatio / secondUnitRatio;
			String formattedResult = getFormattedResult(result, numberOfDecimalPlaces);

			return formattedResult;
		}
		else
		{
			throw new InvalidNumberFormatException();
		}
	}
}
