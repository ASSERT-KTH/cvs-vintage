package org.eclipse.ui.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. � This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
�
Contributors:
**********************************************************************/
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.NewWizard;

/**
 * Invoke the resource creation wizard selection Wizard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class NewWizardAction extends Action {

	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;

	/**
	 * The id of the category to show or <code>null</code> to
	 * show all the categories.
	 */
	private String categoryId = null;
	
/**
 *	Create a new instance of this class
 */
public NewWizardAction() {
	super(WorkbenchMessages.getString("NewWizardAction.text")); //$NON-NLS-1$
	setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ));
	setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_HOVER));
	setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_DISABLED));
	setToolTipText(WorkbenchMessages.getString("NewWizardAction.toolTip"));	 //$NON-NLS-1$
	setAccelerator(SWT.CTRL | 'N'); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.NEW_ACTION);
}
/**
 * Returns the id of the category of wizards to show
 * or <code>null</code> to show all categories.
 */
public String getCategoryId() {
	return categoryId;
}
/**
 * Sets the id of the category of wizards to show
 * or <code>null</code> to show all categories.
 */
public void setCategoryId(String id) {
	categoryId = id;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	IWorkbench workbench = PlatformUI.getWorkbench();
	NewWizard wizard = new NewWizard();
	wizard.setCategoryId(categoryId);

	ISelection selection = workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
	IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
	if (selection instanceof IStructuredSelection) {
		selectionToPass = (IStructuredSelection) selection;
	} else {
		// Build the selection from the IFile of the editor
		IWorkbenchPart part = workbench.getActiveWorkbenchWindow().getPartService().getActivePart();
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart)part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				selectionToPass = new StructuredSelection(((IFileEditorInput)input).getFile());
			}	
		}
	}
	
	wizard.init(workbench, selectionToPass);
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings wizardSettings = workbenchSettings.getSection("NewWizardAction");//$NON-NLS-1$
	if (wizardSettings == null)
		wizardSettings = workbenchSettings.addNewSection("NewWizardAction");//$NON-NLS-1$
	wizard.setDialogSettings(wizardSettings);
	wizard.setForcePreviousAndNextButtons(true);
	
	Shell parent = workbench.getActiveWorkbenchWindow().getShell();
	WizardDialog dialog = new WizardDialog(parent, wizard);
	dialog.create();
	dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
	WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.NEW_WIZARD);
	dialog.open();
}
}
