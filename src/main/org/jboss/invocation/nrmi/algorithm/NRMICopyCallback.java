/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.nrmi.algorithm;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * A callback to update the original object after the call completes
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
 * */

public class NRMICopyCallback implements DFObjectWalker.EntryExitCallback
{
	
public NRMICopyCallback (Object newRepresentation[], Object oldRepresentation[])
								throws NRMICopyCallback.Exception {
_NewToOld = createNewToOldMap (newRepresentation, oldRepresentation);
	
}
	
public static class Exception extends RuntimeException
{
public Exception (String msg) {
	super (msg);
}
}
	
private Map _NewToOld;
private final static Class OBJECT_CLASS = Object.class;	

private Map createNewToOldMap (Object newRepresentation[], Object oldRepresentation[])
								throws NRMICopyCallback.Exception {
if (newRepresentation.length != oldRepresentation.length ) {
	throw new NRMICopyCallback.Exception 
		("The old and new representation arrays have different length");
}
_NewToOld = FastHashMapFactory.createHashMap (2053); // first prime above 2K 
		
for (int idx = 0; idx < newRepresentation.length; ++idx) {
	_NewToOld.put (newRepresentation [idx], oldRepresentation [idx]);
	/*
	System.out.println ("newRepresentation [idx], oldRepresentation [idx] " +
						newRepresentation[idx].getClass().getName() + "\n" +
						newRepresentation[idx] + "!!!" + oldRepresentation[idx]);
	*/
}
return _NewToOld;
}
	
public void onEntry (Object obj) {
Object old = _NewToOld.get (obj);
if (null != old) {
	copyAllFields (old, obj);
}
}
	
public void onExit (Object obj) {
if (null == _NewToOld.get (obj)) {	
	adjustObjectsFields (obj);    
}
}	
	
protected void adjustObjectsFields (Object obj) {
//System.out.println ("adjustObjectsFields " + obj.getClass().getName());	
if (null == obj)
	return;	
			
int modifiers = Caches.Modifiers.getClassModifiers (obj.getClass ());
if (Modifier.isFinal (modifiers))
	return;

Class curClass = null;
curClass = obj.getClass ();
		
do {
		
	Caches.ClassDeclaredFields.FieldsInfo fInfo = Caches.ClassDeclaredFields.getDeclaredFields (curClass);
	Object[] objFlds = new Object[fInfo.fields.length];
	try {
	for (int i = 0; i < fInfo.fields.length; ++i) {
		if (!fInfo.isSerializable [i])
			continue;
					
		if (!fInfo.isPrimitive [i]) {
			//Object fieldValue = getFieldNative (obj, fInfo.fields[i]);
			Object fieldValue = Caches.ClassDeclaredFields.getObjField (obj, fInfo.fields[i]);
			if (null == _NewToOld.get (fieldValue)) continue;

			Caches.ClassDeclaredFields.setObjField (obj, fInfo.fields[i], _NewToOld.get (fieldValue));
		}
	} //for				
	} catch (java.lang.IllegalAccessException e1) {
        		System.out.println (e1);
	}
	curClass = Caches.ClassInfo.getSuperclass (curClass);
			
} while (curClass != OBJECT_CLASS && curClass != null);
	
}
	
protected void copyAllFields (Object oldObj, Object newObj)
									throws NRMICopyCallback.Exception {
if (oldObj == null || newObj == null)
	return;
	
Class objClass = oldObj.getClass ();
//System.out.println ("oldObj class is " + objClass);	
//System.out.println ("copyAllFields oldObj, newObj " + oldObj + ", " + newObj);

if (oldObj.getClass ().isArray () &&
    (Array.getLength (oldObj) == Array.getLength (newObj))) { 
			
	if (Caches.ClassInfo.isPrimitive (objClass.getComponentType())) {
		System.arraycopy (newObj, 0, oldObj, 
				  0, Array.getLength (newObj));
	} else {
		int arrayLen = Array.getLength (newObj);	
		Object[] alias = (Object[])newObj;	
		for (int j = 0; j < arrayLen; ++j) {
			//Object newElement = Array.get (newObj, j);
			Object newElement = alias[j];
			//if (_NewToOld.containsKey (newElement))
			Object elem = _NewToOld.get (newElement);	
			//System.out.println ("elem is " + elem);	
			if (null != elem)
				newElement = elem;
			Array.set (oldObj, j, newElement);
		} //for			
	} //else
			
	return;	
} //if
	
int modifiers = Caches.Modifiers.getClassModifiers (objClass);
if (Modifier.isFinal (modifiers))
	return;
		
Class curClass = newObj.getClass ();
		
do {
Caches.ClassDeclaredFields.FieldsInfo fInfo = Caches.ClassDeclaredFields.getDeclaredFields (curClass);
Field [] newFlds = fInfo.fields;
		
try {

for (int i = 0; i < newFlds.length; ++i) {
	if (!fInfo.isSerializable[i])
		continue;
					
	Class type = newFlds[i].getType ();	
	if (fInfo.isPrimitive [i]) {
		if (type == Integer.TYPE) {
			//types[i] = 'I';
			Caches.ClassDeclaredFields.copyIntField (oldObj, newObj, fInfo.fields[i]);
		} else if (type == Character.TYPE) {
			//types[i] = 'C';
			Caches.ClassDeclaredFields.copyCharField (oldObj, newObj, fInfo.fields[i]);
		} else if (type == Byte.TYPE) {
			//types[i] = 'B';
			Caches.ClassDeclaredFields.copyByteField (oldObj, newObj, fInfo.fields[i]);
		} else if (type == Long.TYPE) {
			//types[i] = 'J';
			Caches.ClassDeclaredFields.copyLongField (oldObj, newObj, fInfo.fields[i]);
		} else if (type == Float.TYPE) {
			//types[i] = 'F';
			Caches.ClassDeclaredFields.copyFloatField (oldObj, newObj, fInfo.fields[i]);
		} else if (type == Double.TYPE) {
			//types[i] = 'D';
			Caches.ClassDeclaredFields.copyDoubleField (oldObj, newObj, fInfo.fields[i]);
		} else if (type == Short.TYPE) {
			//types[i] = 'S';
			Caches.ClassDeclaredFields.copyShortField (oldObj, newObj, fInfo.fields[i]);
		} else if (type == Boolean.TYPE) {
			//types[i] = 'Z';
			Caches.ClassDeclaredFields.copyBooleanField (oldObj, newObj, fInfo.fields[i]);
		/*	
		} else if (type == Void.TYPE) {
			types[i] = 'V';
			*/	
		} else {
    			throw new InternalError();
		}
	} else {
		Object newObjField = Caches.ClassDeclaredFields.getObjField (newObj, fInfo.fields[i]);
		Object oldObjField = _NewToOld.get (newObjField);	
		if (null != oldObjField) {
			Caches.ClassDeclaredFields.setObjField (oldObj, fInfo.fields[i], oldObjField);
		} else {
			Caches.ClassDeclaredFields.setObjField (oldObj, fInfo.fields[i], newObjField);
		}
	} //else
	
} //for
			       	
} catch (java.lang.IllegalAccessException e1) {
	throw new NRMICopyCallback.Exception (e1.getMessage());
}
//System.out.println ("end of copyAllFields oldObj, newObj " + oldObj + ", " + newObj);	
curClass = Caches.ClassInfo.getSuperclass (curClass);
			
} while (curClass != OBJECT_CLASS && curClass != null);
		
}
	
	
}
