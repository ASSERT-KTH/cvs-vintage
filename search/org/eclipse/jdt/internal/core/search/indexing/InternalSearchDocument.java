/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.internal.core.index.Index;

/**
 * Internal search document implementation
 */
public class InternalSearchDocument {
	Index index;
	/*
	 * Hidden by API SearchDocument subclass
	 */
	public void addIndexEntry(char[] category, char[] key) {
		if (this.index != null)
			index.addIndexEntry(category, key, this);
	}
	/*
	 * Hidden by API SearchDocument subclass
	 */
	public void removeAllIndexEntries() {
		if (this.index != null)
			index.remove(getPath());
	}
	/*
	 * Hidden by API SearchDocument subclass
	 */
	public String getPath() {
		return null; // implemented by subclass
	}
}
