package org.jboss.metadata.ejbjar;

import java.util.*;
import org.jboss.metadata.*;
import org.jboss.metadata.plugins.AbstractBean;

public class EJBBean extends AbstractBean {
    public String description;
    public String displayName;
    public Class homeClass;
    public Class remoteClass;
    public Class implementationClass;
    public String persistanceType;
    public Class primaryKeyClass;
    public boolean reentrant;

    public EJBBean() {
        super();
        setContainerMetaData(new EJBContainer());
    }

    public MetaDataPlugin getManager() {
        return EJBPlugin.instance();
    }
}