package org.jboss.metadata;

import org.jboss.metadata.io.*;

public interface MetaDataPlugin {
    public Class getServerClass();
    public Class getBeanClass();
    public Class getContainerClass();
    public Class getMethodClass();
    public Class getFieldClass();
    public XMLReader getXMLReader();
}