/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is a partial implementation of <code>IHandler</code>. This
 * abstract implementation provides support for handler listeners. You should
 * subclass from this method unless you want to implement your own listener
 * support. Subclasses should call
 * {@link AbstractHandler#fireHandlerChanged(HandlerEvent)}when the handler
 * changes. Subclasses should also override
 * {@link AbstractHandler#getAttributeValuesByName()}if they have any
 * attributes.
 * 
 * @since 3.0
 */
public abstract class AbstractHandler implements IHandler {

    /**
     * Those interested in hearing about changes to this instance of
     * <code>IHandler</code>.
     */
    private List handlerListeners;

    /**
     * @see IHandler#addHandlerListener(IHandlerListener)
     */
    public void addHandlerListener(IHandlerListener handlerListener) {
        if (handlerListener == null) throw new NullPointerException();
        if (handlerListeners == null) handlerListeners = new ArrayList();
        if (!handlerListeners.contains(handlerListener))
                handlerListeners.add(handlerListener);
    }

    /**
     * The default implementation does nothing. Subclasses who attach listeners
     * to other objects are encouraged to detach them in this method.
     * 
     * @see org.eclipse.ui.commands.IHandler#dispose()
     */
    public void dispose() {
        // Do nothing.
    }
    
    /**
     * Fires an event to all registered listeners describing changes to this
     * instance.
     * 
     * @param handlerEvent
     *            the event describing changes to this instance. Must not be
     *            <code>null</code>.
     */
    protected void fireHandlerChanged(HandlerEvent handlerEvent) {
        if (handlerEvent == null) throw new NullPointerException();
        if (handlerListeners != null)
                for (int i = 0; i < handlerListeners.size(); i++)
                    ((IHandlerListener) handlerListeners.get(i))
                            .handlerChanged(handlerEvent);
    }

    /**
     * This simply return an empty map. The default implementation has no
     * attributes.
     * 
     * @see IHandler#getAttributeValuesByName()
     */
    public Map getAttributeValuesByName() {
        return Collections.EMPTY_MAP;
    }

    /**
     * @see IHandler#removeHandlerListener(IHandlerListener)
     */
    public void removeHandlerListener(IHandlerListener handlerListener) {
        if (handlerListener == null) throw new NullPointerException();
        if (handlerListeners != null) handlerListeners.remove(handlerListener);
    }
}