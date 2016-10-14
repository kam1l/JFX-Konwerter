package application.model.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import application.model.converter.exception.InvalidNumberBaseException;
import application.model.converter.exception.InvalidNumberFormatException;

public class NumberBaseConverter implements Converter
{
	private String userInput;
	private BigInteger firstNumberBase;
	private BigInteger secondNumberBase;

	private String result;
	private String stringDecPart;
	private String stringIntPart;

	private boolean isNegative;
	private boolean hasValidCharacters;
	private boolean isInvalidStringValue;

	private int dotIndex;
	private BigDecimal decimalFractionPart = BigDecimal.ZERO;
	private BigInteger integerPart = BigInteger.ZERO;

	private List<Double> doubleValuesOfEachCharOfIntPart;
	private List<Double> doubleValuesOfEachCharOfDecPart;

	{
		hasValidCharacters = true;
	}

	public NumberBaseConverter(String firstNumberBase, String secondNumberBase) throws InvalidNumberBaseException
	{
		try
		{
			this.firstNumberBase = new BigInteger(firstNumberBase);
			this.secondNumberBase = new BigInteger(secondNumberBase);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidNumberBaseException();
		}
	}

	@Override
	public String doValueConversion(String userInput, int numberOfDecimalPlaces)
			throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		this.userInput = userInput;
		hasValidCharacters = preprocessUserInput();

		if (hasValidCharacters)
		{
			if (isDecimalFraction())
			{
				result = convertIntPart() + "." + convertDecPart(numberOfDecimalPlaces);
			}
			else
			{
				result = convertIntPart();
			}

			String formattedResult = formatResult();

			return formattedResult;
		}
		else
		{
			throw new InvalidNumberBaseException(firstNumberBase.intValue());
		}
	}

	@Override
	public Boolean preprocessUserInput() throws InvalidNumberFormatException
	{
		isInvalidStringValue = userInput.isEmpty() || userInput.length() == 1 && userInput.charAt(0) == '-' ? true
				: false;

		if (isInvalidStringValue)
		{
			throw new InvalidNumberFormatException();
		}

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

		return hasValidCharacters;
	}

	private String convertIntPart()
	{
		BigInteger reminder;
		String result = new String("");

		while (integerPart.compareTo(BigInteger.ZERO) == 1)
		{
			reminder = integerPart.remainder(secondNumberBase);
			result = convertLongToCharacter(reminder.longValue()) + result;
			integerPart = integerPart.divide(secondNumberBase);
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
			decimalFractionPart = decimalFractionPart.multiply(new BigDecimal(secondNumberBase));
			intP = decimalFractionPart.longValue();
			decimalFractionPart = decimalFractionPart.subtract(new BigDecimal(intP));

			result += convertLongToCharacter(intP);

			numberOfDigits++;
		}
		while ((decimalFractionPart.compareTo(BigDecimal.ZERO) != 0) && (numberOfDigits < numberOfDecimalPlaces));

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
		return currentCharacterValue >= firstNumberBase.intValue();
	}

	private void calculateDecPart()
	{
		BigDecimal val;
		BigDecimal firstBase = new BigDecimal(firstNumberBase);

		for (int i = 0, j = -1; i < stringDecPart.length(); i++, j--)
		{
			val = new BigDecimal(doubleValuesOfEachCharOfDecPart.get(i));
			decimalFractionPart = decimalFractionPart.add(firstBase.pow(j, new MathContext(100)).multiply(val));
		}
	}

	private void calculateIntPart()
	{
		BigInteger val;

		for (int i = 0, j = stringIntPart.length() - 1; i < stringIntPart.length(); i++, j--)
		{
			val = new BigDecimal(doubleValuesOfEachCharOfIntPart.get(i)).toBigInteger();
			integerPart = integerPart.add(firstNumberBase.pow(j).multiply(val));
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

	@Override
	public String formatResult()
	{
		return removeNegativeZero(removeTrailingZeros(isNegative ? "-" + result : result));
	}

	private String removeTrailingZeros(String value)
	{
		return (value.indexOf(".") == -1) ? value : value.replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	private String removeNegativeZero(String value)
	{
		return value.matches("-|-0") ? "0" : value;
	}

}
