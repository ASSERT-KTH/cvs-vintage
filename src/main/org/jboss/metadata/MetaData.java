package org.jboss.metadata;

import java.util.*;

public interface MetaData extends Map {
    public boolean hasProperty(String name);
    public Object getProperty(String name);
    public void setProperty(String name, Object value);
    public String[] getPropertyNames();
    public MetaDataPlugin getManager();
}