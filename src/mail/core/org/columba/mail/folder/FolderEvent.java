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

package org.columba.mail.folder;

import org.columba.mail.gui.table.util.*;
import org.columba.mail.message.*;
import java.util.*;

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