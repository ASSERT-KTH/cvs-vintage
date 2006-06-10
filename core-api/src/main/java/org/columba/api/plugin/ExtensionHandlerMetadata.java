package org.columba.api.plugin;

public class ExtensionHandlerMetadata {

	public String id;

	public String parent;

	/**
	 * @param id		unique id
	 * @param parent	unique id of parent this extension handler depends on, can be <code>null</code>
	 */
	public ExtensionHandlerMetadata(String id, String parent) {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		this.id = id;
		this.parent = parent;
	}

	/**
	 * @return Returns the parent.
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
}
