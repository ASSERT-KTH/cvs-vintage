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

package org.columba.addressbook.folder;


public abstract interface DataStorage
{
	public abstract void saveDefaultCard( DefaultCard card, Object uid );

	//public abstract ContactCard loadContactCard( Object uid );
	//public abstract void removeContactCard( Object uid );
	//public abstract void saveGroupListCard( GroupListCard card, Object uid );
	//public abstract GroupListCard loadGroupListCard( Object uid );
	//public abstract void removeGroupListCard( Object uid );
	
	public abstract DefaultCard loadDefaultCard( Object uid );

	public abstract void removeCard( Object uid );
	
	public abstract void modifyCard( DefaultCard card, Object uid );
	
	//public abstract void getContactCard( Object uid );
}
