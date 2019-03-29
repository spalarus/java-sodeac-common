package org.sodeac.common.modeling;

public class ComplexType<T> implements IType<T>
{
	public ComplexType<T> setAnchor(ComplexType<?> anchor)
	{
		return this;
	}
	
}
