package org.sodeac.common.modeling;

public interface IField<A extends ComplexType<?>, T extends IType<?>>
{
	public Class<T> getTypeClass();
	public Class<A> getAnchorClass();
	
	public default T getType()
	{
		try
		{
			return getTypeClass().newInstance(); // TODO Registry, context of this field
		} 
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public default A getAnchor()
	{
		try
		{
			return getAnchorClass().newInstance(); // TODO Registry, context of this field
		} 
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}
