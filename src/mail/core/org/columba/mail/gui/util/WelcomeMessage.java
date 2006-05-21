package org.columba.mail.gui.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.columba.core.command.CommandProcessor;
import org.columba.core.io.DiskIO;
import org.columba.core.io.StreamUtils;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.command.AddMessageCommand;

/**
 * Create welcome message.
 * 
 * @author fdietz
 */
public class WelcomeMessage {

	private static String createMessage(String to, String accountUid)
			throws Exception {
		StringBuffer buf = new StringBuffer();
		buf.append("Subject: Welcome to Columba\r\n");
		buf.append("From: columba-users@lists.sourceforge.net\r\n");
		buf.append("To: "+to+"\r\n");
		buf.append("\r\n");

		InputStream is = DiskIO
				.getResourceStream("org/columba/mail/welcome_message_body.txt");
		String body = StreamUtils.readCharacterStream(is).toString();
		buf.append(body);
		
		return buf.toString();
	}

	/**
	 * Create welcome message and add it to folder.
	 * 
	 * @param folder		selected folder (usually this is inbox)
	 * @param to			user's email address
	 * @param accountUid	account id
	 * @throws Exception
	 */
	public static void addWelcomeMessage(IMailFolder folder, String to, String accountUid)
			throws Exception {
		if ( folder == null) throw new IllegalArgumentException("folder == null");
		if ( to == null || to.length() == 0) throw new IllegalArgumentException("to == nllu");
		if ( accountUid == null ) throw new IllegalArgumentException("account uid == null");
		

		// create message
		String message = createMessage(to, accountUid);
		
		// convert to inputstream
		InputStream is = new ByteArrayInputStream(message.getBytes("UTF-8"));
		
		// add to folder
		CommandProcessor.getInstance().addOp(
				new AddMessageCommand(new MailFolderCommandReference(
						folder), is));
	}

}
