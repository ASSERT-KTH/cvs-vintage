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
 * $Id: AbstractVerifier.java,v 1.2 2000/06/11 21:22:26 juha Exp $
 */

// standard imports
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


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
 * @version $Revision: 1.2 $
 * @since  	JDK 1.3
 */
public abstract class AbstractVerifier implements VerificationStrategy {

    /*
     * checks if a class's member (method, constructor or field) has a 'static'
     * modifier.
     */
    public boolean isStaticMember(Member member) {
        
        if (member.getModifiers() == Modifier.STATIC)
            return true;
            
        return false;
    }
    
    /*
     * checks if a class's member (method, constructor or field) has a 'final'
     * modifier.
     */
    public boolean isFinalMember(Member member) {
        
        if (member.getModifiers() == Modifier.FINAL)
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


    /*
     * checks if the given class is declared as public
     */
    public boolean isPublicClass(Class c) {

        if (c.getModifiers() == Modifier.PUBLIC)
            return true;

        return false;
    }


    /*
     * checks if the given class is declared as final
     */
    public boolean isFinalClass(Class c) {

        if (c.getModifiers() == Modifier.FINAL)
            return true;

        return false;
    }


    /*
     * checks if the given class is declared as abstract
     */
    public boolean isAbstractClass(Class c) {

        if (c.getModifiers() == Modifier.ABSTRACT)
            return true;

        return false;
    }

    /*
     * Finds java.ejb.SessionBean interface from the class
     */
    public boolean hasSessionBeanInterface(Class c) {

        Class[] interfaces = c.getInterfaces();

        for (int i = 0; i < interfaces.length; ++i) {

            if ((SESSIONBEAN_INTERFACE).equals(interfaces[i].getName()))
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

            if ( (SESSIONSYNCHRONIZATION_INTERFACE).equals(interfaces[i].getName()) )
                return true;
        }

        return false;
    }


    public boolean hasDefaultConstructor(Class c) {
        try {
            c.newInstance();
        } catch(Exception e) {
            return false;
        }
        return true;
    }


    public boolean hasFinalizer(Class c) {

        try {
            Method finalizer = c.getDeclaredMethod("finalize", new Class[0]);

            if (finalizer.getModifiers() != Modifier.PROTECTED)
                return false;
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
     * Searches for an instance of a public ejbCreate method from the class
     */
    public boolean hasEJBCreateMethod(Class c) {

        try {
            Method[] method = c.getMethods();

            for (int i = 0; i < method.length; ++i) {

                String name = method[i].getName();

                if (name.equals(EJB_CREATE_METHOD))
                    // check the requirements for ejbCreate methods (spec 6.10.3)
                    // check for public modifier done by getMethods() call
                    // (it only returns public member methods)
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

    
    public boolean hasDefaultCreateMethod(Class c) {
        
        // NOT YET IMPLEMENTED
        
        return true;
    }
    
    public boolean hasRemoteReturnType(Method m) {
        
        // NOT YET IMPLEMENTED
        
        return true;
    }
    
    public Method getDefaultCreateMethod(Class c) {
        
        // NOT YET IMPLEMENTED
        
        return null;
    }
    
    public boolean hasMoreThanOneCreateMethods(Class c) {
        
        // NOT YET IMPLEMENTED
        
        return false;
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


    private final static String SESSIONBEAN_INTERFACE =
        "javax.ejb.SessionBean";

    private final static String SESSIONSYNCHRONIZATION_INTERFACE =
        "javax.ejb.SessionSynchronization";

    private final static String EJB_CREATE_METHOD     =
        "ejbCreate";

}


