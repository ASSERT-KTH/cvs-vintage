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
 * $Id: AbstractVerifier.java,v 1.14 2000/11/05 19:02:36 juha Exp $
 */

// standard imports
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;

import java.net.URL;
import java.net.URLClassLoader;


// non-standard class dependencies
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.SessionMetaData;

import org.jboss.verifier.factory.VerificationEventFactory;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.Section;

import org.gjt.lindfors.pattern.StrategyContext;


/**
 * Abstract superclass for verifiers containing a bunch of useful methods.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     org.jboss.verifier.strategy.VerificationStrategy
 *
 * @author 	Juha Lindfors (jplindfo@helsinki.fi)
 * @author  Aaron Mulder  (ammulder@alumni.princeton.edu)
 *
 * @version $Revision: 1.14 $
 * @since  	JDK 1.3
 */
public abstract class AbstractVerifier implements VerificationStrategy {

    protected final static String EJB_OBJECT_INTERFACE  =
        "javax.ejb.EJBObject";

    protected final static String EJB_HOME_INTERFACE    =
        "javax.ejb.EJBHome";


    /**
     * The application classloader. This can be provided by the context directly
     * via {@link VerificationContext#getClassLoader} method, or constructed
     * by this object by creating a classloader to the URL returned by 
     * {@link VerificationContext#getJarLocation} method. <p>
     *
     * Initialized in the constructor.
     */
    protected ClassLoader classloader          = null;

    /**
     * Factory for generating the verifier events. <p>
     *
     * Initialized in the constructor.
     * 
     * @see org.jboss.verifier.factory.DefaultEventFactory
     */
    private VerificationEventFactory factory = null;

    /** 
     * Context is used for retrieving application level information, such
     * as the application meta data, location of the jar file, etc. <p>
     *
     * Initialized in the constructor.
     */
    private VerificationContext context      = null;

/*
 *************************************************************************
 *
 *      CONSTRUCTORS
 *
 *************************************************************************
 */

    public AbstractVerifier(VerificationContext      context, 
                            VerificationEventFactory factory) {
        
        this.factory     = factory;
        this.context     = context;
        this.classloader = context.getClassLoader();

        if (this.classloader == null) {
            URL[] list = { context.getJarLocation() };

            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            this.classloader   = new URLClassLoader(list, parent);
        }
        
    }


/*
 *************************************************************************
 *
 *      PUBLIC INSTANCE METHODS
 *
 *************************************************************************
 */
    
    public boolean hasLegalRMIIIOPArguments(Method method) {

        Class[] params = method.getParameterTypes();

        for (int i = 0; i < params.length; ++i)
            if (!isRMIIIOPType(params[i]))
                return false;

        return true;
    }

    public boolean hasLegalRMIIIOPReturnType(Method method) {
        return isRMIIIOPType(method.getReturnType());
    }


    /*
     * checks if the method includes java.rmi.RemoteException or its
     * subclass in its throws clause.
     */
    public boolean throwsRemoteException(Method method) {

        Class[] exception = method.getExceptionTypes();

        for (int i = 0; i < exception.length; ++i)
            if (java.rmi.RemoteException.class.isAssignableFrom(exception[i]))
                return true;

        return false;
    }

    /*
     * checks if the method includes java.ejb.CreateException in its
     * throws clause.
     */
    public boolean throwsCreateException(Method method) {

        Class[] exception = method.getExceptionTypes();

        for (int i = 0; i < exception.length; ++i)
            if (javax.ejb.CreateException.class.isAssignableFrom(exception[i]))
                return true;

        return false;
    }



    /*
     * checks if a class's member (method, constructor or field) has a 'static'
     * modifier.
     */
    public boolean isStatic(Member member) {
        return (Modifier.isStatic(member.getModifiers()));
    }

    /*
     * checks if the given class is declared as static (inner classes only)
     */
    public boolean isStatic(Class c) {
        return (Modifier.isStatic(c.getModifiers()));
    }

    /*
     * checks if a class's member (method, constructor or field) has a 'final'
     * modifier.
     */
    public boolean isFinal(Member member) {
        return (Modifier.isFinal(member.getModifiers()));
    }

    /*
     * checks if the given class is declared as final
     */
    public boolean isFinal(Class c) {
        return (Modifier.isFinal(c.getModifiers()));
    }

    /*
     * checks if a class's memeber (method, constructor or field) has a 'public'
     * modifier.
     */
    public boolean isPublic(Member member) {
        return (Modifier.isPublic(member.getModifiers()));
    }

    /*
     * checks if the given class is declared as public
     */
    public boolean isPublic(Class c) {
        return (Modifier.isPublic(c.getModifiers()));
    }

    /**
     * Checks whether all the fields in the class are declared as public.
     */
    public boolean isAllFieldsPublic(Class c) {
        try {
            Field list[] = c.getFields();
            for(int i=0; i<list.length; i++)
                if(!Modifier.isPublic(list[i].getModifiers()))
                    return false;
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    /*
     * checks if the given class is declared as abstract
     */
    public boolean isAbstract(Class c) {
        return (Modifier.isAbstract(c.getModifiers()));
    }

    /*
     * checks if finder returns the primary key type
     */
    public boolean isSingleObjectFinder(EntityMetaData entity, Method finder) {
        return hasPrimaryKeyReturnType(entity, finder);
    }

    /*
     * checks if finder method returns either Collection or Enumeration
     */
    public boolean isMultiObjectFinder(Method finder) {
        return (java.util.Collection.class.isAssignableFrom(finder.getReturnType())
             || java.util.Enumeration.class.isAssignableFrom(finder.getReturnType()));
    }

    /*
     *  checks the return type of method matches the bean's remote interface
     */
    public boolean hasRemoteReturnType(BeanMetaData bean, Method m) {
        return (m.getReturnType().getName().equals(bean.getRemote()));
    }

    /*
     * checks if a method has a void return type
     */
    public boolean hasVoidReturnType(Method method) {
        return (method.getReturnType() == Void.TYPE);
    }

    /*
     * Finds java.ejb.SessionBean interface from the class
     */
    public boolean hasSessionBeanInterface(Class c) {
        return javax.ejb.SessionBean.class.isAssignableFrom(c);
    }

    /*
     * Finds java.ejb.EntityBean interface from the class
     */
     public boolean hasEntityBeanInterface(Class c) {
         return javax.ejb.EntityBean.class.isAssignableFrom(c);
     }

    /*
     * Finds java.ejb.EJBObject interface from the class
     */
    public boolean hasEJBObjectInterface(Class c) {
        return javax.ejb.EJBObject.class.isAssignableFrom(c);
    }

    /*
     * Finds javax.ejb.EJBHome interface from the class or its superclasses
     */
    public boolean hasEJBHomeInterface(Class c) {
        return javax.ejb.EJBHome.class.isAssignableFrom(c);
    }

    /*
     * Finds javax.ejb.SessionSynchronization interface from the class
     */
    public boolean hasSessionSynchronizationInterface(Class c) {
        return javax.ejb.SessionSynchronization.class.isAssignableFrom(c);
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

        return true;
    }

    /*
     * check if a class has one or more finder methods
     */
    public boolean hasFinderMethod(Class c) {
        Method[] method = c.getMethods();

        for (int i = 0; i <  method.length; ++i) {

            String name = method[i].getName();

            if (name.startsWith("ejbFind"))
                return true;
        }

        return false;
    }

    public boolean isFinderMethod(Method m) {
        return (m.getName().startsWith("find"));
    }

    public boolean isCreateMethod(Method m) {
        return (m.getName().equals(CREATE_METHOD));
    }


    /**
     * Checks for at least one non-static field.
     */
    public boolean hasANonStaticField(Class c) {
        try {
            Field list[] = c.getFields();
            for(int i=0; i<list.length; i++)
                if(!Modifier.isStatic(list[i].getModifiers()))
                    return true;
        }
        catch(Exception ignored) {}

        return false;
    }

    /*
     * Searches for an instance of a public create method from the class
     */
    public boolean hasCreateMethod(Class c) {

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i) {

            String name = method[i].getName();

            if (name.equals(CREATE_METHOD))
                return true;
        }

        return false;
    }

    /*
     * Searches for an instance of a public ejbCreate method from the class
     */
    public boolean hasEJBCreateMethod(Class c, boolean isSession) {

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i) {

            String name = method[i].getName();

            if (name.equals(EJB_CREATE_METHOD))
                if (!isStatic(method[i])
                        && !isFinal(method[i])
                        && ((isSession && hasVoidReturnType(method[i]))
                            || (!isSession)
                           )
                   )

                    return true;
        }

        return false;
    }


    /*
     * Searches the class or interface, and its superclass or superinterface
     * for a create() method that takes no arguments
     */
    public boolean hasDefaultCreateMethod(Class home) {

        Method[] method = home.getMethods();

        for (int i = 0; i < method.length; ++i) {

            String name = method[i].getName();

            if (name.equals(CREATE_METHOD)) {
                Class[] params = method[i].getParameterTypes();

                if (params.length == 0)
                    return true;
            }
        }

        return false;
    }

    /*
     * checks if the class has an ejbFindByPrimaryKey method
     */
    public boolean hasEJBFindByPrimaryKey(Class c) {

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i) {

            String name = method[i].getName();

            if (name.equals(EJB_FIND_BY_PRIMARY_KEY))
                return true;
        }

        return false;
    }

    /*
     * checks the return type of method matches the entity's primary key class
     */
    public boolean hasPrimaryKeyReturnType(EntityMetaData entity, Method m) {
        return (m.getReturnType().getName().equals(entity.getPrimaryKeyClass()));
    }

    /*
     * Returns the default create method. Return null if not found.
     */
    public Method getDefaultCreateMethod(Class c) {

        Method method = null;

        try {
            method = c.getMethod(CREATE_METHOD, null);
        }
        catch (NoSuchMethodException ignored) {}

        return method;
    }

    /*
     * Returns the ejbFindByPrimaryKey method
     */
    public Method getEJBFindByPrimaryKey(Class c) {

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i) {

            String name = method[i].getName();

            if (name.equals(EJB_FIND_BY_PRIMARY_KEY))
                return method[i];
        }

        return null;
    }

    /*
     * returns the ejbFind<METHOD> methods of a bean
     */
    public Iterator getFinderMethods(Class c) {

        List finders = new LinkedList();

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i) {

            if (method[i].getName().startsWith("ejbFind"))
                finders.add(method[i]);
        }

        return finders.iterator();
    }


    /*
     * Returns the ejbCreate(...) methods of a bean
     */
    public Iterator getEJBCreateMethods(Class c) {

        List ejbCreates = new LinkedList();

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i)
            if (method[i].getName().equals(EJB_CREATE_METHOD))
                ejbCreates.add(method[i]);

        return ejbCreates.iterator();
    }

    public Iterator getCreateMethods(Class c) {

        List creates = new LinkedList();

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i)
            if (isCreateMethod(method[i]))
                creates.add(method[i]);

        return creates.iterator();
    }

    public boolean hasMoreThanOneCreateMethods(Class c) {

        int count = 0;

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i) {

            String name = method[i].getName();

            if (name.equals(CREATE_METHOD)) {
                ++count;
            }
        }

        return (count > 1);
    }


    public boolean hasMatchingExceptions(Method source, Method target) {

        // target must be a superset of source

        Class[] a = source.getExceptionTypes();
        Class[] b = target.getExceptionTypes();

        for (int i = 0; i < a.length; ++i) {

            boolean found = false;

            for (int j = 0; j < b.length; ++j)
                if (a[i] == b[j]) {
                    found = true;
                    break;
                }

            if (!found)
                return false;
        }

        return true;
    }

    public boolean hasMatchingMethod(Class bean, Method remote) {

        String methodName = remote.getName();

        try {
            bean.getMethod(methodName, remote.getParameterTypes());

            return true;
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    public boolean hasMatchingReturnType(Method a, Method b) {
        return (a.getReturnType() == b.getReturnType());
    }

    public boolean hasMatchingEJBPostCreate(Class bean, Method ejbCreate) {
        try {
            return (bean.getMethod(EJB_POST_CREATE_METHOD, ejbCreate.getParameterTypes()) != null);
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    public boolean hasMatchingEJBCreate(Class bean, Method create) {
        try {
            return (bean.getMethod(EJB_CREATE_METHOD, create.getParameterTypes()) != null);
        }
        catch (NoSuchMethodException e) {
            return false;
        }
    }

    public Method getMatchingEJBPostCreate(Class bean, Method ejbCreate) {

        try {
            return bean.getMethod(EJB_POST_CREATE_METHOD, ejbCreate.getParameterTypes());
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    public Method getMatchingEJBCreate(Class bean, Method create) {

        try {
            return bean.getMethod(EJB_CREATE_METHOD, create.getParameterTypes());
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

/*
 *************************************************************************
 *
 *      PROTECTED INSTANCE METHODS
 *
 *************************************************************************
 */
 
    protected void fireSpecViolationEvent(BeanMetaData bean, Section section) {
        fireSpecViolationEvent(bean, null /* method */, section);
    }
    
    protected void fireSpecViolationEvent(BeanMetaData bean, Method method,
                                          Section section) {

        VerificationEvent event = factory.createSpecViolationEvent(context, section);
        event.setName(bean.getEjbName());
        event.setMethod(method);
        
        context.fireSpecViolation(event);
    }

    protected void fireBeanVerifiedEvent(BeanMetaData bean) {

        VerificationEvent event = factory.createBeanVerifiedEvent(context);
        event.setName(bean.getEjbName());

        context.fireBeanChecked(event);
    }

    
/*
 *************************************************************************
 *
 *      IMPLEMENTS VERIFICATIONSTRATEGY INTERFACE
 *
 *************************************************************************
 */

    /**
     * Provides an empty default implementation for EJB 1.1 verifier (message
     * beans are for EJB 2.0 and greater only).
     *
     * @param beans  the message bean to verify
     */
    public void checkMessageBean(BeanMetaData bean) {}

    /**
     * Returns the context object reference for this strategy implementation.
     *
     * @return  the client object using this algorithm implementation
     */
    public StrategyContext getContext() {
        return context;
    }

    
/*
 *************************************************************************
 *
 *      PRIVATE INSTANCE METHODS
 *
 *************************************************************************
 */

    private boolean isRMIIIOPType(Class type) {

        /*
         *  Java Language to IDL Mapping
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
         */

         /*
         * Primitive types.
         *
         * Spec 28.2.2
         */
        if (type.isPrimitive())
            return true;

        /*
         * Conforming array.
         *
         * Spec 28.2.5
         */
        if (type.isArray())
            return isRMIIIOPType(type.getComponentType());

        /*
         * Conforming CORBA reference type
         *
         * Spec 28.2.7
         */
        if (org.omg.CORBA.Object.class.isAssignableFrom(type))
            return true;

        /*
         * Conforming IDL Entity type
         *
         * Spec 28.2.8
         */
        if (org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(type))
            return true;

        /*
         * Conforming remote interface.
         *
         * Spec 28.2.3
         */
        if (isRMIIDLRemoteInterface(type))
            return true;

        /*
         * Conforming exception.
         *
         * Spec 28.2.6
         */
        if (isRMIIDLExceptionType(type))
            return true;

        /*
         * Conforming value type.
         *
         * Spec 28.2.4
         */
        if (isRMIIDLValueType(type))
            return true;

        return false;
    }


    private boolean isRMIIDLRemoteInterface(Class type) {

        /*
         * If does not implement java.rmi.Remote, cannot be valid RMI-IDL
         * remote interface.
         */
        if (!java.rmi.Remote.class.isAssignableFrom(type))
            return false;
        
        Iterator methodIterator = Arrays.asList(type.getMethods()).iterator();

        while (methodIterator.hasNext()) {
            Method m = (Method)methodIterator.next();

            /*
             * All methods in the interface MUST throw
             * java.rmi.RemoteException or its subclass.
             *
             * Spec 28.2.3 (2)
             */
            if (!throwsRemoteException(m)) {
                return false;
            }

            /*
             * All checked exception classes used in method declarations
             * (other than java.rmi.RemoteException) MUST be conforming
             * RMI/IDL exception types.
             *
             * Spec 28.2.3 (4)
             */
            Iterator it = Arrays.asList(m.getExceptionTypes()).iterator();

            while (it.hasNext()) {
                Class exception = (Class)it.next();

                if (!isRMIIDLExceptionType(exception))
                    return false;
            }
        }

        /*
         * The constant values defined in the interface MUST be
         * compile-time types of RMI/IDL primitive types or String.
         *
         * Spec 28.2.3 (6)
         */
        Iterator fieldIterator = Arrays.asList(type.getFields()).iterator();

        while (fieldIterator.hasNext()) {

            Field f = (Field)fieldIterator.next();

            if (f.getType().isPrimitive())
                continue;

            if (f.getType().equals(java.lang.String.class))
                continue;

            return false;
        }

        return true;
    }


    private boolean isRMIIDLExceptionType(Class type) {

        /*
         * A conforming RMI/IDL Exception class MUST be a checked
         * exception class and MUST be a valid RMI/IDL value type.
         *
         * Spec 28.2.6
         */
        if (!Throwable.class.isAssignableFrom(type))
            return false;

        if (Error.class.isAssignableFrom(type))
            return false;

        if (RuntimeException.class.isAssignableFrom(type))
            return false;

        if (!isRMIIDLValueType(type))
            return false;

        return true;
    }

    private boolean isRMIIDLValueType(Class type) {

        /*
         * A value type MUST NOT either directly or indirectly implement the
         * java.rmi.Remote interface.
         *
         * Spec 28.2.4 (4)
         */
        if (java.rmi.Remote.class.isAssignableFrom(type))
            return false;


        /*
         * If class is a non-static inner class then its containing class must
         * also be a conforming RMI/IDL value type.
         *
         * Spec 2.8.4 (3)
         */
        if (type.getDeclaringClass() != null && isStatic(type))
            if (!isRMIIDLValueType(type.getDeclaringClass()))
                return false;

        return true;
    }


/*
 *************************************************************************
 *
 *      STRING CONSTANTS
 *
 *************************************************************************
 */

    /*
     * Ejb-jar DTD
     */
    public final static String BEAN_MANAGED_TX       =
        "Bean";

    public final static String CONTAINER_MANAGED_TX  =
        "Container";

    public final static String STATEFUL_SESSION      =
        "Stateful";

    public final static String STATELESS_SESSION     =
        "Stateless";


    /*
     * method names
     */
    private final static String EJB_FIND_BY_PRIMARY_KEY =
        "ejbFindByPrimaryKey";

    private final static String EJB_CREATE_METHOD     =
        "ejbCreate";

    private final static String EJB_POST_CREATE_METHOD =
        "ejbPostCreate";

    private final static String EJB_POST_METHOD       =
        "ejbCreate";

    private final static String CREATE_METHOD         =
        "create";

    private final static String FINALIZE_METHOD       =
        "finalize";

    private final static String REMOVE_METHOD         =
        "remove";

    private final static String GET_HOME_HANDLE_METHOD =
        "getHomeHandle";

    private final static String GET_EJB_METADATA_METHOD =
        "getEJBMetaData";


}


