/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.preference.BooleanFieldEditor;

import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.preferences.ViewSettingsDialog;

import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The JobsViewPreferenceDialog is the dialog that
 * allows the user to set the preferences.
 */
public class JobsViewPreferenceDialog extends ViewSettingsDialog {

	BooleanFieldEditor verboseEditor;


	/**
	 * Create a new instance of the receiver.
	 * @param parentShell
	 */
	public JobsViewPreferenceDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ProgressMessages.getString("JobsViewPreferenceDialog.Title")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite) super.createDialogArea(parent);
		
		Composite editArea = new Composite(top,SWT.NONE);
		editArea.setLayout(new GridLayout());
		editArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		
		Label note = new Label(editArea,SWT.NONE);
		note.setText(ProgressMessages.getString("JobsViewPreferenceDialog.Note")); //$NON-NLS-1$
		
		verboseEditor = new BooleanFieldEditor("verbose", ProgressMessages //$NON-NLS-1$
				.getString("ProgressView.VerboseAction"), editArea); //$NON-NLS-1$
		verboseEditor.setPreferenceName(IWorkbenchPreferenceConstants.SHOW_SYSTEM_JOBS);
		verboseEditor.setPreferenceStore(PrefUtil.getAPIPreferenceStore());
		verboseEditor.load();
		
		
		return top;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		verboseEditor.store();
		super.okPressed();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.preferences.ViewSettingsDialog#performDefaults()
	 */
	protected void performDefaults() {
		verboseEditor.loadDefault();
	}

	

}
