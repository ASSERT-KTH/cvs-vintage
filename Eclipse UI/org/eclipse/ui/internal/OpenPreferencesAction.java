package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;
import org.eclipse.ui.help.*;

/**
 * Open the preferences dialog
 */
public class OpenPreferencesAction extends Action {
	protected IWorkbenchWindow window;
/**
 * Create a new <code>OpenPreferenceAction</code>
 * This default constructor allows the the action to be called from the welcome page.
 */
public OpenPreferencesAction() {
	this(((Workbench)PlatformUI.getWorkbench()).getActiveWorkbenchWindow());
}
/**
 * Create a new <code>OpenPreferenceAction</code> and initialize it 
 * from the given resource bundle.
 */
public OpenPreferencesAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("OpenPreferences.text")); //$NON-NLS-1$
	this.window = window;
	setToolTipText(WorkbenchMessages.getString("OpenPreferences.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_PREFERENCES_ACTION);
}
/**
 * Perform the action: open the preference dialog.
 */
public void run() {
	PreferenceManager pm = WorkbenchPlugin.getDefault().getPreferenceManager();
	
	if (pm != null) {
		PreferenceDialog d = new WorkbenchPreferenceDialog(window.getShell(), pm);
		d.create();
		WorkbenchHelp.setHelp(d.getShell(), IHelpContextIds.PREFERENCE_DIALOG);
		d.open();	
	}
}
}
