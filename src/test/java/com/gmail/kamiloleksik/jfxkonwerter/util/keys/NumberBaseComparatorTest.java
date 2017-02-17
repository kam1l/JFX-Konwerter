package com.gmail.kamiloleksik.jfxkonwerter.util.keys;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gmail.kamiloleksik.jfxkonwerter.model.entity.Unit;

public class NumberBaseComparatorTest
{
	private Unit u1;
	private Unit u2;
	private int comparisonResult;
	private NumberBaseComparator nbc = new NumberBaseComparator();
	
	@Test
	public void ifSecondNumberBaseIsGreaterComparisonResultShouldBeNegative()
	{
		u1 = new Unit(0, "base 6", null, null, null, null);
		u2 = new Unit(0, "base 7", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult < 0);
		
		u1 = new Unit(0, "base 2 (binary)", null, null, null, null);
		u2 = new Unit(0, "base 30", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult < 0);
		
		u1 = new Unit(0, "base 10 (decimal)", null, null, null, null);
		u2 = new Unit(0, "base 16 (hexadecimal)", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult < 0);
	}
	
	@Test
	public void ifFirstNumberBaseIsGreaterComparisonResultShouldBePositive()
	{
		u1 = new Unit(0, "base 7", null, null, null, null);
		u2 = new Unit(0, "base 6", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult > 0);
		
		u1 = new Unit(0, "base 30", null, null, null, null);
		u2 = new Unit(0, "base 2 (binary)", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult > 0);
		
		u1 = new Unit(0, "base 16 (hexadecimal)", null, null, null, null);
		u2 = new Unit(0, "base 10 (decimal)", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult > 0);
	}
	
	@Test
	public void ifNumberBasesAreEqualComparisonResultShouldEqualZero()
	{
		u1 = new Unit(0, "base 7", null, null, null, null);
		u2 = new Unit(0, "base 7", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult == 0);
		
		u1 = new Unit(0, "base 16 (hexadecimal)", null, null, null, null);
		u2 = new Unit(0, "base 16 (hexadecimal)", null, null, null, null);
		
		comparisonResult = nbc.compare(u1, u2);
		
		assertTrue(comparisonResult == 0);
	}
}
