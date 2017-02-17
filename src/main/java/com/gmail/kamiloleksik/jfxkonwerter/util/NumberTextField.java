package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.util.regex.Pattern;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{
	public static final Pattern NUMBER_WITH_LETTERS = Pattern.compile("(-)?([0-9A-Za-z]+(\\.[0-9A-Za-z]*)?)?");
	public static final Pattern NUMBER_WITHOUT_DECIMAL_MARK = Pattern
			.compile("(-)?([0-9]+((e|E)(\\+|-)?[0-9]{0,2})?)?");
	public static final Pattern NUMBER_WITH_DECIMAL_MARK = Pattern
			.compile("(-)?([0-9]+(\\.([0-9]+((e|E)(\\+|-)?[0-9]{0,2})?)?)?)?");

	private int start, end;
	private String typedText;
	private boolean lettersAreAllowed;

	public NumberTextField()
	{
		super();
	}
	
	@Override
	public void replaceText(int start, int end, String typedText)
	{
		this.start = start;
		this.end = end;
		this.typedText = typedText;

		if (validate())
		{
			super.replaceText(start, end, typedText);
		}
	}

	public boolean validate()
	{
		String currentText = getCharacters().toString();
		String newText = getNewText(currentText);

		if (lettersAreAllowed)
		{
			return NUMBER_WITH_LETTERS.matcher(newText).matches();
		}
		else
		{
			return typedText.isEmpty() || NUMBER_WITHOUT_DECIMAL_MARK.matcher(newText).matches()
					|| NUMBER_WITH_DECIMAL_MARK.matcher(newText).matches();
		}
	}

	public void setLettersAreAllowed(boolean lettersAreAllowed)
	{
		this.lettersAreAllowed = lettersAreAllowed;
	}
	
	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getEnd()
	{
		return end;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}
	
	public String getTypedText()
	{
		return typedText;
	}

	public void setTypedText(String typedText)
	{
		this.typedText = typedText;
	}
	
	private String getNewText(String currentText)
	{
		return currentText.substring(0, start) + typedText + currentText.substring(end);
	}
}