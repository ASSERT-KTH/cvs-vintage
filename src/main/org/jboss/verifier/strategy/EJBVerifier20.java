package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.EJBVerifier20
 * Copyright (C) 2000  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This package and its source code is available at www.jboss.org
 * $Id: EJBVerifier20.java,v 1.15 2002/04/12 03:38:32 jwalters Exp $
 */


// standard imports
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.lang.reflect.Field;


// non-standard class dependencies
import org.jboss.verifier.Section;
import org.jboss.verifier.factory.DefaultEventFactory;

import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;



/**
 * EJB 2.0 bean verifier.
 *
 * @author 	Juha Lindfors   (jplindfo@helsinki.fi)
 * @author  Jay Walters     (jwalters@computer.org)
 * @version $Revision: 1.15 $
 * @since  	JDK 1.3
 */
public class EJBVerifier20 extends AbstractVerifier {

    /*
     * Constructor
     */
    public EJBVerifier20(VerificationContext context) {
        super(context, new DefaultEventFactory());
    }

    
/*
 ***********************************************************************
 *
 *    IMPLEMENTS VERIFICATION STRATEGY INTERFACE
 *
 ***********************************************************************
 */
    
    public void checkSession(SessionMetaData session)
    {
        boolean beanVerified   = false;
        boolean remoteHomeVerified   = false;
        boolean remoteVerified = false;
        boolean localHomeVerified   = false;
        boolean localVerified = false;
        boolean localOrHomeExists = true;

        beanVerified   = verifySessionBean(session);
        remoteHomeVerified   = verifySessionHome(session);
        remoteVerified = verifySessionRemote(session);
        localHomeVerified   = verifySessionLocalHome(session);
        localVerified = verifySessionLocal(session);

        /*
         * The session bean MUST implement either a remote home and remote, or 
         * local home and local interface.  It MAY implement a remote home, remote,
         * local home or local interface.
         *
         * Spec 7.10.1
         */
        if (!(remoteHomeVerified && remoteVerified) &&
            !(localHomeVerified && localVerified)) {
            localOrHomeExists = false;
            fireSpecViolationEvent(session, new Section("7.10.1"));
        }

        if (beanVerified && localOrHomeExists) {
            /*
             * Verification for this session bean done. Fire the event
             * to tell listeners everything is ok.
             */
            fireBeanVerifiedEvent(session);
        }
    }            

    public void checkEntity(EntityMetaData entity)
    {
        boolean pkVerified     = false;
        boolean beanVerified   = false; 
        boolean remoteHomeVerified = false;
        boolean remoteVerified = false;
        boolean localHomeVerified  = false;
        boolean localVerified = false;
        boolean localOrHomeExists = true;

        remoteHomeVerified   = verifyEntityHome(entity);
        localHomeVerified   = verifyEntityLocalHome(entity);
        System.out.println("WARNING: EJBVerifier2.0 Entity Bean verification not complete");
        if (entity != null) return;

        if (entity.isCMP())
          beanVerified   = verifyCMPEntityBean(entity);
        else if (entity.isBMP())
          beanVerified   = verifyBMPEntityBean(entity);
        remoteVerified = verifyEntityRemote(entity);
        localVerified = verifyEntityLocal(entity);
        pkVerified     = verifyPrimaryKey(entity);

        /*
         * The entity bean MUST implement either a remote home and remote, or 
         * local home and local interface.  It MAY implement a remote home, remote,
         * local home or local interface.
         *
         * Spec 12.2.1
         */
        if (!(remoteHomeVerified && remoteVerified) &&
            !(localHomeVerified && localVerified)) {
            localOrHomeExists = false;
            fireSpecViolationEvent(entity, new Section("12.2.1"));
        }

        if ( beanVerified && localOrHomeExists && pkVerified) {
            /*
             * Verification for this entity bean done. Fire the event
             * to tell listeneres everything is ok.
             */
            fireBeanVerifiedEvent(entity);
        }
    }
        
    public void checkMessageBean(BeanMetaData bean)
    {
       System.out.println("WARNING: EJBVerifier2.0 Message Driven Bean verification not implemented");
    }

    public boolean isCreateMethod(Method m) 
    {
        return m.getName().startsWith(CREATE_METHOD);
    }

    public boolean isEjbCreateMethod(Method m) 
    {
        return m.getName().startsWith(EJB_CREATE_METHOD);
    }

	public boolean isEjbSelectMethod(Method m)
	{
		return m.getName().startsWith(EJB_SELECT_METHOD);
	}

    public Iterator getEjbSelectMethods(Class c) {

        List selects = new LinkedList();

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i)
            if (isEjbSelectMethod(method[i]))
                selects.add(method[i]);

        return selects.iterator();
    }

    public boolean isEjbHomeMethod(Method m) 
    {
        return m.getName().startsWith(EJB_HOME_METHOD);
    }

    /**
	 * Home methods are any method on the home interface which isn't a create
	 * or find method.
	 */
    public Iterator getHomeMethods(Class c) {

        List homes = new LinkedList();

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i)
            if (!isCreateMethod(method[i]) && !isFinderMethod(method[i]))
                homes.add(method[i]);

        return homes.iterator();
    }

    public Iterator getEjbHomeMethods(Class c) {

        List homes = new LinkedList();

        Method[] method = c.getMethods();

        for (int i = 0; i < method.length; ++i)
            if (isEjbHomeMethod(method[i]))
                homes.add(method[i]);

        return homes.iterator();
    }

/*
 *****************************************************************************
 *
 *      VERIFY SESSION BEAN HOME INTERFACE
 *
 *****************************************************************************
 */

    /**
     * Verifies the session bean remote home interface against the EJB 2.0
     * specification.
     *
     * @param   session     XML metadata of the session bean
     */
    private boolean verifySessionHome(SessionMetaData session) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String name = session.getHome();
        if (name == null)
           return false;

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
             * Spec 7.8
             */
             if (session.isStateless()) {

                 if (!hasDefaultCreateMethod(home)) {
                    fireSpecViolationEvent(session, new Section("7.10.6.d2"));
                    status = false;
                 }

                 else {
                     Method create = getDefaultCreateMethod(home);

                     if (hasMoreThanOneCreateMethods(home)) {
                         fireSpecViolationEvent(session, new Section("7.10.6.d2"));
                         status = false;
                     }
                 }
             }

            /*
             * The session bean's home interface MUST extend the
             * javax.ejb.EJBHome interface.
             *
             * Spec 7.10.6
             */
            if (!hasEJBHomeInterface(home)) {
                fireSpecViolationEvent(session, new Section("7.10.6.a"));
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
             * Spec 7.10.6
             */
            Iterator it = Arrays.asList(home.getMethods()).iterator();

            while (it.hasNext()) {

                Method method = (Method)it.next();

                if (!hasLegalRMIIIOPArguments(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.6.b1"));
                    status = false;
                }

                if (!hasLegalRMIIIOPReturnType(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.6.b2"));
                    status = false;
                }

                if (!throwsRemoteException(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.6.b3"));
                    status = false;
                }
            }

            /*
             * A session bean's home interface MUST define one or more
             * create(...) methods.
             *
             * Spec 7.10.6
             */
            if (!hasCreateMethod(home)) {
                fireSpecViolationEvent(session, new Section("7.10.6.d1"));
                status = false;
            }


            /*
             * Each create(...) method in the session bean's home interface MUST
             * have a matching ejbCreate(...) method in the session bean's class.
             *
             * Each create(...) method in the session bean's home interface MUST
             * have the same number and types of arguments to its matching
             * ejbCreate(...) method.
             *
             * The return type for a create(...) method MUST be the session
             * bean's remote interface type.
             *
             * All the exceptions defined in the throws clause of the matching
             * ejbCreate(...) method of the enterprise bean class MUST be
             * included in the throws clause of a matching create(...) method.
             *
             * The throws clause of a create(...) method MUST include the
             * javax.ejb.CreateException.
             *
             * Spec 7.10.6
             */
            Iterator createMethods = getCreateMethods(home);

            try {
                String beanClass   = session.getEjbClass();
                Class  bean        = classloader.loadClass(beanClass);

                while (createMethods.hasNext()) {

                    Method create = (Method)createMethods.next();

                    if (!hasMatchingEJBCreate(bean, create)) {
                        fireSpecViolationEvent(session, create, new Section("7.10.6.e"));
                        status = false;
                    }

                    if (!hasRemoteReturnType(session, create)) {
                        fireSpecViolationEvent(session, create, new Section("7.10.6.f"));
                        status = false;
                    }

                    if (hasMatchingEJBCreate(bean, create)) {
                        Method ejbCreate     = getMatchingEJBCreate(bean, create);
                        if (!hasMatchingExceptions(ejbCreate, create)) {
                            fireSpecViolationEvent(session, create, new Section("7.10.6.g"));
                        }
                    }

                    if (!throwsCreateException(create)) {
                        fireSpecViolationEvent(session, create, new Section("7.10.6.h"));
                        status = false;
                    }
                }
            }
            catch (ClassNotFoundException ignored) {}


        }
        catch (ClassNotFoundException e) {

            /*
             * The bean provider MUST specify the fully-qualified name of the
             * enterprise bean's  home interface in the <home> element.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(session, new Section("22.2"));
            status = false;
        }

        return status;

    }

    /**
     * Verifies the session bean local home interface against the EJB 2.0
     * specification.
     *
     * @param   session     XML metadata of the session bean
     */
    private boolean verifySessionLocalHome(SessionMetaData session) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String name = session.getLocalHome();
        if (name == null)
           return false;

        try {
            Class home = classloader.loadClass(name);

            /*
             * The local home interface of a stateless session bean MUST have one
             * create() method that takes no arguments.
             *
             * There CAN NOT be other create() methods in the home interface.
             *
             * Spec 7.8
             */
             if (session.isStateless()) {

                 if (!hasDefaultCreateMethod(home)) {
                    fireSpecViolationEvent(session, new Section("7.10.8.d2"));
                    status = false;
                 } else {
                     Method create = getDefaultCreateMethod(home);

                     if (hasMoreThanOneCreateMethods(home)) {
                         fireSpecViolationEvent(session, new Section("7.10.8.d2"));
                         status = false;
                     }
                 }
             }

            /*
             * The session bean's home interface MUST extend the
             * javax.ejb.EJBLocalHome interface.
             *
             * Spec 7.10.8
             */
            if (!hasEJBLocalHomeInterface(home)) {
                fireSpecViolationEvent(session, new Section("7.10.8.a"));
                status = false;
            }

            /*
             * Methods defined in the local home interface MUST NOT include
             * java.rmi.RemoteException in their throws clause.
             *
             * Spec 7.10.8
             */
            Iterator it = Arrays.asList(home.getMethods()).iterator();

            while (it.hasNext()) {

                Method method = (Method)it.next();

                if (throwsRemoteException(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.8.b"));
                    status = false;
                }
            }

            /*
             * A session bean's home interface MUST define one or more
             * create(...) methods.
             *
             * Spec 7.10.8
             */
            if (!hasCreateMethod(home)) {
                fireSpecViolationEvent(session, new Section("7.10.8.d1"));
                status = false;
            }


            /*
             * Each create(...) method in the session bean's local home interface
             * MUST have a matching ejbCreate(...) method in the session bean's
             * class.
             *
             * Each create(...) method in the session bean's home interface MUST
             * have the same number and types of arguments to its matching
             * ejbCreate(...) method.
             *
             * The return type for a create(...) method MUST be the session
             * bean's local interface type.
             *
             * All the exceptions defined in the throws clause of the matching
             * ejbCreate(...) method of the enterprise bean class MUST be
             * included in the throws clause of a matching create(...) method.
             *
             * The throws clause of a create(...) method MUST include the
             * javax.ejb.CreateException.
             *
             * Spec 7.10.8
             */
            Iterator createMethods = getCreateMethods(home);

            try {
                String beanClass   = session.getEjbClass();
                Class  bean        = classloader.loadClass(beanClass);

                while (createMethods.hasNext()) {

                    Method create = (Method)createMethods.next();

                    if (!hasMatchingEJBCreate(bean, create)) {
                        fireSpecViolationEvent(session, create, new Section("7.10.8.e"));
                        status = false;
                    }

                    if (!hasLocalReturnType(session, create)) {
                        fireSpecViolationEvent(session, create, new Section("7.10.8.f"));
                        status = false;
                    }

                    if (hasMatchingEJBCreate(bean, create)) {
                        Method ejbCreate     = getMatchingEJBCreate(bean, create);
                        if (!hasMatchingExceptions(ejbCreate, create)) {
                            fireSpecViolationEvent(session, create, new Section("7.10.6.g"));
                        }
                    }

                    if (!throwsCreateException(create)) {
                        fireSpecViolationEvent(session, create, new Section("7.10.6.h"));
                        status = false;
                    }
                }
            }
            catch (ClassNotFoundException ignored) {}


        }
        catch (ClassNotFoundException e) {

            /*
             * The bean provider MUST specify the fully-qualified name of the
             * enterprise bean's  home interface in the <home> element.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(session, new Section("22.2"));
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
        if (name == null)
           return false;


        try {
            Class remote = classloader.loadClass(name);

            /*
             * The remote interface MUST extend the javax.ejb.EJBObject
             * interface.
             *
             * Spec 7.10.5
             */
            if (!hasEJBObjectInterface(remote)) {
                fireSpecViolationEvent(session, new Section("7.10.5.a"));
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
             * Spec 7.10.5
             */
            Iterator it = Arrays.asList(remote.getMethods()).iterator();

            while (it.hasNext()) {

                Method method = (Method)it.next();

                if (!hasLegalRMIIIOPArguments(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.5.b1"));
                    status = false;
                }

                if (!hasLegalRMIIIOPReturnType(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.5.b2"));
                    status = false;
                }

                if (!throwsRemoteException(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.5.b3"));
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
             * Spec 7.10.5
             */
            String beanName   = session.getEjbClass();
            Class  bean       = classloader.loadClass(beanName);

            Iterator iterator = Arrays.asList(remote.getDeclaredMethods()).iterator();

            while (iterator.hasNext()) {

                Method remoteMethod  = (Method)iterator.next();

                if (!hasMatchingMethod(bean, remoteMethod)) {
                    fireSpecViolationEvent(session, remoteMethod, new Section("7.10.5.d1"));

                    status = false;
                }

                if (hasMatchingMethod(bean, remoteMethod)) {
                    try {
                        Method beanMethod = bean.getMethod(
                                remoteMethod.getName(), remoteMethod.getParameterTypes());

                        if (!hasMatchingReturnType(remoteMethod, beanMethod)) {
                            fireSpecViolationEvent(session, remoteMethod, new Section("7.10.5.d2"));

                            status = false;
                        }

                        if (!hasMatchingExceptions(beanMethod, remoteMethod)) {

                            fireSpecViolationEvent(session, remoteMethod, new Section("7.10.5.d3"));
                            status = false;
                        }
                    } catch (NoSuchMethodException ignored) {}
                }
            }

        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * enterprise bean's remote interface in the <remote> element.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(session, new Section("22.2"));

            status = false;
        }

        return status;
    }

/*
 *************************************************************************
 *
 *      VERIFY SESSION BEAN LOCAL INTERFACE
 *
 *************************************************************************
 */

    private boolean verifySessionLocal(SessionMetaData session) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String  name   = session.getLocal();
        if (name == null)
           return false;

        try {
            Class local = classloader.loadClass(name);

            /*
             * The local interface MUST extend the javax.ejb.EJBLocalObject
             * interface.
             *
             * Spec 7.10.7
             */
            if (!hasEJBLocalObjectInterface(local)) {
                fireSpecViolationEvent(session, new Section("7.10.7.a"));
                status = false;
            }

            /*
             * Methods defined in the local interface MUST NOT include
             * java.rmi.RemoteException in their throws clause.
             *
             * Spec 7.10.7
             */
            Iterator it = Arrays.asList(local.getMethods()).iterator();

            while (it.hasNext()) {
                Method method = (Method)it.next();
                if (throwsRemoteException(method)) {
                    fireSpecViolationEvent(session, method, new Section("7.10.7.b"));
                    status = false;
                }
            }

            /*
             * For each method defined in the local interface, there MUST be
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
             * Spec 7.10.7
             */
            String beanName   = session.getEjbClass();
            Class  bean       = classloader.loadClass(beanName);

            Iterator iterator = Arrays.asList(local.getDeclaredMethods()).iterator();

            while (iterator.hasNext()) {
                Method localMethod  = (Method)iterator.next();

                if (!hasMatchingMethod(bean, localMethod)) {
                    fireSpecViolationEvent(session, localMethod, new Section("7.10.7.d1"));
                    status = false;
                }

                if (hasMatchingMethod(bean, localMethod)) {
                    try {
                        Method beanMethod = bean.getMethod(
                                localMethod.getName(), localMethod.getParameterTypes());

                        if (!hasMatchingReturnType(localMethod, beanMethod)) {
                            fireSpecViolationEvent(session, localMethod, new Section("7.10.7.d2"));

                            status = false;
                        }

                        if (!hasMatchingExceptions(beanMethod, localMethod)) {

                            fireSpecViolationEvent(session, localMethod, new Section("7.10.7.d3"));
                            status = false;
                        }
                    } catch (NoSuchMethodException ignored) {}
                }
            }

        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * enterprise bean's local interface in the <local> element.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(session, new Section("22.2"));

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
             * Spec 7.10.2
             */
            if (!hasSessionBeanInterface(bean)) {
                fireSpecViolationEvent(session, new Section("7.10.2.a"));
                status = false;
            }


            /*
             * Only a stateful container-managed transaction demarcation
             * session bean MAY implement the SessionSynchronization interface.
             *
             * A stateless Session bean MUST NOT implement the
             * SessionSynchronization interface.
             *
             * Spec 7.5.3
             */
            if (hasSessionSynchronizationInterface(bean)) {

                if (session.isStateless()) {
                    fireSpecViolationEvent(session, new Section("7.5.3.a"));
                    status = false;
                }

                if (session.isBeanManagedTx()) {
                    fireSpecViolationEvent(session, new Section("7.5.3.b"));
                    status = false;
                }
            }


            /*
             * A session bean MUST implement AT LEAST one ejbCreate method.
             *
             * Spec 7.10.3
             */
            if (!hasEJBCreateMethod(bean, true)) {
                fireSpecViolationEvent(session, new Section("7.10.3"));
                status = false;
            }


            /*
             * A session with bean-managed transaction demarcation CANNOT
             * implement the SessionSynchronization interface.
             *
             * Spec 7.6.1 (table 2)
             */
            if (hasSessionSynchronizationInterface(bean) && session.isBeanManagedTx()) {
                fireSpecViolationEvent(session, new Section("7.6.1"));
                status = false;
            }

            /*
             * The session bean class MUST be defined as public.
             *
             * Spec 7.10.2
             */
            if (!isPublic(bean)) {
               fireSpecViolationEvent(session, new Section("7.10.2.b1"));
               status = false;
            }

            /*
             * The session bean class MUST NOT be final.
             *
             * Spec 7.10.2
             */
            if (isFinal(bean)) {
                fireSpecViolationEvent(session, new Section("7.10.2.b2"));
                status = false;
            }

            /*
             * The session bean class MUST NOT be abstract.
             *
             * Spec 7.10.2
             */
            if (isAbstract(bean)) {
                fireSpecViolationEvent(session, new Section("7.10.2.b3"));
                status = false;
            }

            /*
             * The session bean class MUST have a public constructor that
             * takes no arguments.
             *
             * Spec 7.10.2
             */
            if (!hasDefaultConstructor(bean)) {
                fireSpecViolationEvent(session, new Section("7.10.2.c"));
                status = false;
            }

            /*
             * The session bean class MUST NOT define the finalize() method.
             *
             * Spec 7.10.2
             */
            if (hasFinalizer(bean)) {
                fireSpecViolationEvent(session, new Section("7.10.2.d"));
                status = false;
            }

            /*
             * The ejbCreate(...) method signatures MUST follow these rules:
             *
             *      - The method name MUST have ejbCreate as its prefix
             *      - The method MUST be declared as public
             *      - The method MUST NOT be declared as final or static
             *      - The return type MUST be void
             *      - The method arguments MUST be legal types for RMI/IIOP
             *      - The method SHOULD not throw a java.rmi.RemoteException
             *        (NOTE we don't test for this as it's not a MUST)
             *
             * Spec 7.10.3
             */
            if (hasEJBCreateMethod(bean, true)) {

                Iterator it = getEJBCreateMethods(bean);

                while (it.hasNext()) {

                    Method ejbCreate = (Method)it.next();

                    if (!isPublic(ejbCreate)) {

                        fireSpecViolationEvent(session, ejbCreate, new Section("7.10.3.b"));
                        status = false;
                    }

                    if ( (isFinal(ejbCreate)) ||
                         (isStatic(ejbCreate)) ) {

                        fireSpecViolationEvent(session, ejbCreate, new Section("7.10.3.c"));
                        status = false;
                    }

                    if (!hasVoidReturnType(ejbCreate)) {

                        fireSpecViolationEvent(session, ejbCreate, new Section("7.10.3.d"));
                        status = false;
                    }

                    if (!hasLegalRMIIIOPArguments(ejbCreate)) {

                        fireSpecViolationEvent(session, ejbCreate, new Section("7.10.3.e"));
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
             * Spec 22.2
             */
            fireSpecViolationEvent(session, new Section("22.2.b"));

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
        if (name == null)
           return false;

        try {
            Class home = classloader.loadClass(name);

            /*
             * Entity bean's home interface MUST extend the javax.ejb.EJBHome
             * interface.
             *
             * Spec 12.2.9
             */
            if (!hasEJBHomeInterface(home)) {
                fireSpecViolationEvent(entity, new Section("12.2.9.a"));
                status = false;
            }

            /*
             * The methods defined in the entity bean's home interface MUST
             * have valid RMI-IIOP argument types.
             *
             * The methods defined in the entity bean's home interface MUST
             * have valid RMI-IIOP return types.
             *
             * The methods defined in the entity bean's home interface MUST
             * have java.rmi.RemoteException in their throws clause.
             *
             * Spec 12.2.9
             */
            Iterator methods = Arrays.asList(home.getMethods()).iterator();

            while (methods.hasNext()) {

                Method method = (Method)methods.next();

                if (!hasLegalRMIIIOPArguments(method)) {
                    fireSpecViolationEvent(entity, method, new Section("12.2.9.b1"));
                    status = false;
                }

                if (!hasLegalRMIIIOPReturnType(method)) {
                    fireSpecViolationEvent(entity, method, new Section("12.2.9.b2"));
                    status = false;
                }

                if (!throwsRemoteException(method)) {
                    fireSpecViolationEvent(entity, method, new Section("12.2.9.b3"));
                    status = false;
                }
            }

            /*
             * Each method defined in the entity bean's home interface must be
             * one of the following:
             *
             *    - a create method
             *    - a finder method
             *    - a home method
             *
             * Spec 12.2.9
             */

            String beanClass   = entity.getEjbClass();
            Class  bean        = classloader.loadClass(beanClass);

            methods = Arrays.asList(home.getMethods()).iterator();

            while (methods.hasNext()) {

                Method method = (Method)methods.next();

                // Do not check the methods of the javax.ejb.EJBHome interface
                if (method.getDeclaringClass().getName().equals(EJB_HOME_INTERFACE))
                    continue;

                if (isCreateMethod(method)) {
                    /*
                     * Each create(...) method in the entity bean's home interface MUST
                     * have a matching ejbCreate(...) method in the entity bean's class.
                     *
                     * Each create(...) method in the entity bean's home interface MUST
                     * have the same number and types of arguments to its matching
                     * ejbCreate(...) method.
                     *
                     * The return type for a create(...) method MUST be the entity
                     * bean's remote interface type.
                     *
                     * All the exceptions defined in the throws clause of the matching
                     * ejbCreate(...) and ejbPostCreate(...) methods of the enterprise
                     * bean class MUST be included in the throws clause of a matching
                     * create(...) method.
                     *
                     * The throws clause of a create(...) method MUST include the
                     * javax.ejb.CreateException.
                     *
                     * Spec 12.2.9
                     */

                    if (!hasMatchingEJBCreate(bean, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.d"));
                        status = false;
                    }

                    if (!hasRemoteReturnType(entity, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.e"));
                        status = false;
                    }

                    if (hasMatchingEJBCreate(bean, method)     &&
                        hasMatchingEJBPostCreate(bean, method)) {

                        Method ejbCreate     = getMatchingEJBCreate(bean, method);
                        Method ejbPostCreate = getMatchingEJBPostCreate(bean, method);

                        if ( !(hasMatchingExceptions(ejbCreate, method)     &&
                               hasMatchingExceptions(ejbPostCreate, method)) ) {
                            fireSpecViolationEvent(entity, method, new Section("12.2.9.f"));
                        }
                    }

                    if (!throwsCreateException(method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.g"));
                        status = false;
                    }
				} else if (isFinderMethod(method)) {
                    /*
                     * Each finder method MUST match one of the ejbFind<METHOD> methods
                     * defined in the entity bean class.
                     *
                     * The matching ejbFind<METHOD> method MUST have the same number and
                     * types of arguments.
                     *
                     * The return type for a find<METHOD> method MUST be the entity
                     * bean's remote interface type (single-object finder) or a
                     * collection thereof (for a multi-object finder).
                     *
                     * All the exceptions defined in the throws clause of an ejbFind
                     * method of the entity bean class MUST be included in the throws
                     * clause of the matching find method of the home interface.
                     *
                     * The throws clause of a finder method MUST include the
                     * javax.ejb.FinderException.
                     *
                     * Spec 12.2.9
                     */

                    if ((entity.isBMP()) && (!hasMatchingEJBFind(bean, method))) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.h"));
                        status = false;
                    }

                    if (entity.isBMP() &&
                        !(hasRemoteReturnType(entity, method) || isMultiObjectFinder(method))) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.j"));
                        status = false;
                    }

                    if ((entity.isBMP()) && (hasMatchingEJBFind(bean, method))) {
                        Method ejbFind  = getMatchingEJBFind(bean, method);
                        if ( !(hasMatchingExceptions(ejbFind, method))) {
                            fireSpecViolationEvent(entity, method, new Section("12.2.9.k"));
                            status = false;
                        }
                    }

                    if (!throwsFinderException(method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.l"));
                        status = false;
                    }
				} else {
                    /*
                     * Each home method MUST match a method defined in the entity bean
                     * class.
                     *
                     * The matching ejbHome<METHOD> method MUST have the same number and
                     * types of arguments, and a matching return type.
                     *
                     * Spec 12.2.9
                     */

                    if (!hasMatchingEJBHome(bean, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.m"));
                        status = false;
                    }

                    /**
					 * Not sure if this should be here or not.
					 if (!hasRemoteReturnType(entity, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.9.n"));
                        status = false;
                    }
					*/
                }
            }
        }
        catch (ClassNotFoundException e) {

            /*
             * The bean provider MUST specify the fully-qualified name of the
             * enterprise bean's  home interface in the <home> element.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(entity, new Section("22.2"));
            status = false;
        }

        return status;
    }


/*
 *************************************************************************
 *
 *      VERIFY ENTITY BEAN LOCAL HOME INTERFACE
 *
 *************************************************************************
 */

    private boolean verifyEntityLocalHome(EntityMetaData entity) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String name = entity.getLocalHome();
        if (name == null)
           return false;

        try {
            Class home = classloader.loadClass(name);
            String beanClass   = entity.getEjbClass();
            Class  bean        = classloader.loadClass(beanClass);

            /*
             * Entity bean's local home interface MUST extend the
             * javax.ejb.EJBLocalHome interface.
             *
             * Spec 12.2.11
             */
            if (!hasEJBLocalHomeInterface(home)) {
                fireSpecViolationEvent(entity, new Section("12.2.11.a"));
                status = false;
            }

            /*
             * The methods defined in the entity bean's home interface MUST NOT
             * have java.rmi.RemoteException in their throws clause.
             *
             * Spec 12.2.11
             */
            Iterator homeMethods = Arrays.asList(home.getMethods()).iterator();

            while (homeMethods.hasNext()) {

                Method method = (Method)homeMethods.next();

                if (throwsRemoteException(method)) {
                    fireSpecViolationEvent(entity, method, new Section("12.2.11.b"));
                    status = false;
                }
            }

            /*
             * Each method defined in the entity bean's local home interface must be
             * one of the following:
             *
             *    - a create method
             *    - a finder method
             *    - a home method
             *
             * Spec 12.2.11
             */

            homeMethods = Arrays.asList(home.getMethods()).iterator();

            while (homeMethods.hasNext()) {

                Method method = (Method)homeMethods.next();

                // Do not check the methods of the javax.ejb.EJBLocalHome interface
                if (method.getDeclaringClass().getName().equals(EJB_LOCAL_HOME_INTERFACE))
                    continue;

				if (isCreateMethod(method)) {
                    /*
                     * Each create(...) method in the entity bean's local home interface
                     * MUST have a matching ejbCreate(...) method in the entity bean's class.
                     *
                     * Each create(...) method in the entity bean's local home interface
                     * MUST have the same number and types of arguments to its matching
                     * ejbCreate(...) method.
                     *
                     * The return type for a create(...) method MUST be the entity
                     * bean's local interface type.
                     *
                     * All the exceptions defined in the throws clause of the matching
                     * ejbCreate(...) and ejbPostCreate(...) methods of the enterprise
                     * bean class MUST be included in the throws clause of a matching
                     * create(...) method.
                     *
                     * The throws clause of a create(...) method MUST include the
                     * javax.ejb.CreateException.
                     *
                     * Spec 12.2.11
                     */

                    if (!hasMatchingEJBCreate(bean, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.11.e"));
                        status = false;
                    }

                    if (!hasLocalReturnType(entity, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.11.f"));
                        status = false;
                    }

                    if (hasMatchingEJBCreate(bean, method)     &&
                        hasMatchingEJBPostCreate(bean, method)) {

                        Method ejbCreate     = getMatchingEJBCreate(bean, method);
                        Method ejbPostCreate = getMatchingEJBPostCreate(bean, method);

                        if ( !(hasMatchingExceptions(ejbCreate, method)     &&
                               hasMatchingExceptions(ejbPostCreate, method)) ) {

                            fireSpecViolationEvent(entity, method, new Section("12.2.11.g"));
                        }
                    }

                    if (!throwsCreateException(method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.11.h"));
                        status = false;
                    }
				} else if (isFinderMethod(method)) {
                    /*
                     * Each finder method MUST match one of the ejbFind<METHOD> methods
                     * defined in the entity bean class.
                     *
                     * The matching ejbFind<METHOD> method MUST have the same number and
                     * types of arguments.
                     *
                     * The return type for a find<METHOD> method MUST be the entity
                     * bean's local interface type (single-object finder) or a
                     * collection thereof (for a multi-object finder).
                     *
                     * All the exceptions defined in the throws clause of an ejbFind
                     * method of the entity bean class MUST be included in the throws
                     * clause of the matching find method of the home interface.
                     *
                     * The throws clause of a finder method MUST include the
                     * javax.ejb.FinderException.
                     *
                     * Spec 12.2.11
                     */

                    if (!(hasLocalReturnType(entity, method) ||
                        isMultiObjectFinder(method))) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.11.j"));
                        status = false;
                    }

                    if (!throwsFinderException(method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.11.m"));
                        status = false;
                    }

                    if (entity.isCMP() && hasMatchingEJBFind(bean, method)) {
                        fireSpecViolationEvent(entity, method, new Section("10.6.2.h"));
                        status = false;
                    } else if (entity.isBMP()) {
                        if (!hasMatchingEJBFind(bean, method)) {
                            fireSpecViolationEvent(entity, method, new Section("12.2.11.i"));
                            status = false;
                        } else {
                            Method ejbFind  = getMatchingEJBFind(bean, method);

                            if ( !(hasMatchingExceptions(ejbFind, method)))
                                fireSpecViolationEvent(entity, method, new Section("12.2.11.l"));
                        }
                    }
				} else {
                    /*
                     * Each home method MUST match a method defined in the entity bean
                     * class.
                     *
                     * The matching ejbHome<METHOD> method MUST have the same number and
                     * types of arguments, and a matching return type.
                     *
                     * Spec 12.2.9
                     */

                    if (!hasMatchingEJBHome(bean, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.11.m"));
                        status = false;
                    }

                    /**
                     * Only check for remote return type if the bean actually has a
                     * remote interface.
                     */
                    if (entity.getRemote() != null &&
                        hasRemoteReturnType(entity, method)) {
                        fireSpecViolationEvent(entity, method, new Section("12.2.11.n"));
                        status = false;
                    }
				}
			}
		}
        catch (ClassNotFoundException e) {

            /*
             * The bean provider MUST specify the fully-qualified name of the
             * enterprise bean's  home interface in the <home> element.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(entity, new Section("22.2"));

            status = false;
        }

        return status;
    }


/*
 *************************************************************************
 *
 *      VERIFY ENTITY BEAN LOCAL INTERFACE
 *
 *************************************************************************
 */

    private boolean verifyEntityLocal(EntityMetaData entity) {

        /*
         * Indicates whether we issued warnings or not during verification.
         * This boolean is returned to the caller.
         */
        boolean status = true;

        String  name   = entity.getLocal();
        if (name == null)
           return false;


        try {
            Class local = classloader.loadClass(name);

            /*
             * Entity bean's local interface MUST extend
             * the javax.ejb.EJBLocalObject interface.
             *
             * Spec 9.2.7
             */
            if (!hasEJBLocalObjectInterface(local)) {
                fireSpecViolationEvent(entity, new Section("9.2.7.a"));
                status = false;
            }

            /*
             * The methods defined in the entity bean's local interface MUST NOT
             * have java.rmi.RemoteException in their throws clause.
             *
             * Spec 9.2.7
             */
            Iterator localMethods = Arrays.asList(local.getMethods()).iterator();

            while (localMethods.hasNext()) {

                Method method = (Method)localMethods.next();

                if (throwsRemoteException(method)) {
                    fireSpecViolationEvent(entity, method, new Section("9.2.7.d"));
                    status = false;
                }
            }

            /*
             * For each method defined in the local interface, there MUST be
             * a matching method in the entity bean's class. The matching
             * method MUST have:
             *
             *     - The same name.
             *     - The same number and types of its arguments.
             *     - The same return type.
             *     - All the exceptions defined in the throws clause of the
             *       matching method of the enterprise Bean class must be
             *       defined in the throws clause of the method of the local
             *       interface.
             *
             * Spec 9.2.7
             */
            localMethods = Arrays.asList(local.getMethods()).iterator();

            try {
                String beanClass   = entity.getEjbClass();
                Class  bean        = classloader.loadClass(beanClass);

                while (localMethods.hasNext()) {

                    Method method = (Method)localMethods.next();

                    // Do not check the methods of the javax.ejb.EJBLocalObject
                    // interface
                    if (method.getDeclaringClass().getName().equals(EJB_LOCAL_OBJECT_INTERFACE))
                        continue;

                    if (!hasMatchingMethod(bean, method)) {
                        fireSpecViolationEvent(entity, method, new Section("9.2.7.e"));
                        status = false;
                    }

                    if (hasMatchingMethod(bean, method)) {

                        try {
                            Method beanMethod = bean.getMethod(method.getName(), method.getParameterTypes());

                            if (!hasMatchingReturnType(beanMethod, method)) {
                                fireSpecViolationEvent(entity, method, new Section("9.2.7.f"));
                                status = false;
                            }

                            if (!hasMatchingExceptions(beanMethod, method)) {

                                fireSpecViolationEvent(entity, method, new Section("9.2.7.g"));

                                status = false;
                            }
                        }
                        catch (NoSuchMethodException ignored) {}
                    }
                }
            }
            catch (ClassNotFoundException ignored) {}

        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * enterprise bean's local interface in the <local> element.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(entity, new Section("22.2"));

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
        if (name == null)
           return false;


        try {
            Class remote = classloader.loadClass(name);

            /*
             * Entity bean's remote interface MUST extend
             * the javax.ejb.EJBObject interface.
             *
             * Spec 9.2.7
             */
            if (!hasEJBObjectInterface(remote)) {

                fireSpecViolationEvent(entity, new Section("9.2.7.a"));

                status = false;
            }

            /*
             * The methods defined in the entity bean's remote interface MUST
             * have valid RMI-IIOP argument types.
             *
             * The methods defined in the entity bean's home interface MUST
             * have valid RMI-IIOP return types.
             *
             * The methods defined in the entity bean's home interface MUST
             * have java.rmi.RemoteException in their throws clause.
             *
             * Spec 9.2.7
             */
            Iterator remoteMethods = Arrays.asList(remote.getMethods()).iterator();

            while (remoteMethods.hasNext()) {

                Method method = (Method)remoteMethods.next();

                if (!hasLegalRMIIIOPArguments(method)) {

                    fireSpecViolationEvent(entity, method, new Section("9.2.7.b"));

                    status = false;
                }

                if (!hasLegalRMIIIOPReturnType(method)) {

                    fireSpecViolationEvent(entity, method, new Section("9.2.7.c"));

                    status = false;
                }

                if (!hasLegalRMIIIOPExceptionTypes(method)) {

                    fireSpecViolationEvent(entity, method, new Section("9.2.7.h"));

                    status = false;
                }

                if (!throwsRemoteException(method)) {

                    fireSpecViolationEvent(entity, method, new Section("9.2.7.d"));

                    status = false;
                }
            }

            /*
             * For each method defined in the remote interface, there MUST be
             * a matching method in the entity bean's class. The matching
             * method MUST have:
             *
             *     - The same name.
             *     - The same number and types of its arguments.
             *     - The same return type.
             *     - All the exceptions defined in the throws clause of the
             *       matching method of the enterprise Bean class must be
             *       defined in the throws clause of the method of the remote
             *       interface.
             *
             * Spec 9.2.7
             */
            remoteMethods = Arrays.asList(remote.getMethods()).iterator();

            try {
                String beanClass   = entity.getEjbClass();
                Class  bean        = classloader.loadClass(beanClass);

                while (remoteMethods.hasNext()) {

                    Method method = (Method)remoteMethods.next();

                    // Do not check the methods of the javax.ejb.EJBObject interface
                    if (method.getDeclaringClass().getName().equals(EJB_OBJECT_INTERFACE))
                        continue;

                    if (!hasMatchingMethod(bean, method)) {

                        fireSpecViolationEvent(entity, method, new Section("9.2.7.e"));

                        status = false;
                    }

                    if (hasMatchingMethod(bean, method)) {

                        try {
                            Method beanMethod = bean.getMethod(method.getName(), method.getParameterTypes());

                            if (!hasMatchingReturnType(beanMethod, method)) {

                                fireSpecViolationEvent(entity, method, new Section("9.2.7.f"));

                                status = false;
                            }

                            if (!hasMatchingExceptions(beanMethod, method)) {

                                fireSpecViolationEvent(entity, method, new Section("9.2.7.g"));

                                status = false;
                            }
                        }
                        catch (NoSuchMethodException ignored) {}
                    }
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
            fireSpecViolationEvent(entity, new Section("16.2.d"));

            status = false;
        }

        return status;
    }



/*
 *************************************************************************
 *
 *      VERIFY CMP ENTITY BEAN CLASS
 *
 *************************************************************************
 */

    private boolean verifyCMPEntityBean(EntityMetaData entity) {

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
             * Spec 10.6.2
             */
            if (!hasEntityBeanInterface(bean)) {
                fireSpecViolationEvent(entity, new Section("10.6.2.a"));
                status = false;
            }

            /*
             * The entity bean class MUST be defined as public and abstract.
             *
             * Spec 10.6.2
             */
            if (!isPublic(bean) || !isAbstract(bean))  {
                fireSpecViolationEvent(entity, new Section("10.6.2.b"));
                status = false;
            }

            /*
             * The entity bean class MUST define a public constructor that
             * takes no arguments
             *
             * Spec 10.6.2
             */
            if (!hasDefaultConstructor(bean)) {
                fireSpecViolationEvent(entity, new Section("10.6.2.c"));
                status = false;
            }

            /*
             * The entity bean class MUST NOT define the finalize() method.
             *
             * Spec 10.6.2
             */
            if (hasFinalizer(bean)) {
                fireSpecViolationEvent(entity, new Section("10.6.2.d"));
                status = false;
            }

            /*
             * The ejbCreate(...) method signatures MUST follow these rules:
             *
             *      - The method MUST be declared as public
             *      - The method MUST NOT be declared as final or static
             *      - The return type MUST be the entity bean's primary key type
             *      --- Only if method is on remote home ---
             *      - The method arguments MUST be legal types for RMI/IIOP
             *      - The method return value type MUST be legal type for RMI/IIOP
             *      --- End of only if method is on remote home ---
             *      - The method must define the javax.ejb.CreateException
             *
             * Spec 10.6.4
             */
            if (hasEJBCreateMethod(bean, false)) {

                Iterator it = getEJBCreateMethods(bean);
                while (it.hasNext()) {
                    Method ejbCreate = (Method)it.next();
                    if (!isPublic(ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("10.6.4.b"));
                        status = false;
                    }

                    if ( (isFinal(ejbCreate)) ||
                         (isStatic(ejbCreate)) ) {

                        fireSpecViolationEvent(entity, ejbCreate, new Section("10.6.4.c"));
                        status = false;
                    }

                    if (!hasPrimaryKeyReturnType(entity, ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("10.6.4.d"));
                        status = false;
                    }

                    /** FIXME
                     *  This is only true if the method is on the remote home
                     * interface
                    if (!hasLegalRMIIIOPArguments(ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("9.2.3.d"));
                        status = false;
                    }

                    if (!hasLegalRMIIIOPReturnType(ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("9.2.3.e"));
                        status = false;
                    }
                    */

                    if (!throwsCreateException(ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("10.6.4.f"));
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
             * Spec 10.6.5
             */

            if (hasEJBCreateMethod(bean, false)) {
                Iterator it =  getEJBCreateMethods(bean);

                while (it.hasNext()) {
                    Method ejbCreate = (Method)it.next();

                    if (!hasMatchingEJBPostCreate(bean, ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("10.6.5.a"));
                        status = false;
                    }

                    if (hasMatchingEJBPostCreate(bean, ejbCreate)) {
                        Method ejbPostCreate = getMatchingEJBPostCreate(bean, ejbCreate);

                        if (!isPublic(ejbPostCreate)) {
                            fireSpecViolationEvent(entity, ejbPostCreate, new Section("10.6.5.b"));
                            status = false;
                        }

                        if (isStatic(ejbPostCreate)) {
                            fireSpecViolationEvent(entity, ejbPostCreate, new Section("10.6.5.c"));
                            status = false;
                        }

                        if (isFinal(ejbPostCreate)) {
                            fireSpecViolationEvent(entity, ejbPostCreate, new Section("10.6.5.d"));
                            status = false;
                        }

                        if(!hasVoidReturnType(ejbPostCreate)) {
                            fireSpecViolationEvent(entity, ejbPostCreate, new Section("10.6.5.e"));
                            status = false;
                        }
                    }
                }
            }

            /*
             * The ejbHome(...) method signatures MUST follow these rules:
             *
             *      - The method name MUST have ejbHome as its prefix.
             *      - The method MUST be declared as public
             *      - The method MUST NOT be declared as static.
             *      - The method MUST NOT define the java.rmi.RemoteException
             *
             * Spec 10.6.6
             */

            Iterator it = getEjbHomeMethods(bean);
            while (it.hasNext()) {
                Method ejbHome = (Method)it.next();
                if (!isPublic(ejbHome)) {
                    fireSpecViolationEvent(entity, ejbHome, new Section("10.6.6.a"));
                    status = false;
                }

                if (isStatic(ejbHome)) {
                    fireSpecViolationEvent(entity, ejbHome, new Section("10.6.6.b"));
                    status = false;
                }

                if (throwsRemoteException(ejbHome)) {
                    fireSpecViolationEvent(entity, ejbHome, new Section("10.6.6.c"));
                    status = false;
                }
            }

            /*
             * The CMP entity bean MUST implement get and set accessor methods for 
			 * each field within the abstract persistance schema.
             *
             * Spec 10.6.2
             */

			/*try {  This isn't quite working right yet,so I'll leave it off
                it = entity.getCMPFields();
                while(it.hasNext()) {
                    String fieldName = (String)it.next();
			        String getName = "get" + fieldName.substring(0,1).toUpperCase() +
				                     fieldName.substring(1);
                    try {
				        Method m = bean.getDeclaredMethod(getName, new Class[0]);
				    } catch (NoSuchMethodException nsme) {
                        fireSpecViolationEvent(entity, new Section("10.6.2.g"));
                        status = false;
				    }
				    String setName = "set" + fieldName.substring(0,1).toUpperCase() +
				                     fieldName.substring(1);
                    Field field = bean.getField(fieldName);
                    Class[] args = new Class[1];
				    args[0] = field.getType();
                    try {
				        Method m = bean.getDeclaredMethod(setName, args);
				    } catch (NoSuchMethodException nsme) {
                        args[0] = classloader.loadClass("java.util.Collection");
                        try {
				            Method m = bean.getDeclaredMethod(setName, args);
                        } catch (NoSuchMethodException nsme2) {
                            fireSpecViolationEvent(entity, new Section("10.6.2.h"));
                            status = false;
                        }
				    }
	             }				 
             } catch (NoSuchFieldException nsfe) {
                 fireSpecViolationEvent(entity, new Section("10.6.2.j"));
                 status = false;
			 }*/

            /*
             * The ejbSelect(...) method signatures MUST follow these rules:
             *
             *      - The method name MUST have ejbSelect as its prefix.
             *      - The method MUST be declared as public
             *      - The method MUST be declared as abstract.
             *      - The method MUST define the javax.ejb.FinderException
             *
             * Spec 10.6.7
             */

            it = getEjbSelectMethods(bean);
            while (it.hasNext()) {
                Method ejbSelect = (Method)it.next();
                if (!isPublic(ejbSelect)) {
                    fireSpecViolationEvent(entity, ejbSelect, new Section("10.6.7.a"));
                    status = false;
                }

                if (!isAbstract(ejbSelect)) {
                    fireSpecViolationEvent(entity, ejbSelect, new Section("10.6.7.b"));
                    status = false;
                }

                if (!throwsFinderException(ejbSelect)) {
                    fireSpecViolationEvent(entity, ejbSelect, new Section("10.6.7.c"));
                    status = false;
                }
            }

            /**
             * A CMP Entity Bean must not define Finder methods.
             *
             * Spec 10.6.2
             */

            if (hasFinderMethod(bean)) {
                fireSpecViolationEvent(entity, new Section("10.6.2.i"));
                status = false;
            }
        }
        catch (ClassNotFoundException e) {

            /*
             * The Bean Provider MUST specify the fully-qualified name of the
             * Java class that implements the enterprise bean's business
             * methods.
             *
             * Spec 22.2
             */
            fireSpecViolationEvent(entity, new Section("22.2"));

            status = false;
        }

        return status;
    }

/*
 *************************************************************************
 *
 *      VERIFY BMP ENTITY BEAN CLASS
 *
 *************************************************************************
 */

    private boolean verifyBMPEntityBean(EntityMetaData entity) {

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
             * Spec 12.2.2
             */
            if (!hasEntityBeanInterface(bean)) {
                fireSpecViolationEvent(entity, new Section("12.2.2.a"));
                status = false;
            }

            /*
             * The entity bean class MUST be defined as public and NOT abstract.
             *
             * Spec 12.2.2
             */
            if (!isPublic(bean) || isAbstract(bean))  {
                fireSpecViolationEvent(entity, new Section("12.2.2.b"));
                status = false;
            }

            /*
             * The entity bean class MUST NOT be defined as final.
             *
             * Spec 12.2.2
             */
            if (isFinal(bean)) {
                fireSpecViolationEvent(entity, new Section("12.2.2.c"));
                status = false;
            }

            /*
             * The entity bean class MUST define a public constructor that
             * takes no arguments
             *
             * Spec 12.2.2
             */
            if (!hasDefaultConstructor(bean)) {
                fireSpecViolationEvent(entity, new Section("12.2.2.d"));
                status = false;
            }

            /*
             * The entity bean class MUST NOT define the finalize() method.
             *
             * Spec 12.2.2
             */
            if (hasFinalizer(bean)) {
                fireSpecViolationEvent(entity, new Section("12.2.2.e"));
                status = false;
            }

            /*
             * The ejbCreate(...) method signatures MUST follow these rules:
             *
             *      - The method MUST be declared as public
             *      - The method MUST NOT be declared as final or static
             *      - The return type MUST be the entity bean's primary key type
             *      --- If the method is on the remote home interface ---
             *      - The method arguments MUST be legal types for RMI/IIOP
             *      - The method return value type MUST be legal type for RMI/IIOP
             *      --- End if the method is on the remote home interface ---
             *
             * Spec 12.2.3
             */
            if (hasEJBCreateMethod(bean, false)) {

                Iterator it = getEJBCreateMethods(bean);
                while (it.hasNext()) {
                    Method ejbCreate = (Method)it.next();
                    if (!isPublic(ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("12.2.3.a"));
                        status = false;
                    }

                    if ( (isFinal(ejbCreate)) ||
                         (isStatic(ejbCreate)) ) {

                        fireSpecViolationEvent(entity, ejbCreate, new Section("12.2.3.b"));
                        status = false;
                    }

                    if (!hasPrimaryKeyReturnType(entity, ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("12.2.3.c"));
                        status = false;
                    }

                    /** FIXME
                     * This code needs to only be invoked if the method is on the
                     * remote home.
                    if (!hasLegalRMIIIOPArguments(ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("9.2.3.d"));
                        status = false;
                    }

                    if (!hasLegalRMIIIOPReturnType(ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("9.2.3.e"));
                        status = false;
                    }
                    */
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
             * Spec 12.2.4
             */
            if (hasEJBCreateMethod(bean, false)) {
                Iterator it =  getEJBCreateMethods(bean);

                while (it.hasNext()) {
                    Method ejbCreate = (Method)it.next();

                    if (!hasMatchingEJBPostCreate(bean, ejbCreate)) {
                        fireSpecViolationEvent(entity, ejbCreate, new Section("12.2.4.a"));

                        status = false;
                    }

                    if (hasMatchingEJBPostCreate(bean, ejbCreate)) {
                        Method ejbPostCreate = getMatchingEJBPostCreate(bean, ejbCreate);

                        if (!isPublic(ejbPostCreate)) {
                            fireSpecViolationEvent(entity, ejbPostCreate, new Section("12.2.4.b"));
                            status = false;
                        }

                        if (isStatic(ejbPostCreate) || isFinal(ejbPostCreate)) {
                            fireSpecViolationEvent(entity, ejbPostCreate, new Section("12.2.4.c"));
                            status = false;
                        }

                        if(!hasVoidReturnType(ejbPostCreate)) {
                            fireSpecViolationEvent(entity, ejbPostCreate, new Section("12.2.4.d"));
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
             * Spec 12.2.5
             */
            if (!hasEJBFindByPrimaryKey(bean)) {
                fireSpecViolationEvent(entity, new Section("12.2.5.e"));
                status = false;
            }

            if (hasEJBFindByPrimaryKey(bean)) {
                Method ejbFindByPrimaryKey = getEJBFindByPrimaryKey(bean);

                if (!hasPrimaryKeyReturnType(entity, ejbFindByPrimaryKey)) {
                    fireSpecViolationEvent(entity, ejbFindByPrimaryKey, new Section("12.2.5.e1"));
                    status = false;
                }

                if (!isSingleObjectFinder(entity, ejbFindByPrimaryKey)) {
                    fireSpecViolationEvent(entity, ejbFindByPrimaryKey, new Section("9.2.5.e2"));
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
             * Spec 12.2.5
             */
            if (hasFinderMethod(bean)) {

                Iterator it = getEJBFindMethods(bean);
                while (it.hasNext()) {
                    Method finder = (Method)it.next();

                    if (!isPublic(finder)) {
                        fireSpecViolationEvent(entity, finder, new Section("12.2.5.a"));
                        status = false;
                    }

                    if (isFinal(finder) || isStatic(finder)) {
                        fireSpecViolationEvent(entity, finder, new Section("12.2.5.b"));
                        status = false;
                    }

                    /** FIXME
                     * this path should only get invoked if the finder is on the
                     * remote interface.
                    if (!hasLegalRMIIIOPArguments(finder)) {
                        fireSpecViolationEvent(entity, finder, new Section("9.2.5.g"));
                        status = false;
                    }
                    */

                    if (! (isSingleObjectFinder(entity, finder)
                        || isMultiObjectFinder(finder))) {
                        fireSpecViolationEvent(entity, finder, new Section("12.2.5.d"));
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
             * Spec 22.2
             */
            fireSpecViolationEvent(entity, new Section("22.2"));
            status = false;
        }

        return status;
    }

    private boolean verifyPrimaryKey(EntityMetaData entity) {
        boolean status = true;
        boolean cmp = entity.isCMP();
        if(entity.getPrimaryKeyClass() == null || entity.getPrimaryKeyClass().length() == 0) {
            if (cmp) fireSpecViolationEvent(entity, new Section("10.6.1.a"));
            else fireSpecViolationEvent(entity, new Section("12.2.1.a"));
            return false; // We can't get any further if there's no PK class specified!
        }

	    /**
         * FIXME - Still missing the bits from 10.8.2 for CMP primary keys.  Primarily
         * the class must be public, all fields in the class must be public and the
         * fields must also be a subset of the CMP fields within the bean.
         */

        Class cls = null;
        try {
            cls = classloader.loadClass(entity.getPrimaryKeyClass());
        } catch(ClassNotFoundException e) {
            if (cmp) fireSpecViolationEvent(entity, new Section("10.6.13.d"));
            else fireSpecViolationEvent(entity, new Section("12.2.12.d"));
            status = false;  // Can't do any other checks if the class is null!
		}

        /**
         * The primary key type must be a valid type in RMI-IIOP.
         *
         * Spec 10.6.13 & 12.2.12
         */
        if (!isRMIIDLValueType(cls)) {
            if (cmp) fireSpecViolationEvent(entity, new Section("10.6.13.a"));
            else fireSpecViolationEvent(entity, new Section("12.2.12.a"));
            status = false;
        }

        /**
         * No primary key field specified, just a primary key class.
         */
        if(entity.getPrimKeyField() == null || entity.getPrimKeyField().length() == 0) {

			/**
             * This is a check for some interesting implementation of equals() and
             * hashCode().  I am not sure how well it works in the end.
             */
            if(!cls.getName().equals("java.lang.Object")) {
                Object one, two;
                try {
                    one = cls.newInstance();
                    two = cls.newInstance();
                    try {
                        if(!one.equals(two)) {
                			if (cmp) fireSpecViolationEvent(entity, new Section("10.6.13.b"));
		                    else fireSpecViolationEvent(entity, new Section("12.2.12.b"));
                            status = false;
                        }
                    } catch(NullPointerException e) {} // That's OK - the implementor expected the fields to have values
                    try {
                        if(one.hashCode() != two.hashCode()) {
                			if (cmp) fireSpecViolationEvent(entity, new Section("10.6.13.c"));
			                else fireSpecViolationEvent(entity, new Section("12.2.12.c"));
                            status = false;
                        }
                    } catch(NullPointerException e) {} // That's OK - the implementor expected the fields to have values
                } catch(IllegalAccessException e) {
                    /**
                     * If CMP primary key class MUST have a public constructor with no
                     * parameters.
                     * 10.8.2.a
                     */
                    if (cmp) {
                        fireSpecViolationEvent(entity, new Section("10.8.2.a"));
                        status = false;
                    }
                } catch(InstantiationException e) {
					//Not sure what condition this is at the moment - JMW
                    //fireSpecViolationEvent(entity, new Section("9.2.9.a"));
                    //status = false;
                }
            }
        } else {
            /**
             *  BMP Beans MUST not include the primkey-field element in their deployment
             *  descriptor.
             *  Deployment descriptor comment
             */
            if(entity.isBMP()) {
                fireSpecViolationEvent(entity, new Section("dd.a"));
                status = false;
            }
            try {
                Class fieldClass = classloader.loadClass(entity.getEjbClass());
                Field field = null;
                try {
                    /**
                     * The class of the primary key field MUST match the primary key
                     * class specified for the entity bean.
					 *
                     * Spec 10.8.1
                     */
                    field = fieldClass.getField(entity.getPrimKeyField());
                    if(!entity.getPrimaryKeyClass().equals(field.getType().getName())) {
                        status = false;
                        fireSpecViolationEvent(entity, new Section("10.8.1.a"));
                    }
                    /**
                     * The primary keyfield MUST be a CMP field within the entity bean.
					 *
                     * Spec 10.8.1
                     */
                    Iterator it = entity.getCMPFields();
                    boolean found = false;
                    while(it.hasNext()) {
                        String fieldName = (String)it.next();
                        if(fieldName.equals(entity.getPrimKeyField())) {
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        status = false;
                        fireSpecViolationEvent(entity, new Section("10.8.1.b"));
                    }
                } catch(NoSuchFieldException e) {
                    /**
                     * The primary keyfield MUST be a CMP field within the entity bean.
					 *
                     * Spec 10.8.1
                     */
                    status = false;
                    fireSpecViolationEvent(entity, new Section("10.8.1.b"));
                }
            } catch(ClassNotFoundException e) {} // reported elsewhere
        }

        return status;
    }
}

