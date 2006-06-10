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
package org.columba.mail.config;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;


public class OutgoingItem extends DefaultItem {
    public static final String USE_DEFAULT_ACCOUNT = "use_default_account";
	public static final String UID = "uid";
	public static final String NONE = "NONE";
	public static final String LOGIN_METHOD = "login_method";
	public static final String ENABLE_SSL = "enable_ssl";
	public static final String SAVE_PASSWORD = "save_password";
	public static final String USER = "user";
	public static final String PORT = "port";
	public static final String HOST = "host";

	public OutgoingItem(XmlElement e) {
        super(e);
    }
}
