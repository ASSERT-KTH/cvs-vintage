package org.jboss.metadata.aggregate;

import org.jboss.metadata.*;
import java.util.*;

public class AggregateMethod extends AggregateMetaData implements MethodMetaData {
    private String name;
    private String[] params;

    public AggregateMethod(String name, String[] paramTypes) {
        this.name = name;
        params = paramTypes;
    }

    public AggregateMethod(String name, String[] paramTypes, MethodMetaData[] plugins) {
        super(plugins);
        this.name = name;
        params = paramTypes;
    }

    public void addPlugin(MethodMetaData plugin) {
        super.addPlugin(plugin);
    }

    public String getName() {
        return name;
    }

    public String[] getParameterTypes() {
        return params;
    }
}