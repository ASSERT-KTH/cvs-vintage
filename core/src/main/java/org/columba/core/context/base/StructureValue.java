package org.columba.core.context.base;

import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.core.context.base.api.IAttributeType;
import org.columba.core.context.base.api.IName;
import org.columba.core.context.base.api.IStructureType;
import org.columba.core.context.base.api.IStructureValue;
import org.columba.core.context.base.api.MULTIPLICITY;
import org.columba.core.context.base.api.IAttributeType.BASETYPE;

// TODO: multiplicity checks
public class StructureValue implements IStructureValue {

	private String name;

	private String namespace;

	private IStructureType type;

	private List<Object> attributeList = new Vector<Object>();

	private Hashtable<IName, Object> attributeMap = new Hashtable<IName, Object>();

	private Hashtable<IName, List<IStructureValue>> valueMap = new Hashtable<IName, List<IStructureValue>>();

	private IStructureValue parent;

	/**
	 * @param name
	 * @param namespace
	 * @param type
	 */
	public StructureValue(String name, String namespace, IStructureType type) {
		this.type = type;
		this.name = name;
		this.namespace = namespace;
	}

	/**
	 * @param name
	 * @param namespace
	 * @param type
	 * @param parent
	 */
	public StructureValue(String name, String namespace, IStructureType type,
			IStructureValue parent) {
		this(name, namespace, type);
		this.parent = parent;

	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#getType()
	 */
	public IStructureType getType() {
		return type;
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#getNamespace()
	 */
	public String getNamespace() {
		return namespace;
	}

	public Object getObject(String name, String namespace) {
		Object obj = attributeMap.get(new Name(name, namespace));
		if (obj == null) {
			// check if default value exists
			IAttributeType t = getType().getAttribute(name, namespace);
			return t.getDefaultValue();
		}

		return obj;
	}

	public void setObject(String name, String namespace, Object value) {
		if ( name == null ) throw new IllegalArgumentException("name == null");
		if ( namespace == null ) throw new IllegalArgumentException("namespace");
		if ( value == null ) throw new IllegalArgumentException("value == null");
		
		IAttributeType attrType = getType().getAttribute(name, namespace);
		if (attrType == null)
			throw new IllegalArgumentException("attribute type <" + name + ","
					+ namespace + "> does not exist");

		// remove old
		Object obj = getObject(name, namespace);
		if (obj != null) {
			attributeMap.remove(obj);
			attributeList.remove(obj);
		}

		attributeMap.put(new Name(name, namespace), value);
		attributeList.add(value);
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#getParent()
	 */
	public IStructureValue getParent() {
		return parent;
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#addChild(java.lang.String,
	 *      java.lang.String)
	 */
	public IStructureValue addChild(String name, String namespace) {
		IStructureType childType = getType().getChild(name, namespace);
		if (childType == null)
			throw new IllegalArgumentException("child structure type for <"
					+ name + "," + namespace + "> does not exist");

		IStructureValue value = new StructureValue(name, namespace, childType,
				this);
		List<IStructureValue> list = getChildStructureList(name, namespace);

		if ((childType.getCardinality().equals(MULTIPLICITY.ONE_TO_ONE) || getType()
				.getCardinality().equals(MULTIPLICITY.ZERO_TO_ONE))
				&& (list.size() == 1)) {
			// contains already a single element
			throw new IllegalArgumentException(
					"multiplicity of ONE_TO_ONE or ZERO_TO_ONE doesn't allow adding more children to this structure");
		}

		list.add(value);

		return value;
	}

	/**
	 * @param name
	 * @param namespace
	 * @return
	 */
	private int getChildStructureCount(String name, String namespace) {
		if (valueMap.containsKey(new Name(name, namespace))) {
			List<IStructureValue> list = valueMap.get(new Name(name, namespace));
			return list.size();
		}
		return 0;
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#removeChild(java.lang.String,
	 *      java.lang.String, int)
	 */
	public IStructureValue removeChild(String name, String namespace, int index) {
		List<IStructureValue> list = valueMap.get(new Name(name, namespace));
		if (list == null)
			throw new IllegalArgumentException("list <" + name + ","
					+ namespace + "> is empty");

		IStructureValue value = list.get(index);
		if (value == null)
			throw new IllegalArgumentException("no element at index " + index);

		list.remove(index);

		return value;
	}

	/**
	 * @param name
	 * @param namespace
	 * @return
	 */
	private List<IStructureValue> getChildStructureList(String name,
			String namespace) {
		int count = getChildStructureCount(name, namespace);

		if (count == 0) {
			// create empty list
			List<IStructureValue> list = new Vector<IStructureValue>();
			valueMap.put(new Name(name, namespace), list);
			return list;
		} else {
			List<IStructureValue> list = valueMap.get(new Name(name, namespace));
			return list;
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("StructureValue[");
		buf.append("name=" + getName());
		buf.append(", namespace" + getNamespace());
		buf.append("]");
		return buf.toString();
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#isValid()
	 */
	public boolean isValid() {
		// TODO implement validation
		return true;
	}

	public Iterator<IName> getAllAttributeNames() {
		return attributeMap.keySet().iterator();
	}
	
	/**
	 * @see org.columba.core.context.base.api.IStructureValue#getAttributeIterator()
	 */
	public Iterator<Object> getAttributeIterator() {
		return attributeList.listIterator();
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#getChildIterator(java.lang.String,
	 *      java.lang.String)
	 */
	public Iterator<IStructureValue> getChildIterator(String name,
			String namespace) {
		List<IStructureValue> list = valueMap.get(new Name(name, namespace));
		if (list == null) {
			// create empty structure value
			list = new Vector<IStructureValue>();
		}
			

		return list.listIterator();

	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#removeAllChildren(java.lang.String,
	 *      java.lang.String)
	 */
	public void removeAllChildren(String name, String namespace) {
		valueMap.remove(new Name(name, namespace));
	}

	/**
	 * @see org.columba.core.context.base.api.IStructureValue#getString(java.lang.String,
	 *      java.lang.String)
	 */
	public String getString(String name, String namespace) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.STRING))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type String");

		return (String) getObject(name, namespace);
	}

	public void setString(String name, String namespace, String value) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.STRING))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type String");

		setObject(name, namespace, value);
	}

	public int getInteger(String name, String namespace) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.INTEGER))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Integer");

		return (Integer) getObject(name, namespace);
	}

	public void setInteger(String name, String namespace, int value) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.INTEGER))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Integer");

		setObject(name, namespace, value);
	}

	public Date getDate(String name, String namespace) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.DATE))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Date");

		return (Date) getObject(name, namespace);
	}

	public void setDate(String name, String namespace, Date value) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.DATE))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Date");

		setObject(name, namespace, value);
	}

	public float getFloat(String name, String namespace) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.FLOAT))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Float");

		return (Float) getObject(name, namespace);
	}

	public void setFloat(String name, String namespace, float value) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.FLOAT))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Float");
		setObject(name, namespace, value);
	}

	public double getDouble(String name, String namespace) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.DOUBLE))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Double");

		return (Double) getObject(name, namespace);
	}

	public void setDouble(String name, String namespace, double value) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.DOUBLE))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type Double");
		setObject(name, namespace, value);
	}

	public byte[] getByteArray(String name, String namespace) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.BINARY))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type binary");

		return (byte[]) getObject(name, namespace);
	}

	public void setByteArray(String name, String namespace, byte[] value) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.BINARY))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type binary");
		setObject(name, namespace, value);
	}

	public InputStream getInputStream(String name, String namespace) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.INPUTSTREAM))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type blob (binary inputstream)");

		return (InputStream) getObject(name, namespace);
	}

	public void setInputStream(String name, String namespace, InputStream value) {
		IAttributeType t = getType().getAttribute(name, namespace);
		if (!t.getBaseType().equals(BASETYPE.INPUTSTREAM))
			throw new IllegalArgumentException("attribute <" + name + ","
					+ namespace + "> is not of type blob (binary inputstream)");
		setObject(name, namespace, value);
	}

	public Iterator<IName> getAllChildNames() {
		return valueMap.keySet().iterator();
	}

	

}
