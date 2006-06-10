package org.columba.core.facade;

import org.columba.core.gui.profiles.IProfileManager;
import org.columba.core.gui.profiles.ProfileManager;

public class ProfileFacade {

	public static IProfileManager getProfileManager() {
		return (IProfileManager) ProfileManager.getInstance();
	}
}
