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
package org.columba.mail.config;

import java.io.File;
import java.util.Vector;

import org.columba.addressbook.config.AdapterNode;
import org.columba.core.config.DefaultXmlConfig;

public class AccountXmlConfig extends DefaultXmlConfig {
	//private File file;

	private AccountList list;
	
	public AccountXmlConfig(File file) {
		super(file);
	}

	// add new account
	// if b==true add pop3 account
	// else add imap4 account

	/*
	public AdapterNode add(boolean b) {
		Element parent = createElementNode("account");

		Element child = createTextElementNode("name", "New Account");
		addElement(parent, child);

		child = createTextElementNode("uid", createUid());
		addElement(parent, child);

		// create identity
		child = createElementNode("identity");

		Element subChild = createTextElementNode("name", "");
		addElement(child, subChild);
		subChild = createTextElementNode("organisation", "");
		addElement(child, subChild);
		subChild = createTextElementNode("address", "");
		addElement(child, subChild);
		subChild = createTextElementNode("replyaddress", "");
		addElement(child, subChild);
		CDATASection cdataChild;
		subChild = createElementNode("signature");
		cdataChild = createCDATAElementNode("signature");
		addCDATASection(subChild, cdataChild);
		addElement(child, subChild);
		subChild = createTextElementNode("attachsignature", "false");
		addElement(child, subChild);

		addElement(parent, child);

		if (b) {

			// create popserver
			child = createElementNode("popserver");

			subChild = createElementNode("user");
			cdataChild = createCDATAElementNode("");
			addCDATASection(subChild, cdataChild);
			addElement(child, subChild);

			subChild = createElementNode("password");
			cdataChild = createCDATAElementNode("");
			addCDATASection(subChild, cdataChild);
			addElement(child, subChild);

			subChild = createTextElementNode("host", "");
			addElement(child, subChild);
			subChild = createTextElementNode("port", "110");
			addElement(child, subChild);
			subChild = createTextElementNode("foldername", "");
			addElement(child, subChild);
			subChild = createTextElementNode("leave", "true");
			addElement(child, subChild);
			subChild = createTextElementNode("savepassword", "false");
			addElement(child, subChild);
			subChild = createTextElementNode("exclude", "false");
			addElement(child, subChild);
			subChild = createTextElementNode("uid", new String("101"));
			addElement(child, subChild);
			subChild = createTextElementNode("mailcheck", "false");
			addElement(child, subChild);
			subChild = createTextElementNode("interval", "60");
			addElement(child, subChild);
			subChild = createTextElementNode("downloadlimit", "50");
			addElement(child, subChild);
			subChild = createTextElementNode("limit", "false");
			addElement(child, subChild);

			addElement(parent, child);
		} else {
			// create imapserver
			child = createElementNode("imapserver");

			subChild = createElementNode("user");
			cdataChild = createCDATAElementNode("");
			addCDATASection(subChild, cdataChild);
			addElement(child, subChild);

			subChild = createElementNode("password");
			cdataChild = createCDATAElementNode("");
			addCDATASection(subChild, cdataChild);
			addElement(child, subChild);

			subChild = createTextElementNode("host", "");
			addElement(child, subChild);
			subChild = createTextElementNode("port", "143");
			addElement(child, subChild);
			subChild = createTextElementNode("savepassword", "false");
			addElement(child, subChild);

			addElement(parent, child);
		}

		// create outgoingserver
		child = createElementNode("smtpserver");

		subChild = createElementNode("user");
		cdataChild = createCDATAElementNode("");
		addCDATASection(subChild, cdataChild);
		addElement(child, subChild);
		subChild = createElementNode("password");
		cdataChild = createCDATAElementNode("");
		addCDATASection(subChild, cdataChild);
		addElement(child, subChild);
		subChild = createTextElementNode("savepassword", "false");
		addElement(child, subChild);
		subChild = createTextElementNode("host", "");
		addElement(child, subChild);
		subChild = createTextElementNode("port", "25");
		addElement(child, subChild);
		subChild = createTextElementNode("esmtp", "");
		addElement(child, subChild);
		
		subChild = createTextElementNode("bccyourself", "");
		addElement(child, subChild);
		subChild = createTextElementNode("bccanother", "");
		addElement(child, subChild);

		addElement(parent, child);

		// create pgp section
		child = createElementNode("pgp");

		subChild = createTextElementNode("id", "user-id");
		addElement(child, subChild);
		subChild = createTextElementNode("type", "0");
		addElement(child, subChild);
		subChild = createTextElementNode("path", "/usr/bin/gpg");
		addElement(child, subChild);
		subChild = createTextElementNode("alwayssign", "false");
		addElement(child, subChild);
		subChild = createTextElementNode("alwaysencrypt", "false");
		addElement(child, subChild);

		addElement(parent, child);

		// create special folders section
		child = createElementNode("specialfolders");

		subChild = createTextElementNode("trash", "105");
		addElement(child, subChild);
		subChild = createTextElementNode("drafts", "102");
		addElement(child, subChild);
		subChild = createTextElementNode("templates", "107");
		addElement(child, subChild);
		subChild = createTextElementNode("sent", "104");
		addElement(child, subChild);

		addElement(parent, child);

		AdapterNode node = new AdapterNode(getDocument());

		AdapterNode rootNode = node.getChild(0);

		//System.out.println("account rootNode: "+ rootNode.getName() );

		rootNode.domNode.appendChild(parent);

		return new AdapterNode(parent);
	}
	*/
	
	public AccountList getAccountList() {

		if (list == null) {
			/*
			AdapterNode node = new AdapterNode(getDocument());

			AdapterNode parent = node.getChild(0);
			*/
			
			list = new AccountList(getRoot().getElement("/accountlist"));
		}

		return list;
	}

	public int count() {
		return -1;
		/*
		AdapterNode node = new AdapterNode(getDocument());
		AdapterNode parent = node.getChild(0);

		int count = parent.getChildCount();

		return count;
		*/
	}

	// create uid list from all accounts
	protected void getUids(Vector v, AdapterNode parent) {
		
		int childCount = parent.getChildCount();

		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				AdapterNode child = parent.getChild(i);
				//System.out.println("name: "+ child.getName() );

				if (child.getName().equals("account")) {
					AdapterNode uidNode = child.getChild("uid");

					Integer j = new Integer(uidNode.getValue());

					v.add(j);
				}

			}
		}
	}

	// find a free uid for a new account
	protected String createUid() {
		/*
		Vector v = new Vector();

		AdapterNode rootNode = new AdapterNode(getDocument());
		AdapterNode accountNode = rootNode.getChild(0);

		getUids(v, accountNode);

		int result = -1;
		boolean hit;
		boolean exit = false;

		while (exit == false) {
			hit = false;
			result++;
			for (int i = 0; i < v.size(); i++) {
				Integer j = (Integer) v.get(i);

				if (j.intValue() == result) {
					hit = true;
				}
			}
			if (hit == false)
				exit = true;
		}

		Integer newUid = new Integer(result);

		return newUid.toString();
		*/
		
		return "";
	}

}
