package org.jboss.metadata.jboss;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.io.XMLReader;

public class JBossPlugin implements MetaDataPlugin {
    private final static JBossPlugin instance = new JBossPlugin();
    public static JBossPlugin instance() {
        return instance;
    }

    private JBossPlugin() {}

    public Class getServerClass() {
        return JBossServer.class;
    }

    public Class getBeanClass() {
        return JBossBean.class;
    }

    public Class getContainerClass() {
        return JBossContainer.class;
    }

    public Class getMethodClass() {
        return JBossMethod.class;
    }

    public Class getFieldClass() {
        return JBossField.class;
    }

    public XMLReader getXMLReader() {
        return new JBossXMLReader();
    }

    public boolean equals(Object o) {
        return o instanceof JBossPlugin;
    }

    public int hashCode() {
        return getClass().hashCode();
    }
}