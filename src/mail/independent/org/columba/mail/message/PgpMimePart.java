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

package org.columba.mail.message;

import org.columba.mail.config.PGPItem;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PgpMimePart extends MimePart {
	
	public static final int SIGNED	= 0;
	public static final int ENCRYPTED = 1;
	
	private PGPItem pgpItem;
	
	public PgpMimePart(MimeHeader header, PGPItem pgpitem, int type) {
		super( header );
		if( type == SIGNED ) {
			header.putContentParameter("protocol","application/pgp-signature");
			header.putContentParameter("micalg","pgp-sha1");
		} else {
			header.putContentParameter("protocol","application/pgp-encrypted");
		}
		
		setPgpItem( pgpitem );
	}

	/**
	 * Returns the pgpitem.
	 * @return PGPItem
	 */
	public PGPItem getPgpItem() {
		return pgpItem;
	}

	/**
	 * Sets the pgpitem.
	 * @param pgpitem The pgpitem to set
	 */
	public void setPgpItem(PGPItem pgpitem) {
		this.pgpItem = pgpitem;
	}

}
