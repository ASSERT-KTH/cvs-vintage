package org.columba.chat.api;


public interface IRoasterTree {

	public abstract IBuddyStatus getSelected();

	public abstract void updateBuddyPresence(IBuddyStatus buddy);

	public abstract void populate();

	public abstract void setEnabled(boolean enabled);
}