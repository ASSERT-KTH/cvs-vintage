package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.ui.actions.RetargetAction;

/**
 * An <code>EditorMenuManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.  
 */
public class EditorMenuManager extends SubMenuManager {
	private IMenuManager parentMgr;
  	private ArrayList wrappers;
  	private boolean enabledAllowed = true;
  	
	private class Overrides implements IContributionManagerOverrides {
		/**
		 * Indicates that the items of this manager are allowed to enable;
		 * <code>true</code> by default.
		 */
		public void updateEnabledAllowed() {
			// update the items in the map
			IContributionItem[] items = EditorMenuManager.super.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				item.update(IContributionManagerOverrides.P_ENABLED);
			}
			// update the wrapped menus
			if (wrappers != null) {
				for (int i = 0; i < wrappers.size(); i++) {
					EditorMenuManager manager = (EditorMenuManager)wrappers.get(i);
					manager.setEnabledAllowed(enabledAllowed);
				}
			}
		}
		public Boolean getEnabled(IContributionItem item) {
			if (((item instanceof ActionContributionItem) &&
				(((ActionContributionItem)item).getAction() instanceof RetargetAction)) ||
				enabledAllowed)
				return null;  
			else
				return Boolean.FALSE;	
		}
		public Integer getAccelerator(IContributionItem item) {
			if (getEnabled(item) == null)
				return parentMgr.getOverrides().getAccelerator(item);
			else
				// no acclerator if the item is disabled
				return new Integer(0);
		}
		public String getAcceleratorText(IContributionItem item) {
			return parentMgr.getOverrides().getAcceleratorText(item);
		}
		public String getText(IContributionItem item) {
			return parentMgr.getOverrides().getText(item);
		}
	}
	private Overrides overrides = new Overrides();
/**
 * Constructs a new editor manager.
 */
public EditorMenuManager(IMenuManager mgr) {
	super(mgr);
	parentMgr = mgr;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem[] getItems() {
	return parentMgr.getItems();
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionManagerOverrides getOverrides() {
	return overrides;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 * Inserts the new item after any action set contributions which may
 * exist within the toolbar to ensure a consistent order for actions.
 */
public void insertAfter(String id, IContributionItem item) {
	IContributionItem refItem = PluginActionSetBuilder.findInsertionPoint(id,
		null, parentMgr, false);
	if (refItem != null) {
		super.insertAfter(refItem.getId(), item);
	} else {
		WorkbenchPlugin.log("Reference item " + id + " not found for action " + item.getId()); //$NON-NLS-1$
	}
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 * Inserts the new item after any action set contributions which may
 * exist within the toolbar to ensure a consistent order for actions.
 */
public void prependToGroup(String groupName, IContributionItem item) {
	insertAfter(groupName, item);
}
/**
 * Sets the visibility of the manager. If the visibility is <code>true</code>
 * then each item within the manager appears within the parent manager.
 * Otherwise, the items are not visible.
 * <p>
 * If force visibility is <code>true</code>, or grayed out if force visibility is <code>false</code>
 * <p>
 * This is a workaround for the layout flashing when editors contribute
 * large amounts of items.</p>
 *
 * @param visible the new visibility
 * @param forceVisibility whether to change the visibility or just the
 * 		enablement state.
 */
public void setVisible(boolean visible, boolean forceVisibility) {
	if (visible) {
		if (forceVisibility) {
			// Make the items visible 
			if (!enabledAllowed) 
				setEnabledAllowed(true);
		} else {
			if (enabledAllowed) 
				setEnabledAllowed(false);
		}
		if (!isVisible())
			setVisible(true);
	}
	else {
		if (forceVisibility)
			// Remove the editor menu items
			setVisible(false);
		else
			// Disable the editor menu items.
			setEnabledAllowed(false);
	}
}
/**
 * Sets the enablement ability of all the items contributed by the editor.
 *
 * @param enabledAllowed <code>true</code> if the items may enable
 * @since 2.0
 */
public void setEnabledAllowed(boolean enabledAllowed) {
	if (this.enabledAllowed == enabledAllowed)
		return;
	this.enabledAllowed = enabledAllowed;
	overrides.updateEnabledAllowed();
}
/* (non-Javadoc)
 * Method declared on SubMenuManager.
 */
protected SubMenuManager wrapMenu(IMenuManager menu) {
	if (wrappers == null)
		wrappers = new ArrayList();
	EditorMenuManager manager = new EditorMenuManager(menu);
	wrappers.add(manager);	
	return manager;
}

protected IAction[] getAllContributedActions() {
	HashSet set = new HashSet();
	getAllContributedActions(set);
	return (IAction[])set.toArray(new IAction[set.size()]);	
}
protected void getAllContributedActions(HashSet set) {
	IContributionItem[] items = super.getItems();
	for (int i = 0; i < items.length; i++)
		getAllContributedActions(set, items[i]);
	if(wrappers == null)
		return;
	for (Iterator iter = wrappers.iterator(); iter.hasNext();) {
		EditorMenuManager element = (EditorMenuManager)iter.next();
		element.getAllContributedActions(set);
	}
}
protected void getAllContributedActions(HashSet set, IContributionItem item) {
	if(item instanceof MenuManager) {
		IContributionItem subItems[] = ((MenuManager)item).getItems();
		for (int j = 0; j < subItems.length; j++)
			getAllContributedActions(set,subItems[j]);
	} else if(item instanceof ActionContributionItem) {
		set.add(((ActionContributionItem)item).getAction());
	}
}

}
