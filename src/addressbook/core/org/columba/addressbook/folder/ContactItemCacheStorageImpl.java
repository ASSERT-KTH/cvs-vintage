// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.addressbook.folder;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.ContactItem;
import org.columba.addressbook.model.ContactItemMap;
import org.columba.addressbook.model.WrongFileFormatException;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlNewIO;
import org.jdom.Document;

/**
 * @author fdietz
 *  
 */
public class ContactItemCacheStorageImpl implements ContactItemCacheStorage {

	/** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger
            .getLogger("org.columba.addressbook.folder");
    
	
	/**
	 * 
	 * keeps a list of HeaderItem's we need for the table-view
	 *  
	 */
	private ContactItemMap headerItemList;

	/**
	 * 
	 * binary file named "header"
	 *  
	 */
	private File headerFile;

	/**
	 * 
	 * boolean variable shows if we already loaded the header-cache from disc
	 *  
	 */
	private boolean headerCacheAlreadyLoaded;

	/**
	 * dirty flag should be set if cached was changed
	 */
	private boolean hasChanged;
	
	/**
	 * directory where contact files are stored
	 */
	private File directoryFile;
	
	private AbstractFolder folder;
	
	/**
	 *  
	 */
	public ContactItemCacheStorageImpl(AbstractFolder folder) {
		super();

		this.folder = folder;
		
		headerItemList = new ContactItemMap();

		directoryFile = folder.getDirectoryFile();
		
		headerFile = new File(directoryFile, ".header");

		/*
		if (headerFile.exists()) {
			try {
				load();
				headerCacheAlreadyLoaded = true;
			} catch (Exception ex) {
				ex.printStackTrace();

				headerCacheAlreadyLoaded = false;
			}
		} else {
			sync();
		}
		*/
		sync();
	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#getHeaderItemMap()
	 */
	public ContactItemMap getContactItemMap() throws Exception {
		return headerItemList;
	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#add(org.columba.addressbook.folder.HeaderItem)
	 */
	public void add(Object uid, ContactItem item) throws Exception {
		getContactItemMap().add(uid, item);

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#remove(java.lang.Object)
	 */
	public void remove(Object uid) throws Exception {
		getContactItemMap().remove(uid);

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#modify(java.lang.Object,
	 *      org.columba.addressbook.folder.HeaderItem)
	 */
	public void modify(Object uid, ContactItem item) throws Exception {
		getContactItemMap().remove(item);
		getContactItemMap().add(uid, item);

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#save()
	 */
	public void save() throws Exception {

	}

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#load()
	 */
	public void load() throws Exception {

	}
	
	public void sync() {
		

	        File[] list = directoryFile.listFiles();
	        List v = new Vector();

	        for (int i = 0; i < list.length; i++) {
	            File file = list[i];
	            File renamedFile;
	            String name = file.getName();
	            int index = name.indexOf("header");

	            if (index == -1) {
	                // message file found
	                String number = name;

	                //                Integer numberString = new Integer( number );
	                //System.out.println("number: "+ number );
	                if ((file.exists()) && (file.length() > 0)) {
	                    renamedFile = new File(file.getParentFile(),
	                            file.getName() + '~');
	                    file.renameTo(renamedFile);

	                    //System.out.println("renamed file:" + renamedFile);
	                    v.add(renamedFile);
	                }

	                //System.out.println("v index: "+ v.indexOf( file ) );
	            } else {
	                // header file found
	                headerFile.delete();
	            }
	        }

	        for (int i = 0; i < v.size(); i++) {
	            File file = (File) v.get(i);

	            File newFile = new File(file.getParentFile(),
	                    (new Integer(i)).toString() + ".xml");
	            file.renameTo(newFile);
	            try {
	            	
	            	Document doc = XmlNewIO.load(newFile);
	            
	            	Contact contact = new Contact(doc, new Integer(i));
	            	ContactItem item = new ContactItem(contact);
	  
	            	item.setUid(new Integer(i));
	             
	                add(new Integer(i), item);
	                
	                folder.setNextMessageUid(i+1);
	            } catch (WrongFileFormatException ex) {
	            	if (MainInterface.DEBUG)
	            		ex.printStackTrace();
	            	// delete corrupt file
	            	newFile.delete();
	            	
	            }catch (Exception ex) {
	                ex.printStackTrace();
	            }
	        }
	        
	        
	        LOG.info("map-size()=="+headerItemList.count());
	        
	    }
	

	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#count()
	 */
	public int count() {
		return headerItemList.count();
	}
	/**
	 * @see org.columba.addressbook.folder.ContactItemCacheStorage#exists(java.lang.Object)
	 */
	public boolean exists(Object uid) {
		return headerItemList.exists(uid);
	}
}