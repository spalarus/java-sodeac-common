package org.sodeac.common.modeling;

public class MandatoryField<A extends ComplexType<?>, T extends IType<?>> implements IField<A, T>
{
	private Class<T> typeClass = null;
	private Class<A> anchorClass = null;
	
	public MandatoryField(Class<A> anchorClass, Class<T> typeClass)
	{
		this.anchorClass = anchorClass;
		this.typeClass = typeClass;
	}

	public Class<T> getTypeClass()
	{
		return typeClass;
	}

	public Class<A> getAnchorClass()
	{
		return anchorClass;
	}
	

}
