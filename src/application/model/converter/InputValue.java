package application.model.converter;

import java.math.BigDecimal;

public class InputValue
{
	public final BigDecimal bigDecimalValue;
	public final String stringIntPart;
	public final String stringDecPart;

	public InputValue(BigDecimal bdInputValue)
	{
		this.bigDecimalValue = bdInputValue;
		stringIntPart = null;
		stringDecPart = null;
	}

	public InputValue(String stringIntPart, String stringDecPart)
	{
		this.stringIntPart = stringIntPart;
		this.stringDecPart = stringDecPart;
		this.bigDecimalValue = null;
	}
}
