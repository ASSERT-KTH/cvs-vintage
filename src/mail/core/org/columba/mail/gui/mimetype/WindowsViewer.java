// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.mimetype;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.columba.core.util.OSInfo;
import org.columba.ristretto.message.MimeHeader;

public class WindowsViewer extends AbstractViewer {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.mimetype");

	public Process openWith(MimeHeader header, File tempFile, boolean blocking) {
		// *20030714, karlpeder* openDocumentWidth now called
		return openDocumentWith(tempFile.getPath());
	}

	public Process open(MimeHeader header, File tempFile, boolean blocking) {
		return openDocument(tempFile.getPath(), blocking);
	}

	public Process openURL(URL url) {
		if (OSInfo.isWin2K() || OSInfo.isWinXP()) {
			Process proc = null;
             
			try {
                //BUG 980606 fixed. 20040630 SWITT: '"' removed from cmd[2] 			
				String[] cmd = new String[]{"rundll32",
						"url.dll,FileProtocolHandler",
						 url.toString() };
				Runtime rt = Runtime.getRuntime();
				LOG.fine("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
				proc = rt.exec(cmd);

				return proc;

			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			return openDocument(url.getPath(), false);
		}

		return null;
	}

	public Process openWithURL(URL url) {
		// *20030714, karlpeder* openDocumentWidth now called
		return openDocumentWith(url.getPath());
	}

	protected Process openDocument(String filename, boolean blocking) {
		Process proc = null;

		try {

			if (OSInfo.isWinNT()) {
				String[] cmd = new String[]{"cmd.exe", "/C", filename};
				Runtime rt = Runtime.getRuntime();
				LOG.fine("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
				proc = rt.exec(cmd);
			} else if (OSInfo.isWin95() || OSInfo.isWin98() || OSInfo.isWinME()) {
				String[] cmd = new String[]{"start", filename};
				Runtime rt = Runtime.getRuntime();
				LOG.fine("Executing " + cmd[0] + " " + cmd[1]);
				proc = rt.exec(cmd);
			} else if (OSInfo.isWin2K() || OSInfo.isWinXP()) {
				/*
				 * *20030526, karlpeder* fixing bug #739277 by: Changing cmd
				 * line from "cmd.exe /C ..." to "cmd.exe /C start ..." So
				 * program execution is not blocked until viewer terminates. NB:
				 * WinNT, Win95, Win98, WinME not considered (not able to try it
				 * out)
				 * 
				 * *20030713, karlpeder* fixing bug #763211 by moving first " in
				 * filename and adding extra parameter to the "start" command =
				 * title of dos window (which is not shown here - but is
				 * necessary).
				 */

				String[] cmd = null;
				if (!blocking) {
					// use "start" parameter to prevent blocking
					cmd = new String[]{"cmd.exe", "/C", "start", "\"dummy\"",
							"\"" + filename + "\""};
				} else {
					// external application blocks active frame of Columba
					// -> needed by the mail composer for "Edit->External
					// Editor" action
					cmd = new String[]{"cmd.exe", "/C", filename};
				}

				Runtime rt = Runtime.getRuntime();

				if (blocking)

					LOG.fine("Executing " + cmd[0] + " " + cmd[1] + " "
							+ cmd[2]);
				else
					LOG.fine("Executing " + cmd[0] + " " + cmd[1] + " "
							+ cmd[2] + " " + cmd[3] + " " + cmd[4]);

				proc = rt.exec(cmd);
			}

			if (proc == null) {
				LOG.info("The underlying Windows version is unknown.");

				return null;
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return proc;
	}

	/**
	 * Used to open a file with an application specified by the user using the
	 * standard Windows Open With dialog.
	 * 
	 * @param filename
	 *            Name of file to open
	 * @author Karl Peder Olesen (karlpeder) 20030714
	 */
	protected Process openDocumentWith(String filename) {
		Process proc = null;

		// TODO: Test with other platforms than Win2000
		try {

			if (OSInfo.isWinNT() || OSInfo.isWin95() || OSInfo.isWin98()
					|| OSInfo.isWinME() || OSInfo.isWin2K() || OSInfo.isWinXP()) {
				String[] cmd = new String[]{"rundll32.exe",
						"SHELL32.DLL,OpenAs_RunDLL", filename};

				Runtime rt = Runtime.getRuntime();
				LOG.fine("Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
				proc = rt.exec(cmd);
			}

			if (proc == null) {
				LOG.info("The underlying Windows version is unknown.");

				return null;
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return proc;
	}

}