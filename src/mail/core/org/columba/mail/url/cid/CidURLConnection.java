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
package org.columba.mail.url.cid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


class CidURLConnection extends URLConnection {
    protected CidURLConnection(URL u) {
        super(u);
    }

    public void connect() throws IOException {
        //How to retrieve the mime part's contentID: String contentID = url.getRef();
        //initialize stuff here
        connected = true;
    }

    public String getContentType() {
        //TODO: Return content type according to the mime part in the mail
        return null;
    }

    public String getContentEncoding() {
        //TODO: Return content encoding according to the mime part in the mail
        return null;
    }

    public InputStream getInputStream() throws IOException {
        //TODO: Open stream from the mime part specified by contentID of the 
        //currently viewed mail
        return null;
    }
}
