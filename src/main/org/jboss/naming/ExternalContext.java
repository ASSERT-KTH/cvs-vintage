/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.io.InputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.ldap.Control;
import javax.naming.spi.ObjectFactory;

import org.jnp.server.Main;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/** A MBean that binds an arbitrary InitialContext into the JBoss default
InitialContext as a Reference. If RemoteAccess is enabled, the reference
is a Serializable object that is capable of creating the InitialContext
remotely. If RemoteAccess if false, the reference is to a nonserializable object
that can only be used from within this VM.

@see org.jboss.naming.NonSerializableFactory

@author Scott_Stark@displayscape.com
@version $Revision: 1.3 $
*/
public class ExternalContext extends ServiceMBeanSupport implements ExternalContextMBean
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private boolean remoteAccess;
    private SerializableInitialContext contextInfo = new SerializableInitialContext();

    // Constructors --------------------------------------------------
    public ExternalContext()
    {
    }
    public ExternalContext(String jndiName, String contextPropsURL)
        throws IOException, NamingException
    {
        setJndiName(jndiName);
        setProperties(contextPropsURL);
    }

    /** Set the jndi name under which the external context is bound.
    */
    public String getJndiName()
    {
       return contextInfo.getJndiName();
    }
    /** Set the jndi name under which the external context is bound.
     */
    public void setJndiName(String jndiName) throws NamingException
    {
        contextInfo.setJndiName(jndiName);
        if( super.getState() == STARTED )
        {
            unbind(jndiName);
            try
            {
                rebind();
            }
            catch(Exception e)
            {
                NamingException ne = new NamingException("Failed to update jndiName");
                ne.setRootCause(e);
                throw ne;
            }
        }
    }

    public boolean getRemoteAccess()
    {
        return remoteAccess;
    }
    public void setRemoteAccess(boolean remoteAccess)
    {
        this.remoteAccess = remoteAccess;
    }

    /** Get the class name of the InitialContext implementation to
	use. Should be one of:
	javax.naming.InitialContext
	javax.naming.directory.InitialDirContext
	javax.naming.ldap.InitialLdapContext
    @return the classname of the InitialContext to use
    */
    public String getInitialContext()
    {
        return contextInfo.getInitialContext();
    }

    /** Set the class name of the InitialContext implementation to
	use. Should be one of:
	javax.naming.InitialContext
	javax.naming.directory.InitialDirContext
	javax.naming.ldap.InitialLdapContext
	The default is javax.naming.InitialContex.
    @param contextClass, the classname of the InitialContext to use
    */
    public void setInitialContext(String className) throws ClassNotFoundException
    {
        contextInfo.loadClass(className);
    }

    /** Set the InitialContex class environment properties.
    */
    public void setProperties(String contextPropsURL) throws IOException
    {
        contextInfo.loadProperties(contextPropsURL);
    }

    public String getName()
    {
        return "ExternalContext(" + contextInfo.getJndiName() + ")";
    }

    public void initService() throws Exception
    {
    }

    /** Start the service by binding the external context into the
        JBoss InitialContext.
    */
    public void startService() throws Exception
    {
        rebind();
    }

    /** Stop the service by unbinding the external context into the
        JBoss InitialContext.
    */
    public void stopService()
    {
        unbind(contextInfo.getJndiName());
    }

    // Protected -----------------------------------------------------
    private static Context createContext(Context rootContext, Name name) throws NamingException
    {
        Context subctx = rootContext;
        for(int n = 0; n < name.size(); n ++)
        {
            String atom = name.get(n);
            try
            {
                Object obj = subctx.lookup(atom);
                subctx = (Context) obj;
            }
            catch(NamingException e)
            {	// No binding exists, create a subcontext
                subctx = subctx.createSubcontext(atom);
            }
        }

        return subctx;
    }

    private void rebind() throws Exception
    {
        Context ctx = contextInfo.newContext();
        Context rootCtx = (Context) new InitialContext();
        log.debug("ctx="+ctx+", env="+ctx.getEnvironment());
        // Get the parent context into which we are to bind
        String jndiName = contextInfo.getJndiName();
        Name fullName = rootCtx.getNameParser("").parse(jndiName);
        log.debug("fullName="+fullName);
        Name parentName = fullName;
        if( fullName.size() > 1 )
            parentName = fullName.getPrefix(fullName.size()-1);
        else
            parentName = new CompositeName();
        log.debug("parentName="+parentName);
        Context parentCtx = createContext(rootCtx, parentName);
        log.debug("parentCtx="+parentCtx);
        Name atomName = fullName.getSuffix(fullName.size()-1);
        String atom = atomName.get(0);
        if( remoteAccess == true )
        {
            // Bind contextInfo as a Referenceable
            parentCtx.rebind(atom, contextInfo);
            /* Cache the context in the NonSerializableFactory to avoid creating
                more than one context for in VM lookups
            */
            NonSerializableFactory.rebind(atom, ctx);
        }
        else
        {
            /* Bind a reference to the extern context using
             NonSerializableFactory as the ObjectFactory */
            NonSerializableFactory.rebind(parentCtx, atom, ctx);
        }
    }

    private void unbind(String jndiName)
    {
        try
        {
            Context rootCtx = (Context) new InitialContext();
            Context ctx = (Context) rootCtx.lookup(jndiName);
            if( ctx != null )
                ctx.close();
            rootCtx.unbind(jndiName);
            NonSerializableFactory.unbind(jndiName);
        }
        catch(NamingException e)
        {
            log.exception(e);
        }
    }

    /** The external InitialContext information class. It acts as the
        RefAddr and ObjectFactory for the external IntialContext and can
        be marshalled to a remote client.
    */
    public static class SerializableInitialContext extends RefAddr
        implements Referenceable, Serializable, ObjectFactory
    {
        private static final long serialVersionUID = -6512260531255770463L;
        private String jndiName;
        private Class contextClass = javax.naming.InitialContext.class;
        private Properties contextProps;
        private transient Context initialContext;

        public SerializableInitialContext()
        {
            this("SerializableInitialContext");
        }
        public SerializableInitialContext(String addrType)
        {
            super(addrType);
        }

        public String getJndiName()
        {
            return jndiName;
        }
        public void setJndiName(String jndiName)
        {
            this.jndiName = jndiName;
        }
        public String getInitialContext()
        {
            return contextClass.getName();
        }
        public void loadClass(String className) throws ClassNotFoundException
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            contextClass = loader.loadClass(className);
        }
        public void loadProperties(String contextPropsURL) throws IOException
        {
            InputStream is = null;
            IOException ex = null;
            contextProps = new Properties();

            // See if this is a URL we can load
            try
            {
                URL url = new URL(contextPropsURL);
                is = url.openStream();
                contextProps.load(is);
                return;
            }
            catch(IOException e)
            {   // Failed, try to locate a classpath resource below
                is = null;
                ex = e;
            }

            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(contextPropsURL);
            if( is == null )
            {
                if( ex != null )
                    throw ex;
                throw new IOException("Failed to locate context props as URL or resource:"+contextPropsURL);
            }
            contextProps.load(is);
        }

        Context newContext() throws Exception
        {
            if( initialContext == null )
            {   // First check the NonSerializableFactory cache
                initialContext = (Context) NonSerializableFactory.lookup(jndiName);
                // Create the context from the contextClass and contextProps
                if( initialContext == null )
                    initialContext = newContext(contextClass, contextProps);
            }
            return initialContext;
        }

        static Context newContext(Class contextClass, Properties contextProps)
            throws Exception
        {
            Context ctx = null;
            try
            {
                ctx = newDefaultContext(contextClass, contextProps);
            }
            catch(NoSuchMethodException e)
            {
                ctx = newLdapContext(contextClass, contextProps);
            }
            return ctx;
        }
        private static Context newDefaultContext(Class contextClass, Properties contextProps)
            throws Exception
        {
            Context ctx = null;
            Class[] types = {Hashtable.class};
            Constructor ctor = contextClass.getConstructor(types);
            Object[] args = {contextProps};
            ctx = (Context) ctor.newInstance(args);
            return ctx;
        }
        private static Context newLdapContext(Class contextClass, Properties contextProps)
            throws Exception
        {
            Context ctx = null;
            Class[] types = {Hashtable.class, Control[].class};
            Constructor ctor = contextClass.getConstructor(types);
            Object[] args = {contextProps, null};
            ctx = (Context) ctor.newInstance(args);
            return ctx;
        }
        
        public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
        {
            Reference ref = (Reference) obj;
            SerializableInitialContext sic = (SerializableInitialContext) ref.get(0);
            return sic.newContext();
        }
        
        public Reference getReference() throws NamingException
        {
            Reference ref = new Reference(Context.class.getName(), this, this.getClass().getName(), null);
            return ref;
        }

        public Object getContent()
        {
            return null;
        }
    }

}
