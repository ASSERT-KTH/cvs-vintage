/*
 * SaveMessageBodyAsCommand.java
 * Created 2003-06-11
 */

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

package org.columba.mail.folder.command;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.config.Config;
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Decoder;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.attachment.AttachmentModel;
import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;
import org.columba.mail.util.MailResourceLoader;

/**
 * This class is used to save a message to file either as 
 * a html file or a text file.
 * 
 * @author Karl Peder Olesen (karlpeder), 20030611
 */
public class SaveMessageBodyAsCommand extends FolderCommand {

	/** Static field representing the system line separator */
	private static final String nl = "\n"; //System.getProperty("line.separator");

	/** The charset to use for decoding messages before save */
	private String charset;
	
	/**
	 * Constructor for SaveMessageBodyAsCommand. Calls super
	 * constructor and saves charset for later use
	 * @param references
	 * @param charset		Charset to use for decoding messages before save
	 */
	public SaveMessageBodyAsCommand(DefaultCommandReference[] references,
								String charset) {
		super(references);
		this.charset = charset;
	}

	/**
	 * Implementation-specific: This method does not perform anything
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updatedGUI() throws Exception {
	}

	/**
	 * This method executes the save action, i.e. it saves the
	 * selected messages to disk as either plain text or as html.<br>
	 * At the momemt no header or attachment information is saved
	 * with the message!
	 * 
	 * @param	worker
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] r = 
				(FolderCommandReference[]) getReferences();
		Object[] uids = r[0].getUids(); // uid for messages to save
		Folder srcFolder = (Folder) r[0].getFolder();

		JFileChooser fileChooser = new JFileChooser();

		// save each message
		for (int j = 0; j < uids.length; j++) {
			Object uid = uids[j];
			ColumbaLogger.log.debug("Saving UID=" + uid);

			// get headers, body part and attachment for message
			ColumbaHeader header = srcFolder.getMessageHeader(uid, worker);
			MimePart bodyPart = getMessageBodyPart(uid, srcFolder, worker);
			AttachmentModel attMod = new AttachmentModel();
			attMod.setCollection(srcFolder.getMimePartTree(uid, worker));
			List attachments = attMod.getDisplayedMimeParts();

			// determine type of body part
			boolean ishtml = false;
			if (bodyPart.getHeader().getContentSubtype().equals("html"))
				ishtml = true;

			// setup filters and filename for file chooser dialog
			ExtensionFileFilter txtFilter  =
					new ExtensionFileFilter("txt", "Text (*.txt)");
		    ExtensionFileFilter htmlFilter =
		    		new ExtensionFileFilter("html", "Html (*.html)");
		    fileChooser.resetChoosableFileFilters();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(txtFilter);
			fileChooser.addChoosableFileFilter(htmlFilter);

			// add check box for incl. of headers
			JCheckBox inclHeaders =
					new JCheckBox(MailResourceLoader.getString(
										"dialog", "saveas",
										"save_all_headers"),
								  getInclAllHeadersOption());
			fileChooser.setAccessory(inclHeaders);
			
			// setup dialog title, active filter and file name
			String defaultName = getValidFilename(
					(String) header.get("Subject"),	false);
			if (ishtml) {
				fileChooser.setDialogTitle(
						MailResourceLoader.getString(
							"dialog", "saveas",
							"save_html_message"));
				fileChooser.setFileFilter(htmlFilter);
				if (defaultName.length() > 0) {
					fileChooser.setSelectedFile(
						new File(defaultName + "." + htmlFilter.getExtension()));
				}
			} else {
				fileChooser.setDialogTitle(
						MailResourceLoader.getString(
							"dialog", "saveas",
							"save_text_message"));
				fileChooser.setFileFilter(txtFilter);
				if (defaultName.length() > 0) {
					fileChooser.setSelectedFile(
						new File(defaultName + "." +  txtFilter.getExtension()));
				}
			}
			
			// show dialog
			int res = fileChooser.showSaveDialog(null);

			if (res == JFileChooser.APPROVE_OPTION)
			{
				File f = fileChooser.getSelectedFile();
				ExtensionFileFilter filter = 
						(ExtensionFileFilter) fileChooser.getFileFilter();
				// Add default extension if no extension is given by the user
				if (ExtensionFileFilter.getFileExtension(f) == null) {
					f = new File(f.getAbsolutePath() + "." + 
								 filter.getExtension());
				}

				int confirm;
				if (f.exists()) {
					// file exists, user needs to confirm overwrite
					confirm = JOptionPane.showConfirmDialog(
								null,
								MailResourceLoader.getString(
									"dialog", "saveas",
									"overwrite_existing_file"),
								MailResourceLoader.getString(
									"dialog", "saveas",
									"file_exists"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				} else {
					confirm = JOptionPane.YES_OPTION;
				}

				if (confirm == JOptionPane.YES_OPTION)
				{
					// store whether all headers should be incl.
					boolean incl = inclHeaders.isSelected();
					storeInclAllHeadersOption(incl);
					ColumbaLogger.log.debug("Incl. all headers: " + incl); 

					// save message
					if (filter.getExtension().equals(htmlFilter.getExtension())) {
						saveMsgBodyHtml(bodyPart, f);
					} else {
						saveMsgAsText(header, bodyPart, attachments, incl, f);
					}
				}				

			}
		} // end of for loop over uids to save 
	}


	/**
	 * Private utility to extract a valid filename from a message
	 * subject or another string.<br>
	 * This means remove the chars: / \ : , \n \t
	 * NB: If the input string is null, an empty string is returned 
	 * @param	subj		Message subject
	 * @param	replSpaces	If true, spaces are replaced by _
	 * @return	A valid filename without the chars mentioned
	 */
	private String getValidFilename(String subj, boolean replSpaces) {
		if (subj == null) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<subj.length(); i++) {
			char c = subj.charAt(i);
			if ((c == '\\') ||
					(c == '/') || (c == ':')  ||
					(c == ',') || (c == '\n') ||
					(c == '\t')) {
				// dismiss char
			} else if ((c == ' ') && (replSpaces)){
				buf.append('_');
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	/**
	 * Gets the value of the option "Incl. all headers"
	 * @return	true if all headers should be included, else false 
	 */
	private boolean getInclAllHeadersOption() {
		boolean defaultValue = false; // default value

		XmlElement options = Config.get("options").getElement("/options");
		if (options == null) {
			return defaultValue;
		}
		
		XmlElement savemsg = options.getElement("/savemsg");
		if (savemsg != null) {
			if (savemsg
					.getAttribute("incl_all_headers", String.valueOf(defaultValue))
					.equals("true")) {
				return true;
			} else {
				return false;
			}
		} else {
			return defaultValue;
		}
	}

	/**
	 * Saves the option "Incl. all headers"
	 * @param	val		Value of the option (true to incl. all headers)
	 */
	private void storeInclAllHeadersOption(boolean val) {
		XmlElement options = Config.get("options").getElement("/options");
		if (options == null) {
			return;
		}
		XmlElement savemsg = options.getElement("/savemsg");
		if (savemsg == null) {
			// create new
			savemsg = new XmlElement("savemsg");
			savemsg.addAttribute("incl_all_headers",
								 String.valueOf(val));
			options.addElement(savemsg);
		} else {
			savemsg.addAttribute("incl_all_headers",
								 String.valueOf(val));
		}
	}

	/**
	 * Private utility to get body part of a message. User preferences
	 * regarding html messages is used to select what to retrieve. If 
	 * the body part retrieved is null, a fake one containing a simple
	 * text is returned
	 * @param 	uid			ID of message
	 * @param	srcFolder	Folder containing the message
	 * @param	worker
	 * @return	body part of message
	 */
	private MimePart getMessageBodyPart(Object uid, 
				Folder srcFolder, Worker worker) 
					throws Exception {
		// Does the user prefer html or plain text?
		XmlElement html =
			MailConfig.getMainFrameOptionsConfig().
				getRoot().getElement("/options/html");
		boolean preferhtml =
			new Boolean(html.getAttribute("prefer")).booleanValue();

		// Get body of message depending on user preferences
		MimePartTree mimePartTree = 
				srcFolder.getMimePartTree(uid, worker);

		MimePart bodyPart = null;
		if (preferhtml)
			bodyPart = mimePartTree.getFirstTextPart("html");
		else
			bodyPart = mimePartTree.getFirstTextPart("plain");

		if (bodyPart == null) {
			bodyPart = new MimePart();
			bodyPart.setBody(new String("<No Message-Text>"));
		} else
			bodyPart =
				srcFolder.getMimePart(uid, bodyPart.getAddress(), worker);
		
		// return the body part found (or constructed)
		return bodyPart;
	}
	
	/**
	 * Private utility to decode the message body with the proper charset
	 * @param	bodyPart	The body of the message
	 * @return	Decoded message body
	 * @author 	Karl Peder Olesen (karlpeder), 20030601
	 */
	private String getDecodedMessageBody(MimePart bodyPart) {
		// First determine which charset to use
		String charsetToUse;
		if (charset.equals("auto")) {
			// get charset from message
			charsetToUse = bodyPart.getHeader().getContentParameter("charset");
		} else {
			// use default charset
			charsetToUse = charset;
		}

		// Decode message according to charset
		Decoder decoder =
			CoderRouter.getDecoder(
				bodyPart.getHeader().contentTransferEncoding);
		String decodedBody = null;
		try {
			// decode using specified charset
			decodedBody = decoder.decode(bodyPart.getBody(), charsetToUse);
		} catch (UnsupportedEncodingException ex) {
			ColumbaLogger.log.info(
				"charset "
					+ charsetToUse
					+ " isn't supported, falling back to default...");
			try {
				// decode using default charset
				decodedBody = decoder.decode(bodyPart.getBody(), null);
			} catch (UnsupportedEncodingException never) {
				// should never happen!?
				never.printStackTrace();
			}
		}

		return decodedBody;
	}

	/**
	 * Method for saving a message body as a html file.
	 * No headers are saved with the message.
	 * @param	bodyPart	Body of message
	 * @param	file		File to output to
	 */
	private void saveMsgBodyHtml(MimePart bodyPart, File file) {
		DocumentParser parser = new DocumentParser();

		// decode message body with respect to charset
		String decodedBody = getDecodedMessageBody(bodyPart);
		
		// determine type of body part
		String mimesubtype = bodyPart.getHeader().getContentSubtype();
		boolean ishtml = false;
		if (mimesubtype.equals("html"))
			ishtml = true;
		
		/*
		 * if it is not a html message body - we have to fake one
		 * by encapsulating the message body in html tags
		 */ 
		String body;
		if (!ishtml) {
			try {
				// substitute special characters like:  <,>,&,\t,\n		
				body = parser.substituteSpecialCharacters(decodedBody);

				// parse for urls / email adr. and substite with HTML-code
				body = parser.substituteURL(body);
				body = parser.substituteEmailAddress(body);

			} catch (Exception e) {
				ColumbaLogger.log.error("Error parsing body", e);
				body = "<em>Error parsing body!!!</em>";
			}

			/* 
			 * substituteSpecialCharacters replaces all spaces with
			 * &nbsp; This results in lines, which are not broken
			 * nicely in a browser. Therefore revert them back to
			 * spaces.
			 * 
			 * TODO: If substitueSpecialCharacters are changed, remove code below
			 */  
			StringBuffer buf = new StringBuffer();
			int pos = 0;
			while (pos < body.length()) {
				char c = body.charAt(pos);
				if (c == '&') {
					if (body.substring(pos).startsWith("&nbsp;")) {
						buf.append(' ');
						pos = pos + 6;
					} else {
						buf.append(c);
						pos++;
					}
				} else {
					buf.append(c);
					pos++;
				}
			}
			body = buf.toString();

			// encapsulate bodytext in html-code
			body = "<html><head>" + nl +
				"<title>E-mail saved by Columba</title>" + nl +  
				"</head><body><p>" + nl + 
				body + nl +
				"</p></body></html>";
		} else {
			// use body as is
			body = decodedBody;
		}

		// save message
		try {
			DiskIO.saveStringInFile(file, body);
			ColumbaLogger.log.info("Html msg saved as " + 
					file.getAbsolutePath());
		} catch (IOException ioe) {
			ColumbaLogger.log.error("Error saving message to file", ioe);
		}
	}

	/**
	 * Method for saving a message in a text file.
	 * No headers are saved with the message.
	 * @param	header			Message headers
	 * @param	bodyPart		Body of message
	 * @param	attachments		List of attachments as MimePart objects
	 * @param	inclAllHeaders	If true all (except Content-Type and 
	 * 							Mime-Version) headers are output. If 
	 * 							false, only a small subset is included
	 * @param	file			File to output to
	 */
	private void saveMsgAsText(ColumbaHeader header, 
							   MimePart bodyPart,
							   List attachments, 
							   boolean inclAllHeaders, 
							   File file) {
		DocumentParser parser = new DocumentParser();
		
		// decode message body with respect to charset
		String decodedBody = getDecodedMessageBody(bodyPart);
		
		String body;
		if (bodyPart.getHeader().getContentSubtype().equals("html")) {
			// strip tags
			body = parser.stripHTMLTags(decodedBody, true);
			body = parser.restoreSpecialCharacters(body);
		} else {
			// use body as is
			body = decodedBody;
		}
		
		// headers
		String[][] headers = getHeadersToSave(header, attachments, 
											  inclAllHeaders);
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<headers[0].length; i++) {
			buf.append(headers[0][i]);
			buf.append(": ");
			buf.append(headers[1][i]);
			buf.append(nl);
		}
		buf.append(nl);
		
		// message composed of headers and body 		
		String msg = buf.toString() + body;

		// save message
		try {
			DiskIO.saveStringInFile(file, msg);
			ColumbaLogger.log.info("Text msg saved as " + 
					file.getAbsolutePath());
		} catch (IOException ioe) {
			ColumbaLogger.log.error("Error saving message to file", ioe);
		}
	}

	/**
	 * Private utility to get headers to save. Headers are returned
	 * in a 2D array, so [0][i] is key[i] and [1][i] is value[i].
	 * @param	header	All message headers
	 * @param	attachments		Attachments, header lines with file
	 * 							names are added
	 * @param	inclAll	true if all headers except Content-Type and
	 * 					Mime-Version should be included
	 * @return	Array of headers to include when saving
	 */
	private String[][] getHeadersToSave(ColumbaHeader header,
										List attachments, 
									    boolean inclAll) {
		List keyList   = new ArrayList();
		List valueList = new ArrayList();
		String from = "";
		String to   = "";
		String date = "";
		String subj = "";

		// loop over all headers 
		Hashtable headerItems = header.getHashtable();
		Enumeration keys = headerItems.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.equals("From")) {
				from = (String) headerItems.get(key);
			} else if (key.equals("To")) {
				to = (String) headerItems.get(key);
			} else if (key.equals("Subject")) {
				subj = (String) headerItems.get(key);
			} else if (key.equals("Date")) {
				// ignore - columba.date is used instead
			} else if (key.startsWith("Content-")) {
				// ignore
			} else if (key.equals("Mime-Version") ||
					   key.equals("MIME-Version")) {
				// ignore
			} else if (key.startsWith("columba")) {
				if (key.equals("columba.date")) {
					DateFormat df = DateFormat.getDateTimeInstance(
							DateFormat.LONG, DateFormat.MEDIUM);
					date = df.format((Date) headerItems.get(key));
				} else {
					// ignore
				}
			} else {
				if (inclAll) {
					// all headers should be included
					keyList.add(key);
					valueList.add((String) headerItems.get(key));
				}
			}
		}

		// add from, to, date, subj so they are the last elements
		keyList.add(MailResourceLoader.getString("header", "from"));
		valueList.add(from);
		keyList.add(MailResourceLoader.getString("header", "to"));
		valueList.add(to);
		keyList.add(MailResourceLoader.getString("header", "date"));
		valueList.add(date);
		keyList.add(MailResourceLoader.getString("header", "subject"));
		valueList.add(subj);

		for (int i = 0; i < attachments.size(); i++) {
			String name = ((MimePart) attachments.get(i))
								.getHeader().getFileName();
			if (name != null) {
				keyList.add(MailResourceLoader.getString(
								"header",
								"attachment"));
				valueList.add(name);
			}
		}

		// create array and return
		String[][] headerArray = new String[2][];
		headerArray[0] = new String[keyList.size()];
		headerArray[1] = new String[keyList.size()];
		for(int i=0; i<keyList.size(); i++) {
			headerArray[0][i] = (String) keyList.get(i);
			headerArray[1][i] = (String) valueList.get(i);
		}
		return headerArray;
	}
}



/** 
 * Represents a file filter selecting only a given type of files.<br>
 * Extension is used to recognize files.<br>
 * Default file type is txt files.
 */
class ExtensionFileFilter extends FileFilter {

	/** extension to accept */
	private String extension = "txt";
	/** description of the file type */
	private String description = "Text files (*.txt)";
	
	/** Constructor setting the extension to accept and a type description*/
	public ExtensionFileFilter(String extension, String description) {
		super();
		this.extension = extension; 
		this.description = description;
	}

	/** Returns true if a given file is of the correct type */
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		// test on extension
		String ext = getFileExtension(f);
		if ((ext != null) && (this.extension.toLowerCase().equals(ext))) {
			return true;
		} else { 
			return false;
		}
	}

	/** 
	 * Static method for extracting the extension of a filename
	 * @return	f	File to get extension for
	 * @return	extension or null if no extension exist 
	 */
	public static String getFileExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if ((i > 0) && (i < (s.length() - 1))) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

	/** Returns the description of this filter / file type */
	public String getDescription() {
		return this.description;
	}
	
	/** Returns the extension used by this filter */
	public String getExtension() {
		return this.extension;
	}
}
