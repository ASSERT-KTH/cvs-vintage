package org.jboss.metadata;

import java.util.Set;

public interface BeanMetaData extends MetaData {
    public MethodMetaData getMethod(String name, Class[] args);
    public MethodMetaData getHomeMethod(String name, Class[] args);
    public FieldMetaData getField(String name);
    public ContainerMetaData getContainer();
    public Set getMethods();
    public Set getHomeMethods();
    public Set getFields();
    public String getName();
}