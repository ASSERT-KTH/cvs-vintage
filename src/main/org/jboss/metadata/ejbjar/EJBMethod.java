package org.jboss.metadata.ejbjar;

import org.jboss.metadata.*;
import org.jboss.metadata.plugins.AbstractMethod;

public class EJBMethod extends AbstractMethod {
    public static final byte TX_NOT_SUPPORTED  = 0;
    public static final byte TX_REQUIRED       = 1;
    public static final byte TX_SUPPORTS       = 2;
    public static final byte TX_REQUIRES_NEW   = 3;
    public static final byte TX_MANDATORY      = 4;
    public static final byte TX_NEVER          = 5;

    public byte transactionAttribute = TX_SUPPORTS;

    public EJBMethod() {
    }

    public MetaDataPlugin getManager() {
        return EJBPlugin.instance();
    }
}