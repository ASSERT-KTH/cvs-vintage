package org.jboss.metadata.jaws;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class JAWSBean extends AbstractBean {
    public String tableName;
    public boolean createTable;
    public boolean removeTable;
    public boolean tunedUpdates;
    public boolean readOnly;
    public int timeOut;

    public JAWSBean() {
        super();
    }

    public MetaDataPlugin getManager() {
        return JAWSPlugin.instance();
    }
}