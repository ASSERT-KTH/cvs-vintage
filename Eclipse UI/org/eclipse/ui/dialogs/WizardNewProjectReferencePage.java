package org.eclipse.ui.dialogs;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Standard project reference page for a wizard that creates a 
 * project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be
 * subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * referencePage = new WizardNewProjectReferencePage("basicReferenceProjectPage");
 * referencePage.setTitle("Project");
 * referencePage.setDescription("Select referenced projects.");
 * </pre>
 * </p>
 */
public class WizardNewProjectReferencePage extends WizardPage {
	// widgets
	private CheckboxTableViewer referenceProjectsViewer;

	private static final String REFERENCED_PROJECTS_TITLE = WorkbenchMessages.getString("WizardNewProjectReferences.title"); //$NON-NLS-1$
	private static final int PROJECT_LIST_MULTIPLIER = 15;
/**
 * Creates a new project reference wizard page.
 *
 * @param pageName the name of this page
 */
public WizardNewProjectReferencePage(String pageName) {
	super(pageName);
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	
	Font font = parent.getFont();
	
	Composite composite = new Composite(parent, SWT.NONE);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	composite.setFont(font);

	WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_REFERENCE_WIZARD_PAGE);
	
	Label referenceLabel = new Label(composite, SWT.NONE);
	referenceLabel.setText(REFERENCED_PROJECTS_TITLE);
	referenceLabel.setFont(font);

	referenceProjectsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
	referenceProjectsViewer.getTable().setFont(composite.getFont());
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;

	data.heightHint =
		getDefaultFontHeight(
			referenceProjectsViewer.getTable(),
			PROJECT_LIST_MULTIPLIER);
	referenceProjectsViewer.getTable().setLayoutData(data);
	referenceProjectsViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
	referenceProjectsViewer.setContentProvider(getContentProvider());
	referenceProjectsViewer.setInput(ResourcesPlugin.getWorkspace());

	setControl(composite);
}
/**
 * Returns a content provider for the reference project
 * viewer. It will return all projects in the workspace.
 *
 * @return the content provider
 */
protected IStructuredContentProvider getContentProvider() {
	return new WorkbenchContentProvider() {
		public Object[] getChildren(Object element) {
			if (!(element instanceof IWorkspace))
				return new Object[0];
			IProject[] projects = ((IWorkspace)element).getRoot().getProjects();
			return projects == null ? new Object[0] : projects;
		}
	};
}
/**
 * Get the defualt widget height for the supplied control.
 * @return int
 * @param control - the control being queried about fonts
 * @param lines - the number of lines to be shown on the table.
 */
private static int getDefaultFontHeight(Control control, int lines) {
	FontData[] viewerFontData = control.getFont().getFontData();
	int fontHeight = 10;

	//If we have no font data use our guess
	if (viewerFontData.length > 0)
		fontHeight = viewerFontData[0].getHeight();
	return lines * fontHeight;

}
/**
 * Returns the referenced projects selected by the user.
 *
 * @return the referenced projects
 */
public IProject[] getReferencedProjects() {
	Object[] elements = referenceProjectsViewer.getCheckedElements();
	IProject[] projects = new IProject[elements.length];
	System.arraycopy(elements, 0, projects, 0, elements.length);
	return projects;	
}
}
