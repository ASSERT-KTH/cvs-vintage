package org.columba.mail.gui.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.columba.core.command.CommandProcessor;
import org.columba.core.io.DiskIO;
import org.columba.core.io.StreamUtils;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ColumbaMessage;
import org.columba.mail.message.IColumbaMessage;
import org.columba.mail.pop3.command.AddPOP3MessageCommand;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.io.TempSourceFactory;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.parser.HeaderParser;
import org.columba.ristretto.parser.ParserException;

/**
 * Create welcome message.
 * 
 * @author fdietz
 */
public class WelcomeMessage {

	private static IColumbaMessage createMessage(String to, String accountUid)
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

		Source source = TempSourceFactory.createTempSource(
				new ByteArrayInputStream(buf.toString().getBytes()), -1);
		ColumbaMessage message;
		try {
			Header header = HeaderParser.parse(source);

			message = new ColumbaMessage(header);
			ColumbaHeader h = (ColumbaHeader) message.getHeader();

			message.setSource(source);

			// message size should be at least 1 KB
			int size = Math.max(source.length() / 1024, 1);
			h.getAttributes().put("columba.size", new Integer(size));

			// set the attachment flag
			h.getAttributes().put("columba.attachment", h.hasAttachments());
			h.getAttributes().put("columba.fetchstate", Boolean.TRUE);
			h.getAttributes().put("columba.accountuid", accountUid);

		} catch (ParserException e) {
			return null;
		}
		
		return message;
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
		
		IColumbaMessage message = createMessage(to, accountUid);
		CommandProcessor.getInstance().addOp(
				new AddPOP3MessageCommand(new MailFolderCommandReference(
						folder, message)));
	}

}
