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

import org.columba.core.config.AdapterNode;
import org.columba.core.config.DefaultItem;
import org.w3c.dom.Document;

public class IdentityItem extends DefaultItem {

	private AdapterNode name,
		organisation,
		address,
		reply,
		signature,
		attachsignature,
		signaturefile;

	private AdapterNode rootNode;

	public IdentityItem(AdapterNode rootNode, Document doc) {
		super(doc);
		this.rootNode = rootNode;

		parse();

		createMissingElements();
	}

	protected void parse() {
		int count = getRootNode().getChildCount();
		
		for (int i = 0; i < count; i++) {
			AdapterNode child = getRootNode().getChildAt(i);
			String str = child.getName();
			
			if (str.equals("name")) {
				name = child;
			} else if (str.equals("organisation")) {
				organisation = child;
			} else if (str.equals("address")) {
				address = child;
			} else if (str.equals("replyaddress")) {
				reply = child;
			} else if (str.equals("signature")) {
				signature = child;
			} else if (str.equals("attachsignature")) {
				attachsignature = child;
			} else if (str.equals("signaturefile")) {
				signaturefile = child;
			}
		}
	}

	protected void createMissingElements() {
		if (signaturefile == null)
			signaturefile = addKey(rootNode, "signaturefile", "~/.signature");
	}

	/*************************************************************************/

	public AdapterNode getRootNode() {
		return rootNode;
	}

	/******************************** set ************************************/

	public void setName(String str) {
		setTextValue(name, str);
	}

	public void setOrganisation(String str) {
		setTextValue(organisation, str);
	}

	public void setAddress(String str) {
		setTextValue(address, str);
	}

	public void setReplyAddress(String str) {
		setTextValue(reply, str);
	}

	public void setSignature(String str) {
		setCDATAValue(signature, str);
	}

	public void setAttachSignature(boolean b) {
		if (b == true)
			setTextValue(attachsignature, "true");
		else
			setTextValue(attachsignature, "false");
	}
	
	public void setSignatureFile( String str )
	{
		setTextValue( signaturefile, str );
	}

	/********************************** get ***************************************/

	public String getName() {
		return getTextValue(name);
	}

	public String getOrganisation() {
		return getTextValue(organisation);
	}

	public String getAddress() {
		return getTextValue(address);
	}

	public String getReplyAddress() {
		return getTextValue(reply);
	}

	public String getSignature() {
		return getCDATAValue(signature);
	}

	public boolean getAttachSignature() {
		String str = (String) getTextValue(attachsignature);

		Boolean b = new Boolean(str);

		return b.booleanValue();
	}

	public String getSignatureFile()
	{
		return getTextValue(signaturefile);
	}
}
