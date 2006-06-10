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

package org.columba.core.url.http;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A replacement for the default HTTP URL handler returning instances of the
 * proxy HttpURLConnection.
 */
public class Handler extends sun.net.www.protocol.http.Handler {
    public Handler() {
        super();
    }
    
    public URLConnection openConnection(URL u) throws IOException {
        return new HttpURLConnection(u, 
                (java.net.HttpURLConnection)super.openConnection(u));
    }
}
