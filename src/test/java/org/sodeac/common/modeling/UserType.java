package org.sodeac.common.modeling;

public class UserType extends ComplexType<UserType>
{
	public static final SingularField<UserType,StringType> name = new SingularField<UserType,StringType>(UserType.class,StringType.class);
	public static final SingularField<UserType,AddressType> address = new SingularField<UserType,AddressType>(UserType.class,AddressType.class);
}
