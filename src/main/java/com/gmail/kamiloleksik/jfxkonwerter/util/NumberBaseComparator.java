package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.util.Comparator;

import com.gmail.kamiloleksik.jfxkonwerter.model.entity.Unit;

public class NumberBaseComparator implements Comparator<Unit>
{
	@Override
	public int compare(Unit o1, Unit o2)
	{
		return extractInt(o1) - extractInt(o2);
	}

	private int extractInt(Unit unit)
	{
		String s = unit.getUnitName();
		String num = s.replaceAll("\\D", "");
		return num.isEmpty() ? 0 : Integer.parseInt(num);
	}
}
