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

package org.columba.core.main;

import javax.swing.plaf.metal.MetalLookAndFeel;

import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.command.DefaultProcessor;
import org.columba.core.config.Config;
import org.columba.core.plugin.PluginManager;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.util.CharsetManager;
import org.columba.mail.gui.frame.MailFrameModel;
import org.columba.mail.gui.tree.TreeModel;
import org.columba.mail.pgp.PGPController;
import org.columba.mail.pop3.POP3ServerCollection;

public class MainInterface {
	public static String version = "0.9.11";
	public static Boolean DEBUG = Boolean.FALSE;

	public static Config config;
	//public static TableController headerTableViewer;
	//public static AttachmentController attachmentViewer;
	//public static MessageController messageViewer;
	//public static TreeController treeController;
	//public static FrameView mainFrame;
	public static POP3ServerCollection popServerCollection;
	//public static Locale currentLocale;
	//public static GlobalActionCollection globalActionCollection;
	// public static DefaultCTheme columbaTheme;
	//public static FolderInfoPanel folderInfoPanel;

	//public static FocusManager focusManager;
	
	public static TreeModel treeModel;
	public static MailFrameModel frameModel;
	
	//public static MailFrameController frameController;
	
	

	//public static ResourceBundle guiLabels, headerLabels;
	//public static GlobalResourceLoader parentResourceLoader;
	//public static String mainFontName;
	//public static int mainFontSize;
	//public static String textFontName;
	//public static int textFontSize;

	/*
	public static HeaderTableItem headerTableItem;
	public static WindowItem mainFrameWindowItem;
	public static WindowItem composerWindowItem;
	public static ThemeItem themeItem;
	*/
	public static MetalLookAndFeel lookAndFeel;

	public static DefaultProcessor processor;

	public static PGPController pgpController;

	//public static ImageSequenceTimer imageSequenceTimer;

	public static AddressbookInterface addressbookInterface;

	public static CharsetManager charsetManager;
	
	public static PluginManager pluginManager;
	
	public static ShutdownManager shutdownManager;

	public MainInterface() {
	}

	public static boolean isDebug() {
		return DEBUG.booleanValue();
	}
}
