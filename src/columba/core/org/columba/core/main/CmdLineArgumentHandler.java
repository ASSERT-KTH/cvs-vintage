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
package org.columba.core.main;

import org.columba.core.util.CmdLineArgumentParser;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.parser.MailUrlParser;

public class CmdLineArgumentHandler {
	//private MainInterface mainInterface;

	public CmdLineArgumentHandler(String[] args) {
		//this.mainInterface = mainInterface;

		ColumbaCmdLineArgumentParser cmdLineParser =
			new ColumbaCmdLineArgumentParser();

		try {
			cmdLineParser.parse(args);
		} catch (CmdLineArgumentParser.UnknownOptionException e) {
			System.err.println(e.getMessage());
			ColumbaCmdLineArgumentParser.printUsage();

		} catch (CmdLineArgumentParser.IllegalOptionValueException e) {
			System.err.println(e.getMessage());
			ColumbaCmdLineArgumentParser.printUsage();

		}

		CmdLineArgumentParser.Option[] allOptions =
			new CmdLineArgumentParser.Option[] {
				ColumbaCmdLineArgumentParser.DEBUG,
				ColumbaCmdLineArgumentParser.COMPOSER,
				ColumbaCmdLineArgumentParser.RCPT,
				ColumbaCmdLineArgumentParser.MESSAGE,
				ColumbaCmdLineArgumentParser.PATH,
				ColumbaCmdLineArgumentParser.MAILURL,
				ColumbaCmdLineArgumentParser.SUBJECT,
				ColumbaCmdLineArgumentParser.CC,
				ColumbaCmdLineArgumentParser.BCC };

		/*
		for (int j = 0; j < allOptions.length; ++j)
		{
			System.out.println(
				allOptions[j].longForm() + ": " + cmdLineParser.getOptionValue(allOptions[j]));
		}
		*/

		Object path =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.PATH);
		Object composer =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.COMPOSER);
		Object rcpt =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.RCPT);
		Object message =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.MESSAGE);
		Object debug =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.DEBUG);
		Object mailurl =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.MAILURL);
		Object subject =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.SUBJECT);
		Object cc =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.CC);
		Object bcc =
			cmdLineParser.getOptionValue(ColumbaCmdLineArgumentParser.BCC);

		if (mailurl != null) {
			String mailto = (String) mailurl;

			if (MailUrlParser.isMailUrl(mailto)) {

				MailUrlParser mailtoParser = new MailUrlParser(mailto);

				composer = new Boolean(true);

				rcpt = mailtoParser.get("mailto:");

				subject = mailtoParser.get("subject=");

				cc = mailtoParser.get("cc=");

				bcc = mailtoParser.get("bcc=");

				message = mailtoParser.get("body=");
			}
		}

		if (composer != null) {
			Boolean bool = (Boolean) composer;

			if (bool.equals(Boolean.TRUE)) {
				// open composer window

				//ComposerFrame frame = new ComposerFrame();
				ComposerController controller = new ComposerController();

				if (rcpt != null) {
					String rcptString = (String) rcpt;

					controller.getModel().setTo(rcptString);
				}

				if (subject != null) {
					String subjectString = (String) rcpt;

					controller.getModel().setSubject(subjectString);
				}

				if (cc != null) {
					String ccString = (String) cc;

					controller.getModel().setHeaderField("Cc", ccString);
				}

				if (bcc != null) {
					String bccString = (String) bcc;

					controller.getModel().setHeaderField("Bcc", bccString);
				}

				if (message != null) {
					String messageString = (String) message;

					controller.getModel().setBodyText(messageString);
				}
				
				controller.showComposerWindow();
			}
		}

   		MainInterface.DEBUG = debug != null ? (Boolean) debug : Boolean.FALSE;
	}
}