package org.jboss.metadata;

public interface MethodMetaData extends MetaData {
    public String getName();
    public Class[] getParameterTypes();
}