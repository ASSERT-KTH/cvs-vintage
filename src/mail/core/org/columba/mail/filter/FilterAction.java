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

package org.columba.mail.filter;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

public class FilterAction extends DefaultItem {

	public FilterAction(XmlElement root) {
		super(root);

	}

	public int getUid() {
		if (contains("uid") == false) {
			set("uid", 101);

			return getInteger("uid");
		} else
			return getInteger("uid");

	}

	public void setUid(int i) {
		set("uid", i);

	}

	public String getAction() {

		return get("type");
	}

	public void setAction(String s) {
		set("type", s);

	}

}
