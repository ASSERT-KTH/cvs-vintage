/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * Part listener list.
 */
public class PartListenerList2 {
	private ListenerList listeners = new ListenerList();
/**
 * PartNotifier constructor comment.
 */
public PartListenerList2() {
	super();
}
/**
 * Adds an PartListener to the part service.
 */
public void addPartListener(IPartListener2 l) {
	listeners.add(l);
}
/**
 * Notifies the listener that a part has been activated.
 */
public void firePartActivated(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partActivated(ref);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been brought to top.
 */
public void firePartBroughtToTop(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partBroughtToTop(ref);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been closed
 */
public void firePartClosed(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partClosed(ref);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been deactivated.
 */
public void firePartDeactivated(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partDeactivated(ref);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePartOpened(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l = (IPartListener2)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partOpened(ref);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePartHidden(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l;
		if(array[i] instanceof IPartListener2)
			l = (IPartListener2)array[i];
		else
			continue;
			
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partHidden(ref);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePartVisible(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l;
		if(array[i] instanceof IPartListener2)
			l = (IPartListener2)array[i];
		else
			continue;
			
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partVisible(ref);
			}
		});
	}
}
/**
 * Notifies the listener that a part has been opened.
 */
public void firePartInputChanged(final IWorkbenchPartReference ref) {
	Object [] array = listeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final IPartListener2 l;
		if(array[i] instanceof IPartListener2)
			l = (IPartListener2)array[i];
		else
			continue;
			
		Platform.run(new SafeRunnable() {
			public void run() {
				l.partInputChanged(ref);
			}
		});
	}
}
/**
 * Removes an IPartListener from the part service.
 */
public void removePartListener(IPartListener2 l) {
	listeners.remove(l);
}
}
