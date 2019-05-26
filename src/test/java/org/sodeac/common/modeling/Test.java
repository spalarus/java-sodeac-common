package org.sodeac.common.modeling;

public class Test
{

	public static void main(String[] args)
	{
		UserType userModel = new UserType();
		
		// entity.getModel().buildPath() <= PathBuilder
		// Fields sind vom Type Optional,1z1,0zn,1zn
		
		ModelPath<UserType, StringType> p = ModelPathBuilder.newBuilder(userModel,StringType.class)
																	.with(UserType.address)
																	.with(AddressType.country)
																	.buildFor(CountryType.name);
		System.out.println("" + p);
		
		ModelPath<UserType, StringType> mp = new ModelPath<UserType, StringType>(null);
		
		Entity<CountryType> country =  Entity.newInstance(CountryType.class);
		Entity<UserType> user = Entity.newInstance(UserType.class);
		SingularEntityField<StringType> st = user.getSingleValue(mp);
		SingularEntityField<StringType> countryName = country.get(CountryType.name);
		System.out.println("ööö1 " + countryName);
		System.out.println("ööö2 " + country.get(CountryType.name));
		//userModel.name.getType()
	}

}
