package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.AbstractVerifier
 * Copyright (C) 2000  Juha Lindfors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This package and its source code is available at www.jboss.org
 * $Id: AbstractVerifier.java,v 1.5 2000/07/25 17:36:12 juha Exp $
 */

// standard imports
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;


// non-standard class dependencies
import com.dreambean.ejx.ejb.Session;


/**
 * Abstract superclass for verifiers containing a bunch of useful methods.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     org.jboss.verifier.strategy.VerificationStrategy
 *
 * @author 	Juha Lindfors (jplindfo@helsinki.fi)
 * @version $Revision: 1.5 $
 * @since  	JDK 1.3
 */
public abstract class AbstractVerifier implements VerificationStrategy {

    /*
     * checks if a class's member (method, constructor or field) has a 'static'
     * modifier.
     */
    public boolean isStaticMember(Member member) {
        
        if (Modifier.isStatic(member.getModifiers()))
            return true;
            
        return false;
    }
    
    /*
     * checks if a class's member (method, constructor or field) has a 'final'
     * modifier.
     */
    public boolean isFinalMember(Member member) {
        
        if (Modifier.isFinal(member.getModifiers()))
            return true;
            
        return false;
    }
    
    /*
     * checks if a class's memeber (method, constructor or field) has a 'public'
     * modifier.
     */
    public boolean isPublicMember(Member member) {
        
        if (Modifier.isPublic(member.getModifiers()))
            return true;
            
        return false;
    }
        

    /*
     * checks if the session type is 'Stateful'
     */
    public boolean isStateful(Session session) {

        if (STATEFUL_SESSION.equals(session.getSessionType()))
            return true;

        return false;
    }
    
    /*
     * checks if the session type is 'Stateless'
     */
    public boolean isStateless(Session session) {
        
        if (STATELESS_SESSION.equals(session.getSessionType()))
            return true;
            
        return false;
    }
    
    /*
     * checks if a method has a void return type
     */
    public boolean hasVoidReturnType(Method method) {
        
        return (method.getReturnType() == Void.TYPE);
    }

    
    
    public boolean hasLegalRMIIIOPReturnType(Method method) {
        return true;
    }
    
    public boolean hasLegalRMIIIOPArguments(Method method) {
        
        return true;
        
        
        /*
         *  ftp://ftp.omg.org/pub/docs/ptc/99-03-09.pdf
         *
         *  A conforming RMI/IDL type is a Java type whose values
         *  may be transmitted across an RMI/IDL remote interface at
         *  run-time. A Java data type is a conforming RMI/IDL type
         *  if it is:
         *
         *  - one of the Java primitive types (see Primitive Types on page 28-2).
         *  - a conforming remote interface (as defined in RMI/IDL Remote Interfaces on page 28-2).
         *  - a conforming value type (as defined in RMI/IDL Value Types on page 28-4).
         *  - an array of conforming RMI/IDL types (see RMI/IDL Arrays on page 28-5).
         *  - a conforming exception type (see RMI/IDL Exception Types on page 28-5).
         *  - a conforming CORBA object reference type (see CORBA Object Reference Types on page 28-6).
         *  - a conforming IDL entity type see IDL Entity Types on page 28-6).
         *
         *
         
        Class[] params = method.getParameterTypes();
        
        for (int i = 0; i < params.length; ++i) {
            
            if (params[i].isPrimitive())
                continue;
                
            if (!isSerializable(params[i]))
                return false;
        }
     
        return true;
        
        */
    }

    
    /*
     * checks if the given class is declared as public
     */
    public boolean isPublicClass(Class c) {

        if (Modifier.isPublic(c.getModifiers()))
            return true;

        return false;
    }


    /*
     * checks if the given class is declared as final
     */
    public boolean isFinalClass(Class c) {

        if (Modifier.isFinal(c.getModifiers()))
            return true;

        return false;
    }


    /*
     * checks if the given class is declared as abstract
     */
    public boolean isAbstractClass(Class c) {

        if (Modifier.isAbstract(c.getModifiers()))
            return true;

        return false;
    }

    
    /*
     * checks if the method includes java.rmi.RemoteException in its
     * throws clause.
     */
    public boolean throwsRemoteException(Method method) {
        
        Class[] exception = method.getExceptionTypes();
        
        for (int i = 0; i < exception.length; ++i) {
            
            if (exception[i].getName().equals(REMOTE_EXCEPTION))
                return true;
        }
        
        return false;
    }
    
    /*
     * Checks if class implements the java.io.Serializable interface
     */
    public boolean isSerializable(Class c) {
    
        Class[] interfaces = c.getInterfaces();
            
        for (int i = 0; i < interfaces.length; ++i) {
                
            if ((SERIALIZATION_INTERFACE).equals(interfaces[i].getName()))
                return true;
        }
                
        return false;
    }
    
    /*
     * Finds java.ejb.SessionBean interface from the class
     */
    public boolean hasSessionBeanInterface(Class c) {

        Class[] interfaces = c.getInterfaces();

        for (int i = 0; i < interfaces.length; ++i) {

            if ((SESSION_BEAN_INTERFACE).equals(interfaces[i].getName()))
                return true;
        }

        return false;
    }

    /*
     * Finds java.ejb.EJBObject interface from the class
     */
    public boolean hasEJBObjectInterface(Class c) {

        Class[] interfaces = c.getInterfaces();

        for (int i = 0; i < interfaces.length; ++i) {

            if ((EJB_OBJECT_INTERFACE).equals(interfaces[i].getName()))
                return true;
        }

        return false;
    }


    /*
     * Finds javax.ejb.EJBHome interface from the class or its superclasses
     */
    public boolean hasEJBHomeInterface(Class c) {
        
        Class[] interfaces = c.getInterfaces();
        
        for (int i = 0; i < interfaces.length; ++i) {
            
            if ((EJB_HOME_INTERFACE).equals(interfaces[i].getName()))
                return true;
        }
        
        return false;
    }
    
    /*
     * Finds javax.ejb.SessionSynchronization interface from the class
     */
    public boolean hasSessionSynchronizationInterface(Class c) {

        Class[] interfaces = c.getInterfaces();

        for (int i = 0; i < interfaces.length; ++i) {

            if ((SESSION_SYNCHRONIZATION_INTERFACE).equals(interfaces[i].getName()))
                return true;
        }

        return false;
    }


    /*
     * Checks if a class has a default (no args) constructor
     */
    public boolean hasDefaultConstructor(Class c) {
        try {
        
            c.newInstance();
        }
        
        catch(Exception e) {
            return false;
        }
        
        return true;
    }


    /*
     * Checks of the class defines a finalize() method
     */
    public boolean hasFinalizer(Class c) {

        try {

            Method finalizer = c.getDeclaredMethod(FINALIZE_METHOD, new Class[0]);

        }

        catch (NoSuchMethodException e) {
            return false;
        }

        catch (SecurityException e) {
            System.err.println(e);
            // [TODO]   Can be thrown by the getDeclaredMethod() call if access is
            //          denied --> createVerifierWarningEvent

            return false;
        }

        return true;
    }

    /*
     * Searches for an instance of a public create method from the class
     */
    public boolean hasCreateMethod(Class c) {
        
        try {
            Method[] method = c.getMethods();
            
            for (int i = 0; i < method.length; ++i) {
                
                String name = method[i].getName();
                
                if (name.equals(CREATE_METHOD))
                    return true;
            }
        }
        
        catch (SecurityException e) {
            System.err.println(e);
            // [TODO]   Can be thrown by the getMethods() call if access is
            //          denied --> createVerifierWarningEvent
        }

        return false;
    }
            
    /*
     * Searches for an instance of a public ejbCreate method from the class
     */
    public boolean hasEJBCreateMethod(Class c) {

        try {
            Method[] method = c.getMethods();

            for (int i = 0; i < method.length; ++i) {

                String name = method[i].getName();

                if (name.equals(EJB_CREATE_METHOD))
                    if (!isStaticMember(method[i])
                            && !isFinalMember(method[i])
                            && hasVoidReturnType(method[i]))

                        return true;
            }
        }
        catch (SecurityException e) {
            System.err.println(e);
            // [TODO]   Can be thrown by the getMethods() call if access is
            //          denied --> createVerifierWarningEvent
        }

        return false;
    }

    
    /*
     * Searches the class or interface, and its superclass or superinterface
     * for a create() method that takes no arguments
     */
    public boolean hasDefaultCreateMethod(Class home) {
        
        try {
            Method[] method = home.getMethods();
            
            for (int i = 0; i < method.length; ++i) {
                
                String name = method[i].getName();
                
                if (name.equals(CREATE_METHOD)) {
                    Class[] params = method[i].getParameterTypes();
                    
                    if (params.length == 0) 
                        return true;
                }
            }
        }
        
        catch (SecurityException e) {
            System.err.println(e);
            // [TODO]   Can be thrown by the getMethods() call if access is
            //          denied --> createVerifierWarningEvent
        }

        return false;
    }
    
    public boolean hasRemoteReturnType(Session session, Method m) {
        
        if (m.getReturnType().getName().equals(session.getRemote()))
            return true;
            
        return false;
    }
    
    
    /*
     * Returns the default create method.
     */
    public Method getDefaultCreateMethod(Class c) {
        
        try {
            Method[] method = c.getMethods();
            
            for (int i = 0; i < method.length; ++i) {
                
                String name = method[i].getName();
                
                if (name.equals(CREATE_METHOD)) {
                    Class[] params = method[i].getParameterTypes();
                    
                    if (params.length == 0) 
                        return method[i];
                }
            }
        }
        
        catch (SecurityException e) {
            System.err.println(e);
            // [TODO]   Can be thrown by the getMethods() call if access is
            //          denied --> createVerifierWarningEvent
        }

        return null;
    }
    
    /*
     * Returns the ejbCreate(...) methods of a bean
     */
    public Iterator getEJBCreateMethods(Class c) {
        
        List ejbCreates = new LinkedList();

        try {
        
            Method[] method = c.getMethods();
        
            for (int i = 0; i < method.length; ++i) {
        
                if (method[i].getName().equals(EJB_CREATE_METHOD))
                    ejbCreates.add(method[i]);
            }
        }

        catch (SecurityException e) {
            System.err.println(e);
            // [TODO]   Can be thrown by the getMethods() call if access is
            //          denied --> createVerifierWarningEvent            
        }
        
        return ejbCreates.iterator();
    }

    /*
     * Returns all methods of a class in an iterator
     */
    public Iterator getMethods(Class c) {
        
        try {
            Method[] method = c.getMethods();
     
            return Arrays.asList(method).iterator();       
        }
        
        catch (SecurityException e) {
            System.err.println(e);
            
            return null;
        }
    }
            
    
    public boolean hasMoreThanOneCreateMethods(Class c) {
        
        int count = 0;
        
        try {
            Method[] method = c.getMethods();
            
            for (int i = 0; i < method.length; ++i) {
                
                String name = method[i].getName();
                
                if (name.equals(CREATE_METHOD)) {
                    ++count;
                }
            }
        }
        
        catch (SecurityException e) {
            System.err.println(e);
            // [TODO]   Can be thrown by the getMethods() call if access is
            //          denied --> createVerifierWarningEvent
        }

        return (count > 1);
    }
    
    public boolean hasMatchingMethodNames(Class a, Class b) {
        
        return true;
    }
    
    public boolean hasMatchingMethodArgs(Class a, Class b) {
        
        return true;
    }
    
    public boolean hasMatchingMethodExceptions(Class a, Class b) {
        
        return true;
    }
    
    
    /*
     * Ejb-jar DTD
     */
    public final static String DTD_EJB_CLASS         =
        "Deployment descriptor DTD: ejb-class";

    public final static String DTD_HOME              =
        "Deployment descriptor DTD: home";
        
    public final static String BEAN_MANAGED_TX       =
        "Bean";

    public final static String CONTAINER_MANAGED_TX  =
        "Container";

    public final static String STATEFUL_SESSION      =
        "Stateful";

    public final static String STATELESS_SESSION     =
        "Stateless";


        
    private final static String SESSION_BEAN_INTERFACE =
        "javax.ejb.SessionBean";

    private final static String SESSION_SYNCHRONIZATION_INTERFACE =
        "javax.ejb.SessionSynchronization";

    private final static String SERIALIZATION_INTERFACE =
        "java.io.Serializable";

    private final static String REMOTE_EXCEPTION      =
        "java.rmi.RemoteException";

    private final static String EJB_OBJECT_INTERFACE  =
        "javax.ejb.EJBObject";

    private final static String EJB_HOME_INTERFACE    =
        "javax.ejb.EJBHome";
        
        
     
    private final static String EJB_CREATE_METHOD     =
        "ejbCreate";
     
    private final static String CREATE_METHOD         =
        "create";

    private final static String FINALIZE_METHOD       =
        "finalize";
        
        
}


