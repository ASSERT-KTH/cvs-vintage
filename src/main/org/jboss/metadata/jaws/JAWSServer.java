package org.jboss.metadata.jaws;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JAWSServer extends AbstractServer {

    public JAWSServer() {
    }

    public MetaDataPlugin getManager() {
        return JAWSPlugin.instance();
    }
}