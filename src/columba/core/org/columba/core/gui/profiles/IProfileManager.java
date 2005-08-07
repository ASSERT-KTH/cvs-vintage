package org.columba.core.gui.profiles;

import org.columba.core.xml.XmlElement;

/**
 * Manages profiles consisting of configuration folders.
 * <p>
 * Every profile has a name and a loation pointing to the configuration folder.
 * <p>
 * A profiles.xml configuration file is saved in the default config directory,
 * storing all profiles information.
 * 
 * @author fdietz
 */
public interface IProfileManager {

	/**
	 * Get profile with name
	 * 
	 * @param name
	 *            name of class
	 * 
	 * @return return profile if available. Otherwise, return null
	 */
	public abstract Profile getProfileForName(String name);

	/**
	 * Get profile.
	 * 
	 * @param location
	 *            location of config folder
	 * 
	 * @return profile if available. Otherwise, return null
	 */
	public abstract Profile getProfile(String location);

	/**
	 * Get formely selected profile. This was selected on the previous startup
	 * of Columba.
	 * 
	 * @return selected profile
	 */
	public abstract String getSelectedProfile();

	/**
	 * Get profiles configuration.
	 * 
	 * @return top-level profiles node
	 */
	public abstract XmlElement getProfiles();

	/**
	 * @return Returns the currentProfile.
	 */
	public abstract Profile getCurrentProfile();

}