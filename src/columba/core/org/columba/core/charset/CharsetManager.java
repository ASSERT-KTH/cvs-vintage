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
//All Rights Reserved.ion, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.charset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenu;

import org.columba.core.gui.menu.CMenuItem;
import org.columba.core.gui.util.CMenu;
import org.columba.core.xml.XmlElement;
import org.columba.mail.util.MailResourceLoader;

/**
 * The CharsetManager is in charge for managing the displayed and selected
 * Charset of e.g. a message. It also provides a menu that can be plugged
 * into the menubar and/or a contextmenu.
 */
public class CharsetManager implements ActionListener {

	// String definitions for the charsetnames
	// NOTE: these are also used to look up the
	// menuentries from the resourceloader  

	private static final String[] charsets = {
		// Auto
		"auto",
		// Global # 1
		"UTF-8", "UTF-16", "US-ASCII",

		// West Europe # 4
		"windows-1252",
			"ISO-8859-1",
			"ISO-8859-15",
			"IBM850",
			"MacRoman",
			"ISO-8859-7",
			"MacGreek",
			"windows-1253",
			"MacIceland",
			"ISO-8859-3",

		// East Europe # 14
		"ISO-8859-4",
			"ISO-8859-13",
			"windows-1257",
			"IBM852",
			"ISO-8859-2",
			"MacCentralEurope",
			"MacCroatian",
			"IBM855",
			"ISO-8859-5",
			"KOI8-R",
			"MacCyrillic",
			"windows-1251",
			"IBM866",
			"MacUkraine",
			"MacRomania",

		// East Asian # 29
		"GB2312",
			"GBK",
			"GB18030",
			"Big5",
			"Big5-HKSCS",
			"EUC-TW",
			"EUC-JP",
			"Shift_JIS",
			"ISO-2022-JP",
			"MS932",
			"EUC-KR",
			"JOHAB",
			"ISO-2022-KR",

		// West Asian # 42
		"TIS620",
			"IBM857",
			"ISO-8859-9",
			"MacTurkish",
			"windows-1254",
			"windows-1258"

		// # 48
	};

	private static final String[] groups =
		{ "global", "westeurope", "easteurope", "eastasian", "seswasian" };

	private static final int[] groupOffset = { 1, 4, 14, 29, 42, 48 };

	private List listeners;

	private CharsetMenuItem selectedMenuItem;

	private XmlElement config;

	private int defaultId;
	private int selectedId;

	/**
	 * 
	 * @param config The configuration that contains the user-selected Charset
	 */
	public CharsetManager(XmlElement config) {
		listeners = new Vector();
		this.config = config;

		// Grab default Charset of the System
		defaultId = getCharsetId(System.getProperty("file.encoding"));
		
		int charsetId;
		selectedId = 0;
		
		
		// Grab the user-selected charset if it exists
		if (config != null) {
			selectedId = getCharsetId(config.getAttribute("name"));
		}


		// if user selected auto or no user-preferences found
		// select the default Charset of the system else select
		// the preferred Charset
		if (selectedId == 0) {
			charsetId = defaultId;
		} else {
			charsetId = selectedId;
		}

		
		// this is the menuitem that shows the selected Charset
		// and changes when the selected/displayed charset changed
		selectedMenuItem =
			new CharsetMenuItem(
				MailResourceLoader.getString(
					"menu",
					"mainframe",
					"menu_view_charset_" + charsets[charsetId]),
				-1,
				0,
				charsets[selectedId]);
	}

	
	
	/**
	 * Change the menuitem that shows the selected/displayed Charset 
	 * 
	 * @param name
	 */
	public void displayCharset(String name) {
		int charsetId = getCharsetId(name);

		if (charsetId != -1) {
			selectedMenuItem.setText(
				MailResourceLoader.getString(
					"menu",
					"mainframe",
					"menu_view_charset_" + charsets[charsetId]));
		}
	}

	private int getCharsetId(String name) {
		// default should be 0
		// -> charsets[0] == "auto"
		if ( name == null ) return -1;
		if( name.equals("auto")) return defaultId;
		// looking if the given charsetname is in the charset-list, if not return default-id
		boolean found=false;
		for (int i=0; i < charsets.length; i++) {
			if (charsets[i].equals(name)) {
				found=true;
				break;
			}
		}
		// if name not found in the charset array return the defaultId
		if (!found) {
			return defaultId;
		}
		int charsetId = 0;
		String charsetCanonicalName = Charset.forName( name ).name();
		//System.out.println( name + " -> " + charsetCanonicalName);

		for (int i = 0; i < charsets.length; i++) {
			if (charsets[i].equals(charsetCanonicalName)) {
				charsetId = i;
			}
		}
		return charsetId;
	}

	/**
	 * Generates a charset menu from which this CharsetManager can
	 * be controlled.
	 * 
	 * @param subMenu Menu in wich the charsetmenu will be inserted
	 * @param handler  
	 */
	public void createMenu(JMenu subMenu, MouseListener handler) {
		JMenu subsubMenu;
		CharsetMenuItem menuItem;

		int groupSize = Array.getLength(groups);
		int charsetSize = Array.getLength(charsets);

		subMenu.add(selectedMenuItem);

		subMenu.addSeparator();

		menuItem =
			new CharsetMenuItem(
				MailResourceLoader.getString(
					"menu",
					"mainframe",
					"menu_view_charset_" + charsets[0]),
				-1,
				0,
				charsets[0]);
		menuItem.addMouseListener(handler);
		menuItem.addActionListener(this);

		subMenu.add(menuItem);

		// Automatic Generation of Groups

		for (int i = 0; i < groupSize; i++) {
			subsubMenu =
				new CMenu(
					MailResourceLoader.getString(
						"menu",
						"mainframe",
						"menu_view_charset_" + groups[i]));
			subMenu.add(subsubMenu);

			for (int j = groupOffset[i]; j < groupOffset[i + 1]; j++) {
				menuItem =
					new CharsetMenuItem(
						MailResourceLoader.getString(
							"menu",
							"mainframe",
							"menu_view_charset_" + charsets[j]),
						-1,
						j,
						charsets[j]);
				menuItem.addMouseListener(handler);
				menuItem.addActionListener(this);
				subsubMenu.add(menuItem);
			}
		}

	}

	/**
	 * Adds a CharcterCodingListener.
	 * @param listener The listener to set
	 */
	public void addCharsetListener(CharsetListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void actionPerformed(ActionEvent e) {
		CharsetEvent event;

		int charsetId = ((CharsetMenuItem) e.getSource()).getId();

		changeCharset(charsetId);
	}

	private void changeCharset(int charsetId) {
		CharsetEvent event;
		event = new CharsetEvent(this, charsetId, charsets[charsetId]);
		selectedId = charsetId;

		// Save the charset as user preference
		// NOTE: This may also be auto
		if (config != null) {
			config.addAttribute("name",charsets[charsetId]);
		}

		// Set displayed Charset
		if (charsetId == 0) {
			selectedMenuItem.setText(
				MailResourceLoader.getString(
					"menu",
					"mainframe",
					"menu_view_charset_" + charsets[defaultId]));
		} else {
			selectedMenuItem.setText(
				MailResourceLoader.getString(
					"menu",
					"mainframe",
					"menu_view_charset_" + charsets[charsetId]));
		}


		// Notify Listeners
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			// passing all events to the listeners
			((CharsetListener) it.next()).charsetChanged(event);
		}
	}
	
	public String getSelectedCharset() {
		return charsets[selectedId];
	}

}

class CharsetMenuItem extends CMenuItem {

	int id;
	String javaCodingName;

	public CharsetMenuItem(String name, int i, int id, String javaCodingName) {
		//super( name, i );
		super(name);

		this.id = id;
		this.javaCodingName = javaCodingName;
	}

	/**
	 * Returns the javaCodingName.
	 * @return String
	 */
	public String getJavaCodingName() {
		return javaCodingName;
	}

	/**
	 * Sets the javaCodingName.
	 * @param javaCodingName The javaCodingName to set
	 */
	public void setJavaCodingName(String javaCodingName) {
		this.javaCodingName = javaCodingName;
	}

	/**
	 * Returns the id.
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

}
