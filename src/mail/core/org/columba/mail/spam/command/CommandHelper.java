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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.spam.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.MessageFolder;
import org.columba.mail.main.MailInterface;
import org.columba.ristretto.coder.Base64DecoderInputStream;
import org.columba.ristretto.coder.QuotedPrintableDecoderInputStream;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeTree;

/**
 * Helper class provides methods for preparing email messages before getting
 * passed along to the spam filter.
 * 
 * @author fdietz
 */
public final class CommandHelper {

	/**
	 * Return bodypart of message as inputstream.
	 * <p>
	 * Note, that this depends on wether the user prefers HTML or text messages.
	 * <p>
	 * Bodypart is decoded if necessary.
	 * 
	 * @param folder
	 *            selected folder containing the message
	 * @param uid
	 *            ID of message
	 * @return inputstream of message bodypart
	 * @throws Exception
	 */
	public static InputStream getBodyPart(MessageFolder folder, Object uid)
			throws Exception {
		MimeTree mimePartTree = folder.getMimePartTree(uid);

		List l = mimePartTree.getLeafsWithContentType(mimePartTree
				.getRootMimeNode(), "text");
		if (l.size() > 1) {
			Vector streamList = new Vector();
			Iterator it = l.iterator();
			while (it.hasNext()) {
				MimePart mp = (MimePart) it.next();

				InputStream s = getBodyPartStream(folder, uid, mp);
				streamList.add(s);
			}

			SequenceInputStream stream = new SequenceInputStream(streamList
					.elements());
			return stream;
		} else if (l.size() == 1) {
			MimePart mp = (MimePart) l.get(0);
			InputStream s = getBodyPartStream(folder, uid, mp);
			return s;
		} else {
			return new ByteArrayInputStream(new byte[0]);

		}

	}

	/**
	 * Get inputstream of bodypart.
	 * <p>
	 * Additionally decode inputstream.
	 * 
	 * @param folder		selected folder
	 * @param uid			selected message UID
	 * @param mp			selected Mimepart
	 * @return				decoded inputstream
	 * @throws Exception
	 */
	private static InputStream getBodyPartStream(MessageFolder folder,
			Object uid, MimePart mp) throws Exception {
		InputStream bodyStream = folder.getMimePartBodyStream(uid, mp
				.getAddress());

		int encoding = mp.getHeader().getContentTransferEncoding();

		switch (encoding) {
		case MimeHeader.QUOTED_PRINTABLE: {
			bodyStream = new QuotedPrintableDecoderInputStream(bodyStream);

			break;
		}

		case MimeHeader.BASE64: {
			bodyStream = new Base64DecoderInputStream(bodyStream);

			break;
		}
		}

		return bodyStream;
	}

	/**
	 * Retrieve account this message is associated to.
	 * 
	 * @param folder
	 *            selected folder
	 * @param uid
	 *            selected message
	 * @return account item
	 * @throws Exception
	 */
	public static AccountItem retrieveAccountItem(MessageFolder folder,
			Object uid) throws Exception {
		AccountItem item = null;

		Object accountUid = folder.getAttribute(uid, "columba.accountuid");
		if (accountUid != null) {
			// try to get account using the account ID
			item = MailInterface.config.getAccountList().uidGet(
					((Integer) accountUid).intValue());

		}

		if (item == null) {
			// try to get the account using the email address
			Header header = folder.getHeaderFields(uid, new String[] { "To" });

			item = MailInterface.config.getAccountList().getAccount(
					header.get("To"));

		}

		if (item == null) {
			// use default account as fallback

			item = MailInterface.config.getAccountList().getDefaultAccount();
		}

		return item;
	}
}