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
package org.columba.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * The CloneInputStream from a master.
 * 
 * @see CloneStreamMaster
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class CloneInputStream extends InputStream {
	private CloneStreamMaster model;

	private int id;

	/**
	 * Constructs a new CloneInputStream.
	 * 
	 * @see CloneStreamMaster#getClone()
	 * 
	 * @param master
	 */
	protected CloneInputStream(CloneStreamMaster model, int id) {
		super();
		this.model = model;
		this.id = id;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		return model.read(id);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		return model.read(id, arg0, arg1, arg2);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() throws IOException {
		return model.available();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		if (model != null) {
			model.close(id);
			model = null;
		}
	}
}
