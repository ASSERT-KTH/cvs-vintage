package org.jboss.metadata.jboss;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JBossField extends AbstractField {

    public JBossField() {
    }

    public MetaDataPlugin getManager() {
        return JBossPlugin.instance();
    }
}