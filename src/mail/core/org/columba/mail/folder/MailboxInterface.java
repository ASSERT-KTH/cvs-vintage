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

import java.io.InputStream;

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.HeaderList;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MessageFolderInfo;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

public interface MailboxInterface {

	/**
	 * Get the MessageFolderInfo of this mailbox
	 * 
	 * @return
	 * @throws IOException
	 */
	public MessageFolderInfo getMessageFolderInfo() throws Exception;

	/**
	 * Removes all messages which are marked as expunged
	 * 	
	 * @throws Exception
	 */
	public void expungeFolder() throws Exception;

	/**
	 * Add message to this folder.
	 * 
	 * @deprecated Use #addMessage(InputStream)
	 * 
	 * @param message Message object can be null 
	 * @param source  raw string of message 
	 * @return Object UID of message
	 * @throws Exception
	 */
	public Object addMessage(ColumbaMessage message) throws Exception;

	/**
	 * Add message to folder.
	 * 
	 * @deprecated Use #addMessage(InputStream)
	 * 
	 * @param source
	 * @return Object
	 * @throws Exception
	 */
	public Object addMessage(String source) throws Exception;

	/**
	 * 
	 * 
	 * @param uid 			UID of message
	 * @return boolean 		true, if message exists
	 * @throws Exception
	 */
	public boolean exists(Object uid) throws Exception;

	/**
	 * Return list of headers.
	 * 
	 * @return HeaderList		list of headers 
	 * @throws Exception
	 */
	public HeaderList getHeaderList() throws Exception;

	/**
	 * Mark messages as read/flagged/expunged/etc.
	 * 
	 * See <class>MarkMessageCommand</class> for more information especially
	 * concerning the variant value.
	 * 
	 * @param uid		array of UIDs 
	 * @param variant	variant can be a value between 0 and 6
	 * @throws Exception
	 */
	public abstract void markMessage(Object[] uids, int variant)
		throws Exception;

	/**
	 * Remove message from folder.
	 * 
	 * @param uid		UID identifying the message to remove
	 * @throws Exception
	 */
	public void removeMessage(Object uid) throws Exception;

	/**
	 * 
	 * Read <class>MimePart</class> and <class>MimePartTree</class> for
	 * more details. Especially on the address parameter.
	 * 
	 * @deprecated Use #getMimePartBodyStream(Object, Integer[]) and #getMimePartSourceStream(Object, Integer[])
	 * 
	 * @param uid			UID of message
	 * @param address		array of Integer, addressing the MimePart
	 * @return MimePart		MimePart of message
	 * @throws Exception
	 */
	public MimePart getMimePart(Object uid, Integer[] address)
		throws Exception;

	/**
	 * Return the source of the message.
	 * 
	 * @deprecated Use #getMessageSourceStream(Object)
	 * 
	 * @param uid		UID of message
	 * @return String		the source of the message
	 * @throws Exception
	 */
	public String getMessageSource(Object uid) throws Exception;

	/**
	 * Return mimepart structure. See <class>MimePartTree</class> for
	 * more details.
	 * 
	 * @param uid				UID of message
	 * @return MimePartTree		return mimepart structure
	 * @throws Exception
	 */
	public MimeTree getMimePartTree(Object uid) throws Exception;

	/**
	 * Return header of message
	 * 
	 * @deprecated Use #getHeaderFields(Object, String[])
	 * 
	 * @param uid					UID of message
	 * @return ColumbaHeader		header of message
	 * @throws Exception
	 */
	public ColumbaHeader getMessageHeader(Object uid) throws Exception;

	/**
	 * Copy messages identified by UID to this folder.
	 * 
	 * This method is necessary for optimization reasons.
	 * 
	 * Think about using local and remote folders. If we would have only
	 * methods to add/remove messages this wouldn't be very efficient
	 * when moving messages between for example IMAP folders on the same
	 * server. We would have to download a complete message to remove it
	 * and then upload it again to add it to the destination folder.
	 * 
	 * Using the innercopy method the IMAP server can use its COPY 
	 * command to move the message on the server-side.
	 * 
	 * @param destFolder		the destination folder of the copy operation
	 * @param uids				an array of UID's identifying the messages
	 * @throws Exception
	 */
	public void innerCopy(MailboxInterface destFolder, Object[] uids)
		throws Exception;

	
	/**
	 * Adds a message to this Mailbox
	 * 
	 * @param in The message InputStream
	 * @return The new uid of the added message or null if not defined
	 * @throws Exception
	 */
	public Object addMessage( InputStream in ) throws Exception;	


	/**
	 * Gets all specified headerfields. An example headerfield might be
	 * "Subject" or "From" (take care of lower/uppercaseletters). 
	 * 
	 * @param uid The uid of the desired message
	 * @param keys The keys like defined in e.g. RFC2822 
	 * @return A {@link Header} containing the headerfields if they were present
	 * @throws Exception
	 */
	public Header getHeaderFields(Object uid, String[] keys) throws Exception;	

	/**
	 * Gets the InputStream from the body of the mimepart. This excludes the header.
	 * 
	 * @param uid The UID of the message
	 * @param address The address of the mimepart
	 * @return
	 * @throws Exception
	 */
	public InputStream getMimePartBodyStream(Object uid, Integer[] address ) throws Exception;
	
	/**
	 * Gets the InputStream from the complete mimepart. This includes the header.
	 * 
	 * @param uid The UID of the message
	 * @param address address The address of the mimepart
	 * @return
	 * @throws Exception
	 */
	public InputStream getMimePartSourceStream(Object uid, Integer[] address ) throws Exception;
	
	/**
	 * Gets the InputStream of the complete messagesource.
	 * 
	 * @param uid The UID of the message
	 * @return
	 * @throws Exception
	 */
	public InputStream getMessageSourceStream(Object uid) throws Exception;
	
	/**
	 * Gets the Flags of the message.
	 * 
	 * @param uid The UID of the message
	 * @return
	 * @throws Exception
	 */
	public Flags getFlags(Object uid) throws Exception;
	
	/**
	 * Gets a attribute from the message
	 * 
	 * @param uid The UID of the message
	 * @param key The name of the attribute (e.g. "columba.subject", "columba.size") 
	 * @return
	 * @throws Exception
	 */
	public Object getAttribute(Object uid, String key) throws Exception;
	
}
