/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import javax.management.*;
import javax.naming.*;

import org.jnp.server.Main;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/** A simple utlity mbean that allows one to recursively list the default
JBoss InitialContext.

@author Scott_Stark@displayscape.com
@version $Revision: 1.1 $
*/
public class JNDIView extends ServiceMBeanSupport implements JNDIViewMBean
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    public JNDIView()
    {
    }

    // Public --------------------------------------------------------

    public String list(boolean verbose)
    {
        StringBuffer buffer = new StringBuffer();
        try
        {
            InitialContext ctx = new InitialContext();
            list(ctx, " ", buffer, verbose);
        }
        catch(NamingException e)
        {
            log.exception(e);
        }
        buffer.insert(0, "<pre>");
        buffer.append("</pre>");
        return buffer.toString();
    }

    public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
    {
        return new ObjectName(OBJECT_NAME);
    }

    public String getName()
    {
        return "JNDIView";
    }

    public void initService()
      throws Exception
    {
    }

    public void startService()
      throws Exception
    {
    }

    public void stopService()
    {
    }

    private void list(Context ctx, String indent, StringBuffer buffer, boolean verbose)
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
             NamingEnumeration ne = ctx.list("");
             while( ne.hasMore() )
             {
                NameClassPair pair = (NameClassPair) ne.next();
                boolean recursive = false;
                try
                {
                    Class c = loader.loadClass(pair.getClassName());
                    if (Context.class.isAssignableFrom (c))
                        recursive = true;
                }
                catch(ClassNotFoundException cnfe)
                {
                }

                buffer.append(indent +  " +- " + pair.getName());
                if( verbose )
                    buffer.append(" (class: "+pair.getClassName()+")");
                buffer.append('\n');
                if( recursive )
                {
                    String ctxName = pair.getName();
                    try
                    {
                        Object value = ctx.lookup(ctxName);
                        if( value instanceof Context )
                        {
                            Context subctx = (Context) value;
                            list(subctx, indent + " |  ", buffer, verbose);
                        }
                        else
                        {
                            buffer.append("NonContext: "+value);
                            buffer.append('\n');
                        }
                    }
                    catch(Throwable t)
                    {
                        buffer.append("Failed to lookup: "+ctxName+", errmsg="+t.getMessage());
                        buffer.append('\n');
                    }
               }
            }
            ne.close ();
        }
        catch(NamingException ne)
        {
            buffer.append("error while listing context "+ctx.toString () + ": " + ne.getMessage());
            buffer.append('\n');
            log.exception(ne);
        }
    }
}

