package org.sodeac.common.typedtree;

public class LangType extends BranchNodeMetaModel
{
	public static final LeafNodeType<LangType,String> name = new LeafNodeType<LangType,String>(LangType.class,String.class);
	public static final LeafNodeType<LangType,String> code = new LeafNodeType<LangType,String>(LangType.class,String.class);
}
