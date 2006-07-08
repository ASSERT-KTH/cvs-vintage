package org.columba.core.context.base;

import org.columba.core.context.base.api.IAttributeType;

public class AttributeType implements IAttributeType {

	private String name;

	private String namespace;

	private BASETYPE baseType;

	private Object defaultValue;

	private boolean optional;

	public AttributeType(String name, String namespace) {
		this.name = name;
		this.namespace = namespace;

		this.baseType = BASETYPE.STRING;
		optional = true;
	}

	public BASETYPE getBaseType() {
		return baseType;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setBaseType(BASETYPE type) {
		this.baseType = type;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public boolean equals(Object obj) {
		AttributeType t = (AttributeType) obj;

		if (!t.getBaseType().equals(getBaseType()))
			return false;

		if (t.getDefaultValue() != null && getDefaultValue() != null) {
			if (!t.getDefaultValue().equals(getDefaultValue()))
				return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("AttributeType");
		buf.append("[");
		buf.append("name=" + getName());
		buf.append("namespace=" + getNamespace());
		buf.append("basetype=" + getBaseType());
		buf.append("]");
		return buf.toString();
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

}
