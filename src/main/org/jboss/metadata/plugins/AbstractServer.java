package org.jboss.metadata.plugins;

import org.jboss.metadata.*;
import java.util.*;

public abstract class AbstractServer extends AbstractMetaData implements ServerMetaData {
    private HashSet beans;

    public AbstractServer() {
        beans = new HashSet();
    }

    public Set getBeans() {
        return beans;
    }

    public void addBean(BeanMetaData bean) {
        beans.add(bean);
    }

    public BeanMetaData getBean(String name) {
        Iterator it = beans.iterator();
        while(it.hasNext()) {
            BeanMetaData bmd = (BeanMetaData)it.next();
            if(bmd.getName().equals(name))
                return bmd;
        }
        throw new IllegalArgumentException("No such bean!");
    }
}