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
package org.sodeac.common.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;
import org.sodeac.common.typedtree.annotation.Domain;
import org.sodeac.common.typedtree.annotation.IgnoreIfFalse;

@Domain(name="org.sodeac.core.model")
public class StacktraceElementNodeType extends BranchNodeMetaModel 
{
	static{ModelRegistry.getBranchNodeMetaModel(StacktraceElementNodeType.class);}
	
	@XmlAttribute(name="class")
	public static volatile LeafNodeType<StacktraceElementNodeType,String> className;
	
	@XmlElement(name="File")
	public static volatile LeafNodeType<StacktraceElementNodeType,String> fileName;
	
	@XmlAttribute(name="linenumber")
	public static volatile LeafNodeType<StacktraceElementNodeType,Integer> lineNumber;
	
	@XmlElement(name="Methode")
	public static volatile LeafNodeType<StacktraceElementNodeType,String> methodName;
	
	@XmlAttribute(name="native")
	@IgnoreIfFalse
	public static volatile LeafNodeType<StacktraceElementNodeType,Boolean> nativeMethod;
}