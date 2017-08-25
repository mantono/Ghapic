package com.mantono.ghapic;

public class Repository
{
	private final int id;
	private final String owner, name;
	
	public Repository(final String owner, final String name)
	{
		this(owner, name, -1);
	}

	public Repository(String owner, String name, int id)
	{
		this.owner = owner;
		this.name = name;
		this.id = id;
	}
	
	public int getId()
	{
		return id;
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
