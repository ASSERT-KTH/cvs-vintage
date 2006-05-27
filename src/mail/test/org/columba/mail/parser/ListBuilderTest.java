package org.columba.mail.parser;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import junit.framework.TestCase;

import org.columba.addressbook.facade.ContactItem;
import org.columba.addressbook.facade.GroupItem;
import org.columba.addressbook.facade.HeaderItem;
import org.columba.addressbook.facade.IContactFacade;
import org.columba.addressbook.facade.IContactItem;
import org.columba.addressbook.facade.IFolder;
import org.columba.addressbook.facade.IFolderFacade;
import org.columba.addressbook.facade.IGroupItem;
import org.columba.addressbook.facade.IHeaderItem;
import org.columba.api.exception.StoreException;
import org.columba.core.services.ServiceRegistry;

public class ListBuilderTest extends TestCase {

	private IContactItem contact1;

	private IContactItem contact2;

	private IGroupItem group1;

	private IGroupItem group2;

	@Override
	protected void setUp() throws Exception {

		IContactFacade contactFacade = new MyContactFacade();
		IFolderFacade folderFacade = new MyFolderFacade();

		ServiceRegistry.getInstance().register(IContactFacade.class,
				contactFacade);
		ServiceRegistry.getInstance().register(IFolderFacade.class,
				folderFacade);

		contact1 = new ContactItem("0", "name1", "firstname1", "lastname1",
				"name1@mail.org");
		contact2 = new ContactItem("1", "name2", "firstname2", "lastname2",
				"name2@mail.org");
		
		// create group containing first contact
		group1 = new GroupItem("0", "groupname1", "description1");
		group1.addContact(contact1);

		// create group with two contacts
		group2 = new GroupItem("1", "groupname2", "description2");
		group2.addContact(contact1);
		group2.addContact(contact2);
	}

	/**
	 * Test if contact display names are resolved correctly in email address
	 */
	public void testCreateFlatListWithNamesOnly() {
		List<String> l = new Vector<String>();
		l.add("name1");
		l.add("name2");

		List<String> result = ListBuilder.createFlatList(l);
		assertEquals("name1@mail.org", result.get(0));
		assertEquals("name2@mail.org", result.get(1));

	}

	/**
	 * Now, mix in real email address
	 */
	public void testCreateFlatListWithEmailAddressMixedIn() {
		List<String> l = new Vector<String>();
		l.add("name1");
		l.add("name2@mail.org");

		List<String> result = ListBuilder.createFlatList(l);
		assertEquals("name1@mail.org", result.get(0));
		assertEquals("name2@mail.org", result.get(1));

	}
	
	/**
	 * Test if groups are resolved correctly 
	 *
	 */
	public void testCreateFlatListWithGroups() {
		List<String> l = new Vector<String>();
		l.add("groupname1");
		l.add("name2@mail.org");

		List<String> result = ListBuilder.createFlatList(l);
		// first group contains first contact item
		assertEquals("name1@mail.org", result.get(0));
		assertEquals("name2@mail.org", result.get(1));

	}
	
	/**
	 * Again, but with a group containing two contact items
	 *
	 */
	public void testCreateFlatListWithGroups2() {
		List<String> l = new Vector<String>();
		l.add("groupname2");
		l.add("name2@mail.org");

		List<String> result = ListBuilder.createFlatList(l);
		// first group contains first contact item
		assertEquals("name1@mail.org", result.get(0));
		assertEquals("name2@mail.org", result.get(1));
		assertEquals("name2@mail.org", result.get(2));

	}

	
	/*
	 * Test method for
	 * 'org.columba.mail.parser.ListBuilder.createStringListFromItemList(List<IHeaderItem>)'
	 */
	public void testCreateStringListFromItemList() {

		List<IHeaderItem> l = new Vector<IHeaderItem>();
		IHeaderItem item1 = new HeaderItem(true);
		item1.setName("name1");
		l.add(item1);
		IHeaderItem item2 = new HeaderItem(true);
		item2.setName("name2");
		l.add(item2);

		List<String> result = ListBuilder.createStringListFromItemList(l);
		assertEquals("name1", result.get(0));
		assertEquals("name2", result.get(1));
	}

	// mock folder class, only returns folder id
	class Folder implements IFolder {
		Folder() {

		}

		public ImageIcon getIcon() {
			return null;
		}

		public String getId() {
			return "101";
		}

		public String getName() {
			return null;
		}

		public TreeNode getChildAt(int childIndex) {
			return null;
		}

		public int getChildCount() {
			return 0;
		}

		public TreeNode getParent() {
			return null;
		}

		public int getIndex(TreeNode node) {
			return 0;
		}

		public boolean getAllowsChildren() {
			return false;
		}

		public boolean isLeaf() {
			return false;
		}

		public Enumeration children() {
			return null;
		}
	}

	// mock object folder facade, only returns folder list
	class MyFolderFacade implements IFolderFacade {
		MyFolderFacade() {

		}

		public IFolder getFolder(String uid) {
			return null;
		}

		public IFolder getCollectedAddresses() {
			return null;
		}

		public IFolder getLocalAddressbook() {
			return null;
		}

		public IFolder getFolderByName(String name) {
			return null;
		}

		public List<IFolder> getAllFolders() {
			List<IFolder> list = new Vector<IFolder>();
			IFolder f = new Folder();

			list.add(f);

			return list;
		}

		public IFolder getRootFolder() {
			return null;
		}
	}

	// mock objects contact facade, only returns getContactItem() and
	// getAllGroups()
	class MyContactFacade implements IContactFacade {
		MyContactFacade() {

		}

		public void addContact(String id, IContactItem contactItem)
				throws StoreException, IllegalArgumentException {

		}

		public void addContacts(String id, IContactItem[] contactItem)
				throws StoreException, IllegalArgumentException {
		}

		public void addContact(IContactItem contactItem) throws StoreException,
				IllegalArgumentException {

		}

		public void addContacts(IContactItem[] contactItems)
				throws StoreException, IllegalArgumentException {

		}

		public IContactItem getContactItem(String folderId, String contactId)
				throws StoreException, IllegalArgumentException {
			if (contactId.equals("0"))
				return contact1;
			else if (contactId.equals("1"))
				return contact2;
			return null;
		}

		public List<IHeaderItem> getAllHeaderItems(String folderId,
				boolean flattenGroupItems) throws StoreException,
				IllegalArgumentException {
			return null;
		}

		public List<IContactItem> getAllContacts(String folderId)
				throws StoreException, IllegalArgumentException {
			return null;
		}

		public List<IGroupItem> getAllGroups(String folderId)
				throws StoreException, IllegalArgumentException {
			List<IGroupItem> list = new Vector<IGroupItem>();
			list.add(group1);
			list.add(group2);
			return list;
		}

		public String findByEmailAddress(String folderId, String emailAddress)
				throws StoreException, IllegalArgumentException {

			return null;
		}

		public String findByName(String folderId, String name)
				throws StoreException, IllegalArgumentException {
			if (name.equals("name1"))
				return "0";
			else if (name.equals("name2"))
				return "1";

			return null;
		}
	}
}
