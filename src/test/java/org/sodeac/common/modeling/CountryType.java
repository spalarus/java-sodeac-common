package org.sodeac.common.modeling;


public class CountryType extends ComplexType<CountryType>
{
	public static final MandatoryField<CountryType,StringType> name = new MandatoryField<CountryType,StringType>(CountryType.class,StringType.class);
}
