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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Perspective listener list.
 */
public class PerspectiveListenerList {
    private ListenerList listeners = new ListenerList();

    /**
     * PerspectiveListenerList constructor comment.
     */
    public PerspectiveListenerList() {
        super();
    }

    /**
     * Adds an IPerspectiveListener to the perspective service.
     */
    public void addPerspectiveListener(IPerspectiveListener l) {
        listeners.add(l);
    }

    /**
     * Notifies the listener that a perspective has been activated.
     */
    public void firePerspectiveActivated(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = listeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            final IPerspectiveListener l = (IPerspectiveListener) array[nX];
            Platform.run(new SafeRunnable() {
                public void run() {
                    l.perspectiveActivated(page, perspective);
                }
            });
        }
    }

    /**
     * Notifies the listener that a perspective has been deactivated.
     * 
     * @since 3.1
     */
    public void firePerspectiveDeactivated(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = listeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveDeactivated(page, perspective);
                    }
                });
            }
        }
    }

    /**
     * Notifies the listener that a perspective has been changed.
     */
    public void firePerspectiveChanged(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective, final String changeId) {
        Object[] array = listeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            final IPerspectiveListener l = (IPerspectiveListener) array[nX];
            Platform.run(new SafeRunnable() {
                public void run() {
                    l.perspectiveChanged(page, perspective, changeId);
                }
            });
        }
    }

    /**
     * Notifies the listener that a part has been affected
     * in the given perspective.
     * 
     * @since 3.0
     */
    public void firePerspectiveChanged(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective,
            final IWorkbenchPartReference partRef, final String changeId) {
        Object[] array = listeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener2) {
                final IPerspectiveListener2 l2 = (IPerspectiveListener2) array[nX];
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l2.perspectiveChanged(page, perspective, partRef,
                                changeId);
                    }
                });
            }
        }
    }

    /**
     * Notifies the listener that a perspective has been closed.
     * 
     * @since 3.1
     */
    public void firePerspectiveClosed(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = listeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveClosed(page, perspective);
                    }
                });
            }
        }
    }

    /**
     * Notifies the listener that a perspective has been opened.
     * 
     * @since 3.1
     */
    public void firePerspectiveOpened(final IWorkbenchPage page,
            final IPerspectiveDescriptor perspective) {
        Object[] array = listeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveOpened(page, perspective);
                    }
                });
            }
        }
    }
    
    /**
     * Notifies the listener that a perspective has been deactivated.
     * 
     * @since 3.1
     */
    public void firePerspectiveSavedAs(final IWorkbenchPage page,
            final IPerspectiveDescriptor oldPerspective,
            final IPerspectiveDescriptor newPerspective) {
        Object[] array = listeners.getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            if (array[nX] instanceof IPerspectiveListener3) {
                final IPerspectiveListener3 l3 = (IPerspectiveListener3) array[nX];
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l3.perspectiveSavedAs(page, oldPerspective, newPerspective);
                    }
                });
            }
        }
    }

    /**
     * Removes an IPerspectiveListener from the perspective service.
     */
    public void removePerspectiveListener(IPerspectiveListener l) {
        listeners.remove(l);
    }
}