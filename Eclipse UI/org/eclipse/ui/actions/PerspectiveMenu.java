/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;
import org.eclipse.ui.internal.registry.IPluginContribution;

/**
 * A menu for perspective selection.  
 * <p>
 * A <code>PerspectiveMenu</code> is used to populate a menu with
 * perspective shortcut items.  If the user selects one of these items 
 * an action is performed for the selected perspective.
 * </p><p>
 * The visible perspective items within the menu are dynamic and reflect the
 * available set generated by each subclass. The default available set consists
 * of the perspective shortcut list of the current perspective.
 * </p><p>
 * This class is abstract.  Subclasses must implement the <code>run</code> method,
 * which performs a specialized action for the selected perspective.
 * </p>
 */
public abstract class PerspectiveMenu extends ContributionItem {
	private static IPerspectiveRegistry reg;

	private IWorkbenchWindow window;
	private boolean showActive = false;
	private boolean dirty = true;
	private IMenuListener menuListener = new IMenuListener() {
		public void menuAboutToShow(IMenuManager manager) {
			manager.markDirty();
			dirty = true;
		}
	};

	private Comparator comparator = new Comparator() {
		private Collator collator = Collator.getInstance();

		public int compare(Object ob1, Object ob2) {
			IPerspectiveDescriptor d1 = (IPerspectiveDescriptor) ob1;
			IPerspectiveDescriptor d2 = (IPerspectiveDescriptor) ob2;
			return collator.compare(d1.getLabel(), d2.getLabel());
		}
	};
	
	private static Hashtable imageCache = new Hashtable(11);

	/**
	 * Constructs a new instance of <code>PerspectiveMenu</code>.  
	 *
	 * @param window the window containing this menu
	 * @param id the menu id
	 */
	public PerspectiveMenu(IWorkbenchWindow window, String id) {
		super(id);
		this.window = window;
		if (reg == null)
			reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
	}
	
	/* (non-Javadoc)
	 * Creates a menu item for a perspective.
	 */
	/* package */
	void createMenuItem(
		Menu menu,
		int index,
		final IPerspectiveDescriptor desc,
		boolean bCheck) {
			
		MenuItem mi = new MenuItem(menu, bCheck ? SWT.RADIO : SWT.PUSH, index);
		mi.setText(desc.getLabel());
		Image image = getImage(desc);
		if (image != null) {
			mi.setImage(image);
		}
		mi.setSelection(bCheck);
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				run(desc, e);
			}
		});
		WorkbenchHelp.setHelp(mi, IHelpContextIds.OPEN_PERSPECTIVE_ACTION);
	}
	
	/* (non-Javadoc)
	 * Creates a menu item for "Other...".
	 */
	/* package */
	void createOtherItem(Menu menu, int index) {
		MenuItem mi = new MenuItem(menu, SWT.PUSH, index);
		mi.setText(WorkbenchMessages.getString("PerspectiveMenu.otherItem"));  //$NON-NLS-1$
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runOther(e);
			}
		});
		WorkbenchHelp.setHelp(mi, IHelpContextIds.OPEN_PERSPECTIVE_OTHER_ACTION);
	}
	
	/* (non-Javadoc)
	 * Fills the menu with perspective items.
	 */
	public void fill(Menu menu, int index) {

		if(getParent() instanceof MenuManager)
			((MenuManager)getParent()).addMenuListener(menuListener);
		
		if(!dirty)
			return;
			
		String checkID = null;
		if (showActive) {
			IWorkbenchPage activePage = window.getActivePage();
			if ((activePage != null) && (activePage.getPerspective() != null))
				checkID = activePage.getPerspective().getId();
		}

		// Collect and sort perspective items.
		ArrayList persps = getPerspectiveItems();
		Collections.sort(persps, comparator);

		// Add perspective shortcut
		for (int i = 0; i < persps.size(); i++) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor) persps.get(i);
			createMenuItem(menu, index++, desc, desc.getId().equals(checkID));
		}

		// Add others item..
		if (persps.size() > 0) {
			new MenuItem(menu, SWT.SEPARATOR, index++);
		}
		createOtherItem(menu, index);
		dirty = false;
	}
	
	/**
	 * Returns an image to show for the corresponding perspective descriptor.
	 *
	 * @param perspDesc the perspective descriptor
	 * @return the image or null
	 */
	private Image getImage(IPerspectiveDescriptor perspDesc) {
		ImageDescriptor imageDesc = perspDesc.getImageDescriptor();
		if (imageDesc == null) {
			imageDesc =
				WorkbenchImages.getImageDescriptor(
					IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE_HOVER);
		}
		if (imageDesc == null) {
			return null;
		}
		Image image = (Image) imageCache.get(imageDesc);
		if (image == null) {
			image = imageDesc.createImage();
			imageCache.put(imageDesc, image);
		}
		return image;
	}
	
	/* (non-Javadoc)
	 * Returns the perspective shortcut items for the active perspective.
	 * 
	 * @return a list of <code>IPerspectiveDescriptor</code> items
	 */
	private ArrayList getPerspectiveShortcuts() {
		ArrayList list = new ArrayList();

		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return list;

		ArrayList ids = ((WorkbenchPage) page).getPerspectiveActionIds();
		if (ids == null)
			return list;

		for (int i = 0; i < ids.size(); i++) {
			String perspID = (String) ids.get(i);
			IPerspectiveDescriptor desc = reg.findPerspectiveWithId(perspID);
			if (desc != null && !list.contains(desc)) {
                if (desc instanceof IPluginContribution) {
                	IPluginContribution contribution = (IPluginContribution) desc;
                    if (contribution.fromPlugin()) {
                    	IIdentifier identifier = PlatformUI.getWorkbench().getActivityManager().getIdentifier(WorkbenchActivityHelper.createUnifiedId(contribution));
                        if (!identifier.isEnabled()) 
                            continue;
                    }
                }
				list.add(desc);
            }
		}

		return list;
	}
	/**
	 * Returns the available list of perspectives to display in the menu.
	 * <p>
	 * By default, the list contains the perspective shortcuts
	 * for the current perspective.
	 * </p><p>
	 * Subclasses can override this method to return a different list.
	 * </p>
	 * 
	 * @return an <code>ArrayList<code> of perspective items <code>IPerspectiveDescriptor</code>
	 */
	protected ArrayList getPerspectiveItems() {
		/* Allow the user to see all the perspectives they have 
	 	 * selected via Customize Perspective. Bugzilla bug #23445 */
		ArrayList shortcuts = getPerspectiveShortcuts();		
		ArrayList list = new ArrayList(shortcuts.size());

		// Add perspective shortcuts from the active perspective
		int size = shortcuts.size();
		for (int i = 0; i < size; i++) {
			if (!list.contains(shortcuts.get(i))) {
				list.add(shortcuts.get(i));
			}
		}

		return list;
	}

	/**
	 * Returns whether the menu item representing the active perspective
	 * will have a check mark.
	 *
	 * @return <code>true</code> if a check mark is shown, <code>false</code> otherwise
	 */
	protected boolean getShowActive() {
		return showActive;
	}
	
	/**
	 * Returns the window for this menu.
	 *
	 * @return the window 
	 */
	protected IWorkbenchWindow getWindow() {
		return window;
	}
	/* (non-Javadoc)
	 * Returns whether this menu is dynamic.
	 */
	public boolean isDirty() {
		return dirty;
	}	
	/* (non-Javadoc)
	 * Returns whether this menu is dynamic.
	 */
	public boolean isDynamic() {
		return true;
	}
	/**
	 * Runs an action for a particular perspective.  The behavior of the
	 * action is defined by the subclass.
	 *
	 * @param desc the selected perspective
	 */
	protected abstract void run(IPerspectiveDescriptor desc);
	
	/**
	 * Runs an action for a particular perspective.  The behavior of the
	 * action is defined by the subclass.
	 *
	 * @param desc the selected perspective
	 * @param event SelectionEvent - the event send along with the selection callback
	 */
	protected void run(IPerspectiveDescriptor desc, SelectionEvent event) {
		//Do a run without the event by default
		run(desc);
	}
	
	/* (non-Javadoc)
	 * Show the "other" dialog, select a perspective, and run it. Pass on the selection
	 * event should the meny need it.
	 */
	void runOther(SelectionEvent event) {
		SelectPerspectiveDialog dlg =
			new SelectPerspectiveDialog(window.getShell(), reg);
		dlg.open();
		if (dlg.getReturnCode() == Window.CANCEL)
			return;
		IPerspectiveDescriptor desc = dlg.getSelection();
		if (desc != null) {
			run(desc, event);
		}
	}
	
	/**
	 * Sets the showActive flag.  If <code>showActive == true</code> then the
	 * active perspective is hilighted with a check mark.
	 *
	 * @param the new showActive flag
	 */
	protected void showActive(boolean b) {
		showActive = b;
	}
}
