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
 * $Id: EJBVerifier11.java,v 1.9 2000/07/19 21:27:45 juha Exp $
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

import com.dreambean.ejx.ejb.Session;
import com.dreambean.ejx.ejb.Entity;


/**
 * Concrete implementation of the <code>VerificationStrategy</code> interface.
 * This class implements the verification of both session and entity beans for
 * Enterprise JavaBeans v1.1 specification.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors (jplindfo@helsinki.fi)
 * @version $Revision: 1.9 $
 * @since  	JDK 1.3
 */
public class EJBVerifier11 extends AbstractVerifier {

    private VerificationContext context      = null;
    private VerificationEventFactory factory = null;
    private ClassLoader classloader          = null;


    /*
     *  Constructor
     */
    public EJBVerifier11(VerificationContext context) {

        URL[] list = { context.getJarLocation() };

        ClassLoader parent = getClass().getClassLoader();
        URLClassLoader cl  = new URLClassLoader(list, parent);

        this.classloader   = cl;
        this.context       = context;

        this.factory       = new DefaultEventFactory();
    }





    public void checkSession(Session session) {

        boolean sessionDescriptorVerified = true; //  false;

        boolean beanVerified   = false;
        boolean homeVerified   = false;
        boolean remoteVerified = false;

        //sessionDescriptorVerified = verifySessionDescriptor();



        beanVerified   = verifySessionBean(session);
        homeVerified   = verifySessionHome(session);
        remoteVerified = verifySessionRemote(session);


        if ( beanVerified && homeVerified && remoteVerified &&
             sessionDescriptorVerified) {

            /*
             * Verification for this session bean done. Fire the event
             * to tell listeneres everything is ok.
             */
            VerificationEvent event = factory.createBeanVerifiedEvent(context);
            context.fireBeanChecked(event);
        }
    }


    public void checkEntity(Entity entity) {
        boolean pkVerified = false;

        // will put this back later
        //pkVerified = verifyPrimaryKey(entity.getPrimaryKeyClass());

        // NO IMPLEMENTATION
    }




/*
 ***********************************************************************
 *
 *    IMPLEMENTS VERIFICATION STRATEGY INTERFACE
 *
 ***********************************************************************
 */

    public StrategyContext getContext() {
        return context;
    }

    public void checkSessions(Iterator beans) {

        while (beans.hasNext()) {
            try {
                checkSession((Session)beans.next());
            }
            catch (ClassCastException e) {
                System.err.println(e);
                // THROW INTERNAL ERROR
            }
        }
    }

    public void checkEntities(Iterator beans) {

        while (beans.hasNext()) {
            try {
                checkEntity((Entity)beans.next());
            }
            catch (ClassCastException e) {
                System.err.println(e);
                // THROW INTERNAL ERROR
            }
        }
    }

    public void checkMessageDriven(Iterator beans) {

            // EMPTY IMPLEMENTATION, EJB 2.0 ONLY
    }


/*
 *****************************************************************************
 *
 *  PRIVATE INSTANCE METHODS
 *
 *****************************************************************************
 */

    private boolean verifySessionHome(Session session) {

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
             if (isStateless(session)) {
                 
                 if (!hasDefaultCreateMethod(home)) {
                    fireSpecViolationEvent(new Section("6.8.a"));

                    status = false;
                 }
                 
                 if (!hasRemoteReturnType(getDefaultCreateMethod(home))) {
                     fireSpecViolationEvent(new Section("6.8.b"));;
                     
                     status = false;
                 }
                 
                 if (hasMoreThanOneCreateMethods(home)) {
                     fireSpecViolationEvent(new Section("6.8.c"));
                     
                     status = false;
                 }   
             }
             
             
        }
        catch (ClassNotFoundException e) {

            VerificationEvent event =
                    factory.createSpecViolationEvent(context, new Section("16.2.c"));

            context.fireBeanChecked(event);

            status = false;
        }
        
        return status;

    }

    
    
    private boolean verifySessionRemote(Session session) {

        // NO IMPLEMENTATION

        return true;
    }

    
    
    private boolean verifySessionBean(Session session) {

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

                fireSpecViolationEvent(new Section("6.5.1"));

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

                if (!isStateful(session)) {
                    fireSpecViolationEvent(new Section("6.5.3.a"));

                    status = false;
                }

                if (!isContainerManagedTx(session)) {
                    fireSpecViolationEvent(new Section("6.5.3.b"));

                    status = false;
                }
            }
            

            /*
             * A session bean MUST implement AT LEAST one ejbCreate method.
             *
             * Spec 6.5.5
             */
            if (!hasEJBCreateMethod(bean)) {
                
                fireSpecViolationEvent(new Section("6.5.5"));

                status = false;

                /*
                 * [TODO] the ejbCreate signature in bean class must match the
                 *        create methods signature in home interface.
                 *
                 *        this is stated implicitly in 6.10.2
                 *        didnt find explicit requirement yet
                 */
            }


            /*
             * A session with bean-managed transaction demarcation CANNOT
             * implement the SessionSynchronization interface.
             *
             * Spec 6.6.1 (table 2)
             */
            if (hasSessionSynchronizationInterface(bean) && isBeanManagedTx(session)) {

                fireSpecViolationEvent(new Section("6.6.1"));

                status = false;
            }

            /*
             * The session bean class MUST be defined as public.
             *
             * Spec 6.10.2
             */
            if (!isPublicClass(bean)) {

               fireSpecViolationEvent(new Section("6.10.2.a"));

               status = false;
            }

            /*
             * The session bean class MUST NOT be final.
             *
             * Spec 6.10.2
             */
            if (isFinalClass(bean)) {

                fireSpecViolationEvent(new Section("6.10.2.b"));

                status = false;
            }

            /*
             * The session bean class MUST NOT be abstract.
             *
             * Spec 6.10.2
             */
            if (isAbstractClass(bean)) {

                fireSpecViolationEvent(new Section("6.10.2.c"));

                status = false;
            }

            /*
             * The session bean class MUST have a public constructor that
             * takes no arguments.
             *
             * Spec 6.10.2
             */
            if (!hasDefaultConstructor(bean)) {

                fireSpecViolationEvent(new Section("6.10.2.d"));

                status = false;
            }

            /*
             * The session bean class MUST NOT define the finalize() method.
             *
             * Spec 6.10.2
             */
            if (hasFinalizer(bean)) {

                fireSpecViolationEvent(new Section("6.10.2.e"));

                status = false;
            }



        }
        catch (ClassNotFoundException e) {

            VerificationEvent event =
                    factory.createSpecViolationEvent(context, new Section("16.2.b"));

            context.fireBeanChecked(event);

            status = false;
        }

        return status;
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

        if(!isPublicClass(cls)) {
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




    private boolean isBeanManagedTx(Session session) {

        if (BEAN_MANAGED_TX.equals(session.getTransactionType()))
            return true;

        return false;
    }


    private boolean isContainerManagedTx(Session session) {

        if (CONTAINER_MANAGED_TX.equals(session.getTransactionType()))
            return true;

        return false;
    }



    private void fireSpecViolationEvent(Section section) {

        VerificationEvent event =
                factory.createSpecViolationEvent(context, section);

        context.fireBeanChecked(event);
    }




}

