package org.jboss.metadata.jboss;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JBossBean extends AbstractBean {

    public JBossBean() {
        super();
    }

    public MetaDataPlugin getManager() {
        return JBossPlugin.instance();
    }
}