package org.columba.contact.search;

import java.net.URI;
import java.net.URISyntaxException;

public class SearchResultBuilder {


	public static URI createURI(String folderId, String contactId) {
		URI uri=null;
		try {
			uri = new URI("columba://org.columba.contact/"+folderId+"/"+contactId);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

}
