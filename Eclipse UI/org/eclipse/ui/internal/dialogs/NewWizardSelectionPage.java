/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 *	New wizard selection tab that allows the user to either select a
 *	registered 'New' wizard to be launched, or to select a solution or
 *	projects to be retrieved from an available server.  This page
 *	contains two visual tabs that allow the user to perform these tasks.
 *
 *  Temporarily has two inner pages.  The new format page is used if the system 
 *  is currently aware of activity categories.
 */
class NewWizardSelectionPage extends WorkbenchWizardSelectionPage {
    private WizardCollectionElement wizardCategories;

    // widgets
    private NewWizardNewPage newResourcePage;

    private WorkbenchWizardElement[] primaryWizards;

    /**
     *	Create an instance of this class
     *
     * @param aWorkbench the workbench
     * @param currentSelection the current selection
     * @param elements the wizard elements
     * @param primary the primary wizard elements
     */
    public NewWizardSelectionPage(IWorkbench aWorkbench,
            IStructuredSelection currentSelection,
            WizardCollectionElement elements, WorkbenchWizardElement[] primary) {
        // override what superclass does with elements
        super("newWizardSelectionPage", aWorkbench, currentSelection, null);//$NON-NLS-1$
        setTitle(WorkbenchMessages
                .getString("NewWizardSelectionPage.description")); //$NON-NLS-1$
        wizardCategories = elements;
        primaryWizards = primary;
    }

    /**
     * Makes the next page visible.
     */
    public void advanceToNextPage() {
        getContainer().showPage(getNextPage());
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        IDialogSettings settings = getDialogSettings();
        newResourcePage = new NewWizardNewPage(this, this.workbench,
                wizardCategories, primaryWizards);
        newResourcePage.setDialogSettings(settings);

        Control control = newResourcePage.createControl(parent);
        getWorkbench().getHelpSystem().setHelp(control,
				IWorkbenchHelpContextIds.NEW_WIZARD_SELECTION_WIZARD_PAGE);
        setControl(control);
    }

    /**
     * Since Finish was pressed, write widget values to the dialog store so that they
     *will persist into the next invocation of this wizard page
     */
    protected void saveWidgetValues() {
        newResourcePage.saveWidgetValues();
    }
}