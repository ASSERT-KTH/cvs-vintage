package org.jboss.metadata.jaws;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.io.XMLReader;

public class JAWSPlugin implements MetaDataPlugin {
    private final static JAWSPlugin instance = new JAWSPlugin();
    public static JAWSPlugin instance() {
        return instance;
    }

    private JAWSPlugin() {}

    public Class getServerClass() {
        return JAWSServer.class;
    }

    public Class getBeanClass() {
        return JAWSBean.class;
    }

    public Class getContainerClass() {
        return JAWSContainer.class;
    }

    public Class getMethodClass() {
        return JAWSMethod.class;
    }

    public Class getFieldClass() {
        return JAWSField.class;
    }

    public XMLReader getXMLReader() {
        return new JAWSXMLReader();
    }
}