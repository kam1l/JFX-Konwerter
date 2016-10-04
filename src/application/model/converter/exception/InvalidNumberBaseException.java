package application.model.converter.exception;

public class InvalidNumberBaseException extends Exception
{
	private static final long serialVersionUID = 14294202620904024L;
	private int firstNumberBase;

	public InvalidNumberBaseException(int firstNumberBase)
	{
		this.firstNumberBase = firstNumberBase;
	}

	public InvalidNumberBaseException()
	{
	}

	public int getInvalidNumberBase()
	{
		return firstNumberBase;
	}
}
