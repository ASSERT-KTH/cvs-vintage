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
package org.columba.mail.main;

import jargs.gnu.CmdLineParser;

import org.columba.core.util.GlobalResourceLoader;
import org.columba.mail.gui.action.NewMessageAction;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.parser.MailUrlParser;

/**
 * Parsing the commandline arguments and setting states, that can be used from
 * other components.
 * 
 * @author waffel
 */
public class ColumbaCmdLineParser {

	private static final String RESOURCE_PATH = "org.columba.core.i18n.global";

	private static CmdLineParser parser;

	private static CmdLineParser.Option composer;

	private static CmdLineParser.Option rcpt;

	private static CmdLineParser.Option body;

	private static CmdLineParser.Option path;

	private static CmdLineParser.Option mailurl;

	private static CmdLineParser.Option subject;

	private static CmdLineParser.Option cc;

	private static CmdLineParser.Option bcc;

	private static CmdLineParser.Option attachment;

	static {
		parser = new CmdLineParser();

		// setting any options
		// the short option '+' is an hack until jargs supports also "only long
		// commands"
		// TODO (@author waffel): make options i18n compatible
		composer = parser.addBooleanOption('c', "composer");
		rcpt = parser.addStringOption('r', "rcpt");
		body = parser.addStringOption('b', "body");
		path = parser.addStringOption('p', "path");
		mailurl = parser.addStringOption('+', "mailurl");
		subject = parser.addStringOption('s', "subject");
		cc = parser.addStringOption('+', "cc");
		bcc = parser.addStringOption('+', "bcc");
		attachment = parser.addStringOption('a', "attachment");
	}

	protected String pathOption;

	protected String rcptOption;

	protected String bodyOption;

	protected boolean composerOption = false;

	protected String mailurlOption;

	protected String subjectOption;

	protected String ccOption;

	protected String bccOption;

	protected String attachmentOption = null;

	public ColumbaCmdLineParser() {
	}

	/**
	 * Parsing the commandline arguments and set the given values to the
	 * commandline arguments.
	 * 
	 * @param args
	 *            commandline arguments to be parsed
	 */
	public void parseCmdLine(String[] args) throws IllegalArgumentException {
		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		
		checkPath();
		checkRCPT();
		checkBody();
		checkComposer();
		checkSubject();
		checkMailurl();
		checkAttachment();

		String mailURL = getMailurlOption();

		if (mailURL != null) {
			if (MailUrlParser.isMailUrl(mailURL)) {
				MailUrlParser mailToParser = new MailUrlParser(mailURL);
				setComposerOption(true);
				setRcptOption((String) mailToParser.get("mailto:"));
				setSubjectOption((String) mailToParser.get("subject="));
				setCcOption((String) mailToParser.get("cc="));
				setBccOption((String) mailToParser.get("bcc="));
				setBodyOption((String) mailToParser.get("body="));
			}
		}

		if (getComposerOption()) {
			ComposerModel model = new ComposerModel();

			if (getRcptOption() != null) {
				model.setTo(getRcptOption());
			}

			if (getSubjectOption() != null) {
				model.setSubject(getSubjectOption());
			}

			if (getCcOption() != null) {
				model.setHeaderField("Cc", getCcOption());
			}

			if (getBccOption() != null) {
				model.setHeaderField("Bcc", getBccOption());
			}

			if (getBodyOption() != null) {
				String body = getBodyOption();

				/*
				 * *20030917, karlpeder* Set the model to html or text based on
				 * the body specified on the command line. This is done using a
				 * simple check: Does the body contain <html> and </html>
				 */
				boolean html = false;
				String lcase = body.toLowerCase();

				if ((lcase.indexOf("<html>") != -1)
						&& (lcase.indexOf("</html>") != -1)) {
					html = true;
				}

				model.setHtml(html);

				// set the body text
				model.setBodyText(body);
			}

			new NewMessageAction().actionPerformed(null);

		}
	}

	/**
	 * prints the usage of the program with commandline arguments.
	 * 
	 * TODO (@author waffel): all options should be printed
	 */
	public static void printUsage() {

		System.out.println("Mail component:\n");
		
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_composeropt"));
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_rcptopt"));
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_bodyopt"));
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_mailurlopt"));
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_subjectopt"));
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_ccopt"));
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_bccopt"));
		System.out.println(GlobalResourceLoader.getString(RESOURCE_PATH,
				"global", "cmdline_attachmentopt"));

	}


	/**
	 * Checks if the commandline option -p,--path is given, if this is true a
	 * new ConfigPath with the path to the configs is generated, else a empty
	 * (default) ConfigPath Object is created
	 * 
	 * @param pathOpt
	 *            the path option
	 * @see CmdLineParser.Option
	 * @param parser
	 *            parser which parsed the Option
	 */
	private void checkPath() {
		String pathValue = (String) parser.getOptionValue(path);
		setPathOption(pathValue);
	}

	/**
	 * Checks if the commandline argument -r,--rcpt (recipient) is given. If
	 * this is true the intern value for recipient is set
	 * 
	 * @see ColumbaCmdLineParser#setRcptOption(String) ,else the option is set
	 *      to null. You can access it via
	 * @see ColumbaCmdLineParser#getRcptOption()
	 * @param rcptOpt
	 *            the recipient Option
	 * @see CmdLineParser.Option
	 * @param parser
	 *            parsed which parsed the Option
	 */
	private void checkRCPT() {
		String rcptValue = (String) parser.getOptionValue(rcpt);
		setRcptOption(rcptValue);
	}

	/**
	 * Checks if the commandline argument -b,--body is given, if this is true,
	 * then the intern value for body is set
	 * 
	 * @see ColumbaCmdLineParser#setBodyOption(String) ,else the option is set
	 *      to null. You can get the option value via
	 * @see ColumbaCmdLineParser#getBodyOption()
	 * @param bodyOpt
	 *            the Option for body
	 * @see CmdLineParser.Option
	 * @param parser
	 *            parser which parsed the Option
	 */
	private void checkBody() {
		String bodyValue = (String) parser.getOptionValue(body);
		setBodyOption(bodyValue);
	}

	/**
	 * Checks the option --composer, if this is true the intern composerValue is
	 * set
	 * 
	 * @see ColumbaCmdLineParser#setComposerOption(boolean) ,else the option is
	 *      set to null. You can access this option via
	 * @see ColumbaCmdLineParser#getComposerOption()
	 * @param composerOpt
	 *            Composer Option
	 * @see CmdLineParser.Option
	 * @param parser
	 *            parser which parsed the Option
	 */
	private void checkComposer() {
		Boolean composerValue = (Boolean) parser.getOptionValue(composer);

		if (composerValue != null) {
			setComposerOption(composerValue.booleanValue());
		}
	}

	/**
	 * Checks the option --mailurl, if this is true the intern mailurlValue is
	 * set
	 * 
	 * @see ColumbaCmdLineParser#setMailurlOption(String), else the option is
	 *      set to null. You can access this option via
	 * @see ColumbaCmdLineParser#getMailurlOption()
	 * @param mailurlOpt
	 *            Composer Option
	 * @see CmdLineParser.Option
	 * @param parser
	 *            parser which parsed the Option
	 */
	private void checkMailurl() {
		String mailurlValue = (String) parser.getOptionValue(mailurl);

		if (mailurlValue != null) {
			setMailurlOption(mailurlValue);
		}
	}

	/**
	 * Checks the option --subject, if this is true the intern subjectValue is
	 * set
	 * 
	 * @see ColumbaCmdLineParser#setSubjectOption(String), else the option is
	 *      set to null. You can access this option via
	 * @see ColumbaCmdLineParser#getSubjectOption()
	 * @see CmdLineParser.Option
	 */
	private void checkSubject() {
		String subjectValue = (String) parser.getOptionValue(subject);
		setSubjectOption(subjectValue);
	}

	/**
	 * Checks the option --attachment or -a, if this is true, the internal
	 * attachmentValue is set, else the options is set to null. You can access
	 * this option via getAttachmentOption.
	 * 
	 * @see ColumbaCmdLineParser#getAttachmentOption()
	 * @see CmdLineParser.Option
	 */
	private void checkAttachment() {
		String attachmentValue = (String) parser.getOptionValue(attachment);
		setAttachmentOption(attachmentValue);
	}

	/**
	 * Gives the value of the Body Option.
	 * 
	 * @return the value of the Body Option.
	 */
	public String getBodyOption() {
		return bodyOption;
	}

	/**
	 * Gives the value of the composer Option.
	 * 
	 * @return the value of the composer Option.
	 */
	public boolean getComposerOption() {
		return composerOption;
	}

	/**
	 * Gives the value of the recipient Option.
	 * 
	 * @return the value of the recipient Option.
	 */
	public String getRcptOption() {
		return rcptOption;
	}

	/**
	 * Sets the path to the configuration Columba should use.
	 */
	public void setPathOption(String string) {
		pathOption = string;
	}

	/**
	 * Sets the value for the Body Option.
	 * 
	 * @param string
	 *            the value for the Body Option.
	 */
	public void setBodyOption(String string) {
		bodyOption = string;
	}

	/**
	 * Sets the value for the composer Option.
	 * 
	 * @param b
	 *            the value for the composer Option.
	 */
	public void setComposerOption(boolean b) {
		composerOption = b;
	}

	/**
	 * Sets the value for the recipient Option.
	 * 
	 * @param string
	 *            the value for the recipient Option.
	 */
	public void setRcptOption(String string) {
		rcptOption = string;
	}

	/**
	 * Gives the value for the Body Option.
	 * 
	 * @return the value for the Body Option.
	 */
	public String getBccOption() {
		return bccOption;
	}

	/**
	 * Gives the value for the Cc Option.
	 * 
	 * @return the value for the Cc Option.
	 */
	public String getCcOption() {
		return ccOption;
	}

	/**
	 * Gives the value for the MailUrl Option.
	 * 
	 * @return the value for the MailUrl Option.
	 */
	public String getMailurlOption() {
		return mailurlOption;
	}

	/**
	 * Gives the value for the attachment Option back. If the option is not set,
	 * null will be returned.
	 * 
	 * @return the value for the AttachmentOption or null, if the option is not
	 *         set.
	 */
	public String getAttachmentOption() {
		return attachmentOption;
	}

	/**
	 * Gives the value for the subject Option.
	 * 
	 * @return the value for the subject Option.
	 */
	public String getSubjectOption() {
		return subjectOption;
	}

	/**
	 * Sets the value for the Bcc Option.
	 * 
	 * @param string
	 *            the value for the Bcc Option.
	 */
	public void setBccOption(String string) {
		bccOption = string;
	}

	/**
	 * Sets the value for the Cc Option.
	 * 
	 * @param string
	 *            the value for the Cc Option.
	 */
	public void setCcOption(String string) {
		ccOption = string;
	}

	/**
	 * Sets the value for the Mailurl Option.
	 * 
	 * @param string
	 *            the value for the Mailurl Option.
	 */
	public void setMailurlOption(String string) {
		mailurlOption = string;
	}

	/**
	 * Sets the value for the Subject Option.
	 * 
	 * @param string
	 *            the value for the Subject Option.
	 */
	public void setSubjectOption(String string) {
		subjectOption = string;
	}

	/**
	 * Sets the value for the Attachment Option.
	 * 
	 * @param string
	 *            the value for the Attachment Option.
	 */
	public void setAttachmentOption(String string) {
		attachmentOption = string;
	}
}