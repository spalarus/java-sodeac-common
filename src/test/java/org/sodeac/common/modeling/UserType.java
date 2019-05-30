package org.sodeac.common.modeling;

public class UserType extends ComplexType
{
	public static final SingularBasicField<UserType,String> name = new SingularBasicField<UserType,String>(UserType.class,String.class);
	public static final SingularComplexField<UserType,AddressType> address = new SingularComplexField<UserType,AddressType>(UserType.class,AddressType.class);
}
