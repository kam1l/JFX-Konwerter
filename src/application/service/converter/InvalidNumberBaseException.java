package application.service.converter;

public class InvalidNumberBaseException extends Exception
{
	private static final long serialVersionUID = 14294202620904024L;
	private final int firstNumberBase;

	public InvalidNumberBaseException(int firstNumberBase)
	{
		this.firstNumberBase = firstNumberBase;
	}

	public int getInvalidFirstNumberBase()
	{
		return firstNumberBase;
	}
}
