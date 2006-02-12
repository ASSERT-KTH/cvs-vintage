package org.columba.calendar.store;

import java.io.File;

import junit.framework.TestCase;

import org.columba.calendar.base.UUIDGenerator;
import org.columba.calendar.model.ICalendarModel;
import org.columba.calendar.model.VEventModel;

public class LocalCalendarStoreTest extends TestCase {

	private File file;

	private LocalCalendarStore storage;

	protected void setUp() throws Exception {
		file = new File("test_calendar");

		storage = new LocalCalendarStore(file);
	}

	public void testAddGet() throws Exception {
		VEventModel model = new VEventModel();
		model.setSummary("summary");
		model.setDescription("description");

		String uuid = new UUIDGenerator().newUUID();
		storage.add(model);

		boolean exists = storage.exists(uuid);
		assertTrue(exists);

		ICalendarModel result = storage.get(uuid);
		assertNotNull(result);

	}

	public void testRemove() throws Exception {
		VEventModel model = new VEventModel();
		model.setSummary("summary");
		model.setDescription("description");

		String uuid = new UUIDGenerator().newUUID();
		storage.add(model);

		storage.remove(uuid);

		ICalendarModel result = storage.get(uuid);
		assertNull(result);

	}

	protected void tearDown() throws Exception {
//		// delete all data in directory
//		File[] list = file.listFiles();
//
//		for (int i = 0; i < list.length; i++) {
//			list[i].delete();
//		}
//
//		// delete folder
//		file.delete();
	}

}
