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

/** Document the purpose of this class.
 *
 * @version 1.0
 * @author Timo Stich
 */

package org.columba.mail.message;

import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;

public class MimeHeader {
	public String contentType;
	public String contentSubtype;
	public Hashtable contentParameter;
	public String contentDisposition;
	public Hashtable dispositionParameter;
	public String contentTransferEncoding;
	public String contentID;
	public String contentDescription;
	public Integer size;

	public MimeHeader() {
		contentParameter = new Hashtable();
		dispositionParameter = new Hashtable();

		contentType = new String("text");
		contentSubtype = new String("plain");
		contentTransferEncoding = new String("7bit");
		contentParameter.put("charset", "ascii");

	}

	public MimeHeader(String type, String subtype) {
		contentParameter = new Hashtable();
		dispositionParameter = new Hashtable();

		contentType = type;
		contentSubtype = subtype;
		contentTransferEncoding = new String("7bit");
	}

	public void putContentParameter(String key, String value) {
		if (value.startsWith("\""))
			value = value.substring(1, value.length() - 1);

		contentParameter.put(key, value);
	}

	public void putDispositionParameter(String key, String value) {
		if (value.startsWith("\""))
			value = value.substring(1, value.length() - 1);

		dispositionParameter.put(key, value);
	}

	public String getContentType() {
		return contentType;
	}

	public String getContentSubtype() {
		return contentSubtype;
	}

	public String getContentDisposition() {
		return contentDisposition;
	}

	public void setContentTransferEncoding(String s) {
		contentTransferEncoding = s;
	}

	public String getContentTransferEncoding() {
		return contentTransferEncoding;
	}

	public String getContentID() {
		return contentID;
	}

	public String getContentDescription() {
		return contentDescription;
	}

	public Integer getSize() {
		return size;
	}

	public String getContentParameter(String key) {
		return (String) contentParameter.get(key);
	}

	public String getDispositionParameter(String key) {
		return (String) dispositionParameter.get(key);
	}

	public String getHeader() {
		Enumeration keys;
		String actKey;
		StringBuffer output = new StringBuffer();
		String mimeType = new String(contentType + "/" + contentSubtype);

		output.append("Content-Type: " + mimeType);

		if (!contentParameter.isEmpty()) {
			keys = contentParameter.keys();
			while (keys.hasMoreElements()) {
				actKey = (String) keys.nextElement();
				output.append(
					";\n\t" + actKey + "=" + contentParameter.get(actKey));
			}
		}
		output.append("\n");

		output.append(
			"Content-Transfer-Encoding: " + contentTransferEncoding + "\n");

		if (contentDisposition != null) {
			output.append("Content-Disposition: " + contentDisposition);
			if (!dispositionParameter.isEmpty()) {
				keys = dispositionParameter.keys();
				while (keys.hasMoreElements()) {
					actKey = (String) keys.nextElement();
					output.append(
						";\n\t"
							+ actKey
							+ "="
							+ dispositionParameter.get(actKey));
				}
			}
			output.append("\n");
		}

		return output.toString();
	}

	public String getFileName() {
		String result = null;

		result = (String) contentParameter.get("name");
		if (result != null)
			return result;

		result = (String) dispositionParameter.get("filename");

		return result;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("Type:  ");
		result.append(getContentType());
		result.append("/");
		result.append(getContentSubtype());
		result.append("\n Encoding:");
		result.append(getContentTransferEncoding());

		return result.toString();
	}

	public boolean equals(Object obj) {
		MimeHeader header = (MimeHeader) obj;
		if( header == null ) return false;

		Object[] thisAttributes = {
			contentType,
			contentSubtype,
			contentParameter,
			contentDisposition,
			dispositionParameter,
			contentDescription,
			contentID,
			size };

		Object[] otherAttributes = {
			header.contentType,
			header.contentSubtype,
			header.contentParameter,
			header.contentDisposition,
			header.dispositionParameter,
			header.contentDescription,
			header.contentID,
			header.size	
		};

		for( int i=0; i<Array.getLength( thisAttributes ); i++ ) {
			if( thisAttributes[i] == otherAttributes[i])
				continue;
			
			if( thisAttributes[i] == null )
				return false;
				
			if( otherAttributes[i] == null )
				return false;
				
			if( !thisAttributes[i].equals( otherAttributes[i] ) )
				return false;
		}
				
		return true;
	}

}
