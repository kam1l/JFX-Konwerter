package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatter
{
	public static String getInScientificNotation(BigDecimal value)
	{
		NumberFormat nF = NumberFormat.getNumberInstance(Locale.ROOT);
		DecimalFormat formatter = (DecimalFormat) nF;

		formatter.applyPattern("0.0E0");
		formatter.setRoundingMode(RoundingMode.HALF_UP);
		formatter.setMinimumFractionDigits((value.scale() > 0) ? value.precision() : value.scale());

		return formatter.format(value);
	}
}
