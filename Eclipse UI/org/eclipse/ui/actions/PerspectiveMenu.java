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
package org.eclipse.ui.actions;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;

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

    /**
     * The translatable message to show when there are no perspectives.
     * 
     * @since 3.1
     */
    private static final String NO_TARGETS_MSG = WorkbenchMessages
            .getString("Workbench.showInNoPerspectives"); //$NON-NLS-1$

    /**
     * The map of perspective identifiers (String) to actions
     * (OpenPerspectiveAction). This map may be empty, but it is never
     * <code>null</code>.
     * 
     * @since 3.1
     */
    private Map actions = new HashMap();

    /**
     * The action for that allows the user to choose any perspective to open.
     * 
     * @since 3.1
     */
    private Action openOtherAction = new Action(WorkbenchMessages
            .getString("PerspectiveMenu.otherItem")) {//$NON-NLS-1$
        public final void runWithEvent(final Event event) {
            runOther(new SelectionEvent(event));
        }
    };


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

    /*
     * (non-Javadoc) Fills the menu with perspective items.
     */
    public void fill(Menu menu, int index) {
        if (getParent() instanceof MenuManager)
            ((MenuManager) getParent()).addMenuListener(menuListener);

        if (!dirty)
            return;

        final MenuManager manager = new MenuManager();
        fillMenu(manager);
        final IContributionItem items[] = manager.getItems();
        if (items.length == 0) {
            MenuItem item = new MenuItem(menu, SWT.NONE, index++);
            item.setText(NO_TARGETS_MSG);
            item.setEnabled(false);
        } else {
            for (int i = 0; i < items.length; i++) {
                items[i].fill(menu, index++);
            }
        }
        dirty = false;
    }

    /**
     * Fills the given menu manager with all the open perspective actions
     * appropriate for the currently active perspective. Filtering is applied to
     * the actions based on the activities and capabilities mechanism.
     * 
     * @param manager
     *            The menu manager that should receive the menu items; must not
     *            be <code>null</code>.
     * @since 3.1
     */
    private final void fillMenu(final MenuManager manager) {
        // Clear out the manager so that we have a blank slate.
        manager.removeAll();

        // Collect and sort perspective descriptors.
        final List persps = getPerspectiveItems();
        Collections.sort(persps, comparator);

        /*
         * Convert the perspective descriptors to actions, and filter out
         * actions using the activity/capability mechanism.
         */
        final List actions = new ArrayList(persps.size());
        for (Iterator i = persps.iterator(); i.hasNext();) {
            final IPerspectiveDescriptor descriptor = (IPerspectiveDescriptor) i
                    .next();
            final IAction action = getAction(descriptor.getId());
            if (action != null) {
                if (WorkbenchActivityHelper.filterItem(action))
                    continue;
                actions.add(action);
            }
        }

        // Go through and add each of the actions to the menu manager.
        for (Iterator i = actions.iterator(); i.hasNext();) {
            manager.add((IAction) i.next());
        }

        // Add a separator and then "Other..."
        if (actions.size() > 0) {
            manager.add(new Separator());
        }
        manager.add(openOtherAction);
    }

    /**
     * Returns the action for the given perspective id. This is a lazy cache. If
     * the action does not already exist, then it is created. If there is no
     * perspective with the given identifier, then the action is not created.
     * 
     * @param id
     *            The identifier of the perspective for which the action should
     *            be retrieved.
     * @return The action for the given identifier; or <code>null</code> if
     *         there is no perspective with the given identifier.
     * @since 3.1
     */
    private final IAction getAction(final String id) {
        IAction action = (IAction) actions.get(id);
        if (action == null) {
            final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
                    .getPerspectiveRegistry();
            final IPerspectiveDescriptor descriptor = registry
                    .findPerspectiveWithId(id);
            if (descriptor != null) {
                action = new OpenPerspectiveAction(window, descriptor, this);
                action.setActionDefinitionId(id);
                actions.put(id, action);
            }
        }
        return action;
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

        String[] ids = page.getPerspectiveShortcuts();

        for (int i = 0; i < ids.length; i++) {
            IPerspectiveDescriptor desc = reg.findPerspectiveWithId(ids[i]);
            if (desc != null && !list.contains(desc)) {
                if (WorkbenchActivityHelper.filterItem(desc))
                    continue;
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
     * Runs an action for a particular perspective. The behavior of the action
     * is defined by the subclass. By default, this just calls
     * <code>run(IPerspectiveDescriptor)</code>.
     * 
     * @param desc
     *            the selected perspective
     * @param event
     *            SelectionEvent - the event send along with the selection
     *            callback
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
        SelectPerspectiveDialog dlg = new SelectPerspectiveDialog(window
                .getShell(), reg);
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
     * @param b the new showActive flag
     */
    protected void showActive(boolean b) {
        showActive = b;
    }
}