/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
/**
 * @since 3.0
 */
class XMLWriter extends PrintWriter {
	/* constants */
	private static final String XML_VERSION= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$
	private static void appendEscapedChar(StringBuffer buffer, char c) {
		String replacement= getReplacement(c);
		if (replacement != null) {
			buffer.append('&');
			buffer.append(replacement);
			buffer.append(';');
		} else {
			buffer.append(c);
		}
	}
	private static String getEscaped(String s) {
		StringBuffer result= new StringBuffer(s.length() + 10);
		for (int i= 0; i < s.length(); ++i)
			appendEscapedChar(result, s.charAt(i));
		return result.toString();
	}
	private static String getReplacement(char c) {
		// Encode special XML characters into the equivalent character references.
		// These five are defined by default for all XML documents.
		switch (c) {
			case '<' :
				return "lt"; //$NON-NLS-1$
			case '>' :
				return "gt"; //$NON-NLS-1$
			case '"' :
				return "quot"; //$NON-NLS-1$
			case '\'' :
				return "apos"; //$NON-NLS-1$
			case '&' :
				return "amp"; //$NON-NLS-1$
		}
		return null;
	}
	private int tab;
	public XMLWriter(Writer writer) {
		super(writer);
		tab= 0;
		println(XML_VERSION);
	}
	public void endTag(String name, boolean insertTab) {
		tab--;
		printTag('/' + name, null, insertTab, true, false);
	}
	private void printTabulation() {
		for (int i= 0; i < tab; i++)
			super.print('\t');
	}
	public void printTag(String name, HashMap parameters, boolean insertTab, boolean insertNewLine, boolean closeTag) {
		StringBuffer sb= new StringBuffer();
		sb.append("<"); //$NON-NLS-1$
		sb.append(name);
		if (parameters != null) {
			for (Enumeration en = Collections.enumeration(parameters.keySet()); en.hasMoreElements();) {
				sb.append(" "); //$NON-NLS-1$
				String key= (String) en.nextElement();
				sb.append(key);
				sb.append("=\""); //$NON-NLS-1$
				sb.append(getEscaped(String.valueOf(parameters.get(key))));
				sb.append("\""); //$NON-NLS-1$
			}
		}
		if (closeTag) {
			sb.append("/>"); //$NON-NLS-1$
		} else {
			sb.append(">"); //$NON-NLS-1$
		}
		if (insertTab) {
			printTabulation();
		}
		if (insertNewLine) {
			println(sb.toString());
		} else {
			print(sb.toString());
		}
	}
	public void startTag(String name, boolean insertTab) {
		printTag(name, null, insertTab, true, false);
		tab++;
	}
}
