package org.columba.mail.folder.headercache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.columba.core.base.ListTools;
import org.columba.mail.folder.AbstractLocalFolder;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.folder.IDataStorage;
import org.columba.mail.folder.IMailboxInfo;
import org.columba.mail.folder.MailboxInfoInvalidException;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;
import org.columba.ristretto.io.Source;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.parser.HeaderParser;

public class SyncHeaderList {

	private static final int WEEK = 1000 * 60 * 60 * 24 * 7;

	/**
	 * @param worker
	 * @throws Exception
	 */
	public static void sync(AbstractMessageFolder folder, IHeaderList headerList)
			throws IOException {
		if (folder.getObservable() != null) {
			folder.getObservable().setMessage(
					folder.getName() + ": Syncing headercache...");
		}

		IDataStorage ds = ((AbstractLocalFolder) folder)
				.getDataStorageInstance();

		Object[] uids = ds.getMessageUids();
		Object[] headerlistUids = headerList.getUids();
		
		// First remove all headers that are in the headerlist but
		// not the Folder
		ArrayList uidsToRemove = new ArrayList(Arrays.asList(headerlistUids));
		ListTools.substract(uidsToRemove, Arrays.asList(uids));
		for( Object u : uidsToRemove) {
			headerList.remove(u);
		}
		
		Date today = Calendar.getInstance().getTime();

		// parse all message files to recreate the header cache
		IColumbaHeader header = null;
		IMailboxInfo messageFolderInfo = folder.getMessageFolderInfo();
		messageFolderInfo.setExists(0);
		messageFolderInfo.setRecent(0);
		messageFolderInfo.setUnseen(0);
		// headerList.clear();

		folder.setChanged(true);

		if (folder.getObservable() != null) {
			folder.getObservable().setMax(uids.length);
			folder.getObservable().resetCurrent();
		}

		for (int i = 0; i < uids.length; i++) {

			if ((folder.getObservable() != null) && ((i % 100) == 0)) {
				folder.getObservable().setCurrent(i);
			}

			if (!headerList.exists(uids[i])) {
				try {
					Source source = ds.getMessageSource(uids[i]);

					if (source.length() == 0) {
						ds.removeMessage(uids[i]);

						continue;
					}

					header = new ColumbaHeader(HeaderParser.parse(source));

					// make sure that we have a Message-ID
					String messageID = (String) header.get("Message-Id");
					if (messageID != null)
						header.set("Message-ID", header.get("Message-Id"));

					header = CachedHeaderfields.stripHeaders(header);

					if (isOlderThanOneWeek(today, ((Date) header
							.getAttributes().get("columba.date")))) {
						header.getFlags().set(Flags.SEEN);
					}

					// message size should be at least 1 KB
					int size = Math.max(source.length() / 1024, 1);
					header.getAttributes().put("columba.size",
							new Integer(size));

					// set the attachment flag
					header.getAttributes().put("columba.attachment",
							header.hasAttachments());

					header.getAttributes().put("columba.uid", uids[i]);

					headerList.add(header, uids[i]);
					source.close();
					source = null;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				header = headerList.get(uids[i]);
			}

			try {
				messageFolderInfo.incExists();
				if (header.getFlags().getRecent()) {
					messageFolderInfo.incRecent();
				}

				if (!header.getFlags().getSeen()) {
					messageFolderInfo.incUnseen();
				}
			} catch (MailboxInfoInvalidException e) {
				// Can't happen
			}


			((AbstractLocalFolder) folder)
					.setNextMessageUid(((Integer) uids[uids.length - 1])
							.intValue() + 1);

			if ((folder.getObservable() != null) && ((i % 100) == 0)) {
				folder.getObservable().setCurrent(i);
			}
		}

		// we are done
		if (folder.getObservable() != null) {
			folder.getObservable().resetCurrent();
		}
	}

	private static boolean isOlderThanOneWeek(Date arg0, Date arg1) {
		return (arg0.getTime() - WEEK) > arg1.getTime();
	}

}
