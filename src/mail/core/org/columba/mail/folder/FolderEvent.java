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
package org.columba.mail.folder;

import java.util.EventObject;

import org.columba.mail.gui.table.util.MessageNode;
import org.columba.mail.message.Message;

public class FolderEvent extends EventObject
{
	public final static int UPDATE = 0;
	public final static int ADD = 1;
	public final static int REMOVE = 2;
	public final static int MARK = 3;

	private int mode;
	private int subMode;
	private Object uid;
	public Object[] uids;
	private Message message;
	private MessageNode node;

	public FolderEvent(Object source, int mode)
	{
		super(source);
		this.mode = mode;
	}

	public FolderEvent(Object source, Object uid, int mode)
	{
		super(source);
		this.mode = mode;
		this.uid = uid;
	}

	public FolderEvent(Object source, Object[] uids, int mode, int sub)
	{
		super(source);
		this.mode = mode;
		this.subMode = sub;
		this.uids = uids;
	}

	public FolderEvent(Object source, Message message, int mode)
	{
		super(source);
		this.mode = mode;
		this.message = message;
	}

	public FolderEvent(Object source, Object[] uids, int mode)
	{
		super(source);
		this.mode = mode;
		this.uids = uids;
	}

	public MessageNode getMessageNode()
	{
		return node;
	}

	public int getMode()
	{
		return mode;
	}

	public Object getUid()
	{
		return uid;
	}

	public Object[] getUids()
	{
		return uids;
	}

	public Message getMessage()
	{
		return message;
	}

	public int getSubMode()
	{
		return subMode;
	}

	public void setSource(Folder f)
	{
		this.source = f;
	}

	public Object clone()
	{
		FolderEvent event = new FolderEvent(source, mode);
		event.mode = mode;
		event.subMode = subMode;
		event.uid = uid;
		if (uids != null)
		{
			event.uids = new Object[uids.length];
			for (int i = 0; i < uids.length; i++)
			{
				event.uids[i] = uids[i];
			}
		}

		event.message = message;
		event.node = node;
		event.source = source;

		return event;
	}
}