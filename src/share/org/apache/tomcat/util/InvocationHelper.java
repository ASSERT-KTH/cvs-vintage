package org.apache.tomcat.util;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

/* Originally part of ant - it's only the invocation part, without
   any reference to internal ant objects.
*/
   

/**
 *  Utilities for using dynamic invocation.
 *
 *  Will try few standard ways to set/get properties - getter, getProperty
 *
 *
 * @author duncan@x180.com
 * @author costin@dnt.ro
 */
public class InvocationHelper {

    /** Get property name using getter ( getName ), then getProperty(name)
     */
    public static String getProperty( Object o, String name ) {
	try {
	    Method getMethod = (Method)getPropertyGetter(o, name);
	    if( getMethod!= null ) {
		// System.out.println("Set" + name);
		Object res=getMethod.invoke(o, new Object[] {});
		return (String)res;
	    }
	    getMethod = getMethod( o, "getProperty" );
	    if( getMethod != null ) {
		//System.out.println("SetProperty" + name);
		return (String)getMethod.invoke(o, new String[] {name});
	    }
	    
	    String msg = "Error getting " + name + " in " + o.getClass();
	    return null;
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " + name;
	    System.out.println("WARNING " + msg);
	    iae.printStackTrace();
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	}
	return null;
    }

    
    /** Set a property, using either the setXXX method or a generic setProperty(name, value)
     *  @returns true if success
     */
    public static void setProperty( Object o, String name, String value ) {
	//	System.out.println("Setting Property " + o.getClass() + " " + name + "=" + value);
	try {
	    Method setMethod = (Method)getPropertySetter(o, name);
	    if( setMethod!= null ) {
		// System.out.println("Set" + name);
		setMethod.invoke(o, new Object[] {value});
		return;
	    }
	    setMethod = getMethod( o, "setProperty" );
	    if( setMethod != null ) {
		//System.out.println("SetProperty" + name);
		setMethod.invoke(o, new String[] {name, value});
		return; 
	    }
	    
	    //	    String msg = "Error setting " + name + " in " + o.getClass();
	    //throw new BuildException(msg);
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " + name;
	    System.out.println("WARNING " + msg);
	    iae.printStackTrace();
	    //	    throw new BuildException(msg);
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	    //	    throw new BuildException(msg);		    
	}
    }

    /** Set an object property using setter or setAttribute(name).
     */
    public static void setAttribute( Object o, String name, Object v ) {
	//	System.out.println("Set Attribute " + o.getClass() + " " + name + " " + v );
	try {
	    Method setMethod = getPropertySetter(o, name);
	    if( setMethod!= null ) {
		//System.out.println("Set object " + name);
		// Avoid conflict with String (properties )
		Class[] ma =setMethod.getParameterTypes();
		if ( (ma.length == 1) && (! ma[0].getName().equals("java.lang.String"))) {
		    setMethod.invoke(o, new Object[] {v});
		    return;
		}
	    }
	    
	    setMethod = getMethod( o, "setAttribute" );
	    if( setMethod != null ) {
		setMethod.invoke(o, new Object[] {name, v});
		return; 
	    }

	    // Silent 
	    //	    String msg = "Error setting " + name + " in " + o.getClass();
	    //	    throw new BuildException(msg);
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " +
		name;
	    iae.printStackTrace();
	    //	    throw new BuildException(msg);
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	    //	    throw new BuildException(msg);		    
	}
    }
    
    /** Calls addXXX( v ) then setAttribute( name, v).
     */
    public static void addAttribute( Object o, String name, Object v ) {
	try {
	    Method setMethod = getMethod(o, "add" + capitalize( name ));
	    //	    System.out.println("ADD: " + name + " " + o.getClass() + " " + v.getClass());
	    if( setMethod!= null ) {
		//		System.out.println("Add object using addXXX " + name);
		// Avoid conflict with String (properties )
		Class[] ma =setMethod.getParameterTypes();
		if ( (ma.length == 1) && (! ma[0].getName().equals("java.lang.String"))) {
		    setMethod.invoke(o, new Object[] {v});
		    return;
		}
	    }
	    
	    // fallback to setAttribute
	    setAttribute( o, name, v);
	} catch (IllegalAccessException iae) {
	    String msg = "Error setting value for attrib: " +
		name;
	    iae.printStackTrace();
	    //	    throw new BuildException(msg);
	} catch (InvocationTargetException ie) {
	    String msg = "Error setting value for attrib: " +
		name + " in " + o.getClass().getName();
	    ie.printStackTrace();
	    ie.getTargetException().printStackTrace();
	    //	    throw new BuildException(msg);		    
	}
    }

    

    
    /** Return a new object of class cName, or null
     */
    public static Object getInstance( String cName ) {
	try {
	    //	    System.out.println("Loading " + cName );
	    Class c=Class.forName( cName );
	    Object o=c.newInstance();
	    return o;
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /** Return the class that is exected in a setXXX 
     */
    public static Class getType( Object o, String prop ) {
	try {
	    Method setMethod = (Method)getPropertySetter(o, prop);
	    Class[] ma =setMethod.getParameterTypes();
	    if( ma.length!= 1 ) return null;
	    return ma[0];
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    return null;
	}
    }

    /** Invoke a method, no exception
     */
    public Object invokeV( Object o, String mname, Object []args ) {
	try {
	    return doInvokeV( o, mname, args);
	} catch( Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    /** Invoke a method
     */
    public Object doInvokeV( Object o, String mname, Object []args )
	throws Exception 
    {
	try {
            Class c=o.getClass();
        
            Method[] methods = c.getMethods();
            Method main = null;

	    
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(mname)) {
		    System.out.println("Found method with " + mname );
		    boolean found=true;
		    Class[] ma = methods[i].getParameterTypes();
		    if(ma.length != args.length)
			found=false;
		    else {
			for( int j=0; j<args.length; j++) {
			    System.out.println("Arg: " + args[j].getClass() + " " + ma[j]);
			    //			    if( (args[j] != null)  ! (args[j] instanceof ma[j]) ) {
				//found=false;
			}
		    }
		    
		    // XXX check argument types for compatibility!!!
		    if( found )
			main = methods[i];
		}
            }
            
            if (main != null) 
                return main.invoke(o, args);
	} catch( Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    // -------------------- Utility - probably not so usefull outside ----------
    
    public static Hashtable getPropertySetters( Object o ) {
	// XXX cache introspection data !!!
	Hashtable propertySetters = new Hashtable();
	BeanInfo beanInfo;
	try {
	    beanInfo = Introspector.getBeanInfo(o.getClass());
	} catch (IntrospectionException ie) {
	    String msg = "Can't introspect task class: " + o.getClass();
	    System.out.println("WARNING " + msg);
	    ie.printStackTrace();
	    return propertySetters;
	}

	PropertyDescriptor[] pda = beanInfo.getPropertyDescriptors();
	for (int i = 0; i < pda.length; i++) {
	    PropertyDescriptor pd = pda[i];
	    String property = pd.getName();
	    //	    System.out.println("Property: " + property);
	    Method setMethod = pd.getWriteMethod();
	    if (setMethod != null) {
		propertySetters.put(property, setMethod);
	    }
	}
	return propertySetters;
    }

    /** Get a setter method or null
     */
    public static PropertyDescriptor getPropertyDescriptor( Object o, String prop ) {
	BeanInfo beanInfo;
	try {
	    beanInfo = Introspector.getBeanInfo(o.getClass());
	} catch (IntrospectionException ie) {
	    String msg = "Can't introspect task class: " + o.getClass();
	    ie.printStackTrace();
	    System.out.println("WARNING " + msg);
	    return null;
	}

	PropertyDescriptor[] pda = beanInfo.getPropertyDescriptors();
	for (int i = 0; i < pda.length; i++) {
	    PropertyDescriptor pd = pda[i];
	    String property = pd.getName();
	    //	    System.out.println("XXX" + prop + " " + property);
	    if( property.equals( prop )) {
		return pda[i];
	    }
	}
	return null;
    }

    /** Get a setter method or null
     */
    public static Method getPropertySetter( Object o, String prop ) {
	PropertyDescriptor pd=getPropertyDescriptor( o, prop );
	if( pd==null) return null;
	Method setMethod = pd.getWriteMethod();
	if (setMethod != null) {
	    return setMethod;
	}
	return null;
    }

    /** Get a setter method or null
     */
    public static Method getPropertyGetter( Object o, String prop )  {
	PropertyDescriptor pd=getPropertyDescriptor( o, prop );
	if( pd==null) return null;
	Method setMethod = pd.getReadMethod();
	if (setMethod != null) {
	    return setMethod;
	}
	return null;
    }

    /** Get a method or null
     */
    public static Method getMethod( Object o, String method ) {
	// XXX cache introspection data !!!
	BeanInfo beanInfo;
	try {
	    beanInfo = Introspector.getBeanInfo(o.getClass());
	} catch (IntrospectionException ie) {
	    String msg = "Can't introspect task class: " + o.getClass();
	    ie.printStackTrace();
	    System.out.println("WARNING " + msg);
	    return null;
	}

	MethodDescriptor[] mda = beanInfo.getMethodDescriptors();
	for (int i = 0; i < mda.length; i++) {
	    MethodDescriptor pd = mda[i];
	    String m = pd.getName();

	    if( m.equals( method ) )
		return pd.getMethod();
	}
	return null;
    }

    /** Reverse of Introspector.decapitalize
     */
    public static String capitalize(String name) {
	if (name == null || name.length() == 0) {
	    return name;
	}
	char chars[] = name.toCharArray();
	chars[0] = Character.toUpperCase(chars[0]);
	return new String(chars);
    }


}









