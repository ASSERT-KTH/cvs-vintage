package org.jboss.metadata.jboss;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JBossMethod extends AbstractMethod {

    public JBossMethod() {
    }

    public MetaDataPlugin getManager() {
        return JBossPlugin.instance();
    }
}