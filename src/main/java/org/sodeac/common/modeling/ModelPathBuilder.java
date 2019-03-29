package org.sodeac.common.modeling;

public class ModelPathBuilder<R extends ComplexType<R>,S extends ComplexType<S>,T extends IType<?>>
{
	private ComplexType<R> root = null;
	private ComplexType<S> self = null;
	
	public static <R extends ComplexType<R>, T extends IType<?>>  RootModelPathBuilder<R,T> newBuilder(ComplexType<R> root, Class<T> clazz)
	{
		return new RootModelPathBuilder<>(root);
	}
	
	private ModelPathBuilder(ComplexType<R> root,ComplexType<S> self)
	{
		super();
		this.root = root;
		this.self = self;
	}
	
	public <N extends ComplexType<N>> ModelPathBuilder<R,N,T> child(IField<S, N> field) // TODO Filter / Booooool
	{
		// TODO 
		return new ModelPathBuilder<R,N,T>(this.root,field.getType());
	}
	
	public ModelPath<R, T> last(IField<S, T> field) // TODO path instated T
	{
		// TODO
		return new ModelPath<R,T>();
	}
	
	protected ComplexType<S> getSelf()
	{
		return this.self;
	}
	
	protected ComplexType<R> getRoot()
	{
		return this.root;
	}
	
	public static class RootModelPathBuilder<R extends ComplexType<R>, T extends IType<?>> extends ModelPathBuilder<R, R, T>
	{
		private RootModelPathBuilder(ComplexType<R> root)
		{
			super(root,root);
		}
	}
}
