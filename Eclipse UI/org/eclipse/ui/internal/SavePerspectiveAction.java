package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.SavePerspectiveDialog;
import org.eclipse.ui.internal.registry.*;

/**
 * Reset the layout within the active perspective.
 */
public class SavePerspectiveAction extends Action {
	private IWorkbenchWindow window;	
/**
 *	Create an instance of this class
 */
public SavePerspectiveAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("SavePerspective.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SavePerspective.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	this.window = window;
	WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_PERSPECTIVE_ACTION);
}
/**
 *	The user has invoked this action
 */
public void run() {
	IWorkbenchPage page = window.getActivePage();
	if (page == null)
		return;
	PerspectiveDescriptor desc = (PerspectiveDescriptor)page.getPerspective();
	if (desc != null) {
		if (desc.isSingleton())
			saveSingleton();
		else
			saveNonSingleton();
	}
}
/** 
 * Save a singleton over itself.
 */
public void saveSingleton() {
	String [] buttons= new String[] { 
		IDialogConstants.OK_LABEL,
		IDialogConstants.CANCEL_LABEL
	};
	MessageDialog d= new MessageDialog(
		window.getShell(),
		WorkbenchMessages.getString("SavePerspective.overwriteTitle"), //$NON-NLS-1$
		null,
		WorkbenchMessages.getString("SavePerspective.singletonQuestion"),  //$NON-NLS-1$
		MessageDialog.QUESTION,
		buttons,
		0
	);
	if (d.open() == 0)
		window.getActivePage().savePerspective();
}

/**
 * Save a singleton over the user selection.
 */
public void saveNonSingleton() {
	// Get reg.
	PerspectiveRegistry reg = (PerspectiveRegistry)WorkbenchPlugin.getDefault()
		.getPerspectiveRegistry();

	// Get persp name.
	SavePerspectiveDialog dlg = new SavePerspectiveDialog(window.getShell(), reg);
	IPerspectiveDescriptor description = reg.findPerspectiveWithId(window.getActivePage().getPerspective().getId());
	dlg.setInitialSelection(description);
	if (dlg.open() != IDialogConstants.OK_ID)
		return;

	// Create descriptor.
	PerspectiveDescriptor desc = (PerspectiveDescriptor)dlg.getPersp();
	if (desc == null) {
		String name = dlg.getPerspName();
		desc = reg.createPerspective(name,(PerspectiveDescriptor)description);
		if (desc == null) {
			MessageDialog.openError(dlg.getShell(), WorkbenchMessages.getString("SavePerspective.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("SavePerspective.errorMessage")); //$NON-NLS-1$
			return;
		}
	}

	// Save state.
	IWorkbenchPage page = window.getActivePage();
	if (page != null) {
		page.savePerspectiveAs(desc);
	}
}
}
