package org.sodeac.common.jdbc.classicmodelcars;

import java.util.Date;

import org.sodeac.common.typedtree.BranchNodeMetaModel;
import org.sodeac.common.typedtree.LeafNodeType;
import org.sodeac.common.typedtree.ModelRegistry;

public class PaymentNodeType extends BranchNodeMetaModel
{
	static{ModelRegistry.getBranchNodeMetaModel(PaymentNodeType.class);}	
	
	public static volatile LeafNodeType<PaymentNodeType,Integer> PAYMENTID;
	public static volatile LeafNodeType<PaymentNodeType,Double> PAYMENTAMOUNT;
	public static volatile LeafNodeType<PaymentNodeType,String> PAYMENTCHECKNUMBER;
	public static volatile LeafNodeType<PaymentNodeType,Date> PAYMENTDATE;
}
