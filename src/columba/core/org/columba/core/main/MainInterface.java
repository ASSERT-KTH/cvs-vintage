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

import java.util.Date;

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

	public static String version = "0.11.0 cvs";

	public static Boolean DEBUG = Boolean.FALSE;

	public static Config config;
	
	public static POP3ServerCollection popServerCollection;
	
	public static TreeModel treeModel;
	public static MailFrameModel frameModel;
	
	public static MetalLookAndFeel lookAndFeel;

	public static DefaultProcessor processor;

	public static PGPController pgpController;


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
