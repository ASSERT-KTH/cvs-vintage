/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.nrmi.algorithm;
import java.io.ObjectStreamField;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;


/**
 * Various caches used throughout the package 
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
 * */
public abstract class Caches
{

public static class ClassInfo
{

private static final Map _primitives = FastHashMapFactory.createHashMap (8); 
private static final Object bogusVal = new Object ();	
static {
	_primitives.put (int.class, bogusVal);	
	_primitives.put (byte.class, bogusVal);	
	_primitives.put (char.class, bogusVal);	
	_primitives.put (short.class, bogusVal);	
	_primitives.put (boolean.class, bogusVal);	
	_primitives.put (long.class, bogusVal);	
	_primitives.put (float.class, bogusVal);	
	_primitives.put (double.class, bogusVal);	
} //static
	
public static boolean isPrimitive (Class c) {
	return _primitives.get (c) != null;
}

private static java.util.Map _classToName = FastHashMapFactory.createHashMap ();
private static synchronized String getClassName (Class c) {
String name = (String) _classToName.get (c);
if (null == name) {
        name = c.getName ();
	_classToName.put (c, name);
}
return name;
}

private static Map _toSuperclass = FastHashMapFactory.createHashMap (); 
public static Class getSuperclass (Class c) {
	Class super_ = (Class)_toSuperclass.get (c);
	if (null == super_) {
		super_ = c.getSuperclass ();
		_toSuperclass.put (c, super_);
	}
	return super_;
}

} //ClassInfo


public static class Modifiers
{
private static Map _toModifiers = FastHashMapFactory.createHashMap (); 
public static synchronized int getClassModifiers (Class c) {
	Integer mod = (Integer) _toModifiers.get (c);
	int modifiers = 0;	
	if (null == mod) {
		modifiers = c.getModifiers (); 
		_toModifiers.put (c, new Integer (modifiers));
	} else {
		modifiers = mod.intValue ();	
	}
	return modifiers;
}
} //Modifiers

public static class ClassDeclaredFields
{

public static class FieldsInfo {
	Field[] fields;
	boolean[] isPrimitive;
	boolean[] isSerializable;
}
	
private static Map _classToFields = FastHashMapFactory.createHashMap (); 
	
public static FieldsInfo getDeclaredFields (Class c) {
FieldsInfo info = (FieldsInfo) _classToFields.get (c);
if (null == info) {
	info = new FieldsInfo ();	
	Field[] flds = info.fields = getDeclaredSerialFields (c); 
	if (null == flds)	
		flds = info.fields = c.getDeclaredFields ();
	AccessibleObject.setAccessible (flds, true);
			
	info.isPrimitive  = new boolean[flds.length];
	info.isSerializable = new boolean[flds.length];
	for (int f = 0; f < flds.length; ++f) {
		info.isPrimitive[f]    = ClassInfo.isPrimitive (flds[f].getType ());
		info.isSerializable[f] = isSerializable (flds[f]); 	
	} //for
	_classToFields.put (c,info);
}//if not found
		            
return info;
}

private final static int SER_MASK = Modifier.FINAL | Modifier.STATIC; 
public static boolean isSerializable (Field f) {
	return ((f.getModifiers() & SER_MASK) == 0);
}

private static Field[] getDeclaredSerialFields(Class cl) {
ObjectStreamField[] serialPersistentFields = null;
try {
	Field f = cl.getDeclaredField("serialPersistentFields");
	int mask = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
        if ((f.getModifiers() & mask) == mask) {
        	f.setAccessible(true);
                serialPersistentFields = (ObjectStreamField[]) f.get(null);
        }
} catch (Exception ex) {}

if (serialPersistentFields == null || serialPersistentFields.length == 0) { 
	return null;
}

Field[] declaredFields = new Field[serialPersistentFields.length]; 
for (int i = 0; i < serialPersistentFields.length; i++) {
	ObjectStreamField spf = serialPersistentFields[i];
        try {
	declaredFields[i] = cl.getDeclaredField(spf.getName());
	} catch (NoSuchFieldException ex) {
		//TODO: search in superclasses	
	}
}
return declaredFields;

}


public static Object getObjField(Object obj, Field field) 
								throws IllegalAccessException {
	return field.get (obj);
}

public static void setObjField(Object obj, Field field, Object val) 
										throws IllegalAccessException {
    field.set (obj, val); 
}

public static void copyObjField(Object dest, Object src, Field field) 
										throws IllegalAccessException {
	field.set (dest, field.get (src));	
}

public static void copyByteField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setByte (dest, field.getByte (src));
}

public static void copyBooleanField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setBoolean (dest, field.getBoolean (src));
}

public static void copyShortField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setShort (dest, field.getShort (src));
}

public static void copyCharField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setChar (dest, field.getChar (src));
}

public static void copyIntField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setInt (dest, field.getInt (src));
}

public static void copyLongField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setLong (dest, field.getLong (src));
}

public static void copyFloatField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setFloat (dest, field.getFloat (src));
}

public static void copyDoubleField(Object dest, Object src, Field field) 
											throws IllegalAccessException {
	field.setDouble (dest, field.getDouble (src));
}
	
} //ClassDeclaredFields


}