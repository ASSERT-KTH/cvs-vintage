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
/*
	columba: a Java open source email client
	http://columba.sourceforge.net/

	Filename: GlobalResourceLoader.java
	Author: Hrk (Luca Santarelli) <hrk@users.sourceforge.net>
	Comments: this is the core class to handle i18n in columba, loading, handling and returning localized strings.
	It should not be used directly, use MailResourceLoader or AddressbookResourceLoader (or *ResourceLoader) instead.
*/

package org.columba.core.util;

//GlobalResourceBundle
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;

/*
	Behaviour.
	When a resource is needed, getString() or getMnemonics() are called. They look for a resource with that name (in the current locale bundles).
	If it is not found, they look for the resource in the global resource bundle (for the current locale). If this is not found, "FIXME" is returned.

	Example of usage: We need to get the text for "my_cool_button" located into "org/columba/modules/mail/i18n/action/something_else_than_action"
	sPath: org/columba/modules/mail/i18n/action/ => The complete package path.
	sName: something_else_than_action => the name of the _it_IT.properties file.
	sID: my_cool_button => the name to be looked for inside sName file.
	We can call:
	a) MailResourceLoader.getString("action", "something_else_than_action", "my_cool_button");
	b) ResourceLoader.getString("org/columba/modules/mail/i18n/action", "something_else_than_action", "my_cool_button");
	They'll both work.

	We need to gets its mnemonic: 
	a) MailResourceLoader.getMnemonic("action", "something_else_than_action", "my_cool_button");
	b) ResourceLoader.getMnemonic("org/columba/modules/mail/i18n/action", "something_else_than_action", "my_cool_button");
*/
public class GlobalResourceLoader {

	//Hashtables of resource bundles: we'll use one for general bundles and one for global bundles.
	protected static Hashtable htBundles = new Hashtable(80);
	//key: sBundlePath, value: rBundle
	protected static ResourceBundle globalBundle;
	//Key: bundle "path" (i.e.: "org/columba/core/i18n/global/global", "org/columba/modules/mail/i18n/action/filter")
	//Value: a ResourceBundle object.
	protected static final String FIX_ME = "FIX ME!";
        
        static {
                try{
                        globalBundle = ResourceBundle.getBundle("org.columba.core.i18n.global.global");
                } catch(MissingResourceException mre) {
                        throw new RuntimeException("Global resource bundle not found, Columba cannot start.");
                }
        }

	protected static String generateBundlePath(String sPath, String sName) {
		return new String(sPath + "." + sName);
	}
        
	/*
		This method returns the translation for the given string identifier. If no translation is found, the default english item is used.
		Should this fail too, a "Fix me!" (private final static String FIXME) string will be returned.
	
		Example usage call: getString("org/columba/modules/mail/i18n/", "dialog", "close")
		We'll look for "close" in "org/columba/modules/mail/i18n/dialog/dialog_locale_LOCALE.properties"
		Thus:
		sPath: "org/columba/modules/mail/i18n/dialog"
		sName: "dialog"
		sID: "close"
		The bundle name will be: "org/columba/modules/mail/i18n/dialog/dialog"
	
		Hypotetically this method should not be available to classes different from *ResourceLoader (example: MailResourceLoader, AddressbookResourceLoader); this means that *ResourceLoader classes *do know* how to call this method.
	*/
	public static String getString(String sPath, String sName, String sID) {
		if (sPath == null || sName == null || sID == null)
			return null;
		if (sPath.equals("") || sName.equals("") || sID.equals(""))
			return null;
		//Find out if we already loaded the needed ResourceBundle object in the hashtable.
		String sBundlePath = generateBundlePath(sPath, sName);
		ResourceBundle bundle = (ResourceBundle) htBundles.get(sBundlePath);
                if (bundle == null) {
                        try {
                                bundle = ResourceBundle.getBundle(sBundlePath);
                                htBundles.put(sBundlePath, bundle);
                                return bundle.getString(sID);
                        } catch (MissingResourceException mre) {
                                bundle = globalBundle;
                        }
                }
                try {
                        return bundle.getString(sID);
                } catch (MissingResourceException mre) {
                        if (MainInterface.DEBUG) {
                                ColumbaLogger.log.error("'"+sID+"' in '"+sBundlePath+"' could not be found.");
                        }
                        return FIX_ME;
                }
	}

	public static char getMnemonic(String sPath, String sName, String sID) {
		/*
			Example: MailResourceLoader.getMnemonic("dialog", "filter", "chose_folder");
			MailResourceLoader changes path to "org/columba/modules/mail/i18n/dialog", then submits the search.
		*/
		String sResult = getString(sPath, sName, sID + "_mnemonic");
		if (sResult != null && sResult != FIX_ME) { //String was found.
			return sResult.charAt(0);
		} else {
			return 0;
                }
	}
}
