package org.columba.addressbook.plugin;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.xml.XmlElement;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FolderPluginHandler extends AbstractPluginHandler {

	protected XmlElement parentNode;
	
	/**
	 * Constructor for FolderPluginHandler.
	 * @param id
	 * @param config
	 */
	public FolderPluginHandler() {
		super("addressbook_folder", "org/columba/addressbook/folder/folder.xml");

		parentNode = getConfig().getRoot().getElement("folderlist");
	}

	/**
	 * @see org.columba.core.plugin.AbstractPluginHandler#getNames()
	 */
	public String[] getDefaultNames() {
		int count = parentNode.count();

		String[] list = new String[count];

		for (int i = 0; i < count; i++) {
			XmlElement action = parentNode.getElement(i);
			String s = action.getAttribute("name");

			list[i] = s;

		}

		return list;
	}

	/**
		 * @see org.columba.core.plugin.AbstractPluginHandler#getPluginClassName(java.lang.String, java.lang.String)
		 */
	protected String getPluginClassName(String name, String id) {

		int count = parentNode.count();

		for (int i = 0; i < count; i++) {

			XmlElement action = parentNode.getElement(i);
			String s = action.getAttribute("name");

			if (name.equalsIgnoreCase(s))
				return action.getAttribute(id);

		}

		return null;
	}

	public Object getPlugin(String name, Object[] args) throws Exception {
		String className = getPluginClassName(name, "class");
		return getPlugin(name, className, args);
	}

	public Class getPluginClass(String name) {
		String className = getPluginClassName(name, "class");
		
		try {

			Class clazz = Class.forName(className);
			return clazz;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
