/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * This page manages enablement of roles and whether or not the entire role
 * filtering system is enabled at all.
 */
public class RolePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	IWorkbench currentWorkbench;
	/**
	 * The button which indicates if the role system is currently enabled.
	 */
	Button roleEnablement;

	/**
	 * Viewer to show the list of known roles.
	 */
	CheckboxTableViewer viewer;

	/*
	 * (non-Javadoc) @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		final RoleManager manager = RoleManager.getInstance();
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(1, true));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		roleEnablement = new Button(composite, SWT.CHECK);
		roleEnablement.setText(RoleMessages.getString("RolePreferencePage.RoleBasedFilteringLabel")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalSpan = 2;
		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		viewer.setContentProvider(new RoleContentProvider());
		viewer.setLabelProvider(new RoleLabelProvider());
		viewer.setInput(manager);
		viewer.getControl().setLayoutData(gd);

		roleEnablement.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				manager.setFiltering(roleEnablement.getSelection());
				updateViewerEnablement();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		loadRoleState();
		return composite;
	}

	/**
	 * Update the enablment of the role table based on whether filtering is
	 * enabled in the RoleManager instance.
	 */
	protected void updateViewerEnablement() {
		boolean filterEnabled = RoleManager.getInstance().isFiltering();
		viewer.getControl().setEnabled(filterEnabled);
		viewer.setAllGrayed(!filterEnabled);
	}

	/**
	 * Sets the state of the viewer based on the currently enabled roles.
	 */
	private void loadRoleState() {
		updateViewerEnablement();
		roleEnablement.setSelection(RoleManager.getInstance().isFiltering());
		Role[] roles = RoleManager.getInstance().getRoles();
		for (int i = 0; i < roles.length; i++) {
			viewer.setChecked(roles[i], roles[i].allEnabled());
		}
	}

	/*
	 * (non-Javadoc) @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		currentWorkbench = workbench;
	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	public boolean performOk() {
		List checked = Arrays.asList(viewer.getCheckedElements());
		RoleManager manager = RoleManager.getInstance();
		Role[] roles = manager.getRoles();
		for (int i = 0; i < roles.length; i++) {
			roles[i].setEnabled(checked.contains(roles[i]));
		}
		manager.setFiltering(roleEnablement.getSelection());
		manager.saveEnabledStates();

		IWorkbenchWindow[] windows = currentWorkbench.getWorkbenchWindows();

		for (int i = 0; i < windows.length; i++) {
			if(windows[i] instanceof WorkbenchWindow){
				WorkbenchWindow window = (WorkbenchWindow) windows[i];
				window.getMenuBarManager().markDirty();
				window.getCoolBarManager().markDirty();
				window.getMenuBarManager().update(true);
				window.getCoolBarManager().update(true);
			}
		}
		
		return super.performOk();
	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		loadRoleState();
	}
	
	
	
	
}
