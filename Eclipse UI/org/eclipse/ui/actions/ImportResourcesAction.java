package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.ImportWizard;

/**
 * Action representing the initiation of an Import operation by the user.
 * <p>
 * This class may be instantiated. It is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class ImportResourcesAction extends SelectionListenerAction {
	private static final int SIZING_WIZARD_WIDTH = 470;
	private static final int SIZING_WIZARD_HEIGHT = 550;
	private IWorkbench workbench;
/**
 *	Create a new instance of this class
 */
public ImportResourcesAction(IWorkbench aWorkbench) {
	super(WorkbenchMessages.getString("ImportResourcesAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ImportResourcesAction.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.IMPORT);
	WorkbenchHelp.setHelp(this, IHelpContextIds.IMPORT_ACTION);
	this.workbench = aWorkbench;
}


/**
 * Invoke the Import wizards selection Wizard.
 *
 * @param browser Window
 */
public void run() {
	ImportWizard wizard = new ImportWizard();
	List selectedResources = getSelectedResources();;
	IStructuredSelection selectionToPass;
	
	if (selectedResources.isEmpty()) {
		// get the current workbench selection
		ISelection workbenchSelection = 
			workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (workbenchSelection instanceof IStructuredSelection)
			selectionToPass = (IStructuredSelection)workbenchSelection;
		else
			selectionToPass = StructuredSelection.EMPTY;
	}
	else
		selectionToPass = new StructuredSelection(selectedResources);
		
	wizard.init(workbench, selectionToPass);
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings wizardSettings = workbenchSettings.getSection("ImportResourcesAction");//$NON-NLS-1$
	if(wizardSettings==null)
		wizardSettings = workbenchSettings.addNewSection("ImportResourcesAction");//$NON-NLS-1$
	wizard.setDialogSettings(wizardSettings);
	wizard.setForcePreviousAndNextButtons(true);
	
	Shell parent = workbench.getActiveWorkbenchWindow().getShell();
	WizardDialog dialog = new WizardDialog(parent, wizard);
	dialog.create();
	dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
	WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.IMPORT_WIZARD);
	dialog.open();
}

/**
 * Sets the current selection. 
 * In for backwards compatability. Use selectionChanged() instead.
 * @param selection the new selection
 * @deprecated
 */
public void setSelection(IStructuredSelection selection) {
	selectionChanged(selection);
}
}
