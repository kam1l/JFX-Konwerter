package application.service;

import java.util.ArrayList;
import java.util.List;

public class Value
{
	private String stringValue;
	private double doubleValue;

	private String stringDecPart;
	private String stringIntPart;

	private boolean isNegative;
	private boolean hasValidCharacters;
	private boolean isValidStringValue;
	private boolean isValidDoubleValue;

	private int dotIndex;
	private double decPart;
	private long intPart;

	private List<Double> doubleValuesOfEachCharOfIntPart;
	private List<Double> doubleValuesOfEachCharOfDecPart;

	public Value(String value)
	{
		stringValue = value;
		isValidStringValue = stringValue.matches("^$|^-$") ? false : true;
		hasValidCharacters = true;

		try
		{
			doubleValue = Double.parseDouble(stringValue);
			isValidDoubleValue = true;
		}
		catch (NumberFormatException e)
		{
			isValidDoubleValue = false;
		}
		catch (NullPointerException e)
		{
			isValidDoubleValue = false;
		}
	}

	public void prepareInputValue()
	{
		stringValue = removeTrailingZeros(stringValue);
		dotIndex = stringValue.indexOf(".");
		isNegative = stringValue.indexOf("-") == -1 ? false : true;

		if (isNegative)
		{
			stringValue = stringValue.substring(1);
			dotIndex--;
		}

		stringIntPart = isDecimalFraction() ? stringValue.substring(0, dotIndex) : stringValue;

		convertEachCharOfIntPartToDoubleValue();
		calculateIntPart();

		if (isDecimalFraction())
		{
			stringDecPart = stringValue.substring(dotIndex + 1);

			convertEachCharOfDecPartToDoubleValue();
			calculateDecPart();
		}
	}

	private void convertEachCharOfIntPartToDoubleValue()
	{
		doubleValuesOfEachCharOfIntPart = convertEachCharToDoubleValue(stringIntPart);
	}

	private void convertEachCharOfDecPartToDoubleValue()
	{
		doubleValuesOfEachCharOfDecPart = convertEachCharToDoubleValue(stringDecPart);
	}

	private List<Double> convertEachCharToDoubleValue(String value)
	{
		List<Double> doubleValuesOfEachChar = new ArrayList<Double>();
		double currentCharacterValue;

		for (int i = 0; i < value.length(); i++)
		{
			currentCharacterValue = convertCharacterToDouble(value.charAt(i));
			doubleValuesOfEachChar.add(currentCharacterValue);

			if (isCurrentCharacterInvalid(currentCharacterValue))
			{
				hasValidCharacters = false;
			}
		}

		return doubleValuesOfEachChar;
	}

	private boolean isCurrentCharacterInvalid(double currentCharacterValue)
	{
		return currentCharacterValue >= Model.getCurrentFirstUnit().getUnitRatio();
	}

	private void calculateDecPart()
	{
		for (int i = 0, j = -1; i < stringDecPart.length(); i++, j--)
		{
			decPart += doubleValuesOfEachCharOfDecPart.get(i) * Math.pow(Model.getCurrentFirstUnit().getUnitRatio(), j);
		}
	}

	private void calculateIntPart()
	{
		for (int i = 0, j = stringIntPart.length() - 1; i < stringIntPart.length(); i++, j--)
		{
			intPart += doubleValuesOfEachCharOfIntPart.get(i) * Math.pow(Model.getCurrentFirstUnit().getUnitRatio(), j);
		}
	}

	private double convertCharacterToDouble(char ch)
	{
		if (Character.isDigit(ch))
		{
			return ch - '0';
		}
		else
		{
			ch = Character.toUpperCase(ch);
			return ch - 55;
		}
	}

	public static String removeNegativeAndTrailingZeros(String value)
	{
		return removeNegativeZero(removeTrailingZeros(value));
	}

	private static String removeTrailingZeros(String value)
	{
		return (value.indexOf(".") == -1) ? value : value.replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	private static String removeNegativeZero(String value)
	{
		return value.matches("-|-0") ? "0" : value;
	}

	public boolean isNegative()
	{
		return isNegative;
	}

	public boolean isDecimalFraction()
	{
		return dotIndex == -1 ? false : true;
	}

	public boolean hasValidCharacters()
	{
		return hasValidCharacters;
	}

	public boolean isValidStringValue()
	{
		return isValidStringValue;
	}

	public boolean isValidDoubleValue()
	{
		return isValidDoubleValue;
	}

	public double getDecPart()
	{
		return decPart;
	}

	public long getIntPart()
	{
		return intPart;
	}

	public double getDoubleValue()
	{
		return doubleValue;
	}
}
