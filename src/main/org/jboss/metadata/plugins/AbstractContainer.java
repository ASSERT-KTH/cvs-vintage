package org.jboss.metadata.plugins;

import org.jboss.metadata.*;
import java.util.*;

public abstract class AbstractContainer extends AbstractMetaData
        implements ContainerMetaData, Cloneable {

    public AbstractContainer() {
    }

    public Object clone() {
        try {
            return super.clone();
        } catch(CloneNotSupportedException e) {
            return null;
        }
    }
}