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

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Manages a pool of shells. This can be used instead of creating and destroying
 * shells. By reusing shells, they will never be disposed until the pool goes away.
 * This is useful in situations where client code may have cached pointers to the
 * shells to use as a parent for dialogs. It also works around bug 86226 (SWT menus
 * cannot be reparented).
 * 
 * @since 3.1
 */
public class ShellPool {
    
    private int flags;
    
    /**
     * Parent shell (or null if none)
     */
    private Shell parentShell;
    
    private LinkedList availableShells = new LinkedList();
    
    private final static String CLOSE_LISTENER = "close listener"; //$NON-NLS-1$
    
    private boolean isDisposed = false;
    
    private Listener closeListener = new Listener() {
        public void handleEvent(Event e) {
                if (isDisposed) {
                    return;
                }
            
                if (e.doit) {
                    Shell s = (Shell)e.widget;
                    ShellListener l = (ShellListener)s.getData(CLOSE_LISTENER);
                    s.removeShellListener(l);
                    s.removeListener(SWT.Close, this);
                    Control[] children = s.getChildren();
                    for (int i = 0; i < children.length; i++) {
                        Control control = children[i];
                      
                        control.dispose();
                    }
                    availableShells.add(s);
                    s.setVisible(false);
                }
                e.doit = false;
         }
    };
    
    /**
     * Creates a shell pool that allocates shells that are children of the
     * given parent and are created with the given flags.
     * 
     * @param parentShell parent shell (may be null, indicating that this pool creates
     * top-level shells)
     * @param childFlags flags for all child shells
     */
    public ShellPool(Shell parentShell, int childFlags) {
        this.parentShell = parentShell;
        this.flags = childFlags;
    }
    
    /**
     * Returns a new shell. The shell must not be disposed directly, but it may be closed.
     * Once the shell is closed, it will be returned to the shell pool. Note: callers must
     * remove all listeners from the shell before closing it.
     */
    public Shell allocateShell(ShellListener closeListener) {
        Shell result;
        if (!availableShells.isEmpty()) {
            result = (Shell)availableShells.removeFirst();
        } else {
            result = new Shell(parentShell, flags);
        }
        
        result.addShellListener(closeListener);
        result.setData(CLOSE_LISTENER, closeListener);
        result.addListener(SWT.Close, this.closeListener);
        return result;
    }
    
    /**
     * Disposes this pool. Any unused shells in the pool are disposed immediately,
     * and any shells in use will be disposed once they are closed.
     * 
     * @since 3.1
     */
    public void dispose() {
        for (Iterator iter = availableShells.iterator(); iter.hasNext();) {
            Shell next = (Shell) iter.next();
            
            next.dispose();
        }
        
        availableShells.clear();
        isDisposed = true;
    }
}
