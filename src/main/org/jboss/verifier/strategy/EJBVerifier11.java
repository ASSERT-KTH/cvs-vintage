package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.EJBVerifier11
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
 * $Id: EJBVerifier11.java,v 1.14 2000/08/26 20:14:14 juha Exp $
 */


// standard imports
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;

// non-standard class dependencies
import org.gjt.lindfors.pattern.StrategyContext;

import org.jboss.verifier.Section;
import org.jboss.verifier.event.VerificationEvent;

import org.jboss.verifier.factory.VerificationEventFactory;
import org.jboss.verifier.factory.DefaultEventFactory;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;


/**
 * Concrete implementation of the <code>VerificationStrategy</code> interface.
 * This class implements the verification of both session and entity beans for
 * Enterprise JavaBeans v1.1 specification.
 *
 * For more detailed documentation, refer to the
 * <a href="http://java.sun.com/products/ejb/docs.html">Enterprise JavaBeans v1.1, Final Release</a>
 *
 * @see     org.jboss.verifier.strategy.AbstractVerifier
 *
 * @author 	Juha Lindfors (jplindfo@helsinki.fi)
 * @version $Revision: 1.14 $
 * @since  	JDK 1.3
 */
public class EJBVerifier11 extends AbstractVerifier {

    private VerificationContext context      = null;
    private VerificationEventFactory factory = null;
    private ClassLoader classloader          = null;


    /*
     * Constructor
     *  
     * @param   context
     */
    public EJBVerifier11(VerificationContext context) {

        URL[] list = { context.getJarLocation() };

        ClassLoader parent = getClass().getClassLoader();
        URLClassLoader cl  = new URLClassLoader(list, parent);

        this.classloader   = cl;
        this.context       = context;

        this.factory       = new DefaultEventFactory();
    }




/*
 ***********************************************************************
 *
 *    IMPLEMENTS VERIFICATION STRATEGY INTERFACE
 *
 ***********************************************************************
 */



    public void checkSession(SessionMetaData session) {

        boolean beanVerified   = false;
        boolean homeVerified   = false;
        boolean remoteVerified = false;


        beanVerified   = verifySessionBean(session);
        homeVerified   = verifySessionHome(session);
        remoteVerified = verifySessionRemote(session);


        if (beanVerified && homeVerified && remoteVerified) {

            /*
             * Verification for this session bean done. Fire the event
             * to tell listeneres everything is ok.
             */
            fireBeanVerifiedEvent(session);
        }
    }


    public void checkEntity(EntityMetaData entity) {
        
        boolean pkVerified     = true;      // false;
        boolean beanVerified   = false;
        boolean homeVerified   = false;
        boolean remoteVerified = false;

        /*
         * [TODO] verify the contents of the ejb-jar.xml:
         *
         *  - prim key class is fully qualified class name
         */
         
        beanVerified   = verifyEntityBean(entity);
        homeVerified   = verifyEntityHome(entity);
        remoteVerified = verifyEntityRemote(entity);


        // will put this back later
        //pkVerified = verifyPrimaryKey(entity.getPrimaryKeyClass());

        if ( beanVerified && homeVerified && remoteVerified && pkVerified) {

            /*
             * Verification for this entity bean done. Fire the event
             * to tell listeneres everything is ok.
             */
            fireBeanVerifiedEvent(entity);
        }

    }

    public StrategyContext getContext() {
        return context;
    }


/*
 *****************************************************************************
 *
 *      VERIFY SESSION BEAN HOME INTERFACE
 *
 *****************************************************************************
 */

    private boolean verifySessionHome(SessionMetaData session) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String name = session.getHome();
        
        try {
            Class home = classloader.loadClass(name);

            /*
             * The home interface of a stateless session bean MUST have one
             * create() method that takes no arguments.
             *
             * The create() method MUST return the session bean's remote
             * interface.
             *
             * There CAN NOT be other create() methods in the home interface.
             *
             * Spec 6.8
             */
             if (session.isStateless()) {
                 
                 if (!hasDefaultCreateMethod(home)) {
                    fireSpecViolationEvent(session, new Section("6.8.a"));

                    status = false;
                 }
                 
                 if (!hasRemoteReturnType(session, getDefaultCreateMethod(home))) {
                     fireSpecViolationEvent(session, new Section("6.8.b"));;
                     
                     status = false;
                 }
                 
                 if (hasMoreThanOneCreateMethods(home)) {
                     fireSpecViolationEvent(session, new Section("6.8.c"));
                     
                     status = false;
                 }   
             }
             
            /*
             * The session bean's home interface MUST extend the
             * javax.ejb.EJBHome interface.
             *
             * Spec 6.10.6
             */
            if (!hasEJBHomeInterface(home)) {
                
                fireSpecViolationEvent(session, new Section("6.10.6.a"));
                
                status = false;
            }
            
            /*                
             * Method arguments defined in the home interface MUST be
             * of valid types for RMI/IIOP.
             *
             * Method return values defined in the home interface MUST
             * be of valid types for RMI/IIOP.
             *
             * Methods defined in the home interface MUST include
             * java.rmi.RemoteException in their throws clause.
             *
             * Spec 6.10.6
             */
            Iterator it = getMethods(home);
            
            while (it.hasNext()) {
                
                Method method = (Method)it.next();    
                
                if (!hasLegalRMIIIOPArguments(method)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.6.b"));
                    
                    status = false;
                }
                
                if (!hasLegalRMIIIOPReturnType(method)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.6.c"));
                    
                    status = false;
                }
                
                if (!throwsRemoteException(method)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.6.d"));
                    
                    status = false;
                }
            }

            /*
             * A session bean's home interface MUST define one or more
             * create(...) methods.
             *
             * Spec 6.10.6
             */
            if (!hasCreateMethod(home)) {
                
                fireSpecViolationEvent(session, new Section("6.10.6.e"));

                status = false;
            }

             
        }
        catch (ClassNotFoundException e) {

            /*
             * The bean provider MUST specify the fully-qualified name of the
             * enterprise bean's  home interface in the <home> element.
             *
             * Spec 16.2
             */
            fireSpecViolationEvent(session, new Section("16.2.c"));

            status = false;
        }
        
        return status;

    }

/*
 *************************************************************************
 *
 *      VERIFY SESSION BEAN REMOTE INTERFACE
 *
 *************************************************************************
 */    
    
    private boolean verifySessionRemote(SessionMetaData session) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String  name   = session.getRemote();


        try {
            Class remote = classloader.loadClass(name);

            /*
             * The remote interface MUST extend the javax.ejb.EJBObject
             * interface.
             *
             * Spec 6.10.5
             */
            if (!hasEJBObjectInterface(remote)) {
                
                fireSpecViolationEvent(session, new Section("6.10.5.a"));
    
                status = false;
            }
    
            /*                
             * Method arguments defined in the remote interface MUST be
             * of valid types for RMI/IIOP.
             *
             * Method return values defined in the remote interface MUST
             * be of valid types for RMI/IIOP.
             *
             * Methods defined in the remote interface MUST include
             * java.rmi.RemoteException in their throws clause.
             *
             * Spec 6.10.5
             */
            Iterator it = getMethods(remote);
            
            while (it.hasNext()) {
                
                Method method = (Method)it.next();    
                
                if (!hasLegalRMIIIOPArguments(method)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.5.b"));
                    
                    status = false;
                }
                
                if (!hasLegalRMIIIOPReturnType(method)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.5.c"));
                    
                    status = false;
                }
                
                if (!throwsRemoteException(method)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.5.d"));
                    
                    status = false;
                }
            }

            /*
             * For each method defined in the remote interface, there MUST be
             * a matching method in the session bean's class. The matching
             * method MUST have:
             *
             *  - the same name
             *  - the same number and types of arguments, and the same
             *    return type
             *  - All the exceptions defined in the throws clause of the 
             *    matching method of the session bean class must be defined
             *    in the throws clause of the method of the remote interface
             *
             * Spec 6.10.5
             */
            try {
                String beanName = session.getEjbClass();
                Class  bean     = classloader.loadClass(beanName);
                
                if (!hasMatchingMethodNames(remote, bean)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.5.e"));
                    
                    status = false;
                }
                
                if (!hasMatchingMethodArgs(remote, bean)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.5.f"));
                    
                    status = false;
                }
                
                if (!hasMatchingMethodExceptions(remote, bean)) {
                    
                    fireSpecViolationEvent(session, new Section("6.10.5.g"));
                    
                    status = false;
                }
            }
            
            catch (ClassNotFoundException ignored) {}
            
        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * enterprise bean's remote interface in the <remote> element.
             *
             * Spec 16.2
             */
            fireSpecViolationEvent(session, new Section("16.2.d"));

            status = false;
        }

        return status;
    }

/*
 *************************************************************************
 *
 *      VERIFY SESSION BEAN CLASS
 *
 *************************************************************************
 */    
    
    private boolean verifySessionBean(SessionMetaData session) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String  name   = session.getEjbClass();


        try {
            Class bean = classloader.loadClass(name);


            /*
             * A session bean MUST implement, directly or indirectly,
             * javax.ejb.SessionBean interface.
             *
             * Spec 6.5.1
             * Spec 6.10.2
             */
            if (!hasSessionBeanInterface(bean)) {

                fireSpecViolationEvent(session, new Section("6.5.1"));

                status = false;
            }


            /*
             * Only a stateful container-managed transaction demarcation
             * session bean MAY implement the SessionSynchronization interface.
             *
             * A stateless Session bean MUST NOT implement the
             * SessionSynchronization interface.
             *
             * Spec 6.5.3
             */
            if (hasSessionSynchronizationInterface(bean)) {

                if (session.isStateless()) {
                    fireSpecViolationEvent(session, new Section("6.5.3.a"));

                    status = false;
                }

                if (session.isBeanManagedTx()) {
                    fireSpecViolationEvent(session, new Section("6.5.3.b"));

                    status = false;
                }
            }
            

            /*
             * A session bean MUST implement AT LEAST one ejbCreate method.
             *
             * Spec 6.5.5
             */
            if (!hasEJBCreateMethod(bean)) {
                
                fireSpecViolationEvent(session, new Section("6.5.5"));

                status = false;
            }


            /*
             * A session with bean-managed transaction demarcation CANNOT
             * implement the SessionSynchronization interface.
             *
             * Spec 6.6.1 (table 2)
             */
            if (hasSessionSynchronizationInterface(bean) && session.isBeanManagedTx()) {

                fireSpecViolationEvent(session, new Section("6.6.1"));

                status = false;
            }

            /*
             * The session bean class MUST be defined as public.
             *
             * Spec 6.10.2
             */
            if (!isPublic(bean)) {

               fireSpecViolationEvent(session, new Section("6.10.2.a"));

               status = false;
            }

            /*
             * The session bean class MUST NOT be final.
             *
             * Spec 6.10.2
             */
            if (isFinal(bean)) {

                fireSpecViolationEvent(session, new Section("6.10.2.b"));

                status = false;
            }

            /*
             * The session bean class MUST NOT be abstract.
             *
             * Spec 6.10.2
             */
            if (isAbstract(bean)) {

                fireSpecViolationEvent(session, new Section("6.10.2.c"));

                status = false;
            }

            /*
             * The session bean class MUST have a public constructor that
             * takes no arguments.
             *
             * Spec 6.10.2
             */
            if (!hasDefaultConstructor(bean)) {

                fireSpecViolationEvent(session, new Section("6.10.2.d"));

                status = false;
            }

            /*
             * The session bean class MUST NOT define the finalize() method.
             *
             * Spec 6.10.2
             */
            if (hasFinalizer(bean)) {

                fireSpecViolationEvent(session, new Section("6.10.2.e"));

                status = false;
            }

            /*
             * The ejbCreate(...) method signatures MUST follow these rules:
             *
             *      - The method MUST be declared as public
             *      - The method MUST NOT be declared as final or static
             *      - The return type MUST be void
             *      - The method arguments MUST be legal types for RMI/IIOP
             *
             * Spec 6.10.3
             */
            if (hasEJBCreateMethod(bean)) {
                
                Iterator it = getEJBCreateMethods(bean);
                
                while (it.hasNext()) {
                    
                    Method ejbCreate = (Method)it.next();
                    
                    if (!isPublic(ejbCreate)) {
                        
                        fireSpecViolationEvent(session, new Section("6.10.3.a"));
                        status = false;
                    }
                    
                    if ( (isFinal(ejbCreate)) ||
                         (isStatic(ejbCreate)) ) {
                              
                        fireSpecViolationEvent(session, new Section("6.10.3.b"));
                        status = false;
                    }
                    
                    if (!hasVoidReturnType(ejbCreate)) {
                        
                        fireSpecViolationEvent(session, new Section("6.10.3.c"));
                        status = false;
                    }
                    
                    if (!hasLegalRMIIIOPArguments(ejbCreate)) {
                        
                        fireSpecViolationEvent(session, new Section("6.10.3.d"));
                        status = false;
                    }
                }
            }


        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * Java class that implements the enterprise bean's business
             * methods.
             *
             * Spec 16.2
             */
            fireSpecViolationEvent(session, new Section("16.2.b"));
            
            status = false;
        }

        return status;
    }


/*
 *************************************************************************
 *
 *      VERIFY ENTITY BEAN HOME INTERFACE
 *
 *************************************************************************
 */
 
    private boolean verifyEntityHome(EntityMetaData entity) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String name = entity.getHome();
        
        try {
            Class home = classloader.loadClass(name);


             
        }
        catch (ClassNotFoundException e) {

            /*
             * The bean provider MUST specify the fully-qualified name of the
             * enterprise bean's  home interface in the <home> element.
             *
             * Spec 16.2
             */
            fireSpecViolationEvent(entity, new Section("16.2.c"));

            status = false;
        }
        
        return status;

    }

    
/*
 *************************************************************************
 *  
 *      VERIFY ENTITY BEAN REMOTE INTERFACE
 *
 *************************************************************************
 */
    
    private boolean verifyEntityRemote(EntityMetaData entity) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String  name   = entity.getRemote();


        try {
            Class home = classloader.loadClass(name);


        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * enterprise bean's remote interface in the <remote> element.
             *
             * Spec 16.2
             */
            fireSpecViolationEvent(entity, new Section("16.2.d"));

            status = false;
        }

        return status;
    }

    

/*
 *************************************************************************
 *
 *      VERIFY ENTITY BEAN CLASS
 *
 *************************************************************************
 */
    
    private boolean verifyEntityBean(EntityMetaData entity) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String  name   = entity.getEjbClass();


        try {
            Class bean = classloader.loadClass(name);

            /*
             * The enterprise bean class MUST implement, directly or
             * indirectly, the javax.ejb.EntityBean interface.
             *
             * Spec 9.2.2
             */
            if (!hasEntityBeanInterface(bean)) {

                fireSpecViolationEvent(entity, new Section("9.2.2.a"));

                status = false;
            }
            
            /*
             * The entity bean class MUST be defined as public.
             *
             * Spec 9.2.2
             */
            if (!isPublic(bean))  {
                
                fireSpecViolationEvent(entity, new Section("9.2.2.b"));
            
                status = false;
            }
            
            /*
             * The entity bean class MUST NOT be defined as abstract.
             *
             * Spec 9.2.2
             */
            if (isAbstract(bean)) {
                     
                fireSpecViolationEvent(entity, new Section("9.2.2.c"));
                
                status = false;
            }
            
            /*
             * The entity bean class MUST NOT be defined as final.
             *
             * Spec 9.2.2
             */
            if (isFinal(bean)) {
            
                fireSpecViolationEvent(entity, new Section("9.2.2.d"));
                
                status = false;
            }
            
            /*
             * The entity bean class MUST define a public constructor that
             * takes no arguments
             *
             * Spec 9.2.2
             */
            if (!hasDefaultConstructor(bean)) {
            
                fireSpecViolationEvent(entity, new Section("9.2.2.e"));
                
                status = false;
            }
            
            /*
             * The entity bean class MUST NOT define the finalize() method.
             *
             * Spec 9.2.2
             */
            if (hasFinalizer(bean)) {
                 
                fireSpecViolationEvent(entity, new Section("9.2.2.f"));
                 
                status = false;
            }
            
            /*
             * The ejbCreate(...) method signatures MUST follow these rules:
             *
             *      - The method MUST be declared as public
             *      - The method MUST NOT be declared as final or static
             *      - The return type MUST be the entity bean's primary key type
             *      - The method arguments MUST be legal types for RMI/IIOP
             *      - The method return value type MUST be legal type for RMI/IIOP
             *
             * Spec 9.2.3
             */
            if (hasEJBCreateMethod(bean)) {
                
                Iterator it = getEJBCreateMethods(bean);
                
                while (it.hasNext()) {
                    
                    Method ejbCreate = (Method)it.next();
                    
                    if (!isPublic(ejbCreate)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.3.a"));
                        status = false;
                    }
                    
                    if ( (isFinal(ejbCreate)) ||
                         (isStatic(ejbCreate)) ) {
                              
                        fireSpecViolationEvent(entity, new Section("9.2.3.b"));
                        status = false;
                    }
                    
                    if (!hasPrimaryKeyReturnType(entity, ejbCreate)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.3.c"));
                        status = false;
                    }
                    
                    if (!hasLegalRMIIIOPArguments(ejbCreate)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.3.d"));
                        status = false;
                    }
                    
                    if (!hasLegalRMIIIOPReturnType(ejbCreate)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.3.e"));
                        status = false;
                    }
                }
            }
            
            /*
             * For each ejbCreate(...) method, the entity bean class MUST
             * define a matching ejbPostCreate(...) method. 
             *
             * The ejbPostCreate(...) method MUST follow these rules:
             *
             *   - the method MUST be declared as public
             *   - the method MUST NOT be declared as final or static
             *   - the return type MUST be void
             *   - the method arguments MUST be the same as the matching
             *     ejbCreate(...) method
             *
             * Spec 9.2.4
             */
            if (hasEJBCreateMethod(bean)) {
                
                Iterator it =  getEJBCreateMethods(bean);
                
                while (it.hasNext()) {
                    
                    Method ejbCreate = (Method)it.next();
                    
                    if (!hasMatchingEJBPostCreate(bean, ejbCreate)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.4.a"));
                        
                        status = false;
                    }
                    
                    if (hasMatchingEJBPostCreate(bean, ejbCreate)) {
                        
                        Method ejbPostCreate = getMatchingEJBPostCreate(bean, ejbCreate);
                        
                        if (!isPublic(ejbPostCreate)) {
                            
                            fireSpecViolationEvent(entity, new Section("9.2.4.b"));
                            
                            status = false;
                        }
                        
                        if (isStatic(ejbPostCreate)) {
                            
                            fireSpecViolationEvent(entity, new Section("9.2.4.c"));
                            
                            status = false;
                        }
                        
                        if (isFinal(ejbPostCreate)) {
                            
                            fireSpecViolationEvent(entity, new Section("9.2.4.d"));
                            
                            status = false;
                        }
                    }
                }
            }
            
            
            /*
             * Every entity bean MUST define the ejbFindByPrimaryKey method.
             *
             * The return type for the ejbFindByPrimaryKey method MUST be the
             * primary key type.
             *
             * The ejbFindByPrimaryKey method MUST be a single-object finder.
             *
             * Spec 9.2.5
             */
            if (entity.isBMP() && (!hasEJBFindByPrimaryKey(bean))) {
                /* Even though the spec states that all entities must have the
                 * ejbFindByPrimaryKey() implementation, we only check BMP.
                 * For CMP it is the responsibility of the container to
                 * provide the implementation. */
                 
                fireSpecViolationEvent(entity, new Section("9.2.5.a"));
                
                status = false;
            }
            
            if (hasEJBFindByPrimaryKey(bean)) {
                
                Method ejbFindByPrimaryKey = getEJBFindByPrimaryKey(bean);
                
                if (!hasPrimaryKeyReturnType(entity, ejbFindByPrimaryKey)) {
                    
                    fireSpecViolationEvent(entity, new Section("9.2.5.b"));
                    
                    status = false;
                }
                
                if (!isSingleObjectFinder(entity, ejbFindByPrimaryKey)) {
                    
                    fireSpecViolationEvent(entity, new Section("9.2.5.c"));
                    
                    status = false;
                }
            }
            
            /*
             * A finder method MUST be declared as public.
             *
             * A finder method MUST NOT be declared as static.
             *
             * A finder method MUST NOT be declared as final.
             *
             * The finder method argument types MUST be legal types
             * for RMI/IIOP
             *
             * The finder method return type MUST be either the entity bean's
             * primary key type, or java.lang.util.Enumeration interface or
             * java.lang.util.Collection interface.
             *
             * Spec 9.2.5
             */
            if (hasFinderMethod(bean)) {
                 
                Iterator it = getFinderMethods(bean);
                
                while (it.hasNext()) {
                    
                    Method finder = (Method)it.next();
                    
                    if (!isPublic(finder)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.5.d"));
                        
                        status = false;
                    }
                    
                    if (isFinal(finder)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.5.e"));
                        
                        status = false;
                    }
                    
                    if (isStatic(finder)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.5.f"));
                        
                        status = false;
                    }
                    
                    if (!hasLegalRMIIIOPArguments(finder)) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.5.g"));
                        
                        status = false;
                    }
                    
                    if (! (isSingleObjectFinder(entity, finder)
                        || isMultiObjectFinder(finder))) {
                        
                        fireSpecViolationEvent(entity, new Section("9.2.5.h"));
                        
                        status = false;
                    }
                } 
            }
            
            
            
            
        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * Java class that implements the enterprise bean's business
             * methods.
             *
             * Spec 16.2
             */
            fireSpecViolationEvent(entity, new Section("16.2.b"));
            
            status = false;
        }

        return status;
    }

    
    
    private void fireSpecViolationEvent(BeanMetaData bean, Section section) {

        VerificationEvent event = factory.createSpecViolationEvent(context, section);
        event.setName(bean.getEjbName());
        
        context.fireSpecViolation(event);
    }
    
    private void fireBeanVerifiedEvent(BeanMetaData bean) {
        
        VerificationEvent event = factory.createBeanVerifiedEvent(context);
        event.setName(bean.getEjbName());
        
        context.fireBeanChecked(event);
    }

    

    
    


    
    private boolean verifyPrimaryKey(String className) {
        boolean status = true;
        Class cls = null;

        try {
            cls = Class.forName(className);
        } catch(Exception e) {
            context.fireBeanChecked(new VerificationEvent(context, "Primary key class is not available."));
            return false;  // Can't do any other checks if the class is null!
        }

        if(!isPublic(cls)) {
            status = false;
            context.fireBeanChecked(new VerificationEvent(context, "Primary key class must be public (see section 9.4.7.2)."));
        }

        if(!isAllFieldsPublic(cls)) {
            status = false;
            context.fireBeanChecked(new VerificationEvent(context, "Primary key fields must all be public (see section 9.4.7.2)."));
        }

        if(!hasANonStaticField(cls)) {
            status = false;
            context.fireBeanChecked(new VerificationEvent(context, "Primary key must have at least one nonstatic field."));
        }

        Object one, two;
        try {
            one = cls.newInstance();
            two = cls.newInstance();
            if(!one.equals(two)) {
                status = false;
                context.fireBeanChecked(new VerificationEvent(context, "Primary key does not implement equals() correctly (see section 9.2.9)."));
            }
            if(one.hashCode() != two.hashCode()) {
                status = false;
                context.fireBeanChecked(new VerificationEvent(context, "Primary key does not implement hashCode() correctly (see section 9.2.9)."));
            }
        } catch(Exception e) {
            status = false;
        }

        return status;
    }


    private boolean isAllFieldsPublic(Class c) {
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

    private boolean hasANonStaticField(Class c) {
        try {
            Field list[] = c.getFields();
            for(int i=0; i<list.length; i++)
                if(!Modifier.isStatic(list[i].getModifiers()))
                    return true;
        } catch(Exception e) {
        }
        return false;
    }



    




}

