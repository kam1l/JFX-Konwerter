package com.gmail.kamiloleksik.jfxkonwerter.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ExchangeRatesDownloader
{
	private static final String EXCHANGE_RATES_URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

	public static Map<String, BigDecimal> getExchangeRatesFromServer()
			throws MalformedURLException, ParserConfigurationException, SAXException, IOException
	{
		return parseNodeList(getNodeList());
	}

	private static NodeList getNodeList()
			throws ParserConfigurationException, MalformedURLException, SAXException, IOException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new URL(EXCHANGE_RATES_URL).openStream());
		doc.getDocumentElement().normalize();

		return doc.getElementsByTagName("Cube");
	}

	private static Map<String, BigDecimal> parseNodeList(NodeList nodeList)
	{
		Map<String, BigDecimal> exchangeRates = new HashMap<>();

		for (int tmp = 2; tmp < nodeList.getLength(); tmp++)
		{
			Node nNode = nodeList.item(tmp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element eElement = (Element) nNode;
				String currentCurrency = eElement.getAttribute("currency");
				BigDecimal currentRate = BigDecimal.ONE.divide(new BigDecimal(eElement.getAttribute("rate")), 300,
						RoundingMode.HALF_EVEN);
				exchangeRates.put(currentCurrency, currentRate);
			}
		}

		return exchangeRates;
	}
}
