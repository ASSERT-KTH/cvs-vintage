package org.jboss.metadata.ejbjar;

import org.jboss.metadata.MetaDataPlugin;
import org.jboss.metadata.plugins.*;

public class EJBField extends AbstractField {
    public boolean isCMP;

    public EJBField() {
    }

    public MetaDataPlugin getManager() {
        return EJBPlugin.instance();
    }
}