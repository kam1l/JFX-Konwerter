package application;

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

		if (!Model.getCurrentUnitTypeClassifier().equals("numbers"))
		{
			return typedText.matches("") || newText.matches("(-)?([0-9]+(\\.[0-9]*)?)?");
		}
		else
		{
			return newText.matches("(-)?([0-9A-Fa-f]+(\\.[0-9A-Fa-f]*)?)?");
		}
	}

	private String getNewText(String currentText)
	{
		return currentText.substring(0, start) + typedText + currentText.substring(end);
	}
}