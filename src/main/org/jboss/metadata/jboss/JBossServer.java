package org.jboss.metadata.jboss;

import org.jboss.metadata.*;
import org.jboss.metadata.plugins.*;

public class JBossServer extends AbstractServer {
    static {
        MetaDataFactory.addPlugin(JBossPlugin.instance());
    }

    public JBossServer() {
    }

    public MetaDataPlugin getManager() {
        return JBossPlugin.instance();
    }
}