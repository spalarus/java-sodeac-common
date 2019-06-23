package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.BranchNode.BranchNodeGetterPolicy;
import org.sodeac.common.typedtree.ModelPathBuilder.RootModelPathBuilder;

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
		
		ModelPath<UserType, String> p2 = ModelPathBuilder.newBuilder(UserType.class,String.class)
																	.with(UserType.address)
																	.with(AddressType.country)
																	.buildFor(CountryType.name);
		System.out.println("yyy " + p2);
		
		
		ModelPath<UserType, String> mp = new ModelPath<UserType, String>(null); // TODO protected
		
		/*Entity<CountryType> country =  Entity.newInstance(CountryType.class);
		Entity<UserType> user = Entity.newInstance(UserType.class);
		BasicObject<UserType,String> st = user.getSingleValue(mp);
		BasicObject<CountryType,String> countryName = country.get(CountryType.name);
		System.out.println("ööö1 " + countryName);
		System.out.println("ööö2 " + country.get(CountryType.name));
		System.out.println("ööö3 " + user.get(UserType.address).getValue());*/
		
		//userModel.name.getType()
		
		TestModel testModel = new TestModel();
		BranchNode<TestModel,UserType> u =  testModel.createRootNode(TestModel.user);
		u
			.setValue(UserType.name,"buzzt")
			.create(UserType.address )
				.setValue(AddressType.street,"MCA");
		
		
		testModel.createRootNode(TestModel.user).setValue(UserType.name, "Icke").create(UserType.address).setValue(AddressType.street,"MCA");
		
		System.out.println("1 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address).get(AddressType.street).getValue());
		
		u =  testModel.createRootNode(TestModel.user);
		u
			.compute(x -> x.setValue(UserType.name,"buzzt"))
			.compute
			(
				x -> x.get(UserType.address,BranchNodeGetterPolicy.CreateIfNull).
					setValue(AddressType.street,"MCA")
			);
		
		System.out.println("2 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address).get(AddressType.street).getValue());
		
		u =  testModel.createRootNode(TestModel.user);
		u
			.compute(x -> x.setValue(UserType.name,"buzzt"))
			.compute(x -> x.computeChildNode(UserType.address,BranchNodeGetterPolicy.CreateIfNull,(y,a) -> a.setValue(AddressType.street,"MCA")));
		
		System.out.println("3 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address).get(AddressType.street).getValue());
		
		 BranchNode<AddressType, CountryType>  country = u.get(UserType.address).create(AddressType.country);
		 country.createItem(CountryType.languageList).setValue(LangType.name, "German").setValue(LangType.code, "de");
		 System.out.println("List: " + country.getUnmodifiableNodeList(CountryType.languageList).size());
		 
		u.remove(UserType.address);
		
		System.out.println("4 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address));
		

	}

}
