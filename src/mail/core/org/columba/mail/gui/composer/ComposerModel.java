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
package org.columba.mail.gui.composer;

import java.util.Vector;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.parser.ListParser;
import org.columba.core.gui.frame.FrameController;
import org.columba.core.gui.frame.SingleViewFrameModel;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimePart;

/**
 * @author frd
 *
 * Model for message composer dialog
 * 
 */
public class ComposerModel extends SingleViewFrameModel {

	Message message;
	AccountItem accountItem;
	String bodytext;
	String charsetName;

	Vector attachments;

	Vector toList;
	Vector ccList;
	Vector bccList;

	boolean signMessage;
	boolean encryptMessage;

	public ComposerModel() {
		this(
			MailConfig.getComposerOptionsConfig().getViewItem().getRoot(),
			new Message());
	}

	public ComposerModel(Message message) {
		this(
			MailConfig.getComposerOptionsConfig().getViewItem().getRoot(),
			message);
	}

	public ComposerModel(XmlElement root) {

		this(root, new Message());
	}

	public ComposerModel(XmlElement root, Message message) {
		super(root);

		this.message = message;

		toList = new Vector();
		ccList = new Vector();
		bccList = new Vector();

		attachments = new Vector();

		charsetName = "auto";
	}

	public void setTo(String s) {
		if (s == null)
			return;

		if (s.length() == 0)
			return;

		int index = s.indexOf(",");
		if (index != -1) {
			String to = s;
			Vector v = ListParser.parseString(to);

			for (int i = 0; i < v.size(); i++) {
				System.out.println("model add:" + v.get(i));
				HeaderItem item = new HeaderItem(HeaderItem.CONTACT);
				item.add("displayname", (String) v.get(i));
				item.add("field", "To");
				getToList().add(item);
			}
		} else {
			HeaderItem item = new HeaderItem(HeaderItem.CONTACT);
			item.add("displayname", s);
			item.add("field", "To");
			getToList().add(item);
		}

	}

	public void setHeaderField(String key, String value) {
		message.getHeader().set(key, value);
	}

	public String getHeaderField(String key) {
		return (String) message.getHeader().get(key);
	}

	public void setToList(Vector v) {
		this.toList = v;
	}

	public void setCcList(Vector v) {
		this.ccList = v;
	}

	public void setBccList(Vector v) {
		this.bccList = v;
	}

	public Vector getToList() {
		return toList;
	}

	public Vector getCcList() {
		return ccList;
	}

	public Vector getBccList() {
		return bccList;
	}

	public void setAccountItem(AccountItem item) {
		this.accountItem = item;
	}

	public AccountItem getAccountItem() {
		if (accountItem == null)
			return MailConfig.getAccountList().get(0);
		else
			return accountItem;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	public String getHeader(String key) {
		return (String) message.getHeader().get(key);
	}

	public void addMimePart(MimePart mp) {
		attachments.add(mp);

		//notifyListeners();

	}

	public void setBodyText(String str) {
		this.bodytext = str;

		//notifyListeners();

	}

	public String getSignature() {
		return "signature";
	}

	public String getBodyText() {
		return bodytext;
	}

	public String getSubject() {
		return (String) message.getHeader().get("Subject");
	}

	public void setSubject(String s) {
		message.getHeader().set("Subject", s);
	}

	public Vector getAttachments() {
		return attachments;
	}

	public void setAccountItem(String host, String address) {
		setAccountItem(
			MailConfig.getAccountList().hostGetAccount(host, address));
	}

	/**
	 * Returns the charsetName.
	 * @return String
	 */
	public String getCharsetName() {
		if (charsetName.equals("auto"))
			charsetName = System.getProperty("file.encoding");

		return charsetName;
	}

	/**
	 * Sets the charsetName.
	 * @param charsetName The charsetName to set
	 */
	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	/**
	 * Returns the signMessage.
	 * @return boolean
	 */
	public boolean isSignMessage() {
		return signMessage;
	}

	/**
	 * Sets the signMessage.
	 * @param signMessage The signMessage to set
	 */
	public void setSignMessage(boolean signMessage) {
		this.signMessage = signMessage;
	}

	/**
	 * Returns the encryptMessage.
	 * @return boolean
	 */
	public boolean isEncryptMessage() {
		return encryptMessage;
	}

	/**
	 * Sets the encryptMessage.
	 * @param encryptMessage The encryptMessage to set
	 */
	public void setEncryptMessage(boolean encryptMessage) {
		this.encryptMessage = encryptMessage;
	}

	public String getPriority() {
		if (message.getHeader().get("X-Priority") == null)
			return "Normal";
		else
			return (String) message.getHeader().get("X-Priority");
	}

	public void setPriority(String s) {
		message.getHeader().set("X-Priority", s);
	}

	public FrameController createInstance(String id) {
		return new ComposerController(id, this);
	}

}