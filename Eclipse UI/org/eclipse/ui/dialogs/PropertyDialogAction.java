/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;

/**
 * Standard action for opening a Property Pages Dialog on the currently selected
 * element.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Generally speaking, this action is useful in pop-up menus because it allows
 * the user to browse and change properties of selected elements. When
 * performed, the action will bring up a Property Pages Dialog containing
 * property pages registered with the workbench for elements of the selected
 * type.
 * </p>
 * <p>
 * Although the action is capable of calculating if there are any applicable
 * pages for the current selection, this calculation is costly because it
 * require searching the workbench registry. Where performance is critical, the
 * action can simply be added to the pop-up menu. In the event of no applicable
 * pages, the action will just open an appropriate message dialog.
 * </p>
 */
public class PropertyDialogAction extends SelectionProviderAction {
	/**
	 * The shell in which to open the property dialog.
	 */
	private Shell shell;

	/**
	 * The id of the page to open up on.
	 */
	private String initialPageId;


	/**
	 * Creates a new action for opening a property dialog on the elements from
	 * the given selection provider.
	 * 
	 * @param shell
	 *            the shell in which the dialog will open
	 * @param provider
	 *            the selection provider whose elements the property dialog will
	 *            describe
	 */
	public PropertyDialogAction(Shell shell, ISelectionProvider provider) {
		super(provider, WorkbenchMessages.getString("PropertyDialog.text")); //$NON-NLS-1$
		Assert.isNotNull(shell);
		this.shell = shell;
		setToolTipText(WorkbenchMessages.getString("PropertyDialog.toolTip")); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.PROPERTY_DIALOG_ACTION);
	}

	/**
	 * Returns whether the provided object has pages registered in the property
	 * page manager.
	 * 
	 * @param object
	 * @return boolean
	 */
	private boolean hasPropertyPagesFor(Object object) {
		PropertyPageContributorManager manager = PropertyPageContributorManager.getManager();
		return manager.hasContributorsFor(object);
	}

	/**
	 * Returns whether this action is actually applicable to the current
	 * selection. If this action is disabled, it will return <code>false</code>
	 * without further calculation. If it is enabled, it will check with the
	 * workbench's property page manager to see if there are any property pages
	 * registered for the selected element's type.
	 * <p>
	 * This method is generally too expensive to use when updating the enabled
	 * state of the action on each selection change.
	 * </p>
	 * 
	 * @return <code>true</code> if the selection is of size 1 and there are
	 *         property pages for the selected element, and <code>false</code>
	 *         otherwise
	 */
	public boolean isApplicableForSelection() {
		if (!isEnabled())
			return false;
		return isApplicableForSelection(getStructuredSelection());
	}

	/**
	 * Returns whether this action is applicable to the current selection. This
	 * checks that the selection is of size 1, and checks with the workbench's
	 * property page manager to see if there are any property pages registered
	 * for the selected element's type.
	 * <p>
	 * This method is generally too expensive to use when updating the enabled
	 * state of the action on each selection change.
	 * </p>
	 * 
	 * @param selection
	 *            The selection to test
	 * @return <code>true</code> if the selection is of size 1 and there are
	 *         property pages for the selected element, and <code>false</code>
	 *         otherwise
	 */
	public boolean isApplicableForSelection(IStructuredSelection selection) {
		return selection.size() == 1 && hasPropertyPagesFor(selection.getFirstElement());
	}

	/**
	 * The <code>PropertyDialogAction</code> implementation of this
	 * <code>IAction</code> method performs the action by opening the Property
	 * Page Dialog for the current selection.
	 */
	public void run() {

		PreferenceDialog dialog = createDialog();
		if (dialog != null)
			dialog.open();
	}

	/**
	 * Create the dialog for the receiver. If no pages are found, an informative
	 * message dialog is presented instead.
	 * 
	 * @return PreferenceDialog or <code>null</code> if no applicable pages
	 *         are found.
	 */
	public PreferenceDialog createDialog() {

		IAdaptable element = (IAdaptable) getStructuredSelection().getFirstElement();
		if (element == null)
			return null;
		return PropertyDialog
				.createDialogOn(shell, initialPageId, element);
	}

	/**
	 * The <code>PropertyDialogAction</code> implementation of this
	 * <code>SelectionProviderAction</code> method enables the action only if
	 * the given selection contains exactly one element.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(selection.size() == 1 && selection.getFirstElement() != null);
	}
}