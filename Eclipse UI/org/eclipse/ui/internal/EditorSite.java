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
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.EditorDescriptor;

/**
 * An editor container manages the services for an editor.
 */
public class EditorSite extends PartSite implements IEditorSite {
	/* package */ static final int PROP_REUSE_EDITOR = -0x101;
	
	private EditorDescriptor desc;
	private boolean reuseEditor = true;
	private ListenerList propChangeListeners = new ListenerList(1);
	
/**
 * Constructs an EditorSite for an editor.  The resource editor descriptor
 * may be omitted for an OLE editor.
 */
public EditorSite(IEditorPart editor, WorkbenchPage page, 
	EditorDescriptor desc) 
{
	super(editor, page);
	if (desc != null) {
		this.desc = desc;
		setConfigurationElement(desc.getConfigurationElement());
	}
}

/**
 * Returns the editor action bar contributor for this editor.
 * <p>
 * An action contributor is responsable for the creation of actions.
 * By design, this contributor is used for one or more editors of the same type.
 * Thus, the contributor returned by this method is not owned completely
 * by the editor.  It is shared.
 * </p>
 *
 * @return the editor action bar contributor
 */
public IEditorActionBarContributor getActionBarContributor() {
	EditorActionBars bars = (EditorActionBars)getActionBars();
	if (bars != null)
		return bars.getEditorContributor();
	else
		return null;
}
/**
 * Returns the extension editor action bar contributor for this editor.
 */
public IEditorActionBarContributor getExtensionActionBarContributor() {
	EditorActionBars bars = (EditorActionBars)getActionBars();
	if (bars != null)
		return bars.getExtensionContributor();
	else
		return null;
}
/**
 * Returns the editor
 */
public IEditorPart getEditorPart() {
	return (IEditorPart)getPart();
}

public EditorDescriptor getEditorDescriptor() {
	return desc;
}

public boolean getReuseEditor() {
	return reuseEditor;
}
	
public void setReuseEditor(boolean reuse) {
	reuseEditor = reuse;
	firePropertyChange(PROP_REUSE_EDITOR);
}
protected String getInitialScopeId() {
	return "org.eclipse.ui.textEditorScope"; //$NON-NLS-1$
}
public void addPropertyListener(IPropertyListener l) {
	propChangeListeners.add(l);
}
public void removePropertyListener(IPropertyListener l) {
	propChangeListeners.remove(l);
}
private void firePropertyChange(final int propertyId) {
	Object [] array = propChangeListeners.getListeners();
	for (int nX = 0; nX < array.length; nX ++) {
		final IPropertyListener l = (IPropertyListener)array[nX];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.propertyChanged(EditorSite.this, propertyId);
			}
			public void handleException(Throwable e) {
				super.handleException(e);
				// if an unexpected exception happens, remove it
				// to make sure the workbench keeps running.
				propChangeListeners.remove(l);
			}
		});
	}
}
}
