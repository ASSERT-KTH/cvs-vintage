package org.columba.core.plugin;

public class ExtensionHandlerMetadata {

	public String id;

	public String className;

	public ExtensionHandlerMetadata(String id, String className) {
		this.id = id;
		this.className = className;
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
}
