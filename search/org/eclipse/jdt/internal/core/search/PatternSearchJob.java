/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

public class PatternSearchJob implements IJob {

	protected SearchPattern pattern;
	protected IJavaSearchScope scope;
	protected SearchParticipant participant;
	protected IndexQueryRequestor requestor;
	protected boolean areIndexesReady;
	protected long executionTime = 0;
	
	public PatternSearchJob(
		SearchPattern pattern,
		SearchParticipant participant,
		IJavaSearchScope scope,
		IndexQueryRequestor requestor) {

		this.pattern = pattern;
		this.participant = participant;
		this.scope = scope;
		this.requestor = requestor;
	}
	public boolean belongsTo(String jobFamily) {
		return true;
	}
	public void cancel() {
		// search job is cancelled through progress 
	}
	public void ensureReadyToRun() {
		if (!this.areIndexesReady) {
			getIndexes(null/*progress*/); // may trigger some index recreation
		}
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();
		boolean isComplete = COMPLETE;
		executionTime = 0;
		Index[] indexes = getIndexes(progressMonitor);
		try {
			int max = indexes.length;
			if (progressMonitor != null) {
				progressMonitor.beginTask("", max); //$NON-NLS-1$
			}
			for (int i = 0; i < max; i++) {
				isComplete &= search(indexes[i], progressMonitor);
				if (progressMonitor != null) {
					if (progressMonitor.isCanceled()) {
						throw new OperationCanceledException();
					} else {
						progressMonitor.worked(1);
					}
				}
			}
			if (JobManager.VERBOSE) {
				JobManager.verbose("-> execution time: " + executionTime + "ms - " + this);//$NON-NLS-1$//$NON-NLS-2$
			}
			return isComplete;
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}
	public Index[] getIndexes(IProgressMonitor progressMonitor) {
		
		// acquire the in-memory indexes on the fly
		IPath[] indexPaths = this.participant.selectIndexes(this.pattern, this.scope);
		int length = indexPaths.length;
		Index[] indexes = new Index[length];
		int count = 0;
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		for (int i = 0; i < length; i++) {
			if (progressMonitor != null && progressMonitor.isCanceled())
				throw new OperationCanceledException();
			// may trigger some index recreation work
			Index index = indexManager.getIndex(indexPaths[i], true /*reuse index file*/, false /*do not create if none*/);
			if (index != null)
				indexes[count++] = index; // only consider indexes which are ready
		}
		if (count == length) 
			this.areIndexesReady = true;
		else
			System.arraycopy(indexes, 0, indexes=new Index[count], 0, count);
		return indexes;
	}	

	public boolean search(Index index, IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled())
			throw new OperationCanceledException();

//			System.out.println("SANITY CHECK: search job using obsolete index: ["+index+ "] instead of: ["+inMemIndex+"]");
		if (index == null)
			return COMPLETE;
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		ReadWriteMonitor monitor = indexManager.getMonitorFor(index);
		if (monitor == null)
			return COMPLETE; // index got deleted since acquired
		try {
			monitor.enterRead(); // ask permission to read

			/* if index has changed, commit these before querying */
			if (index.hasChanged()) {
				try {
					monitor.exitRead(); // free read lock
					monitor.enterWrite(); // ask permission to write
					indexManager.saveIndex(index);
				} catch (IOException e) {
					return FAILED;
				} finally {
					monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
				}
			}
			long start = System.currentTimeMillis();
			pattern.findIndexMatches(
				index,
				requestor,
				this.participant,
				this.scope,
				progressMonitor);
			executionTime += System.currentTimeMillis() - start;
			return COMPLETE;
		} catch (IOException e) {
			return FAILED;
		} finally {
			monitor.exitRead(); // finished reading
		}
	}
	public String toString() {
		return "searching " + pattern.toString(); //$NON-NLS-1$
	}
}
