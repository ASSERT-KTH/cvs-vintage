/*
 * SaveMessageAsCommand.java
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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.io.DiskIO;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;
import org.columba.mail.coder.CoderRouter;
import org.columba.mail.coder.Decoder;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;
import org.columba.mail.util.MailResourceLoader;

/**
 * This class is used to save a message to file either as 
 * a html file or a text file.
 * 
 * @author Karl Peder Olesen (karlpeder), 20030611
 */
public class SaveMessageAsCommand extends FolderCommand {

	/** Static field representing the system line separator */
	private static final String nl = "\n"; //System.getProperty("line.separator");

	/** The charset to use for decoding messages before save */
	private String charset;
	
	/**
	 * Constructor for SaveMessageAsCommand. Calls super
	 * constructor and saves charset for later use
	 * @param references
	 * @param charset		Charset to use for decoding messages before save
	 */
	public SaveMessageAsCommand(DefaultCommandReference[] references,
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

			// get body part of message
			MimePart bodyPart = getMessageBodyPart(uid, srcFolder, worker);
			
			// determine type of body part
			String mimesubtype = bodyPart.getHeader().getContentSubtype();
			boolean ishtml = false;
			if (mimesubtype.equals("html"))
				ishtml = true;
			ColumbaLogger.log.debug("Body part is html: " + ishtml);

			// setup filters for file chooser dialog
			ExtensionFileFilter txtFilter  = new ExtensionFileFilter(
									"txt",  "Text (*.txt)");
		    ExtensionFileFilter htmlFilter = new ExtensionFileFilter(
		    						"html", "Html (*.html)");
		    fileChooser.resetChoosableFileFilters();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(txtFilter);
			fileChooser.addChoosableFileFilter(htmlFilter);

			// setup dialog title
			if (ishtml) {
				fileChooser.setDialogTitle(
						MailResourceLoader.getString(
							"dialog", "general",
							"save_html_message") + 
						" " + (j+1) + ":" + uids.length + ")");
				fileChooser.setFileFilter(htmlFilter);
			} else {
				fileChooser.setDialogTitle(
						MailResourceLoader.getString(
							"dialog", "general",
							"save_text_message") +
						" " + (j+1) + ":" + uids.length + ")");
				fileChooser.setFileFilter(txtFilter);
			}
			
			// show dialog
			int res = fileChooser.showSaveDialog(null);

			if (res == JFileChooser.APPROVE_OPTION)
			{
				File f = fileChooser.getSelectedFile();
				ExtensionFileFilter filter = 
						(ExtensionFileFilter) fileChooser.getFileFilter();
				// Add default extension if no extension is given by the user
				String filename = f.getAbsolutePath();
				String ext = ExtensionFileFilter.getFileExtension(f);
				if (ext == null) {
					filename = filename + "." + filter.getExtension();	
				}
				f = new File(filename);

				int confirm;
				if (f.exists()) {
					// file exists, user needs to confirm overwrite
					confirm = JOptionPane.showConfirmDialog(
								null,
								"Do you want to overwrite existing file?",
								"File exists!",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				} else {
					confirm = JOptionPane.YES_OPTION;
				}

				if (confirm == JOptionPane.YES_OPTION)
				{
					// save message
					if (filter.getExtension().equals(htmlFilter.getExtension())) {
						saveMsgBodyHtml(bodyPart, f);
					} else {
						saveMsgBodyText(bodyPart, f);
					}
				}				

			}
		} // end of for loop over uids to save 

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
		Message message = new Message();
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
	 * Method for saving a message body as a text file.
	 * No headers are saved with the message.
	 * @param	bodyPart	Body of message
	 * @param	file		File to output to
	 */
	private void saveMsgBodyText(MimePart bodyPart, File file) {
		DocumentParser parser = new DocumentParser();

		// decode message body with respect to charset
		String decodedBody = getDecodedMessageBody(bodyPart);
		
		// determine type of body part
		String mimesubtype = bodyPart.getHeader().getContentSubtype();
		boolean ishtml = false;
		if (mimesubtype.equals("html"))
			ishtml = true;
		
		// if it is not a plain text body - we have to strip tags
		String body;
		if (ishtml) {
			// strip tags
			body = stripHtmlTags(decodedBody);
		} else {
			// use body as is
			body = decodedBody;
		}

		// save message
		try {
			DiskIO.saveStringInFile(file, body);
			ColumbaLogger.log.info("Text msg saved as " + 
					file.getAbsolutePath());
		} catch (IOException ioe) {
			ColumbaLogger.log.error("Error saving message to file", ioe);
		}
	}

	/**
	 * Strips html tags. The method used is very simple:
	 * Everything between tag-start (&lt) and tag-end (&gt) is removed.
	 * br tags are replaced by newline and p tags with double newline.
	 * Special entities are replaced by their "real" counterparts,
	 * e.g. nbsp with space.
	 * @param	input	Text with html tags
	 * @return	Text without html tags
	 */
	private String stripHtmlTags(String input) {
		StringBuffer buf = new StringBuffer();

		boolean inTag = false;
		StringBuffer tag = new StringBuffer();
		int pos = 0;
		
		// loop over text
		while (pos < input.length()) {
			char c = input.charAt(pos);
			if (c == '<') {
				inTag = true;
				pos++;
			} else if (c == '>') {
				inTag = false;
				String tagName = tag.toString().trim().toLowerCase();
				if (tagName.startsWith("br")) {
					buf.append(nl);			// replace with newline
				} else if (tagName.startsWith("/p")) {
					buf.append(nl + nl);	// replace with 2x newline
				}
				tag = new StringBuffer();
				pos++;
			} else {
				if (inTag) {
					tag.append(c);
					pos++;
				} else {
					if (c == '&') {
						// special entity - replace with "normal" char
						if 		  (input.substring(pos).startsWith("&lt;")) {
							tag.append('<');
							pos = pos + 4;
						} else if (input.substring(pos).startsWith("&gt;")) {
							tag.append('>');
							pos = pos + 4;
						} else if (input.substring(pos).startsWith("&amp;")) {
							tag.append('&');
							pos = pos + 5;
						} else if (input.substring(pos).startsWith("&nbsp;")) {
							tag.append(' ');
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
			}
		}
		
		// return text stripped for html tags
		return buf.toString();
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
