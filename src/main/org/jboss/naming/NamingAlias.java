/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import org.jboss.util.ServiceMBean;
import org.jboss.util.ServiceMBeanSupport;

/** A simple utility mbean that allows one to create an alias in
the form of a LinkRef from one JNDI name to another.

@author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.2 $
*/
public class NamingAlias extends ServiceMBeanSupport implements NamingAliasMBean
{
    private String fromName;
    private String toName;

    /** Creates new NamingAlias */
    public NamingAlias()
    {
    }
    public NamingAlias(String fromName, String toName)
    {
    }

// --- Begin NamingAliasMBean interface methods
    public String getFromName()
    {
        return fromName;
    }
    public void setFromName(String name) throws NamingException
    {
        removeLinkRef(fromName);
        this.fromName = name;
        createLinkRef();        
    }
    public String getToName()
    {
        return toName;
    }
    public void setToName(String name) throws NamingException
    {
        this.toName = name;
        createLinkRef();
    }
// --- End NamingAliasMBean interface methods

// --- Begin ServiceMBeanSupport methods
    public String getName()
    {
        return "NamingAlias("+fromName+" -> "+toName+")";
    }
    public void startService() throws Exception
    {
        if( fromName == null )
            throw new IllegalStateException("fromName is null");
        if( toName == null )
            throw new IllegalStateException("toName is null");
        createLinkRef();
    }
    public void stopService()
    {
        try
        {
            removeLinkRef(fromName);
        }
        catch(Exception e)
        {
        }
    }
// --- Begin ServiceMBeanSupport methods

    /**
    */
    private void createLinkRef() throws NamingException
    {
        if( super.getState() == ServiceMBean.STARTING || super.getState() == ServiceMBean.STARTED )
        {
            InitialContext ctx = new InitialContext();
            LinkRef link = new LinkRef(toName);
            Context fromCtx = ctx;
            Name name = ctx.getNameParser("").parse(fromName);
            String atom = name.get(name.size()-1);
            for(int n = 0; n < name.size()-1; n ++)
            {
                String comp = name.get(n);
                try
                {
                    fromCtx = (Context) fromCtx.lookup(comp);
                }
                catch(NameNotFoundException e)
                {
                    fromCtx = fromCtx.createSubcontext(comp);
                }
            }
            fromCtx.rebind(atom, link);
        }
    }
    /** Unbind the name value if we are in the STARTED state.
    */
    private void removeLinkRef(String name) throws NamingException
    {
        if( super.getState() == ServiceMBean.STARTED )
        {
            InitialContext ctx = new InitialContext();
            ctx.unbind(name);
        }
    }
}
