package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Opens a new window. The initial perspective
 * for the new window will be the same type as
 * the active perspective in the window which this
 * action is running in. The default input for the 
 * new window's page is the workspace root.
 */
public class OpenInNewWindowAction extends Action {
	private IWorkbenchWindow workbenchWindow;
	private IAdaptable pageInput;

	/**
	 * Creates a new <code>OpenInNewWindowAction</code>. Sets
	 * the new window page's input to be the workspace root
	 * by default.
	 * 
	 * @param window the workbench window containing this action
	 */
	public OpenInNewWindowAction(IWorkbenchWindow window) {
		this(window, WorkbenchPlugin.getPluginWorkspace().getRoot());
	}

	/**
	 * Creates a new <code>OpenInNewWindowAction</code>.
	 * 
	 * @param window the workbench window containing this action
	 * @param input the input for the new window's page
	 */
	public OpenInNewWindowAction(IWorkbenchWindow window, IAdaptable input) {
		super(WorkbenchMessages.getString("OpenInNewWindowAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("OpenInNewWindowAction.toolTip")); //$NON-NLS-1$
		workbenchWindow = window;
		pageInput = input;
		WorkbenchHelp.setHelp(this,IHelpContextIds.OPEN_NEW_WINDOW_ACTION);
	}

	/**
	 * Set the input to use for the new window's page.
	 */
	public void setPageInput(IAdaptable input) {
		pageInput = input;
	}
	
	/**
	 * The implementation of this <code>IAction</code> method
	 * opens a new window. The initial perspective
	 * for the new window will be the same type as
	 * the active perspective in the window which this
	 * action is running in.
	 */
	public void run() {
		try {
			String perspId;
			
			IWorkbenchPage page = workbenchWindow.getActivePage();
			if (page != null && page.getPerspective() != null)
				perspId = page.getPerspective().getId();
			else
				perspId = workbenchWindow.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();

			workbenchWindow.getWorkbench().openWorkbenchWindow(perspId, pageInput);
		} catch (WorkbenchException e) {
			ErrorDialog.openError(
				workbenchWindow.getShell(),
				WorkbenchMessages.getString("OpenInNewWindowAction.errorTitle"), //$NON-NLS-1$,
				e.getMessage(),
				e.getStatus());
		}
	}
}
