package org.jboss.metadata.ejbjar;

import java.util.*;
import org.jboss.metadata.*;
import org.jboss.metadata.plugins.AbstractBean;

/**
 * Holds the properties from ejb-jar.xml for an individual bean.
 * Note that the "class" variable hold class names not Class objects so that
 * the validation can be done elsewhere, and we can store the class names at
 * configuration time even if the classes are not on the classpath.
 */
public class EJBBean extends AbstractBean {
    public String description;
    public String displayName;
    public String homeClass;
    public String remoteClass;
    public String implementationClass;
    public String persistanceType;
    public String primaryKeyClass;
    public boolean reentrant;

    public EJBBean() {
        super();
        setContainerMetaData(new EJBContainer());
    }

    public MetaDataPlugin getManager() {
        return EJBPlugin.instance();
    }
}