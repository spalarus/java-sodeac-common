package org.sodeac.common.typedtree;

public class LeafNode<P extends BranchNodeType,T> extends EntityField<P,T>
{
	private T value = null;
	
	public T getValue()
	{
		return this.value;
	}
	
	public void setValue(T value)
	{
		this.value = value;
	}
}
