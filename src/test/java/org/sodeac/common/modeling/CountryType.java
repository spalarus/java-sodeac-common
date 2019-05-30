package org.sodeac.common.modeling;


public class CountryType extends ComplexType
{
	public static final SingularBasicField<CountryType,String> name = new SingularBasicField<CountryType,String>(CountryType.class,String.class);
}
