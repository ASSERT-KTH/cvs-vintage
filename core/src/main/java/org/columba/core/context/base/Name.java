package org.columba.core.context.base;

import org.columba.core.context.base.api.IName;

public class Name implements IName {

	private String name;

	private String namespace;

	public Name(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public boolean equals(Object obj) {
		Name n = (Name) obj;

		if (!n.getName().equals(getName()))
			return false;
		if (!n.getNamespace().equals(getNamespace()))
			return false;

		return true;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(name);
		if (namespace != null)
			buf.append("." + namespace);
		return buf.toString();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		
		hash = 31 * hash + name.hashCode();
		hash = 31 * hash + namespace.hashCode();
		
		return hash;
	}

}
