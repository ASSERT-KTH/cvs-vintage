/*
GNU Lesser General Public License

HTMLUtilities - Special Utility Functions For Ekit
Copyright (C) 2003 Rafael Cieplinski, modified by Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

/*
 * Copied from Ekit (and modified slightly): 
 * package com.hexidec.ekit.component;
 * by Karl Peder Olesen (karlpeder), 2003-09-07 
 */

package org.columba.mail.gui.composer.html.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

import org.columba.mail.gui.composer.html.HtmlEditorView;

/**
 * Misc. utilities for manipulating html tags etc. - to use 
 * with HtmlEditorView.
 * <br>
 * Originally the class was based on a reference to EkitCore.
 * This has been changed in this new context
 * 
 * @author Karl Peder Olesen, (originally Ekit authors)
 * 
 */
public class HTMLUtilities
{
	/** Reference to the view, that this object is helper for */
	private HtmlEditorView parent;
	/** Table containing all (known) HTML tags */
	private Hashtable tags = new Hashtable();

	/**
	 * Default constructor, which stores reference to the view, that
	 * this object shall be helper for. Also builds a table of 
	 * all (known) HTML tags
	 * 
	 * @param newParent		The view, that this object shall be helper for
	 */
	public HTMLUtilities(HtmlEditorView newParent)
	{
		parent = newParent;
		HTML.Tag[] tagList = HTML.getAllTags();
		for(int i = 0; i < tagList.length; i++)
		{
			tags.put(tagList[i].toString(), tagList[i]);
		}
	}

	/**
	 * Convenience method for refreshing the view (parent)
	 * when other methods of this class has modified it.
	 * 
	 * @author	Karl Peder Olesen
	 */
	protected void refreshParent() {
		JTextPane textPane = (JTextPane) parent;
		textPane.setText(textPane.getText());
		//purgeUndos(); ** ??? **
		textPane.repaint();
	}

	/** 
	  * Diese Methode f�gt durch String-Manipulation in jtpSource
	  * ein neues Listenelement hinzu, content ist dabei der Text
	  * der in dem neuen Element stehen soll
	  */
	public void insertListElement(String content) {
		
		// TODO: Test this code if used (not in use as of 2003-09-07)
		
		int pos = parent.getCaretPosition();
		String source = ((JTextPane) parent).getText();
		/* Replaced with call to getUniString
		boolean hit = false;
		String idString;
		int counter = 0;
		do
		{
			hit = false;
			idString = "diesisteineidzumsuchenimsource" + counter;
			if(source.indexOf(idString) > -1)
			{
				counter++;
				hit = true;
				if(counter > 10000)
				{
					return;
				}
			}
		} while(hit);
		*/
		String[] uniques = getUniString(1);
		String idString = uniques[0];
		Element element = getListItemParent();
		if(element == null) {
			return;
		}
		SimpleAttributeSet sa = new SimpleAttributeSet(element.getAttributes());
		sa.addAttribute("id", idString);
		parent.getHtmlDoc().replaceAttributes(element, sa, HTML.Tag.LI);
		refreshParent();	//parent.refreshOnUpdate();
		source = ((JTextPane) parent).getText();
		StringBuffer newHtmlString = new StringBuffer();
		int[] positions = getPositions(element, source, true, idString);
		newHtmlString.append(source.substring(0, positions[3]));
		newHtmlString.append("<li>");
		newHtmlString.append(content);
		newHtmlString.append("</li>");
		newHtmlString.append(source.substring(positions[3] + 1, source.length()));
		((JTextPane) parent).setText(newHtmlString.toString());
		refreshParent();	//parent.refreshOnUpdate();
		parent.setCaretPosition(pos - 1);
		element = getListItemParent();
		sa = new SimpleAttributeSet(element.getAttributes());
		sa = removeAttributeByKey(sa, "id");
		parent.getHtmlDoc().replaceAttributes(element, sa, HTML.Tag.LI);
	}

	
	/** Diese Methode l�scht durch Stringmanipulation in jtpSource
	  * das �bergebene Element, Alternative f�r removeElement in
	  * ExtendedHTMLDocument, mit closingTag wird angegeben
	  * ob es ein schlie�enden Tag gibt
	  */
	public void removeTag(Element element, boolean closingTag) {
		
		// TODO: Test this code if used (not in use as of 2003-09-07)
		
		if(element == null) {
			return;
		}
		int pos = parent.getCaretPosition();
		HTML.Tag tag = getHTMLTag(element);
		// Versieht den Tag mit einer einmaligen ID
		////String source = parent.getSourcePane().getText();
		String source = ((JTextPane) parent).getText();
		/* Replaced with call to getUniString
		boolean hit = false;
		String idString;
		int counter = 0;
		do
		{
			hit = false;
			idString = "diesisteineidzumsuchenimsource" + counter;
			if(source.indexOf(idString) > -1)
			{
				counter++;
				hit = true;
				if(counter > 10000)
				{
					return;
				}
			}
		} while(hit);
		*/
		String[] uniques = getUniString(1);
		String idString = uniques[0];
		
		SimpleAttributeSet sa = new SimpleAttributeSet(element.getAttributes());
		sa.addAttribute("id", idString);
		parent.getHtmlDoc().replaceAttributes(element, sa, tag);
		refreshParent();	//parent.refreshOnUpdate();
		source = ((JTextPane) parent).getText();
		StringBuffer newHtmlString = new StringBuffer();
		int[] position = getPositions(element, source, closingTag, idString);
		if(position == null) {
			return;
		}
		for(int i = 0; i < position.length; i++) {
			if(position[i] < 0) {
				return;
			}
		}
		int beginStartTag = position[0];
		int endStartTag = position[1];
		if(closingTag) {
			int beginEndTag = position[2];
			int endEndTag = position[3];
			newHtmlString.append(source.substring(0, beginStartTag));
			newHtmlString.append(source.substring(endStartTag, beginEndTag));
			newHtmlString.append(source.substring(endEndTag, source.length()));
		} else {
			newHtmlString.append(source.substring(0, beginStartTag));
			newHtmlString.append(source.substring(endStartTag, source.length()));
		}
		((JTextPane) parent).setText(newHtmlString.toString());
		refreshParent();	//parent.refreshOnUpdate();
	}

	
	/** 
	  * Diese Methode gibt jeweils den Start- und Endoffset des Elements
	  * sowie dem entsprechenden schlie�enden Tag zur�ck
	  */
	private int[] getPositions(Element element,
							   String source, 
							   boolean closingTag, 
							   String idString) {
		
		// TODO: Test this code if used (not in use as of 2003-09-07)
		
		HTML.Tag tag = getHTMLTag(element);
		int[] position = new int[4];
		for(int i = 0; i < position.length; i++) {
			position[i] = -1;
		}
		String searchString = "<" + tag.toString();
		int caret = -1; // aktuelle Position im sourceString
		if((caret = source.indexOf(idString)) != -1) {
			position[0] = source.lastIndexOf("<",caret);
			position[1] = source.indexOf(">",caret)+1;
		}
		if(closingTag) {
			String searchEndTagString = "</" + tag.toString() + ">";
			int hitUp = 0;
			int beginEndTag = -1;
			int endEndTag = -1;
			caret = position[1];
			boolean end = false;
			// Position des 1. Treffer auf den End-Tag wird bestimmt
			beginEndTag = source.indexOf(searchEndTagString, caret);
			endEndTag = beginEndTag + searchEndTagString.length();
			// Schleife l�uft solange, bis keine neuen StartTags mehr gefunden werden
			int interncaret = position[1];
			do {
				int temphitpoint = -1;
				boolean flaghitup = false;
				// Schleife sucht zwischen dem Start- und End-Tag nach neuen Start-Tags
				hitUp = 0;
				do {
					flaghitup = false;
					temphitpoint = source.indexOf(searchString, interncaret);
					if(temphitpoint > 0 && temphitpoint < beginEndTag) {
						hitUp++;
						flaghitup = true;
						interncaret = temphitpoint + searchString.length();
					}
				} while(flaghitup);
				// hitUp enth�lt die Anzahl der neuen Start-Tags
				if(hitUp == 0) {
					end = true;
				} else {
					for(int i = 1; i <= hitUp; i++) {
						caret = endEndTag;
						beginEndTag = source.indexOf(searchEndTagString, caret);
						endEndTag = beginEndTag + searchEndTagString.length();
					}
					end = false;
				}
			} while(!end);
			if(beginEndTag < 0 | endEndTag < 0) {
				return null;
			}
			position[2] = beginEndTag;
			position[3] = endEndTag;
		}
		return position;
	}
	
	/** 
	 * Diese Methode pr�ft ob der �bergebene Tag sich in der 
	 * Hierachie nach oben befindet
	 */
	public boolean checkParentsTag(HTML.Tag tag) {

		// TODO: Test this code if used (not in use as of 2003-09-07)

		Element e = parent.getHtmlDoc().
				getParagraphElement(parent.getCaretPosition());
		String tagString = tag.toString();
		if(e.getName().equalsIgnoreCase(tag.toString())) {
			return true;
		}
		do {
			if((e = e.getParentElement()).getName().
						equalsIgnoreCase(tagString)) {
				return true;
			}
		} while(!(e.getName().equalsIgnoreCase("html")));
		return false;
	}
	
	/**
	 * Diese Methoden geben das erste gefundende dem
	 * �bergebenen tags entsprechende Element zur�ck
	 */
	public Element getListItemParent() {
		
		// TODO: Test this code if used (not in use as of 2003-09-07)
		
		String listItemTag = HTML.Tag.LI.toString();
		Element eleSearch = parent.getHtmlDoc().
				getCharacterElement(parent.getCaretPosition());
		do {
			if(listItemTag.equals(eleSearch.getName()))	{
				return eleSearch;
			}
			eleSearch = eleSearch.getParentElement();
		} while(eleSearch.getName() != HTML.Tag.HTML.toString());
		return null;
	}
	
	/** 
	  * Diese Methoden entfernen Attribute aus dem SimpleAttributeSet,
	  * gem�� den �bergebenen Werten, und
	  * geben das Ergebnis als SimpleAttributeSet zur�ck
	  */
	public SimpleAttributeSet removeAttributeByKey(
								SimpleAttributeSet sourceAS,
								String removeKey) {
		
		// TODO: Test this code if used (not in use as of 2003-09-07)
		
		SimpleAttributeSet temp = new SimpleAttributeSet();
		temp.addAttribute(removeKey, "NULL");
		return removeAttribute(sourceAS, temp);
	}
	
	public SimpleAttributeSet removeAttribute(
								SimpleAttributeSet sourceAS, 
								SimpleAttributeSet removeAS) {
		
		// TODO: Test this code if used (not in use as of 2003-09-07)
		
		try	{
			String[] sourceKeys = new String[sourceAS.getAttributeCount()];
			String[] sourceValues = new String[sourceAS.getAttributeCount()];
			Enumeration sourceEn = sourceAS.getAttributeNames();
			int i = 0;
			while(sourceEn.hasMoreElements()) {
				Object temp = new Object();
				temp = sourceEn.nextElement();
				sourceKeys[i] = (String) temp.toString();
				sourceValues[i] = new String();
				sourceValues[i] = (String) sourceAS.getAttribute(temp).toString();
				i++;
			}
			String[] removeKeys = new String[removeAS.getAttributeCount()];
			String[] removeValues = new String[removeAS.getAttributeCount()];
			Enumeration removeEn = removeAS.getAttributeNames();
			int j = 0;
			while(removeEn.hasMoreElements()) {
				removeKeys[j] = (String) removeEn.nextElement().toString();
				removeValues[j] = (String) removeAS.getAttribute(removeKeys[j]).toString();
				j++;
			}
			SimpleAttributeSet result = new SimpleAttributeSet();
			boolean hit = false;
			for(int countSource = 0;
			    	countSource < sourceKeys.length;
			    	countSource++) {
				hit = false;
				if(sourceKeys[countSource] == "name" |
						sourceKeys[countSource] == "resolver") {
					hit = true;
				} else {
					for(int countRemove = 0;
							countRemove < removeKeys.length; 
							countRemove++) {
						if(removeKeys[countRemove] != "NULL") {
							if(sourceKeys[countSource].toString() == 
									removeKeys[countRemove].toString())	{
								if(removeValues[countRemove] != "NULL")	{
									if(sourceValues[countSource].toString() == 
											removeValues[countRemove].toString()) {
										hit = true;
									}
								} else if(removeValues[countRemove] == "NULL") {
									hit = true;
								}
							}
						} else if(removeKeys[countRemove] == "NULL") {
							if(sourceValues[countSource].toString() == 
									removeValues[countRemove].toString()) {
								hit = true;
							}
						}
					}
				}
				if(!hit) {
					result.addAttribute(
							sourceKeys[countSource].toString(), 
							sourceValues[countSource].toString());
				}
			}
			return result;
		}
		catch (ClassCastException cce) {
			return null;
		}
	}

	
	/** liefert den entsprechenden HTML.Tag zum Element zur�ck */
	public HTML.Tag getHTMLTag(Element e) {
		
		// TODO: Test this code if used (not in use as of 2003-09-07)
		
		if(tags.containsKey(e.getName()))
		{
			return (HTML.Tag)tags.get(e.getName());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Returns an array of strings, which are unique within the 
	 * html document
	 * 
	 * @param	strings		Number of unique strings to return
	 * @return	Array of unique strings
	 */
	private String[] getUniString(int strings) {
		refreshParent();	//parent.refreshOnUpdate();
		String[] result = new String[strings];
		String source = parent.getText(); //parent.getSourcePane().getText();
		for(int i=0; i<strings; i++) {
			int start = -1, end = -1;
			boolean hit = false;
			String idString;
			int counter = 0;
			do {
				hit = false;
				idString = "diesisteineidzumsuchen" + counter + "#" + i;
				if(source.indexOf(idString) > -1) {
					counter++;
					hit = true;
					if(counter > 10000) {
						return null;
					}
				}
			} while(hit);
			result[i] = idString;
		}
		return result;
	}

	/**
	 * Deletes html content corresponding to the selected
	 * text in the text pane
	 */
	public void delete()
			throws BadLocationException,IOException {
		
		// FIXME: Deletion of all text => start- and end p tags are deleted => NOT CORRECT!
		// This means that the next text typed isn't inside p tags - it is 
		// directly within the body tags. This is NOT correct!
		
		JTextPane textPane = (JTextPane) parent;
		ExtendedHTMLDocument htmlDoc = parent.getHtmlDoc();
		// FIXME: getSelectionStart / -End doesn't give correct indices first time
		//        this code is executed for some strange reason (they are offset
		//        by one!!!)
		int selStart = textPane.getSelectionStart();
		int selEnd   = textPane.getSelectionEnd();
		int caretPos = selStart; // So it can be restored after deletion

		String[] posStrings = getUniString(2); // two unique strings
		if(posStrings == null) {
			return;
		}
		htmlDoc.insertString(selStart,posStrings[0],null);
		htmlDoc.insertString(selEnd+posStrings[0].length(),posStrings[1],null);
		refreshParent();	//parent.refreshOnUpdate();
		
		/* we are not working with a source view here, instead
		 * I'm trying to fetch text from the html view directly
		 */
		int start = textPane.getText().indexOf(posStrings[0]);
		int end = textPane.getText().indexOf(posStrings[1]);
		if(start == -1 || end == -1)
		{
			return;
		}
		String htmlString = new String();
		String textPaneText = textPane.getText();
		htmlString += textPaneText.substring(0,start);
		htmlString += textPaneText.substring(start + posStrings[0].length(), end);
		htmlString += textPaneText.substring(end + posStrings[1].length(), textPane.getText().length());
		String source = htmlString;
		end = end - posStrings[0].length();
		htmlString = new String();
		htmlString += source.substring(0,start);
		htmlString += getAllTableTags(source.substring(start, end));
		htmlString += source.substring(end, source.length());
		textPane.setText(htmlString);
		refreshParent();	//parent.refreshOnUpdate();
		textPane.setCaretPosition(caretPos); // Restore previous caret pos.
	}
	
	/**
	 * Extracts all table tags from a given string and returns them
	 */
	private String getAllTableTags(String source)
			throws BadLocationException,IOException {
		
		// TODO: Test this code!!! (if tables are implemented someday)
		
		StringBuffer result = new StringBuffer();
		int caret = -1;
		do {
			caret++;
			int[] tableCarets = new int[6];
			tableCarets[0] = source.indexOf("<table",caret);
			tableCarets[1] = source.indexOf("<tr",caret);
			tableCarets[2] = source.indexOf("<td",caret);
			tableCarets[3] = source.indexOf("</table",caret);
			tableCarets[4] = source.indexOf("</tr",caret);
			tableCarets[5] = source.indexOf("</td",caret);
			java.util.Arrays.sort(tableCarets);
			caret = -1;
			for(int i=0; i<tableCarets.length; i++) {
				if(tableCarets[i] >= 0) {
					caret = tableCarets[i];
					break;
				}
			}
			if(caret != -1) {
				result.append(source.substring(caret,source.indexOf(">",caret)+1));
			}
		} while(caret != -1);
		return result.toString();
	}

}