package org.jboss.metadata.plugins;

import java.util.*;
import org.jboss.metadata.*;

public abstract class AbstractMethod extends AbstractMetaData implements MethodMetaData {
    private String name;
    private Class[] paramTypes;

    public AbstractMethod() {
        paramTypes = new Class[0];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class[] getParameterTypes() {
        return paramTypes;
    }

    public void setParameterTypes(Class[] paramTypes) {
        this.paramTypes = paramTypes;
    }
}