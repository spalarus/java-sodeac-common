package org.sodeac.common.modeling;


public class AddressType extends ComplexType<AddressType>
{
	public static final MandatoryField<AddressType,StringType> street = new MandatoryField<AddressType,StringType>(AddressType.class,StringType.class);
	public static final MandatoryField<AddressType,CountryType> country = new MandatoryField<AddressType,CountryType>(AddressType.class,CountryType.class);
	public static final MandatoryField<AddressType,UserType> parentuser = new MandatoryField<AddressType,UserType>(AddressType.class,UserType.class);
}
