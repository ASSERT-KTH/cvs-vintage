/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:  Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 * font should be activated and used by other components.
 */
package org.eclipse.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.CreateLinkedResourceGroup;

/**
 * The NewFolderDialog is used to create a new folder.
 * The folder can optionally be linked to a file system folder.
 * <p>
 * NOTE: 
 * A linked folder can only be created at the project 
 * level. The widgets used to specify a link target are disabled 
 * if the supplied container is not a project.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class NewFolderDialog extends SelectionDialog {
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	// widgets
	private Text folderNameField;
	private CreateLinkedResourceGroup linkedResourceGroup;
	private MessageLine statusLine;	

	private boolean createLink = false;

	IContainer container;
	
/**
 * Creates a NewFolderDialog
 * 
 * @param parentShell parent of the new dialog
 * @param container parent of the new folder
 */
public NewFolderDialog(Shell parentShell, IContainer container) {
	super(parentShell);
	this.container = container;
	linkedResourceGroup = new CreateLinkedResourceGroup(
		IResource.FOLDER,
		new Listener() {
			public void handleEvent(Event e) {
				validateLinkedResource();
			}
		});
	setTitle(WorkbenchMessages.getString("NewFolderDialog.title")); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, IHelpContextIds.NEW_FOLDER_DIALOG);
}
/**
 * @see org.eclipse.jface.window.Window#create()
 */
public void create() {
	super.create();
	// initially disable the ok button since we don't preset the
	// folder name field
	getButton(IDialogConstants.OK_ID).setEnabled(false);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	Font font = parent.getFont();
	Composite composite = (Composite) super.createDialogArea(parent);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));

	createFolderNameGroup(composite);
	linkedResourceGroup.createContents(composite);
	statusLine = new MessageLine(composite);
	statusLine.setAlignment(SWT.LEFT);
	statusLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	statusLine.setErrorStatus(null);
	statusLine.setFont(font);
	
	return composite;
}
/**
 * Creates the folder name specification controls.
 *
 * @param parent the parent composite
 */
private void createFolderNameGroup(Composite parent) {
	Font font = parent.getFont();
	// project specification group
	Composite folderGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	folderGroup.setLayout(layout);
	folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	// new project label
	Label folderLabel = new Label(folderGroup,SWT.NONE);
	folderLabel.setFont(font);
	folderLabel.setText(WorkbenchMessages.getString("NewFolderDialog.nameLabel"));	//$NON-NLS-1$

	// new project name entry field
	folderNameField = new Text(folderGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	folderNameField.setLayoutData(data);
	folderNameField.setFont(font);
	folderNameField.addListener(SWT.Modify, new Listener() {
		public void handleEvent(Event event) {
			validateLinkedResource();
		}
	});
}
private IFolder createFolderHandle(String folderName) {
	IWorkspaceRoot workspaceRoot = container.getWorkspace().getRoot();
	IPath folderPath = container.getFullPath().append(folderName);
	IFolder folderHandle = workspaceRoot.getFolder(folderPath);
	
	return folderHandle;
}
/**
 * Creates a new folder with the given name and optionally linking to
 * the specified link target.
 * 
 * @param folderName name of the new folder
 * @param linkTargetName name of the link target folder. may be null.
 * @return IFolder the new folder
 */
private IFolder createNewFolder(String folderName, final String linkTargetName) {
	final IFolder folderHandle = createFolderHandle(folderName);
	
	WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
		public void execute(IProgressMonitor monitor) throws CoreException {
			try {
				monitor.beginTask(WorkbenchMessages.getString("NewFolderDialog.progress"), 2000); //$NON-NLS-1$
				if (linkTargetName == null)
					folderHandle.create(false, true, monitor);
				else
					folderHandle.createLink(new Path(linkTargetName), IResource.NONE, monitor);
				if (monitor.isCanceled())
					throw new OperationCanceledException();
			} finally {
				monitor.done();
			}
		}
	};

	try {
		new ProgressMonitorDialog(getShell()).run(true, true, operation);
	} catch (InterruptedException exception) {
		return null;
	} catch (InvocationTargetException exception) {
		if (exception.getTargetException() instanceof CoreException) {
			ErrorDialog.openError(
				getShell(),
				WorkbenchMessages.getString("NewFolderDialog.errorTitle"),  //$NON-NLS-1$
				null,	// no special message
				((CoreException) exception.getTargetException()).getStatus());
		}
		else {
			// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log(MessageFormat.format(
				"Exception in {0}.createNewFolder(): {1}", 					//$NON-NLS-1$
				new Object[] {getClass().getName(), exception.getTargetException()}));
			MessageDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("NewFolderDialog.errorTitle"), 	//$NON-NLS-1$
				WorkbenchMessages.format("NewFolderDialog.internalError", 	//$NON-NLS-1$
				new Object[] {exception.getTargetException().getMessage()}));
		}
		return null;
	}
	return folderHandle;
}
/**
 * Creates the folder using the name and link target entered
 * by the user.
 * Sets the dialog result to the created folder.  
 */
protected void okPressed() {
	String linkTarget = null; 
		
	if(createLink) {
		linkTarget = linkedResourceGroup.getLinkTarget();
	}
	IFolder folder = createNewFolder(folderNameField.getText(), linkTarget);
	setSelectionResult(new IFolder[] {folder});
	super.okPressed();
}
/**
 * Update the dialog's status line to reflect the given status. It is safe to call
 * this method before the dialog has been opened.
 */
private void updateStatus(IStatus status) {
	if (statusLine != null && statusLine.isDisposed() == false) {
		statusLine.setErrorStatus(status);
	}
}
/**
 * Update the dialog's status line to reflect the given status. It is safe to call
 * this method before the dialog has been opened.
 */
private void updateStatus(int severity, String message) {
	updateStatus(
		new Status(
			severity,
			WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
			severity,
			message,
			null));
}
/**
 * Checks whether the folder name and link location are valid.
 *
 * @return null if the folder name and link location are valid.
 * 	a message that indicates the problem otherwise.
 */
private void validateLinkedResource() {
	boolean valid = validateFolderName();

	if (valid) {
		IFolder linkHandle = createFolderHandle(folderNameField.getText());
		IStatus status = linkedResourceGroup.validateLinkLocation(linkHandle);
		
		if (status.getSeverity() != IStatus.ERROR)
			getOkButton().setEnabled(true);
		else
			getOkButton().setEnabled(false);
			
		updateStatus(status);
	}
}
/**
 * Checks if the folder name is valid.
 *
 * @return null if the new folder name is valid.
 * 	a message that indicates the problem otherwise.
 */
private boolean validateFolderName() {
	String name = folderNameField.getText();
	IWorkspace workspace = container.getWorkspace();
	IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);

	if ("".equals(name)) {
		updateStatus(IStatus.ERROR, WorkbenchMessages.getString("NewFolderDialog.folderNameEmpty"));	//$NON-NLS-1$
		return false;
	}
	if (nameStatus.isOK() == false) {
		updateStatus(nameStatus);
		return false;
	}
	IFolder newFolder = container.getFolder(new Path(name));
	if (newFolder.exists()) {
		updateStatus(IStatus.ERROR, WorkbenchMessages.format("NewFolderDialog.alreadyExists", new Object[] { name }));	//$NON-NLS-1$
		return false;
	}
	updateStatus(null);
	return true;
}
}