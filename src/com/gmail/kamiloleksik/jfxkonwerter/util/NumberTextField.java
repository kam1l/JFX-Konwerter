package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.util.regex.Pattern;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{
	private int start, end;
	private String typedText;
	private boolean lettersAreAllowed;
	private static final Pattern numberWithLetters = Pattern.compile("(-)?([0-9A-Za-z]+(\\.[0-9A-Za-z]*)?)?");
	private static final Pattern numberWithoutDecimalMark = Pattern.compile("(-)?([0-9]+((e|E)(\\+|-)?[0-9]{0,2})?)?");
	private static final Pattern numberWithDecimalMark = Pattern
			.compile("(-)?([0-9]+(\\.([0-9]+((e|E)(\\+|-)?[0-9]{0,2})?)?)?)?");

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

	private boolean validate()
	{
		String currentText = getCharacters().toString();
		String newText = getNewText(currentText);

		if (lettersAreAllowed)
		{
			return numberWithLetters.matcher(newText).matches();
		}
		else
		{
			return typedText.isEmpty() || numberWithoutDecimalMark.matcher(newText).matches()
					|| numberWithDecimalMark.matcher(newText).matches();
		}
	}

	private String getNewText(String currentText)
	{
		return currentText.substring(0, start) + typedText + currentText.substring(end);
	}

	public void setLettersAreAllowed(boolean lettersAreAllowed)
	{
		this.lettersAreAllowed = lettersAreAllowed;
	}
}