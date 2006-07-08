package org.columba.core.context.base.api;


public interface IAttributeType {
	
	public enum BASETYPE { STRING, INTEGER, FLOAT, DOUBLE, DATE, BINARY, INPUTSTREAM};
	
	public String getName();
	public String getNamespace();
	
	public BASETYPE getBaseType();
	public void  setBaseType(BASETYPE type);
	
	public boolean isOptional();
	public void setOptional(boolean optional);
	
	public Object getDefaultValue();
	public void setDefaultValue(Object defaultValue);
}
