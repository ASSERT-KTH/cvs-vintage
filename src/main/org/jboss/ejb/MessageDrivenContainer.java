/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

/**
 * MessageDrivenContainer, based on the StatelessSessionContainer.
 *
 * @see StatelessSessionContainer
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @version $Revision: 1.16 $
 */
public class MessageDrivenContainer
    extends Container
    implements ContainerInvokerContainer, InstancePoolContainer
{
    /**
     * These are the mappings between the remote interface methods
     * and the bean methods.
     */
    protected Map beanMapping;

    /** This is the container invoker for this container. */
    protected ContainerInvoker containerInvoker;

    /** This is the instancepool that is to be used. */
    protected InstancePool instancePool;

    /**
     * This is the first interceptor in the chain.
     * The last interceptor must be provided by the container itself.
     */
    protected Interceptor interceptor;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    // Public --------------------------------------------------------

    public void setContainerInvoker(ContainerInvoker ci)
    {
        if (ci == null)
            throw new IllegalArgumentException("Null invoker");

        this.containerInvoker = ci;
        ci.setContainer(this);
    }

    public ContainerInvoker getContainerInvoker()
    {
        return containerInvoker;
    }

    public LocalContainerInvoker getLocalContainerInvoker()
    {
        return localContainerInvoker;
    }

    public void setInstancePool(InstancePool ip)
    {
        if (ip == null)
            throw new IllegalArgumentException("Null pool");

        this.instancePool = ip;
        ip.setContainer(this);
    }

    public InstancePool getInstancePool()
    {
        return instancePool;
    }

    public void addInterceptor(Interceptor in)
    {
        if (interceptor == null) {
            interceptor = in;
        }
        else {
            Interceptor current = interceptor;

            while (current.getNext() != null) {
                current = current.getNext();
            }

            current.setNext(in);
        }
    }

    public Interceptor getInterceptor()
    {
        return interceptor;
    }

    /**
     * ContainerInvokerContainer - not needed, should we skip inherit this
     * or just throw Error??
     */
    public Class getHomeClass()
    {
       //throw new Error("HomeClass not valid for MessageDriven beans");
       return null;
    }

    public Class getRemoteClass()
    {
       //throw new Error("RemoteClass not valid for MessageDriven beans");
       return null;
    }

    public Class getLocalClass()
    {
        return null;
    }

    public Class getLocalHomeClass()
    {
       //throw new Error("LocalHomeClass not valid for MessageDriven beans");
       return null;
    }

    // Container implementation - overridden here ----------------------

    public void init() throws Exception
    {
        try {
            // Associate thread with classloader
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClassLoader());

            // Call default init
            super.init();

            // Map the bean methods
            setupBeanMapping();

            // Initialize pool
            instancePool.init();

            // Init container invoker
            containerInvoker.init();

            // Initialize the interceptor by calling the chain
            Interceptor in = interceptor;
            while (in != null) {
                in.setContainer(this);
                in.init();
                in = in.getNext();
            }

            // Reset classloader
            Thread.currentThread().setContextClassLoader(oldCl);
        }
        catch (Exception e) {
            log.error("Serious error in init: ", e);
            throw e;
        }
    }

    public void start()
        throws Exception
    {
        // Associate thread with classloader
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());

        // Call default start
        super.start();

        // Start container invoker
        containerInvoker.start();
        // Start the instance pool
        instancePool.start();

        // Start all interceptors in the chain
        Interceptor in = interceptor;
        while (in != null)
        {
           in.start();
           in = in.getNext();
        }

        // Reset classloader
        Thread.currentThread().setContextClassLoader(oldCl);
    }

    public void stop()
    {
        // Associate thread with classloader
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());

        // Call default stop
        super.stop();

        // Stop container invoker
        containerInvoker.stop();

        // Stop the instance pool
        instancePool.stop();

        // Stop all interceptors in the chain
        Interceptor in = interceptor;
        while (in != null)
        {
           in.stop();
           in = in.getNext();
        }

        // Reset classloader
        Thread.currentThread().setContextClassLoader(oldCl);
    }

    public void destroy()
    {
        // Associate thread with classloader
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());

        // Call default destroy
        super.destroy();

        // Destroy container invoker
        containerInvoker.destroy();

        // Destroy the pool
        instancePool.destroy();

        // Destroy all the interceptors in the chain
        Interceptor in = interceptor;
        while (in != null)
        {
           in.destroy();
           in = in.getNext();
        }

        // Reset classloader
        Thread.currentThread().setContextClassLoader(oldCl);
    }


    public Object invokeHome(MethodInvocation mi)
        throws Exception
    {
        throw new Error("invokeHome not valid for MessageDriven beans");
        //return getInterceptor().invokeHome(mi);
    }

    /**
     * This method does invocation interpositioning of tx and security,
     * retrieves the instance from an object table, and invokes the method
     * on the particular instance
     */
    public Object invoke(MethodInvocation mi)
        throws Exception
    {
        // Invoke through interceptors
        return getInterceptor().invoke(mi);
    }


    // EJBHome implementation ----------------------------------------

    public EJBObject createHome()
        throws java.rmi.RemoteException, CreateException
    {
        throw new Error("createHome not valid for MessageDriven beans");
    }


    public void removeHome(Handle handle)
        throws java.rmi.RemoteException, RemoveException
    {
        throw new Error("removeHome not valid for MessageDriven beans");
        // TODO
    }

    public void removeHome(Object primaryKey)
        throws java.rmi.RemoteException, RemoveException
    {
        throw new Error("removeHome not valid for MessageDriven beans");
        // TODO
    }

    public EJBMetaData getEJBMetaDataHome()
        throws java.rmi.RemoteException
    {
        // TODO
        //return null;
        throw new Error("getEJBMetaDataHome not valid for MessageDriven beans");
    }

    public HomeHandle getHomeHandleHome()
        throws java.rmi.RemoteException
    {
        // TODO
        //return null;
        throw new Error("getHomeHandleHome not valid for MessageDriven beans");
    }

    protected void setupBeanMapping()
        throws NoSuchMethodException
    {
        Map map = new HashMap();

        //
        // Here we should have a way of looking up wich message class
        // the MessageDriven bean implements, by doing this we might
        // be able to use other MOM systems, aka XmlBlaser. TODO!
        //

        String msgInterface = "javax.jms.MessageListener";
        String msgMethod = "onMessage";
        String msgArgument = "javax.jms.Message";

        // Get the method
        Class msgInterfaceClass = null;
        Class argumentClass = null;

        try {
            msgInterfaceClass = Class.forName(msgInterface);
            argumentClass = Class.forName(msgArgument);
        } catch (ClassNotFoundException ex) {
            log.error("Could not get the classes for message interface" + msgInterface, ex);
            // Hackish
            throw new NoSuchMethodException("Could not get the classes for message interface" + msgInterface + ": " + ex);
        }

        Method m = msgInterfaceClass.getMethod(msgMethod, new Class[] {argumentClass});
        // Implemented by bean
        map.put(m, beanClass.getMethod(m.getName(), m.getParameterTypes()));
        //DEBUG Logger.debug("Mapped "+m.getName()+" "+m.hashCode()+"to "+map.get(m));
        beanMapping = map;
    }

    Interceptor createContainerInterceptor()
    {
        return new ContainerInterceptor();
    }

    // This is the last step before invocation - all interceptors are done
    class ContainerInterceptor
        implements Interceptor
    {
        public void setContainer(Container con) {}

        public void setNext(Interceptor interceptor) {}
        public Interceptor getNext() { return null; }

        public void init() {}
        public void start() {}
        public void stop() {}
        public void destroy() {}

        public Object invokeHome(MethodInvocation mi)
            throws Exception
        {
            throw new Error("invokeHome not valid for MessageDriven beans");
        }

        /**
         * FIXME Design problem, who will do the acknowledging for
         * beans with bean managed transaction?? Probably best done in the
         * listener "proxys"
         */
        public Object invoke(MethodInvocation mi)
            throws Exception
        {
            // wire the transaction on the context,
            // this is how the instance remember the tx
            if (mi.getEnterpriseContext().getTransaction() == null) {
                mi.getEnterpriseContext().setTransaction(mi.getTransaction());
            }

            // Get method and instance to invoke upon
            Method m = (Method)beanMapping.get(mi.getMethod());


            // we have a method that needs to be done by a bean instance
            {
                // Invoke and handle exceptions
                try {
                    return m.invoke(mi.getEnterpriseContext().getInstance(),
                                    mi.getArguments());
                }
                catch (IllegalAccessException e) {
                    // Throw this as a bean exception...(?)
                    throw new EJBException(e);
                }
                catch (InvocationTargetException e) {
                    Throwable ex = e.getTargetException();
                    if (ex instanceof EJBException) {
                        throw (EJBException)ex;
                    }
                    else if (ex instanceof RuntimeException) {
                        // Transform runtime exception into what a
                        // bean *should* have thrown
                        throw new EJBException((Exception)ex);
                    }
                    else if (ex instanceof Exception) {
                        throw (Exception)ex;
                    }
                    else {
                        // TODO: this could break if a Throwable
                        // (not an Error) is thrown.
                        throw (Error)ex;
                    }
                }
            }
        }
      // Monitorable implementation ------------------------------------
      public void sample(Object s)
      {
        // Just here to because Monitorable request it but will be removed soon
      }
      public Map retrieveStatistic()
      {
        return null;
      }
      public void resetStatistic()
      {
      }
    }
}
