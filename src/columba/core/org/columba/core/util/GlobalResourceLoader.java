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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.columba.core.logging.ColumbaLogger;

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
	protected static final int INITIAL_CAPACITY = 80;
	protected static final int INITIAL_CAPACITY_GLOBAL = 5;
	protected static Hashtable htBundles = null;
	//key: sBundlePath, value: rBundle
	protected static Hashtable htGlobalBundles = null;
	//key: sName, value: rBundle
	protected static ResourceBundle rbGlobal = null;
	//Key: bundle "path" (i.e.: "org/columba/core/i18n/global/global", "org/columba/modules/mail/i18n/action/filter")
	//Value: a ResourceBundle object.
	protected static final String GLOBAL_RESOURCE_PATH =
		"org.columba.core.i18n.global";
	protected static final String FIX_ME = "FIX ME!";

	protected static Locale currentLocale = Locale.getDefault();
	//	protected static boolean useEnglishFallback = true;

	//===== Methods =====
	//Helper method
	protected static String generateBundlePath(String sPath, String sName) {
		return new String(sPath + "." + sName);
	}
	/*
		//Helper method, sets a flag related to translators (who will get a FIXME if there is an untranslated string) and users (who will get the english fallback if there is an untranslated string). Returns the previous value of the flag.
		protected static boolean setUseEnglishFallback(boolean newValue) {
			boolean previousValue = useEnglishFallback;
			useEnglishFallback = newValue;
			return previousValue;
		}
	*/
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
		if (htBundles == null)
			htBundles = new Hashtable(INITIAL_CAPACITY);
		ResourceBundle rBundle = (ResourceBundle) htBundles.get(sBundlePath);
		boolean loadOk = false;
		if (rBundle == null) {
			//The bundle wasn't found in the hashtable, we need to load it from the resources and then put it into the hashtable.
			rBundle = loadResourceBundle(sBundlePath);
			if (rBundle != null) { //The resource was loaded.
				loadOk = true;
				htBundles.put(sBundlePath, rBundle);
				//We put it into the hashtable of the bundles.
			} else
				loadOk = false;
		} else { //The bundle was found, we can read from it.
			loadOk = true;
		}
		//Now we have two scenarios: the bundle exixts (it has been loaded) or it doesn't exist (it wasn't in the table and could not get loaded).
		//If it could not be loaded, we'll look for sID inside the global resource bundle.
		//if it could be loaded, we'll lok for sID into it and, if a failure occurrs, inside the global resource bundle.
		boolean foundID = false;
		if (loadOk) { //Look for the string in the resource bundle.
			try {
				return (rBundle.getString(sID));
			} catch (MissingResourceException ex) { //The string was not found.
				foundID = false;
				//We fall through and we'll reach the global resource bundle.
			}
		}
		if (loadOk == false || foundID == false) {
			//Either the resource bundle wasn't loaded or the item wasn't found: global resource bundles!
			sBundlePath = generateBundlePath(GLOBAL_RESOURCE_PATH, sName);
			if (htGlobalBundles == null)
				htGlobalBundles = new Hashtable(INITIAL_CAPACITY_GLOBAL);
			rBundle = (ResourceBundle) htGlobalBundles.get(sName);
			if (rBundle == null) { //The bundle has not been loaded yet.
				rBundle = loadResourceBundle(sBundlePath);
				if (rBundle != null) {
					loadOk = true;
					htGlobalBundles.put(sName, rBundle);
				} else
					loadOk = false;
			} else { //The bundle was found in the hashtable.
				loadOk = true;
			}
			if (loadOk) { //Bundle was successfully loaded.
				try {
					return (rBundle.getString(sID));
				} catch (MissingResourceException ex) {
					foundID = false;
				}
			}
			if (loadOk == false || foundID == false) {
				//we'll need to look in the global/global.properties file.
				//This is the last chance to find a bundle (or a string) for translators. Users will get another chance with the english langpack.
				if (rbGlobal == null)
					rbGlobal =
						loadResourceBundle(
							generateBundlePath(GLOBAL_RESOURCE_PATH, "global"));
				if (rbGlobal == null) {
					if (/*!useEnglishFallback*/
						org.columba.core.main.MainInterface.DEBUG == true) {
						//It's a translator: return FIX_ME
						return FIX_ME;
					} //Else fallback.
				} else {
					try {
						return (rbGlobal.getString(sID));
					} catch (MissingResourceException ex) {

						if (/*!useEnglishFallback*/
							org.columba.core.main.MainInterface.DEBUG
								== true) {
							ColumbaLogger.log.error(
								"'"
									+ sID
									+ "' in '"
									+ sPath
									+ "."
									+ sName
									+ "' cannot be found.");
							return FIX_ME;
							//Else fallback.
						}

					}
				} //We get here if we want the english fallback; let's try the english bundles (which we will not cache).
				sBundlePath = generateBundlePath(sPath, sName);
				ResourceBundle rbEnglish = null;
				try {
					rbEnglish =
						ResourceBundle.getBundle(sBundlePath, Locale.ENGLISH);
					return rbEnglish.getString(sID);
				} catch (MissingResourceException ex) {
					//This is launched either if the bundle does not exist or if the string is not available. We'll check the global bundles.
					sBundlePath =
						generateBundlePath(GLOBAL_RESOURCE_PATH, sName);
					try {
						rbEnglish =
							ResourceBundle.getBundle(
								sBundlePath,
								Locale.ENGLISH);
						return rbEnglish.getString(sID);
					} catch (MissingResourceException exGlobals) { //Just like the previous catch block. This time we'll look in the global/global.properties file.
						sBundlePath =
							generateBundlePath(GLOBAL_RESOURCE_PATH, "global");
						try {
							rbEnglish =
								ResourceBundle.getBundle(
									sBundlePath,
									Locale.ENGLISH);
							return rbEnglish.getString(sID);
						} catch (MissingResourceException exGlobal) {
							if (/*!useEnglishFallback*/
								org.columba.core.main.MainInterface.DEBUG
									== true)
								ColumbaLogger.log.error(
									"'"
										+ sID
										+ "' in '"
										+ sPath
										+ "."
										+ sName
										+ "' cannot be found in the english langpack either.");
							return FIX_ME;

						}
					}
				}
			}
		}
		//We can't get here, but JDK can't understand it.
		return null;
		//("If you see this, ResourceLoader.Java has a bug. Report this message.");
	}

	public static char getMnemonic(String sPath, String sName, String sID) {
		/*
			Example: MailResourceLoader.getMnemonic("dialog", "filter", "chose_folder");
			MailResourceLoader changes path to "org/columba/modules/mail/i18n/dialog", then submits the search.
		*/
		String sResult = getString(sPath, sName, sID + "_mnemonic");
		if (sResult != null && !sResult.equals(FIX_ME)) { //String was found.
			return sResult.toCharArray()[0];
		} else
			return 0;
	}

	//This method reloads the bundles in the hashtable for the new current locale, then calls the garbage collector.
	//This method will only reload the bundles, it will not change the display text.
	public static void reload() {
		currentLocale = Locale.getDefault();
		Enumeration e = htBundles.keys();
		String sKey;
		ResourceBundle rBundle;
		for (htBundles.clear();
			e.hasMoreElements();
			) { //For each key in the hashtable (now empty) we try to reload the bundle.
			sKey = (String) e.nextElement();
			try {
				rBundle = loadResourceBundle(sKey);
				if (rBundle != null)
					htBundles.put(sKey, rBundle);
			} catch (MissingResourceException ex) { //Do nothing, the for-loop will move to the next element.
			}
		}
		//Global resource bundles.
		e = htGlobalBundles.keys();
		for (htGlobalBundles.clear(); e.hasMoreElements();) {
			sKey = (String) e.nextElement();
			try {
				rBundle = loadResourceBundle(sKey);
				if (rBundle != null)
					htGlobalBundles.put(sKey, rBundle);
			} catch (MissingResourceException ex) {
			}
		}
		//And now the global resource bundle.
		try {
			rbGlobal =
				ResourceBundle.getBundle(
					generateBundlePath(GLOBAL_RESOURCE_PATH, "global"));
		} catch (MissingResourceException ex) {
			rbGlobal = null;
		}
		//Finally run the garbage collector to free the memory of the old bundles.
		System.gc();
	}

	//This method simply empties the cache of bundles and calls the garbage collector.
	public static void empty() {
		htBundles.clear();
		htGlobalBundles.clear();
		rbGlobal = null;
		System.gc();
	}

	/*
		This method loads a resource bundle and returns it. On failure, it returns null.
		The first place to look for a bundle is a "langpack_xx_XX_xxxx.jar" file mapping to the default locale. Example: langpack_it_IT_Friendly.jar
		Obviously this is not the "default" locale of the JVM, but it has been set with Locale.setDefault(Locale locale) somewhere.
		If the bundle is not found in the .jar file or if the file itself does not exist, we fall back to the english (en_UK) bundles which are directly inside the main columba.jar.
	*/
	protected static ResourceBundle loadResourceBundle(String sBundle) {
		if (sBundle == null || sBundle.equals(""))
			return null;
		/*
		Since we're looking for a locale which is the current one, the possible filenames are:
			langpack_language_COUNTRY_variant
			langpack_language_COUNTRY
		On the contrary, the possible bundle names are:
			sBundle_language_COUNTRY_variant
			sBundle_language_COUNTRY
			sBundle_language
			sBundle
		We must look for files, and secondly entries, in this particular order.
		*/
		//We'll look for langpack_it_IT_variant.jar first, and for langpack_it_IT.jar in case of failure. If this fails too, we assume there is no langpack file.
		boolean jarFileFound = false, bundleFileFound = false;
		String sFileName =
			new String(
				"langpack_"
					+ currentLocale.getLanguage()
					+ "_"
					+ currentLocale.getCountry()
					+ "_"
					+ currentLocale.getVariant()
					+ ".jar");
		File f = new File(sFileName);
		if (f.exists())
			//langpack_it_IT_variant.jar exists: we're using a "variant" locale.
			jarFileFound = true;
		else {
			sFileName =
				new String(
					"langpack_"
						+ currentLocale.getLanguage()
						+ "_"
						+ currentLocale.getCountry()
						+ ".jar");
			f = new File(sFileName);
			if (f.exists())
				//We're using a standard langpack file (example: langpack_it_IT.jar)
				jarFileFound = true;
			else //There is no valid langpack file.
				jarFileFound = false;
		}
		//We now know if there is a .jar file.
		if (!jarFileFound) { //There isn't any jarfile, fall back to the english language.
			try {
				return (ResourceBundle.getBundle(sBundle, currentLocale));
			} catch (MissingResourceException ex) { //The resource wasn't found.
				return null;
			}
		} else { //A valid langpack was found and f references it.
			ZipFile zFile = null;
			try {
				zFile = new ZipFile(f);
			} catch (Exception ex) { //We can get a ZipException or a IOException, but we can do nothing, so re simply exit.
				return null;
			}
			//Now zFile points to our valid langpack file. We need to look for the bundle entry and we need to look for different values! See doc above.
			ZipEntry zeBundle = null;
			String sEntry = null;
			//The bundle name was given in a platform independent format (using '.'). We need to convert it somehow. Zipfiles should work well with "/" as path separator.
			sBundle = sBundle.replace('.', '/');

			//Let's begin.
			sEntry =
				sBundle
					+ "_"
					+ currentLocale.getLanguage()
					+ "_"
					+ currentLocale.getCountry()
					+ "_"
					+ currentLocale.getVariant()
					+ ".properties";
			zeBundle = zFile.getEntry(sEntry);
			if (zeBundle != null)
				//The entry was found in the "it_IT_variant" form.
				bundleFileFound = true;
			else {
				sEntry =
					sBundle
						+ "_"
						+ currentLocale.getLanguage()
						+ "_"
						+ currentLocale.getCountry()
						+ ".properties";
				zeBundle = zFile.getEntry(sEntry);
				if (zeBundle != null)
					//The entry was found in the "it_IT" form.
					bundleFileFound = true;
				else {
					sEntry =
						sBundle
							+ "_"
							+ currentLocale.getLanguage()
							+ ".properties";
					zeBundle = zFile.getEntry(sEntry);
					if (zeBundle != null)
						//The entry was found in the "it" form.
						bundleFileFound = true;
					else {
						sEntry = sBundle + ".properties";
						zeBundle = zFile.getEntry(sEntry);
						if (zeBundle != null)
							//The entry was found in the "no locale" form
							bundleFileFound = true;
						else { //It was not found in the langpack
							bundleFileFound = false;
						}
					}
				}
			}
			//Either it was found or it wasn't.
			if (bundleFileFound == false) {
				//The entry was not found in the jarfile: we'll try the default english bundle.
				try {
					return (ResourceBundle.getBundle(sBundle, currentLocale));
				} catch (MissingResourceException ex) { //The resource wasn't found.
					return null;
				}
			} else { //A file entry was found and zeBundle points to it: we can (try to) load it and return it.
				InputStream ios = null;
				try {
					ios =
						new BufferedInputStream(zFile.getInputStream(zeBundle));
				} catch (IOException ex) { //We can do nothing.
					return null;
				}
				// Let's make a PropertyResourceBundle out of the InputStream representing the zip entry.
				try {
					PropertyResourceBundle prb =
						new PropertyResourceBundle(ios);
					return prb;
				} catch (MissingResourceException ex) { //This is not possible, unless the *.properties file is not a real PropertyResourceBundle, which means the translator made a real mess!
					return null;
				} catch (IOException ex) { //This is not possible since the file has already been verified and the input stream obtained.
					return null;
				}
			}
		}
	}
}
