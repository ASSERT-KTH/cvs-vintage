package org.columba.core.context.base.api;

import java.util.Collection;

public interface IStructureType {

	public String getName();
	public String getNamespace();
	
	public IAttributeType addAttribute(String name, String namespace);
	public IAttributeType getAttribute(String name, String namespace);
	public Collection<IAttributeType> getAttributes();
	
	public MULTIPLICITY getCardinality();
	public void setCardinality(MULTIPLICITY cardinality);
	
	public IStructureType addChild(String name, String namespace);
	public IStructureType addChild(IStructureType type);
	public IStructureType removeChild(String name, String namespace);
	public IStructureType getChild(String name, String namespace);
	
	public Collection<IStructureType> getChildren();
	

}
