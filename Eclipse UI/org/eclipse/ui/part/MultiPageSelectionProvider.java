/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorPart;

/**
 * Manages the current selection in a multi-page editor by tracking the active
 * nested editor within the multi-page editor. When the selection changes,
 * notifications are sent to all registered listeners.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * The base implementation of <code>MultiPageEditor.init</code> creates 
 * an instance of this class.
 * </p>
 */
public class MultiPageSelectionProvider implements ISelectionProvider {

    /**
     * Registered selection changed listeners (element type: 
     * <code>ISelectionChangedListener</code>).
     */
    private ListenerList listeners = new ListenerList();

    /**
     * The multi-page editor.
     */
    private MultiPageEditorPart multiPageEditor;

    /**
     * Creates a selection provider for the given multi-page editor.
     *
     * @param multiPageEditor the multi-page editor
     */
    public MultiPageSelectionProvider(MultiPageEditorPart multiPageEditor) {
        Assert.isNotNull(multiPageEditor);
        this.multiPageEditor = multiPageEditor;
    }

    /* (non-Javadoc)
     * Method declared on <code>ISelectionProvider</code>.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all registered selection changed listeners that the editor's 
     * selection has changed. Only listeners registered at the time this method is
     * called are notified.
     *
     * @param event the selection changed event
     */
    public void fireSelectionChanged(final SelectionChangedEvent event) {
        Object[] listeners = this.listeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
            Platform.run(new SafeRunnable() {
                public void run() {
                    l.selectionChanged(event);
                }
            });
        }
    }

    /**
     * Returns the multi-page editor.
     */
    public MultiPageEditorPart getMultiPageEditor() {
        return multiPageEditor;
    }

    /* (non-Javadoc)
     * Method declared on <code>ISelectionProvider</code>.
     */
    public ISelection getSelection() {
        IEditorPart activeEditor = multiPageEditor.getActiveEditor();
        if (activeEditor != null) {
            ISelectionProvider selectionProvider = activeEditor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null)
                return selectionProvider.getSelection();
        }
        return null;
    }

    /* (non-JavaDoc)
     * Method declaed on <code>ISelectionProvider</code>.
     */
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * Method declared on <code>ISelectionProvider</code>.
     */
    public void setSelection(ISelection selection) {
        IEditorPart activeEditor = multiPageEditor.getActiveEditor();
        if (activeEditor != null) {
            ISelectionProvider selectionProvider = activeEditor.getSite()
                    .getSelectionProvider();
            if (selectionProvider != null)
                selectionProvider.setSelection(selection);
        }
    }
}