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

package org.columba.mail.config;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

public class AccountList extends DefaultItem {
	
	int nextUid;
	AccountItem defaultAccount;

	public AccountList(XmlElement root) {
		super(root);
		
		AccountItem item;
		
		nextUid = -1;
		
		int uid;
		
		for( int i=0; i<count(); i++ ) {
			item = get(i);
			uid = item.getInteger("uid");
			if( uid > nextUid) nextUid = uid;			
		}
		nextUid++;
	}

	public AccountItem get(int index) {

		XmlElement e = getChildElement(index);
		//XmlElement.printNode(e,"");

		/*
		if ((index >= 0) && (index < list.size()))
			return (AccountItem) list.get(index);
		
		return null;
		*/

		return new AccountItem(e);
	}

	public AccountItem uidGet(int uid) {
		XmlElement e;

		for (int i = 0; i < count(); i++) {
			e = getChildElement(i);

			int u = Integer.parseInt(e.getAttribute("uid"));

			if (uid == u)
				return new AccountItem(e);
		}

		return null;
	}
/*
	public PGPItem getPGPItem(String to) {

		System.out.println("------>to: " + to);

		int result = -1;

		for (int i = 0; i < count(); i++) {
			AccountItem item = (AccountItem) get(i);
			PGPItem pgpItem = item.getPGPItem();
			String id = pgpItem.getId();
			to = to.toLowerCase();
			id = id.toLowerCase();

			if (to.indexOf(id) != -1)
				return pgpItem;
			else if (id.indexOf(to) != -1)
				return pgpItem;
		}

		return null;
	}
*/
	public AccountItem hostGetAccount(String host, String address) {

		System.out.println("------>host: " + host);
		XmlElement account, server, identity;
		int result = -1;
		if (address == null)
			return get(0);

		for (int i = 0; i < count(); i++) {
			account = getChildElement(i);
			
			server = account.getElement("popserver");
			if (server == null) {
				server = account.getElement("imapserver");
			}
			
			if( server.getAttribute("host").equals(host))
				return new AccountItem(account);
		}

		for (int i = 0; i < count(); i++) {
			account = getChildElement(i);

			identity = account.getElement("identity");
			if (identity.getAttribute("address").indexOf(address) != -1)
				return new AccountItem(account);
		}

		return null;

	}

	/*
	public String hostGetAccountName(String host) {
		for (int i = 0; i < count(); i++) {
			AccountItem item = (AccountItem) get(i);
			String s = null;
			if (item.isPopAccount()) {
				PopItem pop = item.getPopItem();
				s = pop.getHost();
			} else {
				ImapItem imap = item.getImapItem();
				s = imap.getHost();
			}
	
			if (s.equals(host))
				return item.getName();
		}
	
		return null;
	}
	*/

	public AccountItem addEmptyAccount(String type) {
		AccountTemplateXmlConfig template = MailConfig.getAccountTemplateConfig();
		
		XmlElement emptyAccount = template.getRoot().getElement("/template/"+type+"/account");
		
		if( emptyAccount != null ) {
			AccountItem newAccount = new AccountItem( (XmlElement) emptyAccount.clone() );
			newAccount.set("uid", getNextUid() );
			add(newAccount);
			return newAccount;			
		}

		return null;
	}

	public void add(AccountItem item) {
		getRoot().addSubElement(item.getRoot());
		if( item.getInteger("uid") >= nextUid ) {
			nextUid = item.getInteger("uid") + 1;
		}
		
		if( count() == 1 ) {
			setDefaultAccount(item.getInteger("uid"));	
		}
	}

	public AccountItem remove(int index) {
		return new AccountItem( getRoot().removeElement(index) );
	}

	public int count() {
		return getRoot().count();
	}

	protected int getNextUid() {
		return nextUid++;
	}

	/**************************** default account ********************/

	public void setDefaultAccount(int uid) {
		set("default", uid);
		defaultAccount = null;
	}

	public int getDefaultAccountUid() {
		return getInteger("default");
	}

	public AccountItem getDefaultAccount() {
		if(defaultAccount == null) {
			defaultAccount = uidGet(getDefaultAccountUid()); 	
		}
		
		return defaultAccount;
	}

}