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
package org.eclipse.ui.internal.misc;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.*;
import java.util.*;
/**
 * Provides content for a tree viewer that shows only containers.
 */
public class ContainerContentProvider implements ITreeContentProvider {
	private Viewer viewer;
	private boolean showClosedProjects = true;
/**
 * Creates a new ResourceContentProvider.
 */
public ContainerContentProvider() {
}
/**
 * The visual part that is using this content provider is about
 * to be disposed. Deallocate all allocated SWT resources.
 */
public void dispose() {}
/**
 * @see ITreeContentProvider#getChildren
 */
public Object[] getChildren(Object element) {
	if (element instanceof IWorkspace) {
		// check if closed projects should be shown
		IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
		if (showClosedProjects)
			return allProjects;
		
		ArrayList accessibleProjects = new ArrayList();
		for (int i = 0; i < allProjects.length; i++){
			if (allProjects[i].isOpen()){
				accessibleProjects.add(allProjects[i]);
			}
		}
		return accessibleProjects.toArray();
	} else if (element instanceof IContainer) {
		IContainer container = (IContainer)element;
		if (container.isAccessible()) {
		    try {
			    List children = new ArrayList();
			    IResource[] members = container.members();
			    for (int i = 0; i < members.length; i++) {
				    if (members[i].getType() != IResource.FILE) {
					    children.add(members[i]);
				    }
			    }
			    return children.toArray();
			} catch (CoreException e) {
				// this should never happen because we call #isAccessible before invoking #members
			}
		}
	}
	return new Object[0];
}
/**
 * @see ITreeContentProvider#getElements
 */
public Object[] getElements(Object element) {
	return getChildren(element);
}
/**
 * @see ITreeContentProvider#getParent
 */
public Object getParent(Object element) {
	if (element instanceof IResource) 
		return ((IResource) element).getParent();
	return null;
}
/**
 * @see ITreeContentProvider#hasChildren
 */
public boolean hasChildren(Object element) {
	return getChildren(element).length > 0;
}
/**
 * @see IContentProvider#inputChanged
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	this.viewer = viewer;
}
/**
 * @see IContentProvider#isDeleted
 */
public boolean isDeleted(Object element) {
	return ((element instanceof IResource) && !((IResource) element).exists());
}
/**
 * Specify whether or not to show closed projects in the tree 
 * viewer.  Default is to show closed projects.
 * 
 * @param show boolean if false, do not show closed projects in the tree
 */
public void showClosedProjects(boolean show){
	showClosedProjects = show;
}

}
