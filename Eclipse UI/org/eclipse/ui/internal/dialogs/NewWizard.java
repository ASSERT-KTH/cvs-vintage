package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.StringTokenizer;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;

/**
 * The new wizard is responsible for allowing the user to choose which
 * new (nested) wizard to run. The set of available new wizards comes
 * from the new extension point.
 */
public class NewWizard extends Wizard {
	private static final String CATEGORY_SEPARATOR = "/"; //$NON-NLS-1$

	private IWorkbench workbench;
	private IStructuredSelection selection;
	private NewWizardSelectionPage mainPage;
	private boolean projectsOnly = false;
	private String categoryId = null;
/**
 * Create the wizard pages
 */
public void addPages() {
	NewWizardsRegistryReader rdr = new NewWizardsRegistryReader(projectsOnly);
	WizardCollectionElement wizards = (WizardCollectionElement)rdr.getWizards();

	if (categoryId != null) {
		WizardCollectionElement categories = wizards;
		StringTokenizer familyTokenizer = new StringTokenizer(categoryId, CATEGORY_SEPARATOR);
		while (familyTokenizer.hasMoreElements()) {
			categories = getChildWithID(categories, familyTokenizer.nextToken());
			if (categories == null)
				break;
		}
		if (categories != null)
			wizards = categories;
	}
	
	mainPage =
		new NewWizardSelectionPage(
			this.workbench,
			this.selection,
			wizards);
	addPage(mainPage);
}
/**
 * Returns the child collection element for the given id
 */
private WizardCollectionElement getChildWithID(WizardCollectionElement parent, String id) {
	Object[] children = parent.getChildren();
	for (int i = 0; i < children.length; ++i) {
		WizardCollectionElement currentChild = (WizardCollectionElement)children[i];
		if (currentChild.getId().equals(id))
			return currentChild;
	}
	return null;
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
/**
 *	Lazily create the wizards pages
 */
public void init(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
	this.workbench = aWorkbench;
	this.selection = currentSelection;

	if (projectsOnly) 
		setWindowTitle(WorkbenchMessages.getString("NewProject.title")); //$NON-NLS-1$
	else 	
		setWindowTitle(WorkbenchMessages.getString("NewWizard.title")); //$NON-NLS-1$
	setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ));
	setNeedsProgressMonitor(true);
}
/**
 *	The user has pressed Finish.  Instruct self's pages to finish, and
 *	answer a boolean indicating success.
 *
 *	@return boolean
 */
public boolean performFinish() {
	//save our selection state
	mainPage.saveWidgetValues();
	return true;
}
/**
 * Sets the projects only flag.  If <code>true</code> only projects will
 * be shown in this wizard.
 */
public void setProjectsOnly(boolean b) {
	projectsOnly = b;
}
}
