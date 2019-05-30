package org.sodeac.common.modeling;

public class TestModel extends Model<TestModel>
{
	public static final SingularComplexField<TestModel,UserType> user = new SingularComplexField<TestModel,UserType>(TestModel.class,UserType.class);
}
