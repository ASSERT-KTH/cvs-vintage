package org.columba.core.command;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class GuiUpdater {
	
	private Worker guiUpdater;

	/**
	 * Returns the guiUpdater.
	 * @return Worker
	 */
	public Worker getGuiUpdater() {
		return guiUpdater;
	}

	/**
	 * Sets the guiUpdater.
	 * @param guiUpdater The guiUpdater to set
	 */
	public void setGuiUpdater(Worker guiUpdater) {
		this.guiUpdater = guiUpdater;
	}
	
	public boolean amIGuiUpdater( Worker w ) {
		return ( w == guiUpdater);	
	}

}
