package org.jboss.metadata;

import java.util.Set;

public interface ServerMetaData extends MetaData {
    public Set getBeans();
    public BeanMetaData getBean(String name);
}