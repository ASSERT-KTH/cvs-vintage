package org.jboss.metadata.plugins;

import java.util.*;
import org.jboss.metadata.*;

public abstract class AbstractMethod extends AbstractMetaData implements MethodMetaData {
    private String name;
    private String[] paramTypes;

    public AbstractMethod() {
        paramTypes = new String[0];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getParameterTypes() {
        return paramTypes;
    }

    public void setParameterTypes(String[] paramTypes) {
        this.paramTypes = paramTypes;
    }
}