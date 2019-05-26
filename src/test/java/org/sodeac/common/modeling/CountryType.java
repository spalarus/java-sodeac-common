package org.sodeac.common.modeling;


public class CountryType extends ComplexType<CountryType>
{
	public static final SingularField<CountryType,StringType> name = new SingularField<CountryType,StringType>(CountryType.class,StringType.class);
}
