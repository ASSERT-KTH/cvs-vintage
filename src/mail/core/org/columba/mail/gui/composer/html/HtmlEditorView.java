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
//All Rights Reserved.undation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.composer.html;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;

import org.columba.core.config.Config;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.gui.composer.html.util.ExtendedHTMLDocument;
import org.columba.mail.gui.composer.html.util.ExtendedHTMLEditorKit;
import org.columba.mail.gui.composer.html.util.HTMLUtilities;


/**
 * View part of controller-view frame work for composing html messages
 * 
 * @author Karl Peder Olesen (karlpeder)
 */
public class HtmlEditorView extends JTextPane
				implements KeyListener {

	/** Reference to the controller of this view */
	private HtmlEditorController controller;
	
	// Members used for html handling:
	
	/** HTML Document */
	private ExtendedHTMLDocument htmlDoc;
	/** Editor kit */
	private ExtendedHTMLEditorKit htmlKit;
	
	///** Helper object for html manipulations */
	//private HTMLUtilities htmlUtil;

	// Actions for text formatting:
	
	/** Action used to toggle bold font on/off */
	private StyledEditorKit.BoldAction actionFontBold;
	/** Action used to toggle italic font on/off */
	private StyledEditorKit.ItalicAction actionFontItalic;
	/** Action used to toggle underline font on/off */
	private StyledEditorKit.UnderlineAction actionFontUnderline;
	/** List of formatting supported (list contains HTML.Tag objects) */
	private List supportedFormats;

	/**
	 * Default constructor. Does initial setup such as creating an
	 * editor kit
	 * @param	ctrl	View controller
	 * @param	doc		HTML document
	 */
	public HtmlEditorView(HtmlEditorController ctrl,
						  ExtendedHTMLDocument doc) {
		super();
		controller = ctrl;
		
		//// initialize helper object
		//htmlUtil = new HTMLUtilities(this);
		
		// set up editor kit and document
		htmlKit = new ExtendedHTMLEditorKit();
		htmlKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));
		if (doc == null)
			doc = (ExtendedHTMLDocument) (htmlKit.createDefaultDocument());
		htmlDoc = doc;

		// attach editor kit and document to the text pane
		setEditorKit(htmlKit);
		setDocument(htmlDoc);

		// further setup of text pane
		Font font = Config.getOptionsConfig().getGuiItem().getTextFont();
		setFont(font);
		addKeyListener(this);
		setPreferredSize(new Dimension(300,200));

		// initialize formatting actions
		actionFontBold        = new StyledEditorKit.BoldAction();
		actionFontItalic      = new StyledEditorKit.ItalicAction();
		actionFontUnderline   = new StyledEditorKit.UnderlineAction();
		
		// initialize supported formats
		supportedFormats = new ArrayList();
		supportedFormats.add(HTML.Tag.P);
		supportedFormats.add(HTML.Tag.H1);
		supportedFormats.add(HTML.Tag.H2);
		supportedFormats.add(HTML.Tag.H3);
		
		// For testing we use another background color
		setBackground(new Color(192, 192, 255));
		
	}

	/** 
	 * Installs editor controller as document listener on the 
	 * underlying document
	 */
	public void installListener(HtmlEditorController ctrl) {
		this.getDocument().addDocumentListener(ctrl);
	}

	/** Returns the underlying HTML document */
	public ExtendedHTMLDocument getHtmlDoc() {
		return htmlDoc;
	}

	/** Sets the charset to use */
	public void setCharset( String charset) {
		setContentType("text/html; charset=\""+charset+"\"");
	}


	// ***************** Methods for formatting and editing ******
	
	/**
	 * Toggles bold font on/off.
	 */
	public void toggleBold() {
		/*
		 * When sending null as the event object, the handler
		 * finds the text component to operate on by searching for
		 * the last text component which had focus. This should
		 * be alright here
		 */
		actionFontBold.actionPerformed(null);
	}
	
	/**
	 * Toggles italic font on/off
	 */
	public void toggleItalic() {
		actionFontItalic.actionPerformed(null); // see note for toggleBold
	}
	
	/** 
	 * Toggles underline font on/off
	 */
	public void toggleUnderline() {
		actionFontUnderline.actionPerformed(null); // see not for toggleBold
	}

	/**
	 * Sets format of selected text
	 * <br>
	 * Formats (tags) supported are defined by the list supportedFormats.
	 *
	 * @param	formatTag	Defines which format to set (H1, P etc.) 
	 */
	public void setFormatOfSelectedText(HTML.Tag formatTag) {

		// Is the requested format supported?
		if (!supportedFormats.contains(formatTag)) {
			ColumbaLogger.log.error("Format not set - <" + formatTag + 
					"> not supported");
			return;
		}

		// get information on selected text
		String selText = getSelectedText();
		int textLength = -1;
		if (selText != null)
			textLength = selText.length();
		if ((selText == null) || (textLength < 1)) {
			// format can only be set if some text is selected
			ColumbaLogger.log.error("Format not set - no text was selected");
			return;
		}

		// set the format
		SimpleAttributeSet sasText = 
				new SimpleAttributeSet(getCharacterAttributes());
		sasText.addAttribute(formatTag, new SimpleAttributeSet());
		int caretOffset = getSelectionStart();
		select(caretOffset, caretOffset + textLength);
		setCharacterAttributes(sasText, false);

		/*
		 * This hack forces the content to refresh properly.
		 * Without it, paragraph formats are applied nested,
		 * e.g. <p><h1><h3>abc</h3></h1></p>, which is not
		 * what we want.
		 */
		this.setText(this.getText());
		
		// reselect text
		select(caretOffset, caretOffset + textLength);

	}

	/** 
	 * Method for inserting a line break (br tag)
	 * 
	 * @author	(originally) Ekit Authors
	 */
	public void insertBreak() {
		try {
			int caretPos = this.getCaretPosition();
			htmlKit.insertHTML(htmlDoc, caretPos, "<br>", 0, 0, HTML.Tag.BR);
			this.setCaretPosition(caretPos + 1);
		} catch (BadLocationException e) {
			ColumbaLogger.log.error("Error inserting br tag", e);
		} catch (IOException e) {
			ColumbaLogger.log.error("Error inserting br tag", e);
		}
	}


	/************************** KeyListener methods ***************************/

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}

	/**
	 * Called on keyboard input. Applies the special handling needed
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 * @author	(originally) Ekit authors
	 */
	public void keyTyped(KeyEvent ke) {
		
		// TODO: Add implementation if lists is to be supported
		
//		Element elem;
//		String selectedText;
//		int pos = this.getCaretPosition();
//		int repos = -1;
//		if(ke.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
//			/*
//			 * Handle delete using "back space"
//			 */
//			try {
//				if(pos > 0)
//				{
//					selectedText = this.getSelectedText();
//					if(selectedText != null) {
//						// some text is selected, delete it
//						htmlUtil.delete();
//						ke.consume(); // *20030901, karlpeder* deletion has been done
//						return;
//					} else {
//						/*
//						 * The section below seems to be just special handling
//						 * of list elements.
//						 */
//						int sOffset = htmlDoc.
//								getParagraphElement(pos).getStartOffset();
//						if(sOffset == this.getSelectionStart())	{
//							boolean content = true;
//							if(htmlUtil.checkParentsTag(HTML.Tag.LI)) {
//								elem = htmlUtil.getListItemParent();
//								content = false;
//								// check if the list element contains anything
//								int so = elem.getStartOffset();
//								int eo = elem.getEndOffset();
//								if(so + 1 < eo)	{
//									char[] temp =
//										this.getText(so, eo - so).toCharArray();
//									for(int i=0; i < temp.length; i++) {
//										if(!Character.isWhitespace(temp[i])) {
//											// none-whitespace found
//											content = true;
//										}
//									}
//								}
//								if(!content) {
//									// Deletion of empty list element
//									Element listElement =
//											elem.getParentElement();
//									htmlUtil.removeTag(elem, true);
//									this.setCaretPosition(sOffset - 1);
//									return;
//								} else {
//									this.setCaretPosition(this.getCaretPosition() - 1);
//									this.moveCaretPosition(this.getCaretPosition() - 2);
//									this.replaceSelection("");
//									return;
//								}
//							} else if(htmlUtil.checkParentsTag(HTML.Tag.TABLE)) {
//								/*
//								 * Handling table element seems not to be
//								 * deleted (just moving caret) for some reason!?
//								 */
//								this.setCaretPosition(this.getCaretPosition() - 1);
//								ke.consume();
//								return;
//							}
//						}
//						// default handling
//						this.replaceSelection("");
//						return;
//					}
//				}
//			}
//			catch (BadLocationException ble) {
//				// TO_DO: Add some proper error handling
//				ble.printStackTrace();
//			}
//			catch (IOException ioe)	{
//				// TO_DO: Add some proper error handling
//				ioe.printStackTrace();
//			}
//
//		} else if(ke.getKeyChar() == KeyEvent.VK_ENTER)	{
//			/*
//			 * Enter
//			 */
//			try	{
//				/*
//				 * The code below provides automatic insertion of 
//				 * list elements when pressing enter inside a list
//				 */
//				if(htmlUtil.checkParentsTag(HTML.Tag.UL) == true |
//						htmlUtil.checkParentsTag(HTML.Tag.OL) == true)	{
//					elem = htmlUtil.getListItemParent();
//					int so = elem.getStartOffset();
//					int eo = elem.getEndOffset();
//					char[] temp = this.getText(so,eo-so).toCharArray();
//					boolean content = false;
//					for(int i=0;i<temp.length;i++) {
//						if(!Character.isWhitespace(temp[i])) {
//							content = true;
//						}
//					}
//					if(content)	{
//						int end = -1;
//						int j = temp.length;
//						do {
//							j--;
//							if(Character.isLetterOrDigit(temp[j]))	{
//								end = j;
//							}
//						} while (end == -1 && j >= 0);
//						j = end;
//						do {
//							j++;
//							if(!Character.isSpaceChar(temp[j])) {
//								repos = j - end -1;
//							}
//						} while (repos == -1 && j < temp.length);
//						if(repos == -1)	{
//							repos = 0;
//						}
//					}
//					if(elem.getStartOffset() == elem.getEndOffset() || !content) {
//						manageListElement(elem);
//					} else {
//						if(this.getCaretPosition() + 1 == elem.getEndOffset()) {
//							insertListStyle(elem);
//							this.setCaretPosition(pos - repos);
//						} else {
//							int caret = this.getCaretPosition();
//							String tempString = this.getText(caret, eo - caret);
//							this.select(caret, eo - 1);
//							this.replaceSelection("");
//							htmlUtil.insertListElement(tempString);
//							Element newLi = htmlUtil.getListItemParent();
//							this.setCaretPosition(newLi.getEndOffset() - 1);
//						}
//					}
//				}
//			}
//			catch (BadLocationException ble) {
//				// TO_DO: Add some proper error handling
//				ble.printStackTrace();
//			}
//			catch (IOException ioe)	{
//				// TO_DO: Add some proper error handling
//				ioe.printStackTrace();
//			}
//		}
	}
	
	/**
	 * Method that handles initial list insertion and deletion
 	 * @author	(originally) Ekit authors
	 */
	protected void manageListElement(Element element) {

		// TODO: Add implementation if lists is to be supported

//		Element h = htmlUtil.getListItemParent();
//		Element listElement = h.getParentElement();
//		if(h != null)
//		{
//			htmlUtil.removeTag(h, true);
//		}
	}

	/**
	 * Handles insertion of a list element, either ordered or unordered
	 * @author	(originally) Ekit authors
	 */	
	protected void insertListStyle(Element element)
			throws BadLocationException,IOException {
		
		// TODO: Add implementation if lists is to be supported

//		if(element.getParentElement().getName() == "ol") {
//			actionListOrdered.actionPerformed(new ActionEvent(new Object(), 0, "newListPoint"));
//		} else {
//			actionListUnordered.actionPerformed(new ActionEvent(new Object(), 0, "newListPoint"));
//		}
	}

}
