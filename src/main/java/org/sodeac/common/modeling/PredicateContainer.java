package org.sodeac.common.modeling;

public class PredicateContainer<T>
{
	public boolean isSingle()
	{
		return false;
	}
	public boolean isFirst()
	{
		return false;
	}
	public boolean isLast()
	{
		return false;
	}
	public boolean hasPrevious()
	{
		return false;
	}
	public boolean hasNext()
	{
		return false;
	}
	public T getChild()
	{
		return null;
	}
}
