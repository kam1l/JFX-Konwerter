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
	private BigInteger firstNumberBase;
	private BigInteger secondNumberBase;

	private boolean isNegative;
	private boolean isInvalidStringValue;

	private int dotIndex;
	private BigDecimal decimalFractionPart = BigDecimal.ZERO;
	private BigInteger integerPart = BigInteger.ZERO;

	private List<Double> doubleValuesOfEachCharOfIntPart;
	private List<Double> doubleValuesOfEachCharOfDecPart;

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
	public String doValueConversion(InputValue inputValue, int numberOfDecimalPlaces)
	{
		String result;

		if (isDecimalFraction())
		{
			result = convertIntPart(inputValue.stringIntPart) + "."
					+ convertDecPart(inputValue.stringDecPart, numberOfDecimalPlaces);
		}
		else
		{
			result = convertIntPart(inputValue.stringIntPart);
		}

		return removeNegativeZero(removeTrailingZeros(isNegative ? "-" + result : result));
	}

	@Override
	public InputValue preprocessUserInput(String userInput)
			throws InvalidNumberFormatException, InvalidNumberBaseException
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

		String intPart = isDecimalFraction() ? userInput.substring(0, dotIndex) : userInput;
		String decPart = null;

		convertEachCharOfIntPartToDoubleValue(intPart);
		calculateIntPart(intPart);

		if (isDecimalFraction())
		{
			decPart = userInput.substring(dotIndex + 1);

			convertEachCharOfDecPartToDoubleValue(decPart);
			calculateDecPart(decPart);
		}

		return new InputValue(intPart, decPart);
	}

	private String convertIntPart(String stringIntPart)
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

	private String convertDecPart(String stringDecPart, int numberOfDecimalPlaces)
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

	private void convertEachCharOfIntPartToDoubleValue(String intPart) throws InvalidNumberBaseException
	{
		doubleValuesOfEachCharOfIntPart = convertEachCharToDoubleValue(intPart);
	}

	private void convertEachCharOfDecPartToDoubleValue(String decPart) throws InvalidNumberBaseException
	{
		doubleValuesOfEachCharOfDecPart = convertEachCharToDoubleValue(decPart);
	}

	private List<Double> convertEachCharToDoubleValue(String value) throws InvalidNumberBaseException
	{
		List<Double> doubleValuesOfEachChar = new ArrayList<Double>();
		double currentCharacterValue;

		for (int i = 0; i < value.length(); i++)
		{
			currentCharacterValue = convertCharacterToDouble(value.charAt(i));
			doubleValuesOfEachChar.add(currentCharacterValue);

			if (currentCharacterIsInvalid(currentCharacterValue))
			{
				throw new InvalidNumberBaseException(firstNumberBase.intValue());
			}
		}

		return doubleValuesOfEachChar;
	}

	private boolean currentCharacterIsInvalid(double currentCharacterValue)
	{
		return currentCharacterValue >= firstNumberBase.intValue();
	}

	private void calculateDecPart(String stringDecPart)
	{
		BigDecimal val;
		BigDecimal firstBase = new BigDecimal(firstNumberBase);

		for (int i = 0, j = -1; i < stringDecPart.length(); i++, j--)
		{
			val = new BigDecimal(doubleValuesOfEachCharOfDecPart.get(i));
			decimalFractionPart = decimalFractionPart.add(firstBase.pow(j, new MathContext(100)).multiply(val));
		}
	}

	private void calculateIntPart(String stringIntPart)
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

	private String removeTrailingZeros(String value)
	{
		return (value.indexOf(".") == -1) ? value : value.replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	private String removeNegativeZero(String value)
	{
		return value.matches("-|-0") ? "0" : value;
	}

}
