/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.nrmi.algorithm;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

/**
 * Depth-first search order traversal algorithm with callbacks.
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
 * */

public class DFObjectWalker
{

public interface EntryExitCallback
{
public void onEntry (Object obj);
public void onExit (Object obj);
}

	
public interface Callback 
{
public void onEntry (Object obj);
public void onExit (Object obj);
public void onNull ();
public void onRepeatedObject (Object obj);
public void onBooleanField (boolean field);
public void onByteField (byte field);
public void onCharField (char field);
public void onShortField (short field);
public void onIntField (int field);	
public void onLongField (long field);
public void onFloatField (float field);
public void onDoubleField (double field);
public void onPrimitiveArray (Object arrayField);
public void onObjectArray (Object arrayField);
}

public abstract static class CallbackAdapter implements Callback
{
public void onNull () {}
public void onRepeatedObject (Object obj) {}
public void onEntry (Object obj) {}
public void onExit (Object obj) {}
public void onBooleanField (boolean field) {}
public void onByteField (byte field) {}
public void onCharField (char field) {}
public void onShortField (short field) {}
public void onIntField (int field) {}	
public void onLongField (long field) {}
public void onFloatField (float field) {}
public void onDoubleField (double field) {}
public void onPrimitiveArray (Object arrayField) {}
public void onObjectArray (Object arrayField) {}
}


private Set			_sVisitedObjs;
private EntryExitCallback	_entryExitCallback;
private Callback		_callback;
private final static Class	_object_class = java.lang.Object.class;

public void walkObject (final Object obj, EntryExitCallback entryExitCallback) {
_sVisitedObjs		= new HashSet (); 
_entryExitCallback	= entryExitCallback;
	
try {
	doWalkObjectOptimized (obj);       			
} catch (java.lang.IllegalAccessException e) {
	e.printStackTrace();
}
}

public void walkObject (Object obj, Callback callback) {
	
_sVisitedObjs	= new HashSet (); 
_callback    	= callback;
	
try {
	doWalkObject (obj);
} catch (java.lang.IllegalAccessException e) {
	e.printStackTrace();
}
}

protected void doWalkObject (Object obj) throws java.lang.IllegalAccessException { 

if (null == obj) {
	_callback.onNull ();
	return;
}


if (_sVisitedObjs.contains (obj)) {
	_callback.onRepeatedObject (obj);
	return;
}

_sVisitedObjs.add (obj);

_callback.onEntry (obj);

Class objClass = obj.getClass ();
if (objClass.isArray ()) {
	if (Caches.ClassInfo.isPrimitive (objClass.getComponentType()))
		_callback.onPrimitiveArray (obj);
	else { //Object Array
		_callback.onObjectArray (obj);
		for (int j = 0; j < Array.getLength (obj); ++j)
			doWalkObject (Array.get (obj, j)); //recurse
	}
}

Caches.ClassDeclaredFields.FieldsInfo fInfo  = Caches.ClassDeclaredFields.getDeclaredFields (objClass);
for (int i = 0; i < fInfo.fields.length; ++i) {
	Class fldClass = fInfo.fields[i].getType ();
	if (Caches.ClassInfo.isPrimitive (fldClass)) {
		switch (fldClass.getName ().charAt (0)) { 
		
		//if (fldClass == boolean.class)
		case 'b':
			if (fldClass.getName ().charAt (1) == 'o')
				_callback.onBooleanField (fInfo.fields[i].getBoolean (obj));
		//else
		//if (fldClass == byte.class)
			else
				_callback.onByteField (fInfo.fields[i].getByte (obj));
		break;
		//else
		//if (fldClass == char.class)
		case 'c':
			_callback.onCharField (fInfo.fields[i].getChar (obj));
		break;
		//else
		//if (fldClass == short.class)
		case 's':
			_callback.onShortField (fInfo.fields[i].getShort (obj));
		break;
		//else
		//if (fldClass == int.class)
		case 'i':
			_callback.onIntField (fInfo.fields[i].getInt (obj));	
		break;
		//else
		//if (fldClass == long.class)
		case 'l':
			_callback.onLongField (fInfo.fields[i].getLong (obj));
		break;
		//else
		//if (fldClass == float.class)
		case 'f':	
			_callback.onFloatField (fInfo.fields[i].getFloat (obj));
		break;
		//else
		//if (fldClass == double.class)
		case 'd':	
			_callback.onDoubleField (fInfo.fields[i].getDouble (obj));
		break;
		default:
			throw new RuntimeException ("illegal primitive field type: " + fldClass.getName());
		} //switch
		 
	} else { //if flds[i] is an array or a reference
		doWalkObject (fInfo.fields[i].get (obj)); //recurse
	}
} //end for

_callback.onExit (obj);	

} //end doWalkObject

protected void doWalkObjectOptimized (Object obj) throws java.lang.IllegalAccessException {

if (null == obj || _sVisitedObjs.contains (obj)) 
	return;


_sVisitedObjs.add (obj);

_entryExitCallback.onEntry (obj);

Class curObjClass = obj.getClass ();
do {

if (curObjClass.isArray ()) {
	if (!Caches.ClassInfo.isPrimitive (curObjClass.getComponentType())) {
		 //Object Array
		int arr_len = Array.getLength(obj);
		Object[] alias = (Object[])obj;	
		for (int j = 0; j < arr_len; ++j)
			//doWalkObjectOptimized (Array.get (obj, j)); //recurse
			doWalkObjectOptimized (alias[j]); //recurse
	}
}

Caches.ClassDeclaredFields.FieldsInfo fInfo  = Caches.ClassDeclaredFields.getDeclaredFields (curObjClass);
for (int i = 0; i < fInfo.fields.length; ++i) {
	if (!fInfo.isPrimitive [i] && fInfo.isSerializable[i]) {
		//if flds[i] is an array or a reference
		doWalkObjectOptimized (fInfo.fields[i].get (obj)); //recurse
	}
} //end for

curObjClass = Caches.ClassInfo.getSuperclass (curObjClass);

} while (curObjClass != _object_class && curObjClass != null);

_entryExitCallback.onExit (obj);	

} //end doWalkObjectOptimized

}
