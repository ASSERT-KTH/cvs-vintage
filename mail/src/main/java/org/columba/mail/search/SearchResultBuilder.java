package org.columba.mail.search;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.columba.mail.folder.IMailbox;
import org.columba.mail.message.IColumbaHeader;
import org.columba.ristretto.message.Address;

public class SearchResultBuilder {

	
	public static String createSubject(IColumbaHeader h) {
		return (String) h.get("columba.subject");
	}
	
	public static Date createDate(IColumbaHeader h) {
		return (Date) h.get("columba.date");
	}
	
	public static String createFrom(IColumbaHeader h) {
		return ((Address) h.get("columba.from")).toString();
	}
	
	public static URI createURI(int folderId, Object messageId) {
		URI uri=null;
		try {
			uri = new URI("columba://org.columba.mail/"+folderId+"/"+messageId);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

}
