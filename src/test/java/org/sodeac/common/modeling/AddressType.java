package org.sodeac.common.modeling;


public class AddressType extends ComplexType
{
	public static final SingularBasicField<AddressType,String> street = new SingularBasicField<AddressType,String>(AddressType.class,String.class);
	public static final SingularComplexField<AddressType,CountryType> country = new SingularComplexField<AddressType,CountryType>(AddressType.class,CountryType.class);
	public static final SingularComplexField<AddressType,UserType> parentuser = new SingularComplexField<AddressType,UserType>(AddressType.class,UserType.class);
}
