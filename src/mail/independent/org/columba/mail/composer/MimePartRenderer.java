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

package org.columba.mail.composer;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import org.columba.core.command.WorkerStatusController;
import org.columba.mail.coder.Base64Encoder;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class MimePartRenderer {
	private static final int BOUNDARY_LENGTH = 32;

	public abstract String getRegisterString();

	public abstract String render(MimePart part, WorkerStatusController workerStatusController);

	protected void appendHeader(StringBuffer result, MimeHeader header) {
		result.append("Content-Type:  ");
		result.append(header.getContentType());
		result.append("/");
		result.append(header.getContentSubtype());
		appendParameters(result, header.contentParameter);
		result.append("\n");

		if (header.contentTransferEncoding != null) {
			result.append("Content-Transfer-Encoding: ");
			result.append(header.getContentTransferEncoding());
			result.append("\n");
		}

		if (header.contentDisposition != null) {
			result.append("Content-Disposition: ");
			result.append(header.getContentDisposition());
			appendParameters(result, header.dispositionParameter);
			result.append("\n");
		}

		if (header.contentDescription != null) {
			result.append("Content-Description: ");
			result.append(header.getContentDescription());
			result.append("\n");
		}

		if (header.contentID != null) {
			result.append("Content-ID: ");
			result.append(header.getContentID());
			result.append("\n");
		}
	}

	private void appendParameters(StringBuffer result, Hashtable parameters) {

		Enumeration keys = parameters.keys();
		String key, value;
		
		// Cant use this because of JDK1.3 
		//int lineLength = result.length() - result.lastIndexOf("\n");
		
		// instead :

		int lineLength = 0;
		int actLength = result.length();
		
		while( result.charAt(actLength - lineLength - 1) != '\n' ) {
			lineLength ++;
			if( actLength == lineLength ) break;
		}		


		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			value = (String) parameters.get(key);

			if (lineLength > 75) {
				result.append(";\n ");
				lineLength = 1;
			} else {
				result.append("; ");
			}

			result.append(key);
			result.append("=\"");
			result.append(value);
			result.append("\"");

			lineLength = lineLength + 3 + key.length() + value.length();
		}
	}
	
	protected String createUniqueBoundary() {		
		Random random = new Random();					
		byte[] bytes = new byte[BOUNDARY_LENGTH];
		Base64Encoder encoder = new Base64Encoder();
		
		random.nextBytes(bytes);
		
		try {
			return encoder.encode( new String( bytes ), "US-ASCII" );	
		} catch (UnsupportedEncodingException e) {
			// Can never be reached
		}
		
		return null;
	}
	
}
