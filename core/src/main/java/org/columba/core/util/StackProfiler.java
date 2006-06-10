package org.columba.core.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.columba.core.logging.Logging;

public class StackProfiler {

	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.core.util"); //$NON-NLS-1$
	
	/**
	 * keeps a list of profile point ids
	 */
	private Vector vector = new Vector();

	/**
	 * key is the id, value is the profile data
	 */
	private Hashtable hashtable = new Hashtable();

	/**
	 * current path in simple tree-like hierarchy
	 */
	private String currentPath = "";

	/**
	 * Set new profiling starting point. This is where the time measurment
	 * actually starts.
	 * 
	 * @param id
	 *            unique id of profiling point
	 */
	public void push(String id) {
		// abort if not in debugging mode
		if (Logging.DEBUG == false)
			return;

		// current time
		long currentTime = System.currentTimeMillis();

		// store profiling point data
		hashtable.put(id, new ProfileData(id, currentPath,
				new Long(currentTime).longValue()));

		// this profiling point is parent of the next one
		currentPath = currentPath + "/" + id;

		// store id
		vector.add(id);
	}

	/**
	 * Close profiling point. This is where the time measurement actually ends.
	 * 
	 * @param id
	 *            unique id of profiling point
	 */
	public void pop(String id) {
		// abort if not in debugging mode
		if (Logging.DEBUG == false)
			return;

		if (id == null)
			throw new IllegalArgumentException("id == null");

		if (hashtable.containsKey(id) == false)
			throw new IllegalArgumentException("id=" + id + " not found");

		long currentTime = System.currentTimeMillis();

		ProfileData bean = (ProfileData) hashtable.get(id);
		bean.endTime = currentTime;

		String firstItem = (String) vector.get(0);
		if (firstItem.equals(id)) {
			// print all collected profile data

			LOG.info("profiler info:"); //$NON-NLS-1$
			Iterator it = vector.listIterator();

			while (it.hasNext()) {
				String nextId = (String) it.next();
				printDuration(nextId);
			}
		} else {
			// current path is the parent of the current element
			int index = currentPath.lastIndexOf("/");
			currentPath = currentPath.substring(0, index);
		}
	}

	/**
	 * Print debug data.
	 * 
	 * @param id
	 *            unique profiling point id
	 */
	private void printDuration(String id) {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (hashtable.containsKey(id) == false)
			throw new IllegalArgumentException("id not found");

		ProfileData bean = (ProfileData) hashtable.get(id);
		String splits[] = bean.path.split("/");

		for (int i = 0; i < splits.length; i++)
			System.out.print("  ");

		long duration = bean.endTime - bean.startTime;
		LOG.info("[" + duration + "ms] - " + id); //$NON-NLS-2$
	}

	/**
	 * Structure containing profiling data.
	 */
	private class ProfileData {
		protected String id;

		protected String path;

		protected long startTime;

		protected long endTime;

		protected ProfileData(String id, String path, long startTime) {
			this.id = id;
			this.path = path;
			this.startTime = startTime;
		}

	}
}
