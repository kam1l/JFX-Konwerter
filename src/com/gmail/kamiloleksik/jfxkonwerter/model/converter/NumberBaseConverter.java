package com.gmail.kamiloleksik.jfxkonwerter.model.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberBaseException;
import com.gmail.kamiloleksik.jfxkonwerter.model.converter.exception.InvalidNumberFormatException;

public class NumberBaseConverter implements Converter
{
	private BigInteger firstNumberBase;
	private BigInteger secondNumberBase;
	private boolean isNegative;
	private boolean isDecimalFraction;

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
	public String doValueConversion(InputValue<?> inputValue, int numberOfDecimalPlaces)
	{
		String result;

		if (isDecimalFraction)
		{
			result = convertIntPart((BigInteger) inputValue.get()[0]) + "."
					+ convertDecPart((BigDecimal) inputValue.get()[1], numberOfDecimalPlaces);
		}
		else
		{
			result = convertIntPart((BigInteger) inputValue.get()[0]);
		}

		return removeNegativeZero(removeTrailingZeros(isNegative ? "-" + result : result));
	}

	@Override
	public InputValue<?> preprocessUserInput(String userInput)
			throws InvalidNumberFormatException, InvalidNumberBaseException
	{
		if (userInputIsNotValid(userInput))
		{
			throw new InvalidNumberFormatException();
		}

		userInput = removeTrailingZeros(userInput);
		int dotIndex = userInput.indexOf(".");
		isDecimalFraction = dotIndex == -1 ? false : true;
		isNegative = userInput.indexOf("-") == -1 ? false : true;

		if (isNegative)
		{
			userInput = userInput.substring(1);
			dotIndex = dotIndex == -1 ? dotIndex : dotIndex - 1;
		}

		String intPart = isDecimalFraction ? userInput.substring(0, dotIndex) : userInput;
		List<Double> valuesOfEachCharOfIntPart = convertEachCharOfIntPartToDoubleValue(intPart);
		BigInteger integerPart = calculateIntPart(valuesOfEachCharOfIntPart, intPart);
		BigDecimal decimalFractionPart = null;

		if (isDecimalFraction)
		{
			String decPart = userInput.substring(dotIndex + 1);
			List<Double> valuesOfEachCharOfDecPart = convertEachCharOfDecPartToDoubleValue(decPart);
			decimalFractionPart = calculateDecPart(valuesOfEachCharOfDecPart, decPart);
		}

		return new InputValue<Object>(new Object[] { integerPart, decimalFractionPart });
	}

	private boolean userInputIsNotValid(String userInput)
	{
		return userInput.isEmpty() || userInput.length() == 1 && userInput.charAt(0) == '-' ? true : false;
	}

	private String convertIntPart(BigInteger integerPart)
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

	private String convertDecPart(BigDecimal decimalFractionPart, int numberOfDecimalPlaces)
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

	private List<Double> convertEachCharOfIntPartToDoubleValue(String intPart) throws InvalidNumberBaseException
	{
		return convertEachCharToDoubleValue(intPart);
	}

	private List<Double> convertEachCharOfDecPartToDoubleValue(String decPart) throws InvalidNumberBaseException
	{
		return convertEachCharToDoubleValue(decPart);
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

	private BigDecimal calculateDecPart(List<Double> valuesOfEachCharOfDecPart, String stringDecPart)
	{
		BigDecimal val;
		BigDecimal firstBase = new BigDecimal(firstNumberBase);
		BigDecimal decimalFractionPart = BigDecimal.ZERO;

		for (int i = 0, j = -1; i < stringDecPart.length(); i++, j--)
		{
			val = new BigDecimal(valuesOfEachCharOfDecPart.get(i));
			decimalFractionPart = decimalFractionPart.add(firstBase.pow(j, new MathContext(100)).multiply(val));
		}

		return decimalFractionPart;
	}

	private BigInteger calculateIntPart(List<Double> valuesOfEachCharOfIntPart, String stringIntPart)
	{
		BigInteger val;
		BigInteger integerPart = BigInteger.ZERO;

		for (int i = 0, j = stringIntPart.length() - 1; i < stringIntPart.length(); i++, j--)
		{
			val = new BigDecimal(valuesOfEachCharOfIntPart.get(i)).toBigInteger();
			integerPart = integerPart.add(firstNumberBase.pow(j).multiply(val));
		}

		return integerPart;
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

	private String removeTrailingZeros(String value)
	{
		return (value.indexOf(".") == -1) ? value : value.replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	private String removeNegativeZero(String value)
	{
		return value.matches("-|-0") ? "0" : value;
	}
}
