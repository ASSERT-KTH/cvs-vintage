package org.columba.core.config;

import java.awt.Font;

import org.columba.core.xml.XmlElement;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class GuiItem extends DefaultItem {

	public GuiItem(XmlElement root) {
		super(root);
	}

	public Font getMainFont() {
		String name = get("mainfont", "name");
		int size = getInteger("mainfont", "size");

		return new Font(name, Font.PLAIN, size);
	}

	public Font getTextFont() {
		String name = get("textfont", "name");
		int size = getInteger("textfont", "size");

		return new Font(name, Font.PLAIN, size);
	}
}
