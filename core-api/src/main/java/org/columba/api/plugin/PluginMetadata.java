// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.api.plugin;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

public class PluginMetadata {

	private String id;

	private String name;

	private String description;

	private String version;

	private String category;

	private boolean enabled;

	private Vector extensions = new Vector();

	private File directory;

	public PluginMetadata(String id, String name, boolean enabled) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
	}

	public PluginMetadata(String id, String name, String description,
			String version, String category, boolean enabled) {
		this(id, name, enabled);

		this.description = description;
		this.version = version;
		this.category = category;

	}

	public void addExtension(ExtensionMetadata metadata) {
		extensions.add(metadata);
	}

	public Enumeration enumExtensions() {
		return extensions.elements();
	}

	/**
	 * @return Returns the category.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the directory.
	 */
	public File getDirectory() {
		return directory;
	}

	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return Returns the extensions.
	 */
	public Vector getExtensions() {
		return extensions;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param enabled
	 *            The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param directory
	 *            The directory to set.
	 */
	public void setDirectory(File directory) {
		this.directory = directory;
	}

}
