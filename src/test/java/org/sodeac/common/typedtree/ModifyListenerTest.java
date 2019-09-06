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
package org.sodeac.common.typedtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.Ref;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.sodeac.common.function.ConplierBean;
import org.sodeac.common.typedtree.IChildNodeListener.ILeafNodeListener;
import org.sodeac.common.typedtree.ModelPath.ModelPathBuilder;
import org.sodeac.common.typedtree.ModelPath.NodeSelector;
import org.sodeac.common.typedtree.ModelPath.ModelPathBuilder.RootModelPathBuilder;
import org.sodeac.common.typedtree.ModelPath.NodeSelector.Axis;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModifyListenerTest
{
	@Test
	public void test0001CreateBuilder()
	{
		assertNotNull("builder should not be null" , ModelPathBuilder.newBuilder(UserType.class));
	}
	
	@Test
	public void test0002CreateRootPath()
	{
		assertNotNull("path should not be null" , ModelPathBuilder.newBuilder(UserType.class).build());
	}
	
	@Test
	public void test0003CreateLeafValuePath()
	{
		assertNotNull("path should not be null" , ModelPathBuilder.newBuilder(UserType.class).buildForValue(UserType.name));
	}
	
	@Test
	public void test0004CreateLeafNodePath()
	{
		assertNotNull("path should not be null" , ModelPathBuilder.newBuilder(UserType.class).buildForNode(UserType.name));
	}
	
	@Test
	public void test0010CloneTest()
	{
		ModelPath<UserType,UserType> path1 = ModelPathBuilder.newBuilder(UserType.class).build();
		ModelPath<UserType,UserType> path1Clone = path1.clone();
		
		assertNotSame("cloned path should not be same", path1, path1Clone);
		assertEquals("cloned path shoutld equals to origin", path1, path1Clone);
		
		ModelPath<UserType,String> path2 = ModelPathBuilder.newBuilder(UserType.class).buildForValue(UserType.name);
		ModelPath<UserType,String> path2Clone = path2.clone();
		
		assertNotSame("cloned path should not be same", path2, path2Clone);
		assertEquals("cloned path shoutld equals to origin", path2, path2Clone);
		
		assertNotEquals("pathes shoult not equals", path1, path2);
		
		ModelPath<UserType,LeafNode<?, String>> path3 = ModelPathBuilder.newBuilder(UserType.class).buildForNode(UserType.name);
		ModelPath<UserType,LeafNode<?, String>> path3Clone = path3.clone();
		
		assertNotSame("cloned path should not be same", path3, path3Clone);
		assertEquals("cloned path shoutld equals to origin", path3, path3Clone);
		
		assertNotEquals("pathes shoult not equals", path1, path3);
		
		ModelPath<UserType,UserType> path1a = ModelPathBuilder.newBuilder(UserType.class).build();
		ModelPath<UserType,String> path2a = ModelPathBuilder.newBuilder(UserType.class).buildForValue(UserType.name);
		ModelPath<UserType,LeafNode<?, String>> path3a = ModelPathBuilder.newBuilder(UserType.class).buildForNode(UserType.name);
		
		assertEquals("path shoutld equals to origin", path1, path1a);
		assertEquals("path shoutld equals to origin", path2, path2a);
		assertEquals("path shoutld equals to origin", path3, path3a);
		
		
		path1.dispose();
		path2.dispose();
		path3.dispose();
		path1a.dispose();
		path2a.dispose();
		path3a.dispose();
		
	}
	
	@Test
	public void test0100ChildLeafNodeListener()
	{
		ConplierBean<String> check = new ConplierBean<String>();
		ILeafNodeListener<UserType, String> testListener = (n,o) -> check.setValue(n.getValue());
		
		RootBranchNode<TestModel,UserType> user = TypedTreeMetaModel.getInstance(TestModel.class).createRootNode(TestModel.user);
		user.addChildNodeListener(UserType.name, testListener);
		
		String value = "Name1";
		
		user.setValue(UserType.name, value);
		assertEquals("name should be correct",value,check.getValue());
		
		value = "Name2";
		
		user.setValue(UserType.name, value);
		assertEquals("name should be correct",value,check.getValue());
		
		user.removeChildNodeListener(testListener);
		
		user.setValue(UserType.name, UUID.randomUUID().toString());
		assertEquals("name should be correct",value,check.getValue());
		
	}
	
	@Test
	public void test0200ModifyListenerRegistrationSimple() throws InstantiationException, IllegalAccessException
	{
		ModifyListenerRegistration<UserType> registration = new ModifyListenerRegistration<>();
		
		IModifyListener<LeafNode<?, String>> listener1 = IModifyListener.onModify( n -> {});
		IModifyListener<LeafNode<?, String>> listener2 = IModifyListener.onModify( n -> {});
		registration.registerListener(ModelPathBuilder.newBuilder(UserType.class).buildForNode(UserType.name), listener1);
		
		List<NodeSelector<UserType, ?>> rootSelectorList = registration.getRootNodeSelectorList();
		
		assertNotNull("root listeners should not be null", rootSelectorList);
		assertEquals("size of root listeners should be correct", 1, rootSelectorList.size());
		
		NodeSelector<UserType, ?> rootSelector = rootSelectorList.get(0);
		
		assertSame("axis of selector should be correct", Axis.SELF, rootSelector.getAxis());
		assertSame("root type of selector should be correct", ModelingProcessor.DEFAULT_INSTANCE.getModel(UserType.class), rootSelector.getRootType());
		
		List<NodeSelector<?, ?>> childSelectorList = rootSelector.getChildSelectorList();
		
		assertNotNull("child selector list should not be null", childSelectorList);
		assertEquals("size of child selector list should be correct", 1, childSelectorList.size());
		
		NodeSelector<?, ?> childSelector = childSelectorList.get(0);
		
		assertSame("axis of selector should be correct", Axis.CHILD, childSelector.getAxis());
		assertSame("root type of selector should be correct", ModelingProcessor.DEFAULT_INSTANCE.getModel(UserType.class), rootSelector.getRootType());
		assertSame("type of selector should be correct", UserType.name, childSelector.getType());
		
		List<IModifyListener<?>> listenerList = childSelector.getModifyListenerList(); 
		assertNotNull("child listener list should not be null", listenerList);
		assertEquals("size of child listener list should be correct", 1, listenerList.size());
		
		assertSame("listener instance should be correct",listener1,listenerList.get(0));
		
		Map<Object,Set<IModifyListener<?>>> registrations = childSelector.getRegistrationObjects();
		assertEquals("registrations size should be correct", 1, registrations.size());
		
		registration.registerListener(ModelPathBuilder.newBuilder(UserType.class).buildForNode(UserType.name), listener2);
		
		rootSelectorList = registration.getRootNodeSelectorList();
		
		assertNotNull("root listeners should not be null", rootSelectorList);
		assertEquals("size of root listeners should be correct", 1, rootSelectorList.size());
		
		rootSelector = rootSelectorList.get(0);
		
		assertSame("axis of selector should be correct", Axis.SELF, rootSelector.getAxis());
		assertSame("root type of selector should be correct", ModelingProcessor.DEFAULT_INSTANCE.getModel(UserType.class), rootSelector.getRootType());
		
		childSelectorList = rootSelector.getChildSelectorList();
		
		assertNotNull("child selector list should not be null", childSelectorList);
		assertEquals("size of child selector list should be correct", 1, childSelectorList.size());
		
		childSelector = childSelectorList.get(0);
		
		assertSame("axis of selector should be correct", Axis.CHILD, childSelector.getAxis());
		assertSame("root type of selector should be correct", ModelingProcessor.DEFAULT_INSTANCE.getModel(UserType.class), rootSelector.getRootType());
		assertSame("type of selector should be correct", UserType.name, childSelector.getType());
		
		listenerList = childSelector.getModifyListenerList(); 
		assertNotNull("child listener list should not be null", listenerList);
		assertEquals("size of child listener list should be correct", 2, listenerList.size());
		
		assertSame("listener instance should be correct",listener1,listenerList.get(0));
		assertSame("listener instance should be correct",listener2,listenerList.get(1));
		
		registrations = childSelector.getRegistrationObjects();
		assertEquals("registrations size should be correct", 1, registrations.size());
		
		registration.dispose();
		
		assertEquals("size of list should be correct", 0, rootSelectorList.size());
		assertEquals("size of list should be correct", 0, childSelectorList.size());
		assertEquals("size of list should be correct", 0, listenerList.size());
		assertEquals("size of list should be correct", 0, registrations.size());
	}
	
	@Test
	public void test1001CreateModifyListener()
	{
		RootBranchNode<TestModel,UserType> user = TypedTreeMetaModel.getInstance(TestModel.class).createRootNode(TestModel.user);
		
		ModelPath<UserType, String> namePath = ModelPathBuilder.newBuilder(UserType.class).buildForValue(UserType.name);
		
		ConplierBean<String> value = new ConplierBean<String>();
		user.registerForModify(namePath, IModifyListener.onModify(value));
		
		assertNull("name should be null", value.get());
		
		user.setValue(UserType.name, "user");
		assertEquals("name should be correct", "user", value.get());
		namePath.dispose();
		user.dispose();
	}
}
