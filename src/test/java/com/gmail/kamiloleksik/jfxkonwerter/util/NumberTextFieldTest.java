package com.gmail.kamiloleksik.jfxkonwerter.util;

import static org.junit.Assert.*;

import org.junit.Test;

import javafx.embed.swing.JFXPanel;

public class NumberTextFieldTest
{
	private NumberTextField ntf;
	private boolean validationResult;

	{
		new JFXPanel();
		ntf = new NumberTextField();
	}

	@Test
	public void ifUnitTypeIsDiffrentThanNumberBasesLettersShouldNotBeAccepted()
	{
		ntf.setLettersAreAllowed(false);
		ntf.setStart(1);
		ntf.setEnd(1);
		ntf.setText("0");
		ntf.setTypedText("a");

		validationResult = ntf.validate();

		assertFalse(validationResult);
	}

	@Test
	public void onlyAlphaNumeric_MinusAndPointCharactersShouldBeAccepted()
	{
		ntf.setLettersAreAllowed(true);
		ntf.setStart(1);
		ntf.setEnd(1);
		ntf.setText("0");
		ntf.setTypedText("@");

		validationResult = ntf.validate();

		assertFalse(validationResult);

		ntf.setTypedText("<");

		validationResult = ntf.validate();

		assertFalse(validationResult);

		ntf.setTypedText(":");

		validationResult = ntf.validate();

		assertFalse(validationResult);
	}

	@Test
	public void minusShouldBeAllowedOnlyAtTheVeryBeginningOfTheNumber()
	{
		ntf.setStart(1);
		ntf.setEnd(1);
		ntf.setText("0");
		ntf.setTypedText("-");

		validationResult = ntf.validate();

		assertFalse(validationResult);
	}

	@Test
	public void decimalPointShouldBeAllowedOnlyAfterDigit()
	{
		ntf.setStart(0);
		ntf.setEnd(0);
		ntf.setText("");
		ntf.setTypedText(".");

		validationResult = ntf.validate();

		assertFalse(validationResult);
	}

	@Test
	public void onlyOneMinusShouldBeAllowed()
	{
		ntf.setStart(1);
		ntf.setEnd(1);
		ntf.setText("-");
		ntf.setTypedText("-");

		validationResult = ntf.validate();

		assertFalse(validationResult);
	}

	@Test
	public void onlyOneDecimalPointShouldBeAllowed()
	{
		ntf.setStart(3);
		ntf.setEnd(3);
		ntf.setText("0.2");
		ntf.setTypedText(".");

		validationResult = ntf.validate();

		assertFalse(validationResult);
	}
}
