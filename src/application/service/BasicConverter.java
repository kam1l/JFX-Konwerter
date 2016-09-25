package application.service;

public class BasicConverter extends Converter
{
	private double value;
	private double firstUnitRatio;
	private double secondUnitRatio;
	private boolean isValidDouble;

	{
		isValidDouble = true;
	}

	public BasicConverter(String userInput, double firstUnitRatio, double secondUnitRatio)
	{
		try
		{
			value = Double.parseDouble(userInput);
		}
		catch (Exception e)
		{
			isValidDouble = false;
		}

		this.firstUnitRatio = firstUnitRatio;
		this.secondUnitRatio = secondUnitRatio;
	}

	@Override
	public String doValueConvesion()
	{
		if (isValidDouble)
		{
			double result = value * firstUnitRatio / secondUnitRatio;
			String formattedResult = getFormattedResult(result);

			return formattedResult;
		}
		else
		{
			return Message.INVALID_NUMBER_FORMAT_MESSAGE;
		}
	}
}
