package org.jboss.metadata.ejbjar;

import org.jboss.metadata.*;
import org.jboss.metadata.plugins.*;

public class EJBServer extends AbstractServer {
    static {
        MetaDataFactory.addPlugin(EJBPlugin.instance());
    }

    public EJBServer() {
    }

    public MetaDataPlugin getManager() {
        return EJBPlugin.instance();
    }
}