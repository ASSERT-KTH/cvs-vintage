package org.jboss.metadata.ejbjar;

import java.util.*;
import org.jboss.metadata.*;
import org.jboss.metadata.plugins.AbstractContainer;

public class EJBContainer extends AbstractContainer implements ContainerMetaData {
    public EJBContainer() {
    }

    public MetaDataPlugin getManager() {
        return EJBPlugin.instance();
    }
}