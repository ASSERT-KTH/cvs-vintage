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
 * $Id: EJBVerifier11.java,v 1.6 2000/06/07 00:02:53 mulder Exp $
 */


// standard imports
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.*;


// non-standard class dependencies
import org.gjt.lindfors.pattern.StrategyContext;

import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationEventFactory;

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
 * @version $Revision: 1.6 $
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

        this.factory       = new VerificationEventFactory();
    }





    public void checkSession(Session session) {

        boolean sessionDescriptorVerified = true; //  false;

        boolean beanVerified   = false;
        boolean homeVerified   = false;
        boolean remoteVerified = false;
        /*
         * [TODO] use state pattern instead, this collection of bools is going
         *        to grow, and managing them will become messy
         */

        //sessionDescriptorVerified = verifySessionDescriptor();

        /*
         * Also, the descriptors should most likely be checked in one place,
         * instead of sprinkling their check code across the bean class
         * checkers..
         */


        beanVerified   = verifySessionBean(session);
        homeVerified   = verifySessionHome(session.getHome());
        remoteVerified = verifySessionRemote(session.getRemote());


        if ( beanVerified && homeVerified && remoteVerified &&
             sessionDescriptorVerified) {

            /*
             * Verification for this session bean done. Fire the event
             * to tell listeneres everything is ok.
             */
            VerificationEvent event =
                    factory.createBeanVerifiedEvent(context, session.getEjbClass());

            context.fireBeanChecked(event);
        }
    }


    public void checkEntity(Entity entity) {
        boolean pkVerified = false;

        pkVerified = verifyPrimaryKey(entity.getPrimaryKeyClass());

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
 *****************************************************************************
 *
 *  PRIVATE INSTANCE METHODS
 *
 *****************************************************************************
 *****************************************************************************
 */

    private boolean verifySessionHome(String name) {

        // NO IMPLEMENTATION

        return true;
    }

    private boolean verifySessionRemote(String name) {

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

                fireSpecViolationEvent(SECTION_6_5_1, name);

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
            if (hasSessionSynchronizationInterface(bean))

                if (!isStateful(session)) {
                    fireSpecViolationEvent(SECTION_6_5_3_a, name);

                    status = false;
                }

                if (!isContainerManagedTx(session)) {
                    fireSpecViolationEvent(SECTION_6_5_3_b, name);

                    status = false;
                }


            /*
             * A session bean MUST implement AT LEAST one ejbCreate method.
             *
             * Spec 6.5.5
             */
            if (!hasEJBCreateMethod(bean)) {

                fireSpecViolationEvent(SECTION_6_5_5, name);

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

                fireSpecViolationEvent(SECTION_6_6_1, name);

                status = false;
            }

            /*
             * The session bean class MUST be defined as public.
             *
             * Spec 6.10.2
             */
            if (!isPublicClass(bean)) {

               fireSpecViolationEvent(SECTION_6_10_2_a, name);

               status = false;
            }

            /*
             * The session bean class MUST NOT be final.
             *
             * Spec 6.10.2
             */
            if (isFinalClass(bean)) {

                fireSpecViolationEvent(SECTION_6_10_2_b, name);

                status = false;
            }

            /*
             * The session bean class MUST NOT be abstract.
             *
             * Spec 6.10.2
             */
            if (isAbstractClass(bean)) {

                fireSpecViolationEvent(SECTION_6_10_2_c, name);

                status = false;
            }

            /*
             * The session bean class MUST have a public constructor that
             * takes no arguments.
             *
             * Spec 6.10.2
             */
            if (!hasDefaultConstructor(bean)) {

                fireSpecViolationEvent(SECTION_6_10_2_d, name);

                status = false;
            }

            /*
             * The session bean class MUST NOT define the finalize() method.
             *
             * Spec 6.10.2
             */
            if (hasFinalizer(bean)) {

                fireSpecViolationEvent(SECTION_6_10_2_e, name);

                status = false;
            }



        }
        catch (ClassNotFoundException e) {

            VerificationEvent event =
                    factory.createSpecViolationEvent(context, DTD_EJB_CLASS, name);

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



    /*
     * Searches for an instance of a public ejbCreate method from the class
     */
    private boolean hasEJBCreateMethod(Class c) {

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



    /*
     * Finds java.ejb.SessionBean interface from the class
     */
    private boolean hasSessionBeanInterface(Class c) {

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
    private boolean hasSessionSynchronizationInterface(Class c) {

        Class[] interfaces = c.getInterfaces();

        for (int i = 0; i < interfaces.length; ++i) {

            if ((SESSIONSYNCHRONIZATION_INTERFACE).equals(interfaces[i].getName()))
                return true;
        }

        return false;
    }


    private boolean hasDefaultConstructor(Class c) {
        try {
            c.newInstance();
        } catch(Exception e) {
            return false;
        }
        return true;
    }


    private boolean hasFinalizer(Class c) {

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

    private boolean isStateful(Session session) {

        if (STATEFUL_SESSION.equals(session.getSessionType()))
            return true;

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


    private boolean isPublicClass(Class c) {

        if (c.getModifiers() == Modifier.PUBLIC)
            return true;

        return false;
    }


    private boolean isFinalClass(Class c) {

        if (c.getModifiers() == Modifier.FINAL)
            return true;

        return false;
    }


    private boolean isAbstractClass(Class c) {

        if (c.getModifiers() == Modifier.ABSTRACT)
            return true;

        return false;
    }


    private void fireSpecViolationEvent(String section, String name) {

        VerificationEvent event =
                factory.createSpecViolationEvent(context, section, name);

        context.fireBeanChecked(event);
    }




    /*
     ****************************************************************
     *
     *      String constants
     *
     ****************************************************************
     */
    private final static String SESSIONBEAN_INTERFACE =
        "javax.ejb.SessionBean";

    private final static String SESSIONSYNCHRONIZATION_INTERFACE =
        "javax.ejb.SessionSynchronization";

    private final static String EJB_CREATE_METHOD     =
        "ejbCreate";



    /*
     * Specification entries
     */
    public final static String SECTION_6_5_1         =
        "Section 6.5.1 Required Sessionbean interface";

    public final static String SECTION_6_5_3_a       =
        "Section 6.5.3 The optional SessionSynchronization interface (stateful)";

    public final static String SECTION_6_5_3_b       =
        "Section 6.5.3 The optional SessionSynchronization interface (stateless)";

    public final static String SECTION_6_5_5         =
        "Section 6.5.5 Session bean's ejbCreate(...) methods";

    public final static String SECTION_6_6_1         =
        "Section 6.6.1 Operations allowed in the methods of a stateful session bean class";

    public final static String SECTION_6_10_2_a      =
        "Section 6.10.2 Session bean class (public class)";

    public final static String SECTION_6_10_2_b      =
        "Section 6.10.2 Session bean class (not final class)";

    public final static String SECTION_6_10_2_c      =
        "Section 6.10.2 Session bean class (not abstract class)";

    public final static String SECTION_6_10_2_d      =
        "Section 6.10.2 Session bean class (public constructor)";

    public final static String SECTION_6_10_2_e      =
        "Section 6.10.2 Session bean class (no finalizer)";

    public final static String SECTION_9_2_9_a       =
        "Section 9.2.9 Primary key class (equals doesn't work)";

    public final static String SECTION_9_2_9_b       =
        "Section 9.2.9 Primary key class (hashCode doesn't work)";

    public final static String SECTION_9_4_7_2_a     =
        "Section 9.4.7.2 Primary key class (not public)";

    public final static String SECTION_9_4_7_2_b     =
        "Section 9.4.7.2 Primary key class (fields not public)";

    /*
     * Ejb-jar DTD
     */
    public final static String DTD_EJB_CLASS         =
        "Deployment descriptor DTD: ejb-class";

    public final static String BEAN_MANAGED_TX       =
        "Bean";

    public final static String CONTAINER_MANAGED_TX  =
        "Container";

    public final static String STATEFUL_SESSION      =
        "Stateful";

    public final static String STATELESS_SESSION     =
        "Stateless";

}

