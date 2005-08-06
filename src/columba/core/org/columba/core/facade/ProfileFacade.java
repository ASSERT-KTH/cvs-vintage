package org.columba.core.facade;

import org.columba.core.profiles.IProfileManager;
import org.columba.core.profiles.ProfileManager;

public class ProfileFacade {

	public static IProfileManager getProfileManager() {
		return (IProfileManager) ProfileManager.getInstance();
	}
}
