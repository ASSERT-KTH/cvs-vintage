/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.io.InputStream;
import java.io.IOException;
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
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.ldap.Control;

import org.jnp.server.Main;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/** A MBean that binds an arbitrary InitialContext into the JBoss default
InitialContext as a Reference to a nonserializable object.

@see org.jboss.naming.NonSerializableFactory

@author Scott_Stark@displayscape.com
@version $Revision: 1.2 $
 */
public class ExternalContext extends ServiceMBeanSupport implements ExternalContextMBean
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private String jndiName;
    private Class contextClass = javax.naming.InitialContext.class;
    private Properties contextProps;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    public ExternalContext()
    {
    }
    public ExternalContext(String jndiName, String contextPropsURL) throws IOException
    {
        setJndiName(jndiName);
        setProperties(contextPropsURL);
    }

    // Public --------------------------------------------------------
    /** Set the jndi name under which the external context is bound.
    */
    public String getJndiName()
    {
       return jndiName;
    }
    /** Set the jndi name under which the external context is bound.
     */
    public void setJndiName(String jndiName)
    {
       this.jndiName = jndiName;
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
	return contextClass.getName();
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
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	contextClass = loader.loadClass(className);
    }

    public void setProperties(String contextPropsURL) throws IOException
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
        if( log != null )
            log.debug("ContextProps: "+contextProps);
    }

    public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
    {
        return new ObjectName(OBJECT_NAME);
    }

    public String getName()
    {
        return "ExternalContext(" + jndiName + ")";
    }

    public void initService()
      throws Exception
    {
    }

    public void startService()
      throws Exception
    {
	Class[] types = {Hashtable.class};
	Class[] ldapTypes = {Hashtable.class, Control[].class};
	Context ctx = null;
	try
	{
	    Constructor ctor = contextClass.getConstructor(types);
	    Object[] args = {contextProps};
	    ctx = (Context) ctor.newInstance(args);
	}
	catch(NoSuchMethodException e)
	{ // Try the ldap constructor
	    Constructor ctor = contextClass.getConstructor(ldapTypes);
	    Object[] args = {contextProps, null};
	    ctx = (Context) ctor.newInstance(args);
	}    
        Context rootCtx = (Context) new InitialContext();
        log.debug("ctx="+ctx+", env="+ctx.getEnvironment());
        // Get the parent context into which we are to bind
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
        // Place the external context into the NonSerializableFactory hashmap
        NonSerializableFactory.rebind(jndiName, ctx);

        // Bind a reference to the extern context using NonSerializableFactory as the ObjectFactory
        String className = "javax.naming.Context";
        String factory = NonSerializableFactory.class.getName();
        StringRefAddr addr = new StringRefAddr("nns", jndiName);
        Reference memoryRef = new Reference(className, addr, factory, null);
        Name atom = fullName.getSuffix(fullName.size()-1);
        parentCtx.rebind(atom, memoryRef);
    }

    public void stopService()
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
}
