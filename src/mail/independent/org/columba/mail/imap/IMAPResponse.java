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
package org.columba.mail.imap;

import java.util.Vector;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IMAPResponse {

	/** 
	 * status can be tagged or untagged 
	 * 
	 */

	/** 
	 * tagged OK respone indicates an successful
	 * completion of the associated command
	 * untagged OK response indicates an informational-
	 * messages
	 */
	public static final int STATUS_OK = 0;

	/** 
	 * tagged NO response indicates an operational 
	 * error message from the server
	 * untagged NO indicates a warning, command can
	 * still be completed successfully
	 */
	public static final int STATUS_NO = 1;

	/**
	 * tagged BAD respone indicate an protocol-level
	 * error in the client's command; the tag indicates
	 * the command that caused the error
	 * 
	 * The untagged form indicates a protocol-level error 
	 * for which the associated command can not be determined; 
	 * it can also indicate an internal server failure.
	 */
	public static final int STATUS_BAD = 2;

	/**
	 *  preauth and bye are always untagged 	  
	 **/

	/** 
	 * The PREAUTH response is always untagged, and is one 
	 * of three possible greetings at connection startup.  
	 * It indicates that the connection has already been 
	 * authenticated by external means and thus no LOGIN 
	 * command is needed.
	 */
	public static final int STATUS_PREAUTH = 3;

	/**
	 * The BYE reponse is always untagged
	 * 
	 */
	public static final int STATUS_BYE = 4;

	public static final int STATUS_UNKNOWN = 4;

	public int status;

	public static final String[] statusCodeString =
		{ "OK", "NO", "BAD", "PREAUTH", "BYE" };

	public static IMAPResponse BYEResponse =
		new IMAPResponse("* BYE Connection closed");
	/** 
	 * status responses can be tagged or untagged
	 * 
	 * tagged responses indicate the completion 
	 * result (OK, NO or BAD status) of a client command,
	 * and have a tag matching the command
	 * 
	 * Some status responses and all server data are
	 * untagged. Untagged response is presentated with
	 * the token "*" instead of a tag.
	 * 
	 */
	public static final int TAGGED = 0;
	public static final int UNTAGGED = 1;
	public static final int CONTINUATION = 2;

	public int tagged;
	public String tag;

	/** 
	 * The human-readable text contains a special alert
	 * that MUST be presented to the user in a fashion
	 * that calls the user's attention to the message.
	 */
	public static final int RESPONSE_CODE_ALERT = 0;

	/**
	 * Followed by a mailbox name and a new mailbox name.
	 * A SELECT or EXAMINE is failing because the target
	 * mailbox name no longer exists because it was
	 * renamed to the new mailbox name.
	 */
	public static final int RESPONSE_CODE_NEWNAME = 1;

	/**
	 * The human-readable text represents an error in
	 * parsing the [RFC-822] header or [MIME-IMB] headers
	 * of a message in the mailbox.
	 */
	public static final int RESPONSE_CODE_PARSE = 2;

	/**
	 * Followed by a parenthesized list of flags,
	 * indicates which of the known flags that the client
	 * can change permanently.
	 */
	public static final int RESPONSE_CODE_PERMANENTFLAGS = 3;

	/**
	 * The mailbox is selected read-only, or its access
	 * while selected has changed from read-write to
	 * read-only.
	 */
	public static final int RESPONSE_CODE_READONLY = 4;

	/**
	 * The mailbox is selected read-write, or its access
	 * while selected has changed from read-only to
	 * read-write.
	 */
	public static final int RESPONSE_CODE_WRITEONLY = 5;

	/**
	 * An APPEND or COPY attempt is failing because the
	 * target mailbox does not exist (as opposed to some
	 * other reason).  This is a hint to the client that
	 * the operation can succeed if the mailbox is first
	 * created by the CREATE command.
	 */
	public static final int RESPONSE_CODE_TRYCREATE = 6;

	/**
	 * 
	 * Followed by a decimal number, indicates the unique
	 * identifier validity value.
	 * 
	 **/
	public static final int RESPONSE_CODE_UIDVALIDITY = 7;

	/**
	 *  Followed by a decimal number, indicates the number
	 * of the first message without the \Seen flag set.
	 */

	public static final int RESPONSE_CODE_UNSEEN = 8;

	public static final int RESPONSE_CODE_UNKNOWN = 9;

	public static final String[] responseCodeString =
		{
			"ALERT",
			"NEWNAME",
			"PARSE",
			"PERMANENTFLAGS",
			"READONLY",
			"WRITEONLY",
			"TRYCREATE",
			"UIDVALIDITY",
			"UNSEEN" };

	public int responseCode;

	protected int atomIndex = -1;

	protected String source;

	public IMAPResponse(String source) {
		this.source = source;

		try {
			parse(source);
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	/**
	 * Method parses a single line server response string
	 * @param str server response
	 * @throws Exception
	 */
	public void parse(String str) throws Exception {
		String nextAtom = null;

		//System.out.println("parsing:" + str);

		source = str;

		// start with beginning of string
		atomIndex = source.indexOf(" ");
		nextAtom = source.substring(0, atomIndex);
		//System.out.println("nextAtom="+nextAtom+">");
		
		if (nextAtom.equals("*") == true)
			tagged = UNTAGGED;
		else if (nextAtom.equals("+") == true)
			tagged = CONTINUATION;
		else {
			tagged = TAGGED;
			tag = source.substring(0, source.indexOf(" "));
			//System.out.println("tag-string=" + tag);
		}

		//System.out.println("tag-id=" + tagged);

		int oldIndex = atomIndex;
		oldIndex++;
		atomIndex = source.indexOf(" ", oldIndex);
		nextAtom = source.substring(oldIndex, atomIndex);

		//System.out.println("status-string=" + nextAtom);

		for (int i = 0; i < statusCodeString.length; i++) {
			if (nextAtom.equalsIgnoreCase(statusCodeString[i])) {
				status = i;
				break;
			}
		}

		if (status == -1)
			status = STATUS_UNKNOWN;

		//System.out.println("status-id=" + status);

		// the next atom is optional
		if (source.charAt(atomIndex + 1) == '[') {
			oldIndex = atomIndex + 1;
			atomIndex = source.indexOf("]", oldIndex);
			nextAtom = source.substring(oldIndex, atomIndex + 1);

			// we found a response code
			//System.out.println("responsecode=" + nextAtom);
			/*
			for (int i = 0; i < responseCodeString.length; i++) {
				if (nextAtom
					.equalsIgnoreCase("[" + responseCodeString[i] + "]")) {
					responseCode = i;
			
					break;
				}
			}
			
			if (responseCode == -1)
				responseCode = RESPONSE_CODE_UNKNOWN;
			*/

		}

		if (source.charAt(atomIndex + 1) == ' ') {
			// we found human readable optional message

			nextAtom = source.substring(atomIndex + 2, source.length() - 1);

			//System.out.println("hrm=" + nextAtom);
		}
	}

	protected String getNextAtom() throws Exception {
		int oldIndex = atomIndex;

		if (oldIndex == -1) {

			oldIndex = 0;
			atomIndex = source.indexOf(" ");
		} else
			atomIndex = source.indexOf(" ", oldIndex + 1);

		if (atomIndex != -1) {
			String substring = source.substring(oldIndex, atomIndex);
			//System.out.println("substring=" + substring);
			return substring;
		}

		throw new IMAPResponseException("next atom not found");

	}

	public String[] getTokenList() {
		Vector v = new Vector();

		String str = source;
		for ( int i=0; i<str.length(); i++ )
		{
			int lsubindex = skipSpaces(str, 0);
		}
		
		
		int size = v.size();
		if (size > 0) {
			String[] s = new String[size];
			v.copyInto(s);
			return s;
		} else 
			return null;
	}

	protected int skipSpaces(String str, int start) {
		int nextspace = str.indexOf(" ", start);

		return nextspace;
	}

	public int getTaggedCode() {
		return tagged;
	}

	public int getStatus() {
		return status;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getTag() {
		return tag;
	}

	public String getSource() {
		if ( source.length() > 1 ) return source.substring(0,source.length()-1);
		
		
		return source;
	}

	public boolean isTagged() {
		if (getTaggedCode() == TAGGED)
			return true;

		return false;
	}

	public boolean isOK() {
		if (isTagged() && getStatus() == STATUS_OK)
			return true;

		return false;
	}

	public boolean isNO() {
		if (isTagged() && getStatus() == STATUS_NO)
			return true;

		return false;
	}

	public boolean isBYE() {
		if (getStatus() == STATUS_BYE)
			return true;

		return false;
	}

	public boolean isBAD() {
		if (getStatus() == STATUS_BAD)
			return true;

		return false;
	}
	
	public boolean isCONTINUATION()
	{
		if ( getTaggedCode()==CONTINUATION ) return true;
		
		return false;
	}

}
