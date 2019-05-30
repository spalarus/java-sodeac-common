/*******************************************************************************
 * Copyright (c) 2019 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.modeling;

public class Model<T extends ComplexType> extends ComplexType
{
	public <F extends ComplexType> ComplexRootObject<F> newInstance(SingularComplexField<T,F> field)
	{
		return null;
	}
	
	public <F> BasicRootObject<F> newInstance(SingularBasicField<T,F> field)
	{
		return null;
	}
	
	public <F extends ComplexType> ComplexList<F> newInstance(MultipleComplexField<T,F> field)
	{
		return null;
	}
	
	public <F> ComplexList<F> newInstance(MultipleBasicField<T,F> field)
	{
		return null;
	}
	
	public static class ComplexRootObject<R  extends ComplexType> extends ComplexObject<R>
	{
		public void dispose()
		{
			// TODO
		}
	}
	
	public static class BasicRootObject<R> extends BasicObject<R>
	{
		public void dispose()
		{
			// TODO
		}
	}
	
	public static class ComplexRootList<R  extends ComplexType> extends ComplexList<R>
	{
		public void dispose()
		{
			// TODO
		}
	}
	
	public static class BasicRootList<R> extends BasicList<R>
	{
		public void dispose()
		{
			// TODO
		}
	}
}
