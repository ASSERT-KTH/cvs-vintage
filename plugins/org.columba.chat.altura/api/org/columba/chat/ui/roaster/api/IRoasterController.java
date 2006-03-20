package org.columba.chat.ui.roaster.api;

import javax.swing.tree.DefaultMutableTreeNode;

import org.columba.chat.model.api.IBuddyStatus;


public interface IRoasterController {

	public abstract IBuddyStatus getSelected();

	public abstract void updateBuddyPresence(IBuddyStatus buddy);

	public abstract void populate(DefaultMutableTreeNode rootNode);

	public abstract void setEnabled(boolean enabled);
}