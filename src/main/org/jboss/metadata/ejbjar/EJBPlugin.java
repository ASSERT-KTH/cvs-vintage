package org.jboss.metadata.ejbjar;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.io.XMLReader;

public class EJBPlugin implements MetaDataPlugin {
    private final static EJBPlugin instance = new EJBPlugin();
    public static EJBPlugin instance() {
        return instance;
    }

    private EJBPlugin() {}

    public Class getServerClass() {
        return EJBServer.class;
    }

    public Class getBeanClass() {
        return EJBBean.class;
    }

    public Class getContainerClass() {
        return EJBContainer.class;
    }

    public Class getMethodClass() {
        return EJBMethod.class;
    }

    public Class getFieldClass() {
        return EJBField.class;
    }

    public XMLReader getXMLReader() {
        return new EJBXMLReader();
    }

    public boolean equals(Object o) {
        return o instanceof EJBPlugin;
    }

    public int hashCode() {
        return getClass().hashCode();
    }
}