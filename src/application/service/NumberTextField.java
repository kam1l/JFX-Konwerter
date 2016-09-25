package application.service;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{
	private int start, end;
	private String typedText;

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

		if (currentUnitsAreNumberBases())
		{
			return newText.matches("(-)?([0-9A-Fa-f]+(\\.[0-9A-Fa-f]*)?)?");
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

	private boolean currentUnitsAreNumberBases()
	{
		return Model.getCurrentUnitTypeClassifier().equals("numbers");
	}
}