package org.columba.core.context.base.api;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public interface IStructureValue {

	public String getName();
	public String getNamespace();
	
	public IStructureType getType();

	public Iterator<Object> getAttributeIterator();
	public Iterator<IName> getAllAttributeNames();
	
	public Object getObject(String name, String namespace);
	public void setObject(String name, String namespace, Object value);
	
	public String getString(String name, String namespace);
	public void setString(String name, String namespace, String value);
	
	public int getInteger(String name, String namespace);
	public void setInteger(String name, String namespace, int value);
	
	public Date getDate(String name, String namespace);
	public void setDate(String name, String namespace, Date value);
	
	public float getFloat(String name, String namespace);
	public void setFloat(String name, String namespace, float value);
	
	public double getDouble(String name, String namespace);
	public void setDouble(String name, String namespace, double value);
	
	public byte[] getByteArray(String name, String namespace);
	public void setByteArray(String name, String namespace, byte[] value);
	
	public InputStream getInputStream(String name, String namespace);
	public void setInputStream(String name, String namespace, InputStream value);
	
	public IStructureValue addChild(String name, String namespace);
	public IStructureValue removeChild(String name, String namespace, int index);
	public void removeAllChildren(String name, String namespace);
	public Iterator<IStructureValue> getChildIterator(String name, String namespace);
	public Iterator<IName> getAllChildNames();
	
	public IStructureValue getParent();
	
	public boolean isValid();
}
