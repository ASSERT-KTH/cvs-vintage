/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

abstract class AddFileToIndex extends IndexRequest {
	IFile resource;
	IPath indexPath;
	IndexManager manager;

	public AddFileToIndex(IFile resource, IPath indexPath, IndexManager manager) {
		this.resource = resource;
		this.indexPath = indexPath;
		this.manager = manager;
	}
	public boolean belongsTo(String jobFamily) {
		return jobFamily.equals(this.indexPath.segment(0));
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return true;

		try {
			/* ensure no concurrent write access to index */
			IIndex index = manager.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
			if (index == null) return true;
			ReadWriteMonitor monitor = manager.getMonitorFor(index);
			if (monitor == null) return true; // index got deleted since acquired

			try {
				monitor.enterWrite(); // ask permission to write
				if (!indexDocument(index)) return false;
			} finally {
				monitor.exitWrite(); // free write lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.resource + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	protected abstract boolean indexDocument(IIndex index) throws IOException;
	public String toString() {
		return "indexing " + this.resource.getFullPath(); //$NON-NLS-1$
	}
}