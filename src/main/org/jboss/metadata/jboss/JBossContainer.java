package org.jboss.metadata.jboss;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JBossContainer extends AbstractContainer {
    public boolean secure;
    public Class configurationClass;
    public String name;
    public boolean callLogging;
    public Class containerInvoker;
    public boolean invokerOptimized;
    public Class instancePool;
    public int poolMinimum;
    public int poolMaximum;
    public Class instanceCache;
    public Class persistenceManager;
    public Class transactionManager;
    public char commitOption;

    public JBossContainer() {
    }

    public MetaDataPlugin getManager() {
        return JBossPlugin.instance();
    }
}