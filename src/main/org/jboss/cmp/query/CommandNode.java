package org.jboss.cmp.query;

import org.jboss.cmp.schema.AbstractType;

public interface CommandNode extends QueryNode
{
   AbstractType[] getParameters();
}
