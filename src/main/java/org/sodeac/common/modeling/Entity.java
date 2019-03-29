package org.sodeac.common.modeling;

public class Entity<T extends ComplexType<T>>
{
	ComplexType<T> model = null;
	public Entity(T model)
	{
		this.model = model;
		
		// mit Registry-Cache: Reflection des Modells
		// erstellen eines Arrays Mit Feldern
		// Felder können über Index addressiert werden
		// Eigentlich kann auch alles in dem ComplexType hinterlegt werden
		
		// Wichtig von Anfang an: GC
	}
	public ComplexType<T> getModel()
	{
		return model;
	}
	
	public <X> X getSingleValue(ModelPath<T,X> path)
	{
		return null;
	}
	
	// getPathValue(). <= returns PathBuilder
	// => ein Path benötigt 2 Typen => den Einstiegspunkt (Model und das Ziel)
}
