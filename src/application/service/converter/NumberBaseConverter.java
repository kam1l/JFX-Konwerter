package application.service.converter;

import java.util.ArrayList;
import java.util.List;

public class NumberBaseConverter extends Converter
{
	private String userInput;
	private int firstNumberBase;
	private int secondNumberBase;

	private String stringDecPart;
	private String stringIntPart;

	private boolean isNegative;
	private boolean hasValidCharacters;
	private boolean isInvalidStringValue;

	private int dotIndex;
	private double decimalFractionPart;
	private long integerPart;

	private List<Double> doubleValuesOfEachCharOfIntPart;
	private List<Double> doubleValuesOfEachCharOfDecPart;

	{
		hasValidCharacters = true;
	}

	public NumberBaseConverter(int firstNumberBase, int secondNumberBase)
	{
		this.firstNumberBase = firstNumberBase;
		this.secondNumberBase = secondNumberBase;
	}

	@Override
	public String doValueConversion(String userInput, int numberOfDecimalPlaces)
			throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		this.userInput = userInput;
		isInvalidStringValue = userInput.matches("^$|^-$") ? true : false;

		if (isInvalidStringValue)
		{
			throw new InvalidNumberFormatException();
		}

		prepareInputValue();

		if (hasValidCharacters)
		{
			String result;

			if (isDecimalFraction())
			{
				result = convertIntPart() + "." + convertDecPart(numberOfDecimalPlaces);
			}
			else
			{
				result = convertIntPart();
			}

			result = isNegative ? "-" + result : result;

			return removeNegativeAndTrailingZeros(result);
		}
		else
		{
			throw new InvalidNumberBaseException(firstNumberBase);
		}
	}

	private void prepareInputValue()
	{
		userInput = removeTrailingZeros(userInput);
		dotIndex = userInput.indexOf(".");
		isNegative = userInput.indexOf("-") == -1 ? false : true;

		if (isNegative)
		{
			userInput = userInput.substring(1);
			dotIndex = dotIndex == -1 ? dotIndex : dotIndex - 1;
		}

		stringIntPart = isDecimalFraction() ? userInput.substring(0, dotIndex) : userInput;

		convertEachCharOfIntPartToDoubleValue();
		calculateIntPart();

		if (isDecimalFraction())
		{
			stringDecPart = userInput.substring(dotIndex + 1);

			convertEachCharOfDecPartToDoubleValue();
			calculateDecPart();
		}
	}

	private String convertIntPart()
	{
		long reminder;
		String result = new String("");

		while (integerPart > 0)
		{
			reminder = integerPart % secondNumberBase;
			result = convertLongToCharacter(reminder) + result;
			integerPart /= secondNumberBase;
		}

		return result.isEmpty() ? "0" : result;
	}

	private String convertDecPart(int numberOfDecimalPlaces)
	{
		long intP;
		int numberOfDigits = 0;
		String result = "";

		do
		{
			decimalFractionPart *= secondNumberBase;
			intP = (long) decimalFractionPart;
			decimalFractionPart -= intP;

			result += convertLongToCharacter(intP);

			numberOfDigits++;
		}
		while ((decimalFractionPart != 0) && (numberOfDigits < numberOfDecimalPlaces));

		return result;
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

			if (currentCharacterIsInvalid(currentCharacterValue))
			{
				hasValidCharacters = false;
			}
		}

		return doubleValuesOfEachChar;
	}

	private boolean currentCharacterIsInvalid(double currentCharacterValue)
	{
		return currentCharacterValue >= firstNumberBase;
	}

	private void calculateDecPart()
	{
		for (int i = 0, j = -1; i < stringDecPart.length(); i++, j--)
		{
			decimalFractionPart += doubleValuesOfEachCharOfDecPart.get(i) * Math.pow(firstNumberBase, j);
		}
	}

	private void calculateIntPart()
	{
		for (int i = 0, j = stringIntPart.length() - 1; i < stringIntPart.length(); i++, j--)
		{
			integerPart += doubleValuesOfEachCharOfIntPart.get(i) * Math.pow(firstNumberBase, j);
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

	private char convertLongToCharacter(long value)
	{
		if (value < 10)
		{
			return (char) (value + 48);
		}
		else
		{
			return (char) (value + 55);
		}
	}

	private boolean isDecimalFraction()
	{
		return dotIndex == -1 ? false : true;
	}
}
