package org.sodeac.common.typedtree;

import org.sodeac.common.typedtree.ModelPathBuilder.RootModelPathBuilder;
import org.sodeac.common.typedtree.TypedTreeMetaModel.RootBranchNode;

public class Test
{

	public static void main(String[] args)
	{
		UserType userModel = new UserType();
		
		// entity.getModel().buildPath() <= PathBuilder
		// Fields sind vom Type Optional,1z1,0zn,1zn
		
		RootModelPathBuilder<UserType> builder = ModelPathBuilder.newBuilder(userModel);
		ModelPathBuilder<UserType,AddressType> x1 = builder.to(UserType.address);
		ModelPathBuilder<UserType,CountryType> x2 = x1.to(AddressType.country);
		ModelPath<UserType, String> p = x2.buildForValue(CountryType.name);
		
		System.out.println("xxx " + p + " -- " + CountryType.languageList.getNodeName());
		
		ModelPath<UserType, String> p2 = ModelPathBuilder.newBuilder(UserType.class)
																	.to(UserType.address)
																	.to(AddressType.country)
																	.buildForValue(CountryType.name);
		System.out.println("yyy " + p2);
		
		ModelPath<UserType, LeafNode<?,String>> p3 = ModelPathBuilder.newBuilder(UserType.class)
				.to(UserType.address)
				.to(AddressType.country)
				.buildForNode(CountryType.name);
		
		System.out.println("zzz " + p3);
		
		ModelPath<UserType, BranchNode<AddressType,UserType>> p4 = ModelPathBuilder.newBuilder(UserType.class)
				.to(UserType.address)
				.buildForNode(AddressType.parentuser);
		
		System.out.println("aaa " + p4);
		
		
		ModelPath<UserType, String> mp = new ModelPath<UserType, String>(null); // TODO protected
		
		/*Entity<CountryType> country =  Entity.newInstance(CountryType.class);
		Entity<UserType> user = Entity.newInstance(UserType.class);
		BasicObject<UserType,String> st = user.getSingleValue(mp);
		BasicObject<CountryType,String> countryName = country.get(CountryType.name);
		System.out.println("ööö1 " + countryName);
		System.out.println("ööö2 " + country.get(CountryType.name));
		System.out.println("ööö3 " + user.get(UserType.address).getValue());*/
		
		//userModel.name.getType()
		
		TestModel testModel = TypedTreeMetaModel.getInstance(TestModel.class);
		RootBranchNode<TestModel,UserType> u =  testModel.createRootNode(TestModel.user);
		u
			.setValue(UserType.name,"buzzt")
			.create(UserType.address )
				.setValue(AddressType.street,"MCA");
		
		
		testModel.createRootNode(TestModel.user).setValue(UserType.name, "Icke").create(UserType.address).setValue(AddressType.street,"MCA");
		
		System.out.println("1 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address).get(AddressType.street).getValue());
		
		u =  testModel.createRootNode(TestModel.user);
		u
			.applyToConsumer(x -> x.setValue(UserType.name,"buzzt"))
			.applyToConsumer
			(
				x -> x.create(UserType.address).
					setValue(AddressType.street,"MCA")
			);
		
		System.out.println("2 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address).get(AddressType.street).getValue());
		
		u =  testModel.createRootNode(TestModel.user).setBranchNodeApplyToConsumerAutoCreate(true);
		u
			.applyToConsumer(x -> x.setValue(UserType.name,"buzzt"))
			.applyToConsumer(x -> x.applyToConsumer(UserType.address,(y,a) -> a.setValue(AddressType.street,"MCA")));
		
		System.out.println("3 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address).get(AddressType.street).getValue());
		
		 BranchNode<AddressType, CountryType>  country = u.get(UserType.address).create(AddressType.country);
		 country.create(CountryType.languageList).setValue(LangType.name, "German").setValue(LangType.code, "de");
		 System.out.println("List: " + country.getUnmodifiableNodeList(CountryType.languageList).size());
		 
		u.remove(UserType.address);
		
		System.out.println("4 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address));
		
		u.dispose();
		
		System.out.println("----------------");
		
		u =  testModel.createRootNode(TestModel.user).setBranchNodeApplyToConsumerAutoCreate(true);
		u
			.applyToConsumer(x -> x.setValue(UserType.name,"buzzt"))
			.applyToConsumer(x -> x.applyToConsumer(UserType.address,(y,a) -> a.setValue(AddressType.street,"MCA")));
		
		System.out.println("3 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address).get(AddressType.street).getValue());
		
		 country = u.get(UserType.address).create(AddressType.country);
		 country.create(CountryType.languageList).setValue(LangType.name, "German").setValue(LangType.code, "de");
		 System.out.println("List: " + country.getUnmodifiableNodeList(CountryType.languageList).size());
		 
		u.remove(UserType.address);
		
		System.out.println("4 " +  u + " " + u.get(UserType.name).getValue() + " " + u.get(UserType.address));
		
		u.dispose();
	}

}
