package org.columba.mail.folder;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.columba.core.config.HeaderTableItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.HeaderItem;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LocalHeaderCache extends AbstractHeaderCache {

	public LocalHeaderCache(LocalFolder folder) {
		super(folder);

	}

	protected void loadHeader(ObjectInputStream p, HeaderInterface h)
		throws Exception {
		Object uid = p.readObject();
		h.set("columba.uid", uid);

		h.set("columba.flags.seen", new Boolean(p.readBoolean()));

		h.set("columba.flags.answered", new Boolean(p.readBoolean()));
		h.set("columba.flags.flagged", new Boolean(p.readBoolean()));
		h.set("columba.flags.expunged", new Boolean(p.readBoolean()));
		h.set("columba.flags.draft", new Boolean(p.readBoolean()));
		h.set("columba.flags.recent", new Boolean(p.readBoolean()));

		Date date = (Date) p.readObject();
		h.set("columba.date", date);

		h.set("columba.size", p.readObject());

		String from = (String) p.readObject();
		h.set("columba.from", from);

		Boolean b = (Boolean) p.readObject();
		h.set("columba.attachment", b);

		//int priority = p.readInt();
		h.set("columba.priority", (Integer) p.readObject());

		String host = (String) p.readObject();
		h.set("columba.host", host);

		HeaderTableItem v =
			MailConfig.getMainFrameOptionsConfig().getHeaderTableItem();
		String column;
		Object o;
		for (int j = 0; j < v.count(); j++) {
			column = (String) v.getName(j);
			o = p.readObject();

			if (o == null) {
			} else if (o instanceof String) {
				String value = (String) o;
				h.set(column, value);
			} else if (o instanceof Integer) {
				Integer value = (Integer) o;
				h.set(column, value);

			} else if (o instanceof Boolean) {
				Boolean value = (Boolean) o;
				h.set(column, value);
			} else if (o instanceof Date) {
				Date value = (Date) o;
				h.set(column, value);
			}

		}

	}

	protected void saveHeader(ObjectOutputStream p, HeaderInterface h)
		throws Exception {
		p.writeObject(h.get("columba.uid"));

		p.writeBoolean(((Boolean) h.get("columba.flags.seen")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.answered")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.flagged")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.expunged")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.draft")).booleanValue());
		p.writeBoolean(((Boolean) h.get("columba.flags.recent")).booleanValue());

		p.writeObject(h.get("columba.date"));

		p.writeObject(h.get("columba.size"));

		p.writeObject(h.get("columba.from"));

		p.writeObject(h.get("columba.attachment"));

		p.writeObject(h.get("columba.priority"));

		p.writeObject(h.get("columba.host"));

		HeaderTableItem v =
			MailConfig.getMainFrameOptionsConfig().getHeaderTableItem();
		String column;
		HeaderItem item;
		Object o;
		for (int j = 0; j < v.count(); j++) {
			column = (String) v.getName(j);
			o = h.get(column);
			p.writeObject(o);
		}
	}
}
