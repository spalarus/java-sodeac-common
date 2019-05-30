package org.sodeac.common.modeling;

import org.sodeac.common.modeling.ModelPathBuilder.RootModelPathBuilder;

public class Test
{

	public static void main(String[] args)
	{
		UserType userModel = new UserType();
		
		// entity.getModel().buildPath() <= PathBuilder
		// Fields sind vom Type Optional,1z1,0zn,1zn
		
		RootModelPathBuilder<UserType,String> builder = ModelPathBuilder.newBuilder(userModel,String.class);
		ModelPathBuilder<UserType,AddressType,String> x1 = builder.with(UserType.address);
		ModelPathBuilder<UserType,CountryType,String> x2 = x1.with(AddressType.country);
		ModelPath<UserType, String> p = x2.buildFor(CountryType.name);
		
		System.out.println("xxx " + p);
		
		ModelPath<UserType, String> p2 = ModelPathBuilder.newBuilder(userModel,String.class)
																	.with(UserType.address)
																	.with(AddressType.country)
																	.buildFor(CountryType.name);
		System.out.println("yyy " + p2);
		
		
		ModelPath<UserType, String> mp = new ModelPath<UserType, String>(null);
		
		Entity<CountryType> country =  Entity.newInstance(CountryType.class);
		Entity<UserType> user = Entity.newInstance(UserType.class);
		BasicObject<String> st = user.getSingleValue(mp);
		BasicObject<String> countryName = country.get(CountryType.name);
		System.out.println("ööö1 " + countryName);
		System.out.println("ööö2 " + country.get(CountryType.name));
		System.out.println("ööö3 " + user.get(UserType.address).getValue());
		
		//userModel.name.getType()
		
		TestModel testModel = new TestModel();
		ComplexObject<UserType> u =  testModel.newInstance(TestModel.user);
		System.out.println("llllllllll " +  u);
	}

}
