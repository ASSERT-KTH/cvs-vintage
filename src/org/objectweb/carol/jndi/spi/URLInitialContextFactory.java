package org.objectweb.carol.jndi.spi;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;

public class URLInitialContextFactory implements InitialContextFactory {

    public Context getInitialContext(Hashtable env) throws NamingException {
        String provider = (String) env.get(Context.PROVIDER_URL);
        String protocol = provider.substring(0, provider.indexOf(':'));
        Context ctx = NamingManager.getURLContext(protocol, env);
        return (Context) ctx.lookup(provider);
    }
}
