/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The GroupedPreferenceContentProvider is the content provider
 * for showing preferences using groups instead of just categories.
 */
public class GroupedPreferenceContentProvider extends PreferenceContentProvider {

	Collection groupedIds;

	WorkbenchPreferenceGroup currentInput;
	
	boolean groupedMode;

	/**
	 * Create a new instance of the receiver indicating
	 * whether or not it is grouped.
	 * @param grouped If <code>true</code> then the 
	 * input wil be a WorkbenchPreferenceGroup, if not a
	 * PreferenceManger.
	 */
	public GroupedPreferenceContentProvider(boolean grouped) {
		super();
		groupedMode = grouped;
		
		groupedIds = new HashSet();
		WorkbenchPreferenceManager manager = (WorkbenchPreferenceManager) WorkbenchPlugin
				.getDefault().getPreferenceManager();
		WorkbenchPreferenceGroup[] groups = manager.groups;
		setManager(manager);

		for (int i = 0; i < groups.length; i++) {
			WorkbenchPreferenceGroup group = groups[i];
			groupedIds.addAll(group.getPageIds());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {

		if(groupedMode)
			return ((WorkbenchPreferenceGroup) inputElement).getPreferenceNodes();
		return super.getElements(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		groupedMode = newInput != null && (newInput instanceof WorkbenchPreferenceGroup);
		
		if (groupedMode){
			currentInput = (WorkbenchPreferenceGroup) newInput;	
		}
		else
			super.inputChanged(viewer, oldInput, newInput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {

		Object[] children = super.getChildren(parentElement);

		if(!groupedMode)//No check if we are not grouping
			return children;
		
		ArrayList returnValue = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof IPreferenceNode) {
				IPreferenceNode node = (IPreferenceNode) children[i];
				if (!groupedIds.contains(node.getId()))
					returnValue.add(node);
			} else
				returnValue.add(children[i]);

		}
		return returnValue.toArray();
	}
}
