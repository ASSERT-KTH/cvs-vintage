package org.jboss.metadata.aggregate;

import org.jboss.metadata.*;
import java.util.*;

public class AggregateContainer extends AggregateMetaData implements ContainerMetaData {
    public AggregateContainer() {
        super();
    }

    public AggregateContainer(ContainerMetaData[] plugins) {
        super(plugins);
    }
}