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
package org.sodeac.common.osgi.impl;

import java.util.HashMap;

import org.sodeac.common.ILifecycle;
import org.sodeac.common.ILifecycle.ILifecycleEvent;
import org.sodeac.common.ILifecycle.ILifecycleState;
import org.sodeac.common.ILifecycle.ILifecycleState.IStartUpState;
import org.sodeac.common.ILifecycle.ILifecycleState.IStartUpState.IBootstrapEvent;
import org.sodeac.common.osgi.api.ILifecycleAdmin;

public class LifecycleAdminImpl //implements ILifecycleAdmin
{

	/*@Override
	public ILifecycleAdmin setLifecycleState(Class<? extends ILifecycleState> stateClass)
	{
		return null;
	}

	@Override
	public ILifecycleAdmin fireLifecycleEvent(Class<? extends ILifecycleEvent> eventClass)
	{
		System.out.println(" " + eventClass.getDeclaringClass()); // automatic stateCChange
		System.out.println(" " + eventClass.getInterfaces()[0]);
		System.out.println("class_ " + (eventClass == IBootstrapEvent.class));
		return null;
	}*/

	public static void main(String[] args)
	{
		/*LifecycleAdminImpl a = new LifecycleAdminImpl();
		a.fireLifecycleEvent(IBootstrapEvent.class);*/
		System.out.println(" " + IBootstrapEvent.class.getDeclaringClass());
		//System.out.println(" " + new StartUp().new BSE().getDeclaredLifecycleState());
	}

	public static class StartUp implements IStartUpState, ILifecycleState
	{
		public class BSE implements IBootstrapEvent
		{
	
			@Override
			public ILifecycleState getContemporaryState()
			{
				// TODO Auto-generated method stub
				return null;
			}
			
		}

		@Override
		public ILifecycle getLifecycle()
		{
			// TODO Auto-generated method stub
			return null;
		}
	}

	/*
	 * public default Class<? extends ILifecycleState> getDeclaredLifecycleState()
		{
			// State Class ist die class/Interface, die als letztes ILifecycleState.class implementiert (getInterfaces) 
			// Oder: nur eine classe darf direkt ILifecycleState.class => diese wird dann auch gepostet
			// Oder Alle? => Dann muss die VerebungsSituations die Reihenfolge angeben
			Class<?> declaringClass = getClass().getDeclaringClass();
			boolean implementsILifecycleState = false;
			Class<?> superClass = declaringClass;
			while(superClass != null)
			{
				System.out.println("sc " + superClass);
				for(Class<?> interfaceClass : superClass.getInterfaces())
				{
					System.out.println("IC2 " + interfaceClass);
					if(interfaceClass == ILifecycleState.class)
					{
						System.out.println("A");
						implementsILifecycleState = true;
						break;
					}
					System.out.println("SuperClass: " + interfaceClass.getSuperclass());
					if(interfaceClass.getSuperclass() == ILifecycleState.class)
					{
						System.out.println("B");
						implementsILifecycleState = true;
						break;
					}
					
					for(Class<?> interfaceClass2 : interfaceClass.getInterfaces())
					{
						if(interfaceClass2 == ILifecycleState.class)
						{
							System.out.println("C");
							implementsILifecycleState = true;
							break;
						}
					}
				}
				superClass = superClass.getSuperclass();
			}
			System.out.println("ils " + implementsILifecycleState);
			return null;
			//throw new RuntimeException("The event interface has to be declared in class / interface implements ILifecycleState ");
		}
	 */
}
