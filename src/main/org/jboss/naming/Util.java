

package org.jboss.naming;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/** A static utility class for common JNDI operations.
 *
 * @author  Scott_Stark@displayscape.com
 * @version 
 */
public class Util
{

    /** Bind val to name in ctx, and make sure that all intermediate contexts exist
    */
    public static void bind(Context ctx, String name, Object value) throws NamingException
    {
        Name n = ctx.getNameParser("").parse(name);
        while( n.size() > 1 )
        {
            String ctxName = n.get(0);
            try
            {
                ctx = (Context) ctx.lookup(ctxName);
            }
            catch(NameNotFoundException e)
            {
                ctx = ctx.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }
        ctx.bind(n.get(0), value);
    }
}
