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

package org.columba.mail.gui.mimetype;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.columba.core.util.OSInfo;

import org.columba.mail.message.MimeHeader;

public class WindowsViewer extends AbstractViewer {

	public Process openWith(MimeHeader header, File tempFile) {
		openDocument(tempFile.getPath());
		return null;
	}

	public Process open(MimeHeader header, File tempFile) {
		openDocument(tempFile.getPath());
		return null;
	}

	public Process openURL(URL url) {
		if (OSInfo.isWin2K() || OSInfo.isWinXP()) {
			Process proc = null;
			try {
				String[] cmd =
					new String[] { "cmd.exe", "/C", "start", url.toString()};
				Runtime rt = Runtime.getRuntime();
				System.out.println(
					"Executing "
						+ cmd[0]
						+ " "
						+ cmd[1]
						+ " "
						+ cmd[2]
						+ " "
						+ cmd[3]);
				proc = rt.exec(cmd);
				// any error message?
				StreamGobbler errorGobbler =
					new StreamGobbler(proc.getErrorStream(), "ERROR");
				errorGobbler.start();
				// any output?
				StreamGobbler outputGobbler =
					new StreamGobbler(proc.getInputStream(), "OUTPUT");
				outputGobbler.start();

				// any error?
				int exitVal = proc.waitFor();
				System.out.println("ExitValue: " + exitVal);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			openDocument(url.getPath());
		}
		return null;
	}

	public Process openWithURL(URL url) {
		openDocument(url.getPath());
		return null;
	}

	protected void openDocument(String filename) {
		try {
			Process proc = null;
			if (OSInfo.isWinNT()) {
				String[] cmd = new String[] { "cmd.exe", "/C", filename };
				Runtime rt = Runtime.getRuntime();
				System.out.println(
					"Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
				proc = rt.exec(cmd);
			} else if (OSInfo.isWin95() || OSInfo.isWin98() || OSInfo.isWinME()) {
				String[] cmd = new String[] { "start", filename };
				Runtime rt = Runtime.getRuntime();
				System.out.println("Executing " + cmd[0] + " " + cmd[1]);
				proc = rt.exec(cmd);
			} else if (OSInfo.isWin2K() || OSInfo.isWinXP()){
				// this includes Windows XP
				String[] cmd = new String[3];
				cmd[0] = "cmd.exe";
				cmd[1] = "/C";
				cmd[2] =
					filename.charAt(0) + "\"" + filename.substring(1) + "\"";

				Runtime rt = Runtime.getRuntime();
				System.out.println(
					"Executing " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
				proc = rt.exec(cmd);
			}
                        
                        if (proc == null) {
                                //unhandled windows version
                                return;
                        }

			// any error message?
			StreamGobbler errorGobbler =
				new StreamGobbler(proc.getErrorStream(), "ERROR");
			errorGobbler.start();
			// any output?
			StreamGobbler outputGobbler =
				new StreamGobbler(proc.getInputStream(), "OUTPUT");
			outputGobbler.start();

			// any error?
			int exitVal = proc.waitFor();
			System.out.println("ExitValue: " + exitVal);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	class StreamGobbler extends Thread {
		InputStream is;
		String type;

		StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					System.out.println(type + ">" + line);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
