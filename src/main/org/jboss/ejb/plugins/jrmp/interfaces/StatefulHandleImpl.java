/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.MarshalledObject;
import java.lang.reflect.Method;

import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;

import org.jboss.security.SecurityAssociation;

/**
 * An EJB stateful session bean handle.
 *
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author  <a href="mailto:marc.fleury@telkel.com>Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.10 $
 */
public class StatefulHandleImpl
    extends AbstractHandle
    implements Handle
{
    // Constants -----------------------------------------------------

    /** Serial Version Identifier. */
    private static final long serialVersionUID = -2592509632957623102L;

    // Static --------------------------------------------------------

    /** A reference to {@link Handle#getEJBObject}. */
    protected static final Method GET_EJB_OBJECT;

    /**
     * Initialize <tt>Handle</tt> method references.
     */
    static {
        try {
            GET_EJB_OBJECT = Handle.class.getMethod("getEJBObject", new Class[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    // Attributes ----------------------------------------------------

    /** The identity of the bean. */
    protected final Object id;

    // Constructors --------------------------------------------------

    /**
     * Construct a <tt>StatefulHandleImpl</tt>.
     *
     * @param handle    The initial context handle that will be used
     *                  to restore the naming context or null to use
     *                  a fresh InitialContext object.
     * @param name      JNDI name.
     * @param id        Identity of the bean.
     */
    public StatefulHandleImpl(final InitialContextHandle handle,
                              final String name,
                              final Object id)
    {
        super(handle, name);
        this.id = id;
    }

    // Public --------------------------------------------------------

    /**
     * Handle implementation.
     *
     * This differs from Stateless and Entity handles which just invoke standard methods
     * (<tt>create</tt> and <tt>findByPrimaryKey</tt> respectively) on the Home interface (proxy).
     * There is no equivalent option for stateful SBs, so a direct invocation on the container has to
     * be made to locate the bean by its id (the stateful SB container provides an implementation of
     * <tt>getEJBObject</tt>).
     *
     * This means the security context has to be set here just as it would be in the Proxy.
     *
     * @return  <tt>EJBObject</tt> reference.
     *
     * @throws ServerException    Could not get EJBObject.
     */
    public EJBObject getEJBObject() throws RemoteException {
        try {
            InitialContext ctx = createInitialContext();
            ContainerRemote container;

            // get a ref to the container, then close the naming context
            try {
                container = (ContainerRemote)ctx.lookup("invokers/" + name);
            }
            finally {
                ctx.close();
            }

            // Create a new MethodInvocation for distribution
            //System.out.println("I am about to invoke and getEJBOBject is "+getEJBObjectMethod.getName() +" My ID is "+id);
            RemoteMethodInvocation rmi =
                new RemoteMethodInvocation(null,
                                           GET_EJB_OBJECT,
                                           new Object[] { id });

            // MF FIXME: WE DEFINITLY NEED THE SECURITY ON SUCH A CALL...
            // We also need a pointer to the TM...:(

            // Set the transaction context
            //rmi.setTransaction(tm != null? tm.getTransaction() : null);

            // Set the security stuff
            // MF fixme this will need to use "thread local" and therefore same construct as above
            // rmi.setPrincipal(sm != null? sm.getPrincipal() : null);
            // rmi.setCredential(sm != null? sm.getCredential() : null);
            // is the credential thread local? (don't think so... but...)
            //rmi.setPrincipal( getPrincipal() );
            // rmi.setCredential( getCredential() );

            // LT: added next two lines as fix for bug 474134 (26/10/01). Not sure which of the above comments are relevant...
            rmi.setPrincipal(SecurityAssociation.getPrincipal());
            rmi.setCredential(SecurityAssociation.getCredential());

            // Invoke on the remote server, enforce marshalling
            MarshalledObject mo = new MarshalledObject(rmi);
            return (EJBObject)container.invokeHome(mo).get();
        }
        catch (Exception e) {
            throw new ServerException("Could not get EJBObject", e);
        }
    }

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}

