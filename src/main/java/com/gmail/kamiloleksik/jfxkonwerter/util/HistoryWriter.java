package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryWriter
{
	public static void write(Entry entry) throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter("history.log", true));

		bw.write(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + ", " + entry.getUnitTypeName() + ", "
				+ entry.getFirstUnitDisplayName() + " -> " + entry.getSecondUnitDisplayName() + ", " + entry.getValue()
				+ " -> " + entry.getResult());
		bw.newLine();
		bw.close();
	}

	public static class Entry
	{
		String unitTypeName, firstUnitDisplayName, secondUnitDisplayName, value, result;

		public Entry(String unitTypeName, String firstUnitDisplayName, String secondUnitDisplayName, String value,
				String result)
		{
			this.unitTypeName = unitTypeName;
			this.firstUnitDisplayName = firstUnitDisplayName;
			this.secondUnitDisplayName = secondUnitDisplayName;
			this.value = value;
			this.result = result;
		}

		public String getUnitTypeName()
		{
			return unitTypeName;
		}

		public String getFirstUnitDisplayName()
		{
			return firstUnitDisplayName;
		}

		public String getSecondUnitDisplayName()
		{
			return secondUnitDisplayName;
		}

		public String getValue()
		{
			return value;
		}

		public String getResult()
		{
			return result;
		}
	}
}
