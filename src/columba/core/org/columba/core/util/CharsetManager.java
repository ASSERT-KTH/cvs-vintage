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

package org.columba.core.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;
import java.util.Vector;

import javax.swing.JMenu;

import org.columba.core.gui.util.CMenu;
import org.columba.core.gui.util.CMenuItem;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author -
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CharsetManager implements ActionListener{

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

	private static final String[] groups = { "global", "westeurope", "easteurope", "eastasian", "seswasian"};

	private static final int[] groupOffset = { 1, 4, 14, 29, 42, 48 };

	private Vector listeners;
	
	private CharsetMenuItem selectedMenuItem;
	private int selectedId;

	public CharsetManager() {
		listeners = new Vector();
		selectedId = 0;	// TODO: Make the menu remember its last setting
	}

	public void createMenu(JMenu subMenu, MouseListener handler) {
		JMenu subsubMenu;
		CharsetMenuItem menuItem;

		int groupSize = Array.getLength(groups);
		int charsetSize = Array.getLength(charsets);

		/*
		subMenu =
			new CMenu(MailResourceLoader.getString("menu","mainframe", "menu_view_charset"));
		subMenu.setIcon( ImageLoader.getImageIcon("stock_font_16.png"));
		*/

		selectedMenuItem = new CharsetMenuItem( 
				MailResourceLoader.getString("menu","mainframe", "menu_view_charset_"+charsets[0]),
				-1, 0, charsets[0]);
		
		selectedMenuItem.addMouseListener(handler);

		subMenu.add(selectedMenuItem);

		subMenu.addSeparator();


		menuItem =
			new CharsetMenuItem(
				MailResourceLoader.getString("menu","mainframe", "menu_view_charset_"+charsets[0]),
				-1, 0, charsets[0]);
		menuItem.addMouseListener(handler);
		menuItem.addActionListener( this );


		subMenu.add(menuItem);

		// Automatic Generation of Groups

		for (int i = 0; i < groupSize; i++) {
			subsubMenu =
				new CMenu(
					MailResourceLoader.getString(
						"menu","mainframe",
						"menu_view_charset_" + groups[i]));
			subMenu.add(subsubMenu);

			for (int j = groupOffset[i]; j < groupOffset[i + 1]; j++) {
				menuItem =
					new CharsetMenuItem(
						MailResourceLoader.getString(
							"menu","mainframe",
							"menu_view_charset_"+charsets[j]),
						-1,
						j, charsets[j]);
				menuItem.addMouseListener(handler);
				menuItem.addActionListener( this );
				subsubMenu.add(menuItem);
			}
		}

	}

	/**
	 * Adds a CharcterCodingListener.
	 * @param listener The listener to set
	 */
	public void addCharsetListener(CharsetListener listener) {
		if( !listeners.contains(listener) )
			listeners.add( listener );
	}
	
	public void actionPerformed( ActionEvent e ) {
		CharsetEvent event;

		int charsetId = ((CharsetMenuItem)e.getSource()).getId();

		event = new CharsetEvent( this, charsetId, charsets[charsetId]);
			
		selectedMenuItem.setText( MailResourceLoader.getString(
							"menu","mainframe",
							"menu_view_charset_"+charsets[event.getId()]) );
		
		for( int i=0; i<listeners.size(); i++ ) 
			((CharsetListener)listeners.get(i)).charsetChanged(event);		
	}

}

class CharsetMenuItem extends CMenuItem{
		
	int id;
	String javaCodingName;
	
	public CharsetMenuItem( String name, int i, int id, String javaCodingName) {
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
