package application.util;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{
	private int start, end;
	private String typedText;
	private boolean lettersAreAllowed;

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
			return newText.matches("(-)?([0-9A-Za-z]+(\\.[0-9A-Za-z]*)?)?");
		}
		else
		{
			return typedText.matches("") || newText.matches("(-)?([0-9]+(\\.[0-9]*)?)?");
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