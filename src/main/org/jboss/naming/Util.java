

package org.jboss.naming;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/** A static utility class for common JNDI operations.
 *
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.3 $
 */
public class Util
{

    /** Create a subcontext including any intermediate contexts.
    @param ctx, the parent JNDI Context under which value will be bound
    @param name, the name relative to ctx of the subcontext.
    @return The new or existing JNDI subcontext
    @throws NamingException, on any JNDI failure
    */
    public static Context createSubcontext(Context ctx, String name) throws NamingException
    {
        Name n = ctx.getNameParser("").parse(name);
        return createSubcontext(ctx, n);
    }
    /** Create a subcontext including any intermediate contexts.
    @param ctx, the parent JNDI Context under which value will be bound
    @param name, the name relative to ctx of the subcontext.
    @return The new or existing JNDI subcontext
    @throws NamingException, on any JNDI failure
    */
    public static Context createSubcontext(Context ctx, Name name) throws NamingException
    {
        Context subctx = ctx;
        for(int pos = 0; pos < name.size(); pos ++)
        {
            String ctxName = name.get(pos);
            try
            {
                subctx = (Context) ctx.lookup(ctxName);
            }
            catch(NameNotFoundException e)
            {
                subctx = ctx.createSubcontext(ctxName);
            }
        }
        return subctx;
    }

    /** Bind val to name in ctx, and make sure that all intermediate contexts exist
    @param ctx, the parent JNDI Context under which value will be bound
    @param name, the name relative to ctx where value will be bound
    @param value, the value to bind.
    */
    public static void bind(Context ctx, String name, Object value) throws NamingException
    {
        Name n = ctx.getNameParser("").parse(name);
        String atom = n.get(n.size()-1);
        Context parentCtx = createSubcontext(ctx, n.getPrefix(n.size()-1));
        parentCtx.bind(atom, value);
    }
}
