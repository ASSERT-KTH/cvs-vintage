// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
