package org.jboss.metadata.jaws;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JAWSField extends AbstractField {
    public String columnName;
    public String sqlType;
    public int jdbcType;

    public JAWSField() {
    }

    public MetaDataPlugin getManager() {
        return JAWSPlugin.instance();
    }
}