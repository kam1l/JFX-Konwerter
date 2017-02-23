package com.gmail.kamiloleksik.jfxkonwerter.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gmail.kamiloleksik.jfxkonwerter.util.NumberBaseNameComparator;

public class NumberBaseNameComparatorTest
{
	private String firstUnitName;
	private String secondUnitName;
	private int comparisonResult;
	private NumberBaseNameComparator nbnc = new NumberBaseNameComparator();

	@Test
	public void ifSecondNumberBaseIsGreaterComparisonResultShouldBeNegative()
	{
		firstUnitName = "base 6";
		secondUnitName = "base 7";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult < 0);

		firstUnitName = "base 2 (binary)";
		secondUnitName = "base 30";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult < 0);

		firstUnitName = "base 10 (decimal)";
		secondUnitName = "base 16 (hexadecimal)";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult < 0);
	}

	@Test
	public void ifFirstNumberBaseIsGreaterComparisonResultShouldBePositive()
	{
		firstUnitName = "base 7";
		secondUnitName = "base 6";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult > 0);

		firstUnitName = "base 30";
		secondUnitName = "base 2 (binary)";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult > 0);

		firstUnitName = "base 16 (hexadecimal)";
		secondUnitName = "base 10 (decimal)";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult > 0);
	}

	@Test
	public void ifNumberBasesAreEqualComparisonResultShouldEqualZero()
	{
		firstUnitName = "base 7";
		secondUnitName = "base 7";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult == 0);

		firstUnitName = "base 16 (hexadecimal)";
		secondUnitName = "base 16 (hexadecimal)";

		comparisonResult = nbnc.compare(firstUnitName, secondUnitName);

		assertTrue(comparisonResult == 0);
	}
}
