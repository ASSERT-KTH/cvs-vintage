package org.tigris.scarab.util;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;

public class ReferenceInsertionFilter
    implements ReferenceInsertionEventHandler
{
    public Object referenceInsert(String reference, Object value)
    {
        if (value instanceof String)
        {
            System.out.println (value);
        }
        return value;
    }
}
