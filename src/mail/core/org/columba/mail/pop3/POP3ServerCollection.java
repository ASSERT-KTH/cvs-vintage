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

package org.columba.mail.pop3;

import java.util.Vector;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.AccountList;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.PopItem;

public class POP3ServerCollection //implements ActionListener
{
	private Vector serverList;
	private POP3Server popServer;

	public POP3ServerCollection() {
		serverList = new Vector();

		AccountList list = MailConfig.getAccountList();

		for (int i = 0; i < list.count(); i++) {
			AccountItem accountItem = list.get(i);
			if (accountItem.isPopAccount()) {

				add(accountItem);
			}
		}

	}

	public POP3ServerController[] getList() {
		POP3ServerController[] list = new POP3ServerController[count()];

		serverList.copyInto(list);

		return list;
	}

	public void add(AccountItem item) {
		POP3ServerController server = new POP3ServerController(item);

		serverList.add(server);
	}

	public POP3ServerController uidGet(int uid) {
		int index = getIndex(uid);

		if (index != -1)
			return get(index);
		else
			return null;
	}

	public POP3ServerController get(int index) {
		return (POP3ServerController) serverList.get(index);
	}

	public int count() {
		return serverList.size();
	}

	public void removePopServer(int uid) {
		int index = getIndex(uid);
		if (index == -1)
			System.out.println("could not find popserver");
		else
			serverList.remove(index);
	}

	public int getIndex(int uid) {
		POP3ServerController c;
		int number;
		PopItem item;
		for (int i = 0; i < count(); i++) {
			c = get(i);
			number = c.getUid();

			if (number == uid) {
				return i;
			}
		}
		return -1;
	}

	public void saveAll() {
		POP3ServerController c;
		for (int i = 0; i < count(); i++) {
			c = get(i);
			try {
				c.getServer().save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public POP3Server getSelected() {
		return popServer;
	}

}
