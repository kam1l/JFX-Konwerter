package com.gmail.kamiloleksik.model.converter;

public class InputValue<T>
{
	private final T[] value;

	public InputValue(T[] value)
	{
		this.value = value;
	}

	public T[] get()
	{
		return value;
	}
}