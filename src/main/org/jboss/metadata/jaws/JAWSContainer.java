package org.jboss.metadata.jaws;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JAWSContainer extends AbstractContainer {
    public String cmpDataSource;
    public String cmpDBType;

    public JAWSContainer() {
    }

    public MetaDataPlugin getManager() {
        return JAWSPlugin.instance();
    }
}