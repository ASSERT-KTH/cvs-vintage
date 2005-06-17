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
package org.columba.mail.gui.message.viewer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.config.Config;
import org.columba.core.gui.focus.FocusManager;
import org.columba.core.gui.htmlviewer.IHTMLViewerPlugin;
import org.columba.core.gui.util.FontProperties;
import org.columba.core.io.DiskIO;
import org.columba.core.io.StreamUtils;
import org.columba.core.io.TempFileStore;
import org.columba.core.main.Main;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginLoadingFailedException;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.HTMLViewerPluginHandler;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.OptionsItem;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.message.MessageController;
import org.columba.mail.gui.message.util.DocumentParser;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.CharsetDecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * IViewer displays message body text.
 * 
 * @author fdietz
 * 
 */
public class TextViewer extends JPanel implements IMimePartViewer, Observer,
		CaretListener {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.mail.gui.message.viewer");

	// parser to transform text to html
	private DocumentParser parser;

	// stylesheet is created dynamically because
	// user configurable fonts are used
	private String css = "";

	// enable/disable smilies configuration
	private XmlElement smilies;

	private boolean enableSmilies;

	// name of font
	private String name;

	// size of font
	private String size;

	// overwrite look and feel font settings
	private boolean overwrite;

	private String body;

	private URL url;

	/**
	 * if true, a html message is shown. Otherwise, plain/text
	 */
	private boolean htmlMessage;

	private MessageController mediator;

	private IHTMLViewerPlugin viewerPlugin;

	public TextViewer(MessageController mediator) {
		super();

		this.mediator = mediator;

		initHTMLViewerPlugin();

		setLayout(new BorderLayout());
		add(viewerPlugin.getView(), BorderLayout.CENTER);

		initConfiguration();

		initStyleSheet();

		// FocusManager.getInstance().registerComponent(new MyFocusOwner());

		// mediator.addMouseListener(this);
	}

	private void initHTMLViewerPlugin() {
		OptionsItem optionsItem = MailConfig.getInstance().getOptionsItem();
		boolean useSystemDefaultBrowser = optionsItem.getBooleanWithDefault(
				OptionsItem.MESSAGEVIEWER,
				OptionsItem.USE_SYSTEM_DEFAULT_BROWSER, false);

		if (useSystemDefaultBrowser) {
			viewerPlugin = createHTMLViewerPluginInstance("JDICHTMLViewerPlugin");
			// in case of an error -> fall-back to Swing's built-in JTextPane
			if ((viewerPlugin == null) || (viewerPlugin.initialized() == false)) {
				LOG.severe("Error while trying to load JDIC based html viewer -> falling back to Swing's JTextPane instead");
				
				viewerPlugin = createHTMLViewerPluginInstance("JavaHTMLViewerPlugin");
			}
		} else {
			viewerPlugin = createHTMLViewerPluginInstance("JavaHTMLViewerPlugin");
		}
	}

	private IHTMLViewerPlugin createHTMLViewerPluginInstance(String pluginId) {
		IHTMLViewerPlugin plugin = null;
		try {

			HTMLViewerPluginHandler handler = (HTMLViewerPluginHandler) PluginManager
					.getInstance().getHandler("org.columba.core.htmlviewer");

			plugin = (IHTMLViewerPlugin) handler.getPlugin(pluginId, null);

			return plugin;
		} catch (PluginHandlerNotFoundException e) {
			LOG.severe("Error while loading viewer plugin: " + e.getMessage());
			if (Main.DEBUG)
				e.printStackTrace();
		} catch (PluginLoadingFailedException e) {
			LOG.severe("Error while loading viewer plugin: " + e.getMessage());
			if (Main.DEBUG)
				e.printStackTrace();
		} catch (Exception e) {
			LOG.severe("Error while loading viewer plugin: " + e.getMessage());
			if (Main.DEBUG)
				e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 */
	private void initConfiguration() {
		XmlElement gui = MailConfig.getInstance().get("options").getElement(
				"/options/gui");
		XmlElement messageviewer = gui.getElement("messageviewer");

		if (messageviewer == null) {
			messageviewer = gui.addSubElement("messageviewer");
		}

		messageviewer.addObserver(this);
		
		smilies = messageviewer.getElement("smilies");

		if (smilies == null) {
			smilies = messageviewer.addSubElement("smilies");
		}

		// register as configuration change listener
		smilies.addObserver(this);

		String enable = smilies.getAttribute("enabled", "true");

		if (enable.equals("true")) {
			enableSmilies = true;
		} else {
			enableSmilies = false;
		}

		XmlElement quote = messageviewer.getElement("quote");

		if (quote == null) {
			quote = messageviewer.addSubElement("quote");
		}

		// register as configuration change listener
		quote.addObserver(this);

		// TODO (@author fdietz): use value in initStyleSheet()
		String enabled = quote.getAttribute("enabled", "true");
		String color = quote.getAttribute("color", "0");

		// register for configuration changes
		Font font = FontProperties.getTextFont();
		name = font.getName();
		size = new Integer(font.getSize()).toString();

		XmlElement options = Config.getInstance().get("options").getElement(
				"/options");
		XmlElement gui1 = options.getElement("gui");
		XmlElement fonts = gui1.getElement("fonts");

		if (fonts == null) {
			fonts = gui1.addSubElement("fonts");
		}

		// register interest on configuratin changes
		fonts.addObserver(this);
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IMimePartViewer#view(org.columba.mail.folder.IMailbox,
	 *      java.lang.Object, java.lang.Integer[],
	 *      org.columba.mail.gui.frame.MailFrameMediator)
	 */
	public void view(IMailbox folder, Object uid, Integer[] address,
			MailFrameMediator mediator) throws Exception {

		MimePart bodyPart = null;
		InputStream bodyStream;

		MimeTree mimePartTree = folder.getMimePartTree(uid);

		bodyPart = mimePartTree.getFromAddress(address);

		if (bodyPart == null) {
			bodyStream = new ByteArrayInputStream("<No Message-Text>"
					.getBytes());
		} else {
			// Shall we use the HTML-IViewer?
			htmlMessage = bodyPart.getHeader().getMimeType().getSubtype()
					.equals("html");

			bodyStream = folder.getMimePartBodyStream(uid, bodyPart
					.getAddress());
		}

		// Which Charset shall we use ?
		Charset charset = ((CharsetOwnerInterface) mediator).getCharset();
		charset = extractCharset(charset, bodyPart);

		bodyStream = decodeBodyStream(charset, bodyPart, bodyStream);

		// Read Stream in String
		StringBuffer text = StreamUtils.readCharacterStream(bodyStream);

		// if HTML stripping is enabled
		if (isHTMLStrippingEnabled()) {
			// strip HTML message -> remove all HTML tags
			text = new StringBuffer(HtmlParser.stripHtmlTags(text.toString(),
					true));

			htmlMessage = false;
		}

		if (htmlMessage) {

			// this is a HTML message

			// create temporary file
			File inputFile = TempFileStore.createTempFileWithSuffix("html");
			// save bodytext to file
			DiskIO.saveStringInFile(inputFile, text.toString());
			// get URL of file
			url = inputFile.toURL();

			// setPage(url);

		} else {
			// this is a text/plain message

			body = transformTextToHTML(text.toString());

			// setText(body);

			LOG.finest("validated bodytext:\n" + body);

		}

		bodyStream.close();

	}

	private boolean isHTMLStrippingEnabled() {
		XmlElement html = MailConfig.getInstance().getMainFrameOptionsConfig()
				.getRoot().getElement("/options/html");

		return Boolean.valueOf(html.getAttribute("disable")).booleanValue();
	}

	/**
	 * @param bodyPart
	 * @param bodyStream
	 * @return
	 */
	private InputStream decodeBodyStream(Charset charset, MimePart bodyPart,
			InputStream bodyStream) throws Exception {

		// default encoding is plain
		int encoding = MimeHeader.PLAIN;

		if (bodyPart != null) {
			encoding = bodyPart.getHeader().getContentTransferEncoding();
		}

		switch (encoding) {
		case MimeHeader.QUOTED_PRINTABLE: {
			bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

			break;
		}

		case MimeHeader.BASE64: {
			bodyStream = new Base64DecoderInputStream(bodyStream);

			break;
		}
		}

		bodyStream = new CharsetDecoderInputStream(bodyStream, charset);

		return bodyStream;
	}

	/**
	 * @param mediator
	 * @param bodyPart
	 * @return
	 */
	private Charset extractCharset(Charset charset, MimePart bodyPart) {

		// no charset specified -> automatic mode
		// -> try to determine charset based on content parameter
		if (charset == null) {
			String charsetName = null;

			if (bodyPart != null) {
				charsetName = bodyPart.getHeader().getContentParameter(
						"charset");
			}

			if (charsetName == null) {
				// There is no charset info -> the default system charset is
				// used
				charsetName = System.getProperty("file.encoding");
				charset = Charset.forName(charsetName);
			} else {
				try {
					charset = Charset.forName(charsetName);
				} catch (UnsupportedCharsetException e) {
					charsetName = System.getProperty("file.encoding");
					charset = Charset.forName(charsetName);
				} catch (IllegalCharsetNameException e) {
					charsetName = System.getProperty("file.encoding");
					charset = Charset.forName(charsetName);
				}
			}

			// ((CharsetOwnerInterface) mediator).setCharset(charset);

		}
		return charset;
	}

	/**
	 * 
	 * read text-properties from configuration and create a stylesheet for the
	 * html-document
	 * 
	 */
	private void initStyleSheet() {
		// read configuration from options.xml file
		// create css-stylesheet string
		// set font of html-element <P>
		css = "<style type=\"text/css\"><!-- .bodytext {font-family:\"" + name
				+ "\"; font-size:\"" + size + "pt; \"}"
				+ ".quoting {color:#949494;}; --></style>";
	}

	/**
	 * @param bodyText
	 * @throws Exception
	 */
	private String transformTextToHTML(String bodyText) throws Exception {
		String body = null;

		// substitute special characters like:
		// <,>,&,\t,\n
		body = HtmlParser.substituteSpecialCharacters(bodyText);

		// parse for urls and substite with HTML-code
		body = HtmlParser.substituteURL(body);

		// parse for email addresses and substite with HTML-code
		body = HtmlParser.substituteEmailAddress(body);

		// parse for quotings and color the darkgray
		body = parser.markQuotings(body);

		// add smilies
		if (enableSmilies == true) {
			body = parser.addSmilies(body);
		}

		// encapsulate bodytext in html-code
		body = transformToHTML(new StringBuffer(body));

		return body;
	}

	/*
	 * 
	 * encapsulate bodytext in HTML code
	 * 
	 */
	protected String transformToHTML(StringBuffer buf) {
		// prepend
		buf.insert(0, "<HTML><HEAD>" + css
				+ "</HEAD><BODY class=\"bodytext\"><P>");

		// append
		buf.append("</P></BODY></HTML>");

		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.gui.config.general.MailOptionsDialog
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1) {
		Font font = FontProperties.getTextFont();
		name = font.getName();
		size = new Integer(font.getSize()).toString();

		initStyleSheet();
		
		initHTMLViewerPlugin();
	}

	public String getSelectedText() {
		return viewerPlugin.getSelectedText();
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#getView()
	 */
	public JComponent getView() {
		return this;
	}

	/**
	 * @see org.columba.mail.gui.message.viewer.IViewer#updateGUI()
	 */
	public void updateGUI() throws Exception {
		// clear text viewer
		viewerPlugin.view("");

		if (!htmlMessage) {
			// display bodytext
			viewerPlugin.view(body);
		} else {
			// this call has to happen in the awt-event dispatcher thread
			viewerPlugin.view(url);
		}

	}

	/**
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	public void caretUpdate(CaretEvent arg0) {
		FocusManager.getInstance().updateActions();
	}

	/** ***************** FocusOwner interface ********************** */

	// class MyFocusOwner implements FocusOwner {
	// /**
	// * @see javax.swing.text.JTextComponent#copy()
	// */
	// public void copy() {
	// int start = getSelectionStart();
	// int stop = getSelectionEnd();
	//
	// StringWriter htmlSelection = new StringWriter();
	//
	// try {
	// htmlEditorKit.write(htmlSelection, getDocument(), start, stop
	// - start);
	//
	// Clipboard clipboard = getToolkit().getSystemClipboard();
	//
	// // Conversion of html text to plain
	// //TODO (@author karlpeder): make a DataFlavor that can handle
	// // HTML
	// // text
	// StringSelection selection = new StringSelection(HtmlParser
	// .htmlToText(htmlSelection.toString(), true));
	// clipboard.setContents(selection, selection);
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (BadLocationException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#cut()
	// */
	// public void cut() {
	// // not supported
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#delete()
	// */
	// public void delete() {
	// // not supported
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#getComponent()
	// */
	// public JComponent getComponent() {
	// return TextViewer.this;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#isCopyActionEnabled()
	// */
	// public boolean isCopyActionEnabled() {
	//
	// if (getSelectedText() == null) {
	// return false;
	// }
	//
	// if (getSelectedText().length() > 0) {
	// return true;
	// }
	//
	// return false;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#isCutActionEnabled()
	// */
	// public boolean isCutActionEnabled() {
	// // action not support
	// return false;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#isDeleteActionEnabled()
	// */
	// public boolean isDeleteActionEnabled() {
	// // action not supported
	// return false;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#isPasteActionEnabled()
	// */
	// public boolean isPasteActionEnabled() {
	// // action not supported
	// return false;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#isRedoActionEnabled()
	// */
	// public boolean isRedoActionEnabled() {
	// // action not supported
	// return false;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#isSelectAllActionEnabled()
	// */
	// public boolean isSelectAllActionEnabled() {
	// return true;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#isUndoActionEnabled()
	// */
	// public boolean isUndoActionEnabled() {
	// // action not supported
	// return false;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#paste()
	// */
	// public void paste() {
	// // action not supported
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#redo()
	// */
	// public void redo() {
	// // action not supported
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#selectAll()
	// */
	// public void selectAll() {
	//
	// selectAll();
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.columba.core.gui.focus.FocusOwner#undo()
	// */
	// public void undo() {
	//
	// }
	// }
}