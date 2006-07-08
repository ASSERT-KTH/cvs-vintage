package org.columba.core.context.base;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.columba.core.context.base.api.IAttributeType;
import org.columba.core.context.base.api.IName;
import org.columba.core.context.base.api.IStructureType;
import org.columba.core.context.base.api.MULTIPLICITY;

public class StructureType implements IStructureType {

	private String name;

	private String namespace;

	private MULTIPLICITY cardinality;

	private List<IStructureType> typeList = new Vector<IStructureType>();

	private List<IAttributeType> attributeList = new Vector<IAttributeType>();

	private Hashtable<IName, IStructureType> typeMap = new Hashtable<IName, IStructureType>();

	private Hashtable<IName, IAttributeType> attributeMap = new Hashtable<IName, IAttributeType>();

	public StructureType(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;
		
		this.cardinality = MULTIPLICITY.ONE_TO_ONE;
	}


	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public MULTIPLICITY getCardinality() {
		return cardinality;
	}

	public IStructureType removeChild(String name, String namespace) {
		IStructureType type = typeMap.remove(new Name(name, namespace));
		typeList.remove(type);
		return type;
	}

	public Collection<IStructureType> getChildren() {
		return typeMap.values();
	}

	public IAttributeType addAttribute(String name, String namespace) {
		IAttributeType attr = new AttributeType(name, namespace);
		attributeMap.put(new Name(name, namespace), attr);
		attributeList.add(attr);
		return attr;
	}

	public IAttributeType getAttribute(String name, String namespace) {
		return attributeMap.get(new Name(name, namespace));
	}

	public Collection<IAttributeType> getAttributes() {
		return attributeList;
	}

	public IStructureType addChild(String name, String namespace) {
		IStructureType type = new StructureType(name, namespace);
		typeMap.put(new Name(name, namespace), type);
		typeList.add(type);

		return type;
	}
	
	public IStructureType addChild(IStructureType type) {
		typeMap.put(new Name(name, namespace), type);
		typeList.add(type);
		return type;
	}

	public IStructureType getChild(String name, String namespace) {
		return typeMap.get(new Name(name, namespace));
	}

	public void setCardinality(MULTIPLICITY cardinality) {
		this.cardinality = cardinality;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("StructureType[");
		buf.append("name="+getName());
		buf.append("namespace"+getNamespace());
		buf.append("]");
		return buf.toString();
	}

	

}
