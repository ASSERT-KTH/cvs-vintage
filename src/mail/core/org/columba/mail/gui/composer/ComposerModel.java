// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.composer;

import java.util.Observable;
import java.util.Vector;

import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.parser.ListParser;
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
public class ComposerModel extends Observable {

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

	ComposerInterface composerInterface;

	public ComposerModel(ComposerInterface ci) {
		this.composerInterface = ci;
		message = new Message();
		toList = new Vector();
		ccList = new Vector();
		bccList = new Vector();
		
		attachments = new Vector();
		
		charsetName = "auto";
	}

	public ComposerModel(ComposerInterface ci, Message message) {
		this.composerInterface = ci;
		this.message = message;

		toList = new Vector();
		ccList = new Vector();
		bccList = new Vector();

		attachments = new Vector();

		charsetName = "auto";
	}

	public void notifyListeners() {
		setChanged();
		notifyObservers();
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
	
	public void setToList( Vector v )
	{
		this.toList = v;
	}
	
	public void setCcList( Vector v )
	{
		this.ccList = v;
	}
	
	public void setBccList( Vector v )
	{
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

		notifyListeners();
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

	public String getPriority() {
		if (message.getHeader().get("X-Priority") == null)
			return "Normal";
		else
			return (String) message.getHeader().get("X-Priority");
	}

	public void setPriority(String s) {
		message.getHeader().set("X-Priority", s);
	}

	public String getHeaderField(String key) {
		return (String) message.getHeader().get(key);
	}

	public void setHeaderField(String key, String value) {
		message.getHeader().set(key, value);
	}

	public Vector getAttachments() {
		return attachments;
	}

	public void setAccountItem(String host, String address) {
		setAccountItem(
			MailConfig.getAccountList().hostGetAccount(
				host,
				address));
	}

	/**
	 * Returns the charsetName.
	 * @return String
	 */
	public String getCharsetName() {
		if( charsetName.equals( "auto" ) )
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

}