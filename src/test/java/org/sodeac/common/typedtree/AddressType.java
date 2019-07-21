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

import org.sodeac.common.typedtree.BranchNodeType;
import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;

public class AddressType extends BranchNodeMetaModel
{
	public static final LeafNodeType<AddressType,String> street = new LeafNodeType<AddressType,String>(AddressType.class,String.class);
	public static final LeafNodeType<AddressType,String> number = new LeafNodeType<AddressType,String>(AddressType.class,String.class);
	public static final LeafNodeType<AddressType,String> city = new LeafNodeType<AddressType,String>(AddressType.class,String.class);
	public static final LeafNodeType<AddressType,Integer> zip = new LeafNodeType<AddressType,Integer>(AddressType.class,Integer.class);
	public static final BranchNodeType<AddressType,CountryType> country = new BranchNodeType<AddressType,CountryType>(AddressType.class,CountryType.class);
	public static final BranchNodeType<AddressType,UserType> parentuser = new BranchNodeType<AddressType,UserType>(AddressType.class,UserType.class);
}
