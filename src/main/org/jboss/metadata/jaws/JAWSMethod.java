package org.jboss.metadata.jaws;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JAWSMethod extends AbstractMethod {

    public JAWSMethod() {
    }

    public MetaDataPlugin getManager() {
        return JAWSPlugin.instance();
    }
}