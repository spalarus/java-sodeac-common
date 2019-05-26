package org.sodeac.common.modeling;


public class AddressType extends ComplexType<AddressType>
{
	public static final SingularField<AddressType,StringType> street = new SingularField<AddressType,StringType>(AddressType.class,StringType.class);
	public static final SingularField<AddressType,CountryType> country = new SingularField<AddressType,CountryType>(AddressType.class,CountryType.class);
	public static final SingularField<AddressType,UserType> parentuser = new SingularField<AddressType,UserType>(AddressType.class,UserType.class);
}
