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

package org.columba.main;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.columba.core.command.DefaultProcessor;
import org.columba.core.config.Config;
import org.columba.core.config.ConfigPath;
import org.columba.core.gui.statusbar.ImageSequenceTimer;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.StartUpFrame;
import org.columba.core.gui.util.ThemeSwitcher;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.CharsetManager;
import org.columba.core.util.CmdLineArgumentParser;
import org.columba.core.util.TempFileStore;
import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.main.AddressbookMain;
import org.columba.mail.coder.Base64Decoder;
import org.columba.mail.coder.Base64Encoder;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.QuotedPrintableDecoder;
import org.columba.mail.coder.QuotedPrintableEncoder;
import org.columba.mail.composer.MimeTypeLookup;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.config.accountwizard.AccountWizard;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.tree.TreeModel;
import org.columba.mail.pop3.POP3ServerCollection;
import org.columba.mail.util.MailResourceLoader;

public class Main {
	private static ColumbaLoader columbaLoader;

	public static void loadInVMInstance(String[] arguments) {
		try {
			Socket clientSocket =
				new Socket("127.0.0.1", ColumbaLoader.COLUMBA_PORT);

			PrintWriter writer =
				new PrintWriter(clientSocket.getOutputStream());

			StringBuffer buf = new StringBuffer();
			buf.append("columba:");
			for (int i = 0; i < arguments.length; i++) {
				buf.append(arguments[i]);
				buf.append("%");
			}

			writer.write(buf.toString());
			writer.flush();
			writer.close();

			clientSocket.close();

			System.exit(5);

		} catch (Exception ex) { // we get a java.net.ConnectException: Connection refused
			//  -> this means that no server is running
			//      -> lets start one
			columbaLoader = new ColumbaLoader();
		}

	}

	public static void main(String[] arg) {
		final String[] args = arg;

		ColumbaCmdLineArgumentParser cmdLineParser =
			new ColumbaCmdLineArgumentParser();

		try {
			cmdLineParser.parse(args);
		} catch (CmdLineArgumentParser.UnknownOptionException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage();
			System.exit(2);
		} catch (CmdLineArgumentParser.IllegalOptionValueException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage();
			System.exit(2);
		}

		CmdLineArgumentParser.Option[] allOptions =
			new CmdLineArgumentParser.Option[] {
				ColumbaCmdLineArgumentParser.DEBUG,
				ColumbaCmdLineArgumentParser.COMPOSER,
				ColumbaCmdLineArgumentParser.RCPT,
				ColumbaCmdLineArgumentParser.MESSAGE,
				ColumbaCmdLineArgumentParser.PATH,
				};

		Object path =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.PATH);
		Object d =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.DEBUG);

		if (d != null)
			MainInterface.DEBUG = Boolean.TRUE;

		if (path != null) {
			new ConfigPath((String) path);

		} else {
			new ConfigPath();

		}

		loadInVMInstance(arg);

		final StartUpFrame frame = new StartUpFrame();
		frame.setVisible(true);

		final StartUpWorker worker = new StartUpWorker() {
			JFrame mainFrame;

			public Object construct() {

				MainInterface.addressbookInterface = new AddressbookInterface();

				// enable logging 
				new ColumbaLogger();

				new Config();

				new MailConfig();

				new AddressbookConfig();

				Config.init();

				new TempFileStore();

				ThemeSwitcher.setTheme();

				frame.advance();

				new ImageLoader();

				new MailResourceLoader();

				MainInterface.popServerCollection = new POP3ServerCollection();

				MainInterface.ext2mime = new MimeTypeLookup();

				new CoderRouter();
				new QuotedPrintableDecoder();
				new QuotedPrintableEncoder();
				new Base64Decoder();
				new Base64Encoder();

				MainInterface.charsetManager = new CharsetManager();

				//MainInterface.focusManager = new FocusManager();

				

				//MainInterface.imageSequenceTimer = new ImageSequenceTimer();

				//MainInterface.popServerCollection.enableMailCheckIcon();

				
				MainInterface.processor = new DefaultProcessor();
				MainInterface.processor.start();

				frame.advance();

				//MainInterface.headerTableViewer = new TableController();

				

				//MainInterface.attachmentViewer = new AttachmentController();

				//MainInterface.messageViewer = new MessageController();
				/*
				MainInterface.charsetManager.addCharsetListener(
					MainInterface.messageViewer);
				*/

				//MainInterface.treeController = new TreeView();

				//MainInterface.globalActionCollection.addActionListeners();

				AddressbookMain.main(null);
				

				doGuiInits();

				MainInterface.treeModel = new TreeModel(MailConfig.getFolderConfig());
				
				frame.advance();
				
				MainInterface.frameController = new MailFrameController();

				return null;
			}

			public void finished() {
				frame.setVisible(false);

				//mainFrame.setVisible(true);

				AccountWizard wizard = new AccountWizard();

				new CmdLineArgumentHandler(args);
			}

		}; // StartupWorker$

		worker.start();

	} // main

	private static void doGuiInits() {
		Keymap keymap;
		Action action;
		KeyStroke keystroke;

		/// CHANGES TO GLOBAL JTextComponent
		keymap = JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);

		// add "CTRL-INS" to "clipboard copy" functionality
		action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				((JTextComponent) e.getSource()).copy();
			}
		};
		keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, Event.CTRL_MASK);
		keymap.addActionForKeyStroke(keystroke, action);

		// add "SHIFT-DEL" to "clipboard cut" functionality
		action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				((JTextComponent) e.getSource()).cut();
			}
		};
		keystroke =
			KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, Event.SHIFT_MASK);
		keymap.addActionForKeyStroke(keystroke, action);

		// add "SHIFT-INS" to "clipboard paste" functionality
		action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				((JTextComponent) e.getSource()).paste();
			}
		};
		keystroke =
			KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, Event.SHIFT_MASK);
		keymap.addActionForKeyStroke(keystroke, action);

	} // doGuiInits

}
