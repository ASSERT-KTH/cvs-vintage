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
package org.columba.core.util;

public class Mutex {
	private boolean mutex;
	String name;
	String lockingThreadName = null;
	public Mutex(String name) {
		mutex = false;
		this.name = name;
	}

	/**
	 *
	 * @return true the mutex was indeed taken anew, false if calling thread already had mutex.
	 */
	public synchronized boolean getMutex() {
		if (mutex) {
			if (lockingThreadName.equals(Thread.currentThread().getName())) {
				// this Thread already has a lock, keep it but without nesting
				return false;
			}
		}
		while (mutex) {
			try {
				//ColumbaLogger.log.debug("thread " + Thread.currentThread().getName() + " waiting for " + name + " held by thread " + lockingThreadName);
				wait();
			} catch (InterruptedException e) {
				if (Thread.currentThread().isInterrupted()) {
					// gota go now
					throw new RuntimeException(
						"waiting for mutex, "
							+ name
							+ ", thread "
							+ Thread.currentThread().getName()
							+ " isInterrupted, throwing RuntimeException");
				}
				// else keep waiting
			}
		}
		mutex = true;
		lockingThreadName = Thread.currentThread().getName();
		//ColumbaLogger.log.debug("thread " + lockingThreadName + " now has mutex " + name);
		return true;
	}

	public synchronized void releaseMutex() {
		if ((Thread.currentThread().getName()).equals(lockingThreadName)) {
			mutex = false;
			lockingThreadName = null;
			//ColumbaLogger.log.debug("thread " + lockingThreadName + " now has mutex " + name);
			notifyAll();
		} else {
			String msg = "";
			if (mutex) {
				msg = " held by thread " + lockingThreadName;
			}
			//ColumbaLogger.log.debug("thread " + Thread.currentThread().getName() + " tried to release unheld mutex " + name + msg);
		}
	}

	public String getLockingThreadName() {
		return lockingThreadName;
	}

	public String getName() {
		return name;
	}
}
