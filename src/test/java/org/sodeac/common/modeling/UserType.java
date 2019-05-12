package org.sodeac.common.modeling;

public class UserType extends ComplexType<UserType>
{
	public static final MandatoryField<UserType,StringType> name = new MandatoryField<UserType,StringType>(UserType.class,StringType.class);
	public static final MandatoryField<UserType,AddressType> address = new MandatoryField<UserType,AddressType>(UserType.class,AddressType.class);
}
