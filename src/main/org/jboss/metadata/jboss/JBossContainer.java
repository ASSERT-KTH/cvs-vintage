package org.jboss.metadata.jboss;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JBossContainer extends AbstractContainer {
    public boolean secure;
    public String configurationClass;
    public String name;
    public boolean callLogging;
    public String containerInvokerClass;
    public boolean invokerOptimized;
    public String instancePoolClass;
    public int poolMinimum;
    public int poolMaximum;
    public String instanceCacheClass;
    public String persistenceManagerClass;
    public String transactionManagerClass;
    public char commitOption;

    public JBossContainer() {
    }

    public MetaDataPlugin getManager() {
        return JBossPlugin.instance();
    }
}