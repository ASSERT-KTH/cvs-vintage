package org.jboss.metadata.plugins;

import org.jboss.metadata.*;

public abstract class AbstractField extends AbstractMetaData implements FieldMetaData {
    private String name;

    public AbstractField() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}