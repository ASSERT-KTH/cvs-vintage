package org.jboss.metadata.aggregate;

import org.jboss.metadata.*;

public class AggregateField extends AggregateMetaData implements FieldMetaData {
    private String name;

    public AggregateField(String name) {
        this.name = name;
    }

    public AggregateField(String name, FieldMetaData[] plugins) {
        super(plugins);
        this.name = name;
    }

    public void addPlugin(FieldMetaData plugin) {
        super.addPlugin(plugin);
    }

    public String getName() {
        return name;
    }
}