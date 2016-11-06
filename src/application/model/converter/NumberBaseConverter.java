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
	private BigInteger integerPart = BigInteger.ZERO;
	private BigDecimal decimalFractionPart = BigDecimal.ZERO;

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
		String value = (String) inputValue.get();

		if (isDecimalFraction)
		{
			int dotIndex = value.indexOf('.');

			result = convertIntPart(value.substring(0, dotIndex)) + "."
					+ convertDecPart(value.substring(dotIndex + 1), numberOfDecimalPlaces);
		}
		else
		{
			result = convertIntPart(value);
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
		String decPart = null;

		List<Double> valuesOfEachCharOfIntPart = convertEachCharOfIntPartToDoubleValue(intPart);
		calculateIntPart(valuesOfEachCharOfIntPart, intPart);

		if (isDecimalFraction)
		{
			decPart = userInput.substring(dotIndex + 1);

			List<Double> valuesOfEachCharOfDecPart = convertEachCharOfDecPartToDoubleValue(decPart);
			calculateDecPart(valuesOfEachCharOfDecPart, decPart);
		}

		return new InputValue<String>(intPart + (decPart == null ? "" : "." + decPart));
	}

	private boolean userInputIsNotValid(String userInput)
	{
		return userInput.isEmpty() || userInput.length() == 1 && userInput.charAt(0) == '-' ? true : false;
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

	private void calculateDecPart(List<Double> valuesOfEachCharOfDecPart, String stringDecPart)
	{
		BigDecimal val;
		BigDecimal firstBase = new BigDecimal(firstNumberBase);

		for (int i = 0, j = -1; i < stringDecPart.length(); i++, j--)
		{
			val = new BigDecimal(valuesOfEachCharOfDecPart.get(i));
			decimalFractionPart = decimalFractionPart.add(firstBase.pow(j, new MathContext(100)).multiply(val));
		}
	}

	private void calculateIntPart(List<Double> valuesOfEachCharOfIntPart, String stringIntPart)
	{
		BigInteger val;

		for (int i = 0, j = stringIntPart.length() - 1; i < stringIntPart.length(); i++, j--)
		{
			val = new BigDecimal(valuesOfEachCharOfIntPart.get(i)).toBigInteger();
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

	private String removeTrailingZeros(String value)
	{
		return (value.indexOf(".") == -1) ? value : value.replaceAll("0+$", "").replaceAll("\\.$", "");
	}

	private String removeNegativeZero(String value)
	{
		return value.matches("-|-0") ? "0" : value;
	}

}
