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
		
		Entity<CountryType> country = new Entity<CountryType>(CountryType.class);
		Entity<UserType> user = new Entity<UserType>(UserType.class);
		StringType st = user.getSingleValue(mp);
		//userModel.name.getType()
	}

}
