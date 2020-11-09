/*******************************************************************************
 * Copyright (c) 2020 Sebastian Palarus
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Sebastian Palarus - initial API and implementation
 *******************************************************************************/
package org.sodeac.common.misc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class LazyContentEnricher<T,R,E> implements AutoCloseable 
{
	private Map<T,Set<R>> referencesByObjectsToBeEnriched;
	private Map<R,Set<T>> objectsToBeEnrichByReference;
	private Consumer<LazyContentEnricher<T,R,E>> contentEnricher;

	
	public static LazyContentEnricher newInstance()
	{
		LazyContentEnricher lce = new LazyContentEnricher<>();
		lce.referencesByObjectsToBeEnriched = new HashMap<>();
		lce.objectsToBeEnrichByReference = new LinkedHashMap<>();
		
		return lce;
	}
	
	public LazyContentEnricher<T,R,E> defineContentEnricher(Consumer<LazyContentEnricher<T,R,E>> contentEnricher)
	{
		this.contentEnricher = contentEnricher;
		return this;
	}
	
	public Set<R> getReferences()
	{
		return this.objectsToBeEnrichByReference.keySet();
	}

	public Map<T, Set<R>> getReferencesByObjectsToBeEnriched()
	{
		return referencesByObjectsToBeEnriched;
	}

	public Map<R, Set<T>> getObjectsToBeEnrichByReference()
	{
		return objectsToBeEnrichByReference;
	}

	public LazyContentEnricher<T,R,E> invokeContentEnricher()
	{
		try
		{
			if(this.contentEnricher != null)
			{
				this.contentEnricher.accept(this);
			}
		}
		finally 
		{
			this.clear();
		}
		return this;
	}
	
	private void clear()
	{
		try
		{
			for(Set<R> reference : this.referencesByObjectsToBeEnriched.values())
			{
				try
				{
					reference.clear();
				}
				catch (Exception | Error e) {}
			}
		}
		catch (Exception | Error e) {}
		
		try
		{
			this.referencesByObjectsToBeEnriched.clear();
		}
		catch (Exception | Error e) {}
		
		try
		{
			for(Set<T> objects : this.objectsToBeEnrichByReference.values())
			{
				try
				{
					objects.clear();
				}
				catch (Exception | Error e) {}
			}
		}
		catch (Exception | Error e) {}
		
		try
		{
			this.objectsToBeEnrichByReference.clear();
		}
		catch (Exception | Error e) {}
		
		try
		{
			if(this.contentEnricher instanceof IClearable)
			{
				((IClearable) this.contentEnricher).clear();
			}
		}
		catch (Exception | Error e) {}
	}
	
	public T register(T objectToBeEnriched, R reference)
	{
		Objects.requireNonNull(objectToBeEnriched, "object to be enriched not defined");
		Objects.requireNonNull(reference, "reference not defined");
		Set<R> registeredReferences = this.referencesByObjectsToBeEnriched.get(objectToBeEnriched);
		if(registeredReferences == null)
		{
			registeredReferences = new LinkedHashSet<>();
			this.referencesByObjectsToBeEnriched.put(objectToBeEnriched,registeredReferences);
		}
		registeredReferences.add(reference);
		Set<T> registeredObjectsToBeEnriched = this.objectsToBeEnrichByReference.get(reference);
		if(registeredObjectsToBeEnriched == null)
		{
			registeredObjectsToBeEnriched = new LinkedHashSet<>();
			this.objectsToBeEnrichByReference.put(reference,registeredObjectsToBeEnriched);
		}
		registeredObjectsToBeEnriched.add(objectToBeEnriched);
		
		return objectToBeEnriched;
	}
	

	@Override
	public void close() throws Exception
	{
		this.clear();
		if(this.contentEnricher instanceof AutoCloseable)
		{
			try
			{
				((AutoCloseable)this.contentEnricher).close();
			}
			catch (Exception e) {}
		}
		
	}
	
	public interface IClearable
	{
		public void clear();
	}
	
	public static class CachedContentEnricher<T,R,E> implements AutoCloseable,IClearable,Consumer<LazyContentEnricher<T,R,E>>
	{
		public static enum CardinalityMode {ONE_REFERENCE_TO_ONE_ENRICHMENT,ONE_REFERENCE_TO_MANY_ENRICHMENTS}
		
		private CardinalityMode cardinalityMode = CardinalityMode.ONE_REFERENCE_TO_ONE_ENRICHMENT;
		private LazyContentEnricher<T,R,E> lazyContentEnricher = null;

		@Override
		public void accept(LazyContentEnricher<T, R, E> lazyContentEnricher)
		{
			this.lazyContentEnricher = lazyContentEnricher;
			
		}

		@Override
		public void clear()
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void close() throws Exception
		{
			// TODO Auto-generated method stub
			
		}

		public LazyContentEnricher<T, R, E> getLazyContentEnricher()
		{
			return lazyContentEnricher;
		}
		
	}

}
