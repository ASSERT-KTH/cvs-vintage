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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Prefence dialog for the workbench including the ability to load/save
 * preferences.
 */
public class WorkbenchPreferenceDialog extends FilteredPreferenceDialog {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * The dialog settings key for the last used import/export path.
	 */
	final static String FILE_PATH_SETTING = "PreferenceImportExportFileSelectionPage.filePath"; //$NON-NLS-1$

	private static boolean groupedMode = false;

	/**
	 * The ICON_MODE is a constant that determines if
	 * the dialog is showing no icons (SWT.NONE), 
	 * small icons (SWT.MIN) or large icons (SWT.MAX).
	 */
	static int ICON_MODE = SWT.MIN;

	/**
	 * There can only ever be one instance of the workbench's preference dialog.
	 * This keeps a handle on this instance, so that attempts to create a second
	 * dialog should just fail (or return the original instance).
	 * 
	 * @since 3.1
	 */
	private static WorkbenchPreferenceDialog instance = null;

	// The id of the last page that was selected
	private static String lastGroupId = null;

	/**
	 * The Load button id.
	 */
	private final static int LOAD_ID = IDialogConstants.CLIENT_ID + 1;

	private static String MODE_ICON = "org.eclipse.ui.internal.dialogs.MODE_ICON";//$NON-NLS-1$

	/**
	 * The Save button id.
	 */
	private final static int SAVE_ID = IDialogConstants.CLIENT_ID + 2;

	/**
	 * The TEXT_SHOWING booean is a constant that determines if
	 * the dialog is showing text or not in the toolbar.
	 */
	static boolean TEXT_SHOWING = true;

	private static String TOOLBAR_ICON = "org.eclipse.ui.internal.dialogs.TOOLBAR_ICON";//$NON-NLS-1$

	static {
		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				PlatformUI.PLUGIN_ID, "icons/full/obj16/layout_co.gif"); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(MODE_ICON, descriptor);
		}

		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(PlatformUI.PLUGIN_ID,
				"icons/full/obj16/toolbar.gif"); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(TOOLBAR_ICON, descriptor);
		}

		groupedMode = ((WorkbenchPreferenceManager) WorkbenchPlugin.getDefault()
				.getPreferenceManager()).getGroups().length > 0;
	}

	/**
	 * Return whether or not groups can be shown
	 * 
	 * @return boolean
	 */
	public static boolean canShowGroups() {
		return getGroups().length > 0;
	}
	
	/**
	 * Creates a workbench preference dialog to a particular preference page. It
	 * is the responsibility of the caller to then call <code>open()</code>.
	 * The call to <code>open()</code> will not return until the dialog
	 * closes, so this is the last chance to manipulate the dialog.
	 * 
	 * @param preferencePageId
	 *            The identifier of the preference page to open; may be
	 *            <code>null</code>. If it is <code>null</code>, then the
	 *            preference page is not selected or modified in any way.
	 * @return The selected preference page.
	 * @deprecated This method will be removed before 3.1 is released.
	 * Use  org.eclipse.ui.dialogs.PreferencesUtil#createPreferenceDialogOn(Shell, String,String[], Object)
	 * @since 3.1
	 * @see org.eclipse.ui.dialogs.PreferencesUtil
	 */
	public static final WorkbenchPreferenceDialog createDialogOn(final String preferencePageId) {
		return createDialogOn(null,preferencePageId);
	}
	
	/**
	 * Creates a workbench preference dialog to a particular preference page.
	 * Show the other pages as filtered results using whatever filtering
	 * criteria the search uses. It is the responsibility of the caller to then
	 * call <code>open()</code>. The call to <code>open()</code> will not
	 * return until the dialog closes, so this is the last chance to manipulate
	 * the dialog.
	 * 
	 * @param preferencePageId
	 *            The identifier of the preference page to open; may be
	 *            <code>null</code>. If it is <code>null</code>, then the
	 *            preference page is not selected or modified in any way.
	 * @param displayedIds
	 *            The ids of the other pages to be highlighted using the same
	 *            filtering criterea as search.
	 * @return The dialog.
	 * @deprecated This method will be removed before 3.1 is released.
	 * Use  org.eclipse.ui.dialogs.PreferencesUtil#createPreferenceDialogOn(Shell, String,String[], Object)
	 * @since 3.1
	 * @see org.eclipse.ui.dialogs.PreferencesUtil
	 */
	public static final WorkbenchPreferenceDialog createDialogOn(final String preferencePageId,
			String[] displayedIds) {
		WorkbenchPreferenceDialog dialog =  createDialogOn(null,preferencePageId);
		if(dialog == null)
			return null;
		if (displayedIds != null)
			dialog.showOnly(displayedIds);
		return dialog;
	}
	
	/**
	 * Creates a workbench preference dialog to a particular preference page. It
	 * is the responsibility of the caller to then call <code>open()</code>.
	 * The call to <code>open()</code> will not return until the dialog
	 * closes, so this is the last chance to manipulate the dialog.
	 * 
	 * @param shell
	 * 			The Shell to parent the dialog off of if it is not
	 * 			already created. May be <code>null</code>
	 * 			in which case the active workbench window will be used
	 * 			if available.
	 * @param preferencePageId
	 *            The identifier of the preference page to open; may be
	 *            <code>null</code>. If it is <code>null</code>, then the
	 *            preference page is not selected or modified in any way.
	 * @return The selected preference page.
	 * @since 3.1
	 */
	public static final WorkbenchPreferenceDialog createDialogOn(Shell shell, final String preferencePageId) {
		final WorkbenchPreferenceDialog dialog;

		if (instance == null) {
			/*
			 * There is no existing preference dialog, so open a new one with
			 * the given selected page.
			 */

			Shell parentShell = shell;
			if (parentShell == null) {
				// Determine a decent parent shell.
				final IWorkbench workbench = PlatformUI.getWorkbench();
				final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
				if (workbenchWindow != null) {
					parentShell = workbenchWindow.getShell();
				} else {
					parentShell = null;
				}
			}

			// Create the dialog
			final PreferenceManager preferenceManager = PlatformUI.getWorkbench()
					.getPreferenceManager();
			dialog = new WorkbenchPreferenceDialog(parentShell, preferenceManager);
			if (preferencePageId != null) {
				dialog.setSelectedNode(preferencePageId);
			}
			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					dialog.getShell(),
					IWorkbenchHelpContextIds.PREFERENCE_DIALOG);

		} else {
			/*
			 * There is an existing preference dialog, so let's just select the
			 * given preference page.
			 */
			dialog = instance;
			if (preferencePageId != null) {
				dialog.setCurrentPageId(preferencePageId);
			}

		}

		// Get the selected node, and return it.
		return dialog;
	}

	/**
	 * Return the groups in the receiver.
	 * 
	 * @return WorkbenchPreferenceGroup[]
	 */
	protected static WorkbenchPreferenceGroup[] getGroups() {
		return ((WorkbenchPreferenceManager) WorkbenchPlugin.getDefault().getPreferenceManager())
				.getGroups();
	}

	/**
	 * Return whether groups are being shown
	 * 
	 * @return boolean
	 */
	public static boolean showingGroups() {
		return groupedMode;
	}

	WorkbenchPreferenceGroup currentGroup;

	/**
	 * The preference page history.
	 * 
	 * @since 3.1
	 */
	private PreferencePageHistory history;

	private CTabItem tab;

	private ToolBar toolBar;

	private Composite toolBarComposite;

	/**
	 * Creates a new preference dialog under the control of the given preference
	 * manager.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param manager
	 *            the preference manager
	 */
	public WorkbenchPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		Assert.isTrue((instance == null),
				"There cannot be two preference dialogs at once in the workbench."); //$NON-NLS-1$
		instance = this;
		history = new PreferencePageHistory(this);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case LOAD_ID: {
			loadPressed();
			return;
		}
		case SAVE_ID: {
			savePressed();
			return;
		}
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * Closes the preference dialog. This clears out the singleton instance
	 * before calling the super implementation.
	 * 
	 * @return <code>true</code> if the dialog is (or was already) closed, and
	 *         <code>false</code> if it is still open
	 * @since 3.1
	 */
	public final boolean close() {
		instance = null;
		history.dispose();
		return super.close();
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButton(parent, LOAD_ID,
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.load"), false); //$NON-NLS-1$
		createButton(parent, SAVE_ID,
				WorkbenchMessages.getString("WorkbenchPreferenceDialog.save"), false); //$NON-NLS-1$

		Label l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		l = new Label(parent, SWT.NONE);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns += 3;
		layout.makeColumnsEqualWidth = false;

		super.createButtonsForButtonBar(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		toolBarComposite = new Composite(composite, SWT.NONE);
		GridLayout toolBarLayout = new GridLayout();
		toolBarLayout.marginHeight = 0;
		toolBarLayout.marginWidth = 0;
		toolBarComposite.setLayout(toolBarLayout);
		toolBarComposite.setBackground(composite.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		toolBarComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));

		if (groupedMode)
			createToolBar(toolBarComposite);

		createDialogContents(composite);

		applyDialogFont(composite);

		return composite;

	}

	/**
	 * Create the contents area of the dialog
	 * 
	 * @param parent
	 */
	void createDialogContents(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		int columns = 3;
		layout.numColumns = columns;
		composite.setLayout(layout);
		GridData compositeData = new GridData(GridData.FILL_BOTH);
		//compositeData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		composite.setLayoutData(compositeData);
		applyDialogFont(composite);
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		Control treeControl = createTreeAreaContents(composite);
		createSash(composite, treeControl);

		Composite pageAreaComposite = new Composite(composite, SWT.NONE);
		pageAreaComposite.setBackground(composite.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));

		pageAreaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout pageAreaLayout = new GridLayout();
		pageAreaLayout.marginHeight = 0;
		pageAreaLayout.marginWidth = 0;
		pageAreaLayout.horizontalSpacing = 0;

		pageAreaComposite.setLayout(pageAreaLayout);

		// Build the Page container
		setPageContainer(createPageContainer(pageAreaComposite));
		getPageContainer().setLayoutData(new GridData(GridData.FILL_BOTH));
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = columns;
		separator.setLayoutData(gd);
	}

	/**
	 * Create the button that switches modes.
	 * 
	 * @param historyManager
	 */
	private void createModeSwitch(ToolBarManager historyManager) {

		if (!canShowGroups())
			return;

		IAction modeSwitchAction = new Action("Modes", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				groupedMode = !groupedMode;

				if (groupedMode) {
					if (currentGroup == null)
						currentGroup = getGroups()[0];
					createToolBar(toolBarComposite);
					selectAndRevealInToolBar(currentGroup);
				} else {
					toolBar.dispose();
					getTreeViewer().setInput(getPreferenceManager());
				}
				//when switching layouts reset the filter text to reveal all items in the tree
				filteredTree.resetText();
				filteredTree.getViewer().getControl().setFocus();
				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
				getShell().layout(true);

			}
		};

		modeSwitchAction.setToolTipText(WorkbenchMessages
				.getString("WorkbenchPreferenceDialog.Switch")); //$NON-NLS-1$
		modeSwitchAction.setImageDescriptor(JFaceResources.getImageRegistry().getDescriptor(
				MODE_ICON));

		historyManager.add(modeSwitchAction);

		IAction toolBarSettings = new Action(EMPTY_STRING, IAction.AS_PUSH_BUTTON) {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				new PreferencesLookDialog(WorkbenchPreferenceDialog.this).open();
			}
		};
		toolBarSettings.setToolTipText(WorkbenchMessages
				.getString("WorkbenchPreferenceDialog.toolBatSettingsTip")); //$NON-NLS-1$
		toolBarSettings.setImageDescriptor(JFaceResources.getImageRegistry().getDescriptor(
				TOOLBAR_ICON));
		historyManager.add(toolBarSettings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPageContainer(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createPageContainer(Composite parent) {

		CTabFolder parentFolder = new CTabFolder(parent, SWT.BORDER);
		parentFolder.setSingle(true);

		tab = new CTabItem(parentFolder, SWT.BORDER);

		parentFolder.setSelectionForeground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_FOREGROUND));
		parentFolder.setSelectionBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));

		tab.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

		Control topBar = getContainerToolBar(parentFolder);
		parentFolder.setTopRight(topBar, SWT.RIGHT);
		int height = topBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		parentFolder.setTabHeight(height);

		parentFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));

		Composite outer = new Composite(parentFolder, SWT.NULL);
		GridLayout layout = new GridLayout();
		outer.setLayout(layout);

		Composite result = new Composite(outer, SWT.NULL);
		result.setLayout(getPageLayout());
		result.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));
		tab.setControl(outer);
		parentFolder.setSelection(0);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPageControl(org.eclipse.jface.preference.IPreferencePage,
	 *      org.eclipse.swt.widgets.Composite)
	 */
	protected void createPageControl(IPreferencePage page, Composite parent) {
		if (page instanceof PreferencePage)
			((PreferencePage) page).createControl(parent);
		else
			super.createPageControl(page, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createSash(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.swt.widgets.Control)
	 */
	protected Sash createSash(Composite composite, Control rightControl) {
		Sash sash = super.createSash(composite, rightControl);
		sash.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		return sash;
	}

	/**
	 * Create the toolbar for the groups.
	 * 
	 * @param composite
	 */
	private void createToolBar(Composite composite) {

		toolBar = new ToolBar(composite, SWT.CENTER | SWT.FLAT);
		toolBar.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		WorkbenchPreferenceGroup[] groups = getGroups();

		for (int i = 0; i < groups.length; i++) {
			final WorkbenchPreferenceGroup group = groups[i];
			ToolItem newItem = new ToolItem(toolBar, SWT.RADIO);
			newItem.setData(group);
			setTextAndImage(group, newItem);
			newItem.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					groupSelected(group);
				}
			});

		}

		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
		data.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		data.verticalIndent = IDialogConstants.VERTICAL_MARGIN;
		toolBar.setLayoutData(data);
	}

	/**
	 * Differs from super implementation in that if the node is found but should
	 * be filtered based on a call to
	 * <code>WorkbenchActivityHelper.filterItem()</code> then
	 * <code>null</code> is returned.
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#findNodeMatching(java.lang.String)
	 */
	protected IPreferenceNode findNodeMatching(String nodeId) {
		IPreferenceNode node = super.findNodeMatching(nodeId);
		if (WorkbenchActivityHelper.filterItem(node))
			return null;
		return node;
	}

	/**
	 * Get the toolbar for the container
	 * 
	 * @return Control
	 */
	private Control getContainerToolBar(Composite composite) {

		ToolBar historyBar = new ToolBar(composite, SWT.HORIZONTAL | SWT.FLAT);
		ToolBarManager historyManager = new ToolBarManager(historyBar);

		history.createHistoryControls(historyBar, historyManager);
		createModeSwitch(historyManager);

		historyManager.update(false);

		return historyBar;
	}


	/**
	 * Return the file name setting or a default value if there isn't one.
	 * 
	 * @param export
	 *            <code>true</code> if an export file name is being looked
	 *            for.
	 * 
	 * @return String if there is a good value to choose. Otherwise return
	 *         <code>null</code>.
	 */
	private String getFileNameSetting(boolean export) {

		String lastFileName = WorkbenchPlugin.getDefault().getDialogSettings().get(
				WorkbenchPreferenceDialog.FILE_PATH_SETTING);
		if (lastFileName == null) {
			if (export)
				return System.getProperty("user.dir") + System.getProperty("file.separator") + WorkbenchMessages.getString("ImportExportPages.preferenceFileName") + AbstractPreferenceImportExportPage.PREFERENCE_EXT; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		} else if ((export) || (new File(lastFileName).exists())) {
			return lastFileName;
		}

		return null;
	}

	/**
	 * Get the file name we are using. Set the button type flag depending on
	 * whether it is import or export operation.
	 * 
	 * @param export
	 *            <code>true</code> if an export file name is being looked
	 *            for.
	 * 
	 * @return IPath or <code>null</code> if no selection is mage.
	 */
	private IPath getFilePath(boolean export) {

		// Find the closest file/directory to what is currently entered.
		String currentFileName = getFileNameSetting(export);

		// Open a dialog allowing the user to choose.
		FileDialog fileDialog = null;
		if (export)
			fileDialog = new FileDialog(getShell(), SWT.SAVE);
		else
			fileDialog = new FileDialog(getShell(), SWT.OPEN);

		if (currentFileName != null)
			fileDialog.setFileName(currentFileName);
		fileDialog
				.setFilterExtensions(PreferenceImportExportFileSelectionPage.DIALOG_PREFERENCE_EXTENSIONS);
		currentFileName = fileDialog.open();

		if (currentFileName == null)
			return null;

		/*
		 * Append the default filename if none was specifed and such a file does
		 * not exist.
		 */
		String fileName = new File(currentFileName).getName();
		if (fileName.lastIndexOf(".") == -1) { //$NON-NLS-1$
			currentFileName += AbstractPreferenceImportExportPage.PREFERENCE_EXT;
		}
		setFileNameSetting(currentFileName);
		return new Path(currentFileName);

	}

	/**
	 * A group has been selected. Update the tree viewer.
	 * 
	 * @param group
	 *            tool item.
	 */
	private void groupSelected(final WorkbenchPreferenceGroup group) {

		lastGroupId = group.getId();
		currentGroup = group;

		getTreeViewer().setInput(group);
		StructuredSelection structured = StructuredSelection.EMPTY;

		Object selection = group.getLastSelection();
		if (selection == null && group.getPreferenceNodes().length > 0) {
			structured = new StructuredSelection(group.getPreferenceNodes()[0]);
		}

		getTreeViewer().setSelection(structured, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog#handleTreeSelectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	protected void handleTreeSelectionChanged(SelectionChangedEvent event) {
		if (!showingGroups())
			return;
		if (event.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (selection.isEmpty())
				return;
			Object item = selection.getFirstElement();
			currentGroup.setLastSelection(item);
		}

	}

	/**
	 * Import a preference file.
	 * 
	 * @param path
	 *            The file path.
	 * @return true if successful.
	 */
	private boolean importPreferences(IPath path) {
		IStatus status = Preferences.validatePreferenceVersions(path);
		if (status.getSeverity() == IStatus.ERROR) {
			// Show the error and about
			ErrorDialog.openError(getShell(), WorkbenchMessages
					.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format("WorkbenchPreferenceDialog.verifyErrorMessage", //$NON-NLS-1$
							new Object[] { path.toOSString() }), status);
			return false;
		} else if (status.getSeverity() == IStatus.WARNING) {
			// Show the warning and give the option to continue
			int result = PreferenceErrorDialog.openError(getShell(), WorkbenchMessages
					.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format("WorkbenchPreferenceDialog.verifyWarningMessage", //$NON-NLS-1$
							new Object[] { path.toOSString() }), status);
			if (result != Window.OK) {
				return false;
			}
		}

		try {
			Preferences.importPreferences(path);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), WorkbenchMessages
					.getString("WorkbenchPreferenceDialog.loadErrorTitle"), //$NON-NLS-1$
					WorkbenchMessages.format("WorkbenchPreferenceDialog.loadErrorMessage", //$NON-NLS-1$
							new Object[] { path.toOSString() }), e.getStatus());
			return false;
		}
		return true;
	}

	/**
	 * Handle a request to load preferences
	 */
	protected void loadPressed() {
		final IPath filePath = getFilePath(false);
		if (filePath == null)
			return;
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				importPreferences(filePath);
			}
		});

		close();
	}

	/**
	 * Handle a request to save preferences
	 */
	protected void savePressed() {
		new PreferencesExportDialog(getShell()).open();
		close();
	}

	/**
	 * Select the group and reveal it in the toolbar.
	 * 
	 * @param group
	 */
	private void selectAndRevealInToolBar(WorkbenchPreferenceGroup group) {
		selectGroupInToolBar(group);
		groupSelected(group);
	}

	/**
	 * @param group
	 */
	private void selectGroupInToolBar(final WorkbenchPreferenceGroup group) {
		ToolItem[] items = toolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			if (group.equals(items[i].getData())) {
				items[i].setSelection(true);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#selectSavedItem()
	 */
	protected void selectSavedItem() {
		if (showingGroups()) {
			WorkbenchPreferenceGroup[] groups = getGroups();

			if (lastGroupId != null) {
				for (int i = 0; i < groups.length; i++) {
					if (lastGroupId.equals(groups[i].getId())) {
						selectAndRevealInToolBar(groups[i]);
						return;
					}
				}
			}
			selectAndRevealInToolBar(groups[0]);
		} else {
			getTreeViewer().setInput(getPreferenceManager());
			super.selectSavedItem();

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog#setContentAndLabelProviders(org.eclipse.jface.viewers.TreeViewer)
	 */
	protected void setContentAndLabelProviders(TreeViewer treeViewer) {
		treeViewer.setContentProvider(new GroupedPreferenceContentProvider(showingGroups()));
		treeViewer.setLabelProvider(new GroupedPreferenceLabelProvider());
	}

	/**
	 * Selects the current page based on the given preference page identifier.
	 * If no node can be found, then nothing will change.
	 * 
	 * @param preferencePageId
	 *            The preference page identifier to select; should not be
	 *            <code>null</code>.
	 */
	public final void setCurrentPageId(final String preferencePageId) {
		final IPreferenceNode node = findNodeMatching(preferencePageId);
		if (node != null) {
			getTreeViewer().setSelection(new StructuredSelection(node));
			showPage(node);
		}
	}

	/**
	 * @param currentFileName
	 */
	private void setFileNameSetting(String currentFileName) {
		if (currentFileName != null)
			WorkbenchPlugin.getDefault().getDialogSettings().put(
					WorkbenchPreferenceDialog.FILE_PATH_SETTING, currentFileName);

	}

	/**
	 * Set te text and images for the group in the item.
	 * @param group
	 * @param item
	 */
	private void setTextAndImage(final WorkbenchPreferenceGroup group, ToolItem item) {
		String text = TEXT_SHOWING ? group.getName() : EMPTY_STRING;
		item.setText(text);

		Image image = null;
		if (ICON_MODE == SWT.MIN)
			image = group.getImage();
		else if (ICON_MODE == SWT.MAX)
			image = group.getLargeImage();

		item.setImage(image);
	}

	/*
	 * @see org.eclipse.jface.preference.PreferenceDialog#showPage(org.eclipse.jface.preference.IPreferenceNode)
	 * @since 3.1
	 */
	protected boolean showPage(IPreferenceNode node) {
		final boolean success = super.showPage(node);
		if (success) {
			history.addHistoryEntry(new PreferenceHistoryEntry(node.getId(), node.getLabelText(),
					null));
		}
		return success;
	}

	/**
	 * The toolbar settings have changed. Update them.
	 */
	public void updateForToolbarChange() {
		toolBar.dispose();
		createToolBar(toolBarComposite);
		selectAndRevealInToolBar(currentGroup);

		getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
		getShell().layout(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateMessage()
	 */
	public void updateMessage() {
		// No longer required as the pages do this.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#updateTitle()
	 */
	public void updateTitle() {
		tab.setText(getCurrentPage().getTitle());
		tab.setImage(getCurrentPage().getImage());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog#createFilteredTree(org.eclipse.swt.widgets.Composite, org.eclipse.ui.internal.dialogs.PatternFilter, int)
	 */
	protected FilteredTree createFilteredTree(Composite parent, PatternFilter filter, int styleBits) {
		FilteredComboTree comboTree =  new FilteredComboTree(parent, styleBits, filter);
//		Combo filteredCombo = comboTree.getFilterCombo();
//		WorkbenchPreferenceManager manager = (WorkbenchPreferenceManager) getPreferenceManager();
//		Iterator values = manager.getKeywords().keySet().iterator();
//		while(values.hasNext()){
//			filteredCombo.add((String) values.next());
//		}
		return comboTree;
	}
}