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

package org.columba.core.command;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressObservedInputStream extends FilterInputStream {

	private WorkerStatusController status;

	private int read;

	/**
	 * Constructs the ProgressObservedInputStream.java.
	 * 
	 * @param arg0
	 */
	public ProgressObservedInputStream(InputStream arg0,
			WorkerStatusController status) {
		this(arg0, status, false);
	}

	/**
	 * Constructs the ProgressObservedInputStream.java.
	 * 
	 * @param arg0
	 */
	public ProgressObservedInputStream(InputStream arg0,
			WorkerStatusController status, boolean relative) {
		super(arg0);
		this.status = status;

		if (!relative) {
			try {
				status.setProgressBarMaximum(arg0.available());
			} catch (IOException e) {
			}
			
			read = 0;
		} else {
			read = status.getProgressBarValue();
		}
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		int result = super.read();
		if (result != -1)
			status.setProgressBarValue(++read);
		return result;
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		int result = super.read(arg0, arg1, arg2);
		read += result;
		status.setProgressBarValue(read);

		return result;
	}
}