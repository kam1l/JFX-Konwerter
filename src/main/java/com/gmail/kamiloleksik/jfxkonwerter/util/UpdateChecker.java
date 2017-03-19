package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.io.IOException;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UpdateChecker
{
	private static final DefaultArtifactVersion APP_VERSION = new DefaultArtifactVersion("1.1.0");

	public static boolean updateIsAvailable() throws IOException
	{
		Document doc = Jsoup.connect("https://github.com/kam1l/JFX-Konwerter/releases").get();
		Elements elementsByClass = doc.getElementsByClass("release-title");

		for (Element element : elementsByClass)
		{
			String text = element.text();
			DefaultArtifactVersion availableVersion = new DefaultArtifactVersion(text);

			if (availableVersion.compareTo(APP_VERSION) > 0)
			{
				return true;
			}
		}

		return false;
	}
}
