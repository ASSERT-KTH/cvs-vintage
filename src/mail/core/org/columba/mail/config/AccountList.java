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

import java.util.Vector;

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.w3c.dom.Document;

public class AccountList extends DefaultItem {
	private Vector list;

	private AdapterNode rootNode;
	private AdapterNode defaultAccountNode;

	//private Document document;

	private AccountXmlConfig config;

	//private AccountList instance;

	public AccountList(
		AccountXmlConfig config,
		AdapterNode rootNode,
		Document doc) {

		super(doc);

		this.rootNode = rootNode;

		this.config = config;

		list = new Vector();

		if (rootNode != null)
		{
			parse();

		createMissingElements();
		}
	}

	protected void parse() {
		AdapterNode child;

		for (int i = 0; i < rootNode.getChildCount(); i++) {
			child = (AdapterNode) rootNode.getChild(i);

			if (child.getName().equals("account")) {
				AccountItem item = new AccountItem(child, getDocument());
				list.add(item);
			} else if (child.getName().equals("default")) {
				defaultAccountNode = child;
			}
		}
	}

	protected void createMissingElements() {
		if (defaultAccountNode == null)
			defaultAccountNode = addKey(rootNode, "default", "0");
	}

	/*
	public AccountList getInstance()
	{
		if ( instance == null ) instance = new AccountList();
		
		return instance;
	}
	*/

	public AdapterNode getRootNode() {
		return rootNode;
	}

	public AccountItem get(int index) {

		if ((index >= 0) && (index < list.size()))
			return (AccountItem) list.get(index);

		return null;
	}

	public AccountItem uidGet(int uid) {
		for (int i = 0; i < count(); i++) {
			AccountItem item = (AccountItem) get(i);
			int u = item.getUid();

			if (uid == u)
				return item;
		}

		return null;
	}

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

	public AccountItem hostGetAccount(String host, String address) {
		System.out.println("------>host: " + host);

		int result = -1;
		if (address == null)
			return get(0);

		for (int i = 0; i < count(); i++) {
			AccountItem item = (AccountItem) get(i);
			String s = null;

			if (item.isPopAccount()) {
				PopItem pop = item.getPopItem();
				s = pop.getHost();
				System.out.println("string: " + s);
			} else {
				ImapItem imap = item.getImapItem();
				s = imap.getHost();
				System.out.println("string: " + s);
			}

			if (s.equals(host))
				result = i;

		}

		if (result != -1)
			return get(result);

		for (int i = 0; i < count(); i++) {
			AccountItem item = (AccountItem) get(i);
			String s = item.getIdentityItem().getAddress();

			if (address.indexOf(s) != -1)
				result = i;
		}

		return get(result);
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

	// p == true -> pop3, else imap4
	public AccountItem addEmptyAccount(boolean b) {
		AdapterNode node = config.add(b);
		AccountItem item = new AccountItem(node, getDocument());

		add(item);

		return item;
	}

	public void add(AccountItem item) {
		list.add(item);
	}

	public AccountItem remove(int index) {
		AccountItem item = (AccountItem) list.remove(index);
		AdapterNode node = item.getRootNode();
		getRootNode().removeChild(node);

		return item;
	}

	public int count() {
		return list.size();
	}

	/**************************** default account ********************/

	public void setDefaultAccount(int uid) {
		defaultAccountNode.setValue((new Integer(uid)).toString());
	}

	public int getDefaultAccountUid() {
		int uid = Integer.parseInt(defaultAccountNode.getValue());

		return uid;
	}

	public AccountItem getDefaultAccount() {

		return uidGet(getDefaultAccountUid());
	}

}