package com.mantono.ghapic;

public class Repository
{
	private final String owner, name;
	
	public Repository(final String owner, final String name)
	{
		this.owner = owner;
		this.name = name;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPath()
	{
		return owner + "/" + name;
	}
	
	@Override
	public String toString()
	{
		return getPath();
	}
	
	@Override
	public int hashCode()
	{
		return owner.hashCode() + name.hashCode();
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object == null)
			return false;
		if(!(object instanceof Repository))
			return false;
		
		Repository other = (Repository) object;
		
		return this.owner.equals(other.owner) && this.name.equals(other.name);
	}
}
