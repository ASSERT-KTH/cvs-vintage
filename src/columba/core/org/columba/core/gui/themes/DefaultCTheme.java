//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.gui.themes;

import java.awt.Font;

import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

import org.columba.core.config.GuiItem;

public class DefaultCTheme extends DefaultMetalTheme {

	Font mainFont;
	Font messageFont;

	private FontUIResource mainFontRessource;
	private FontUIResource messageFontRessource;

	protected GuiItem guiItem;

	public DefaultCTheme(GuiItem item) {
		this.guiItem = item;

		mainFont = guiItem.getMainFont();

		messageFont = guiItem.getTextFont();

	}

	public DefaultCTheme(Font mFont, Font eFont) {
		super();

		mainFont = mFont;
		messageFont = eFont;
	}

	public void setMainFont(Font f) {
		mainFont = f;
		mainFontRessource = new FontUIResource(mainFont);
	}

	public void setMessageFont(Font f) {
		messageFont = f;
		messageFontRessource = new FontUIResource(messageFont);
	}

	public String getName() {
		return "Default Columba Look and Feel";
	}

	public FontUIResource getMenuTextFont() {
		if (mainFontRessource == null) {
			mainFontRessource = new FontUIResource(mainFont);
		}

		return mainFontRessource;
	}

	public FontUIResource getControlTextFont() {
		if (mainFontRessource == null) {
			mainFontRessource = new FontUIResource(mainFont);
		}

		return mainFontRessource;
	}

	public FontUIResource getSubTextFont() {
		if (mainFontRessource == null) {
			mainFontRessource = new FontUIResource(mainFont);
		}

		return mainFontRessource;

	}

	public FontUIResource getSystemTextFont() {
		if (mainFontRessource == null) {
			mainFontRessource = new FontUIResource(mainFont);
		}

		return mainFontRessource;
	}

	public FontUIResource getUserTextFont() {
		if (messageFontRessource == null) {
			mainFontRessource = new FontUIResource(mainFont);
		}

		return mainFontRessource;

	}

	public void addCustomEntriesToTable(UIDefaults table) {
	}

}
