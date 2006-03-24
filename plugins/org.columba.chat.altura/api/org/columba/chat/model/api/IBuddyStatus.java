package org.columba.chat.model.api;

import org.jivesoftware.smack.packet.Presence;

public interface IBuddyStatus {

	/**
	 * @return Returns the jabberId.
	 */
	public abstract String getJabberId();

	/**
	 * @return Returns the presenceMode.
	 */
	public abstract Presence.Mode getPresenceMode();

	/**
	 * @return Returns the signedOn.
	 */
	public abstract boolean isSignedOn();

	/**
	 * @return Returns the statusMessage.
	 */
	public abstract String getStatusMessage();

	/**
	 * @return Returns the mediator.
	 */
	//public abstract IChatMediator getChatMediator();

	/**
	 * @param mediator The mediator to set.
	 */
	//public abstract void setChatMediator(IChatMediator mediator);

	/**
	 * @param presenceMode The presenceMode to set.
	 */
	public abstract void setPresenceMode(Presence.Mode presenceMode);

	/**
	 * @param signedOn The signedOn to set.
	 */
	public abstract void setSignedOn(boolean signedOn);

	/**
	 * @param statusMessage The statusMessage to set.
	 */
	public abstract void setStatusMessage(String statusMessage);

	/**
	 * @return Returns the user.
	 */
	public abstract String getName();

}