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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;

public class SubTypeSearchJob extends PatternSearchJob {

	Map inputs = new HashMap(5);

public SubTypeSearchJob(
		SearchPattern pattern,
		SearchParticipant participant,
		IJavaSearchScope scope,
		IndexQueryRequestor requestor) {

		super(
			pattern,
			participant,
			scope,
			requestor);
}
public void closeAll(){

	Iterator openedInputs = inputs.values().iterator();
	while (openedInputs.hasNext()){
		IndexInput input = (IndexInput) openedInputs.next();
		try {
			input.close();
		} catch(IOException e){
			// ignore
		}
	} 
}
/**
 * execute method comment.
 */
public boolean search(Index index, IProgressMonitor progressMonitor) {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	if (index == null) return COMPLETE;		
	IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
	ReadWriteMonitor monitor = indexManager.getMonitorFor(index);
	if (monitor == null) return COMPLETE; // index got deleted since acquired
	try {
		monitor.enterRead(); // ask permission to read

		/* if index has changed, commit these before querying */
		if (index.hasChanged()){
			try {
				monitor.exitRead(); // free read lock
				monitor.enterWrite(); // ask permission to write
				indexManager.saveIndex(index);
			} catch(IOException e){
				return FAILED;
			} finally {
				monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
			}
		}
		long start = System.currentTimeMillis();

		IndexInput input;
		if ((input = (IndexInput) inputs.get(index)) == null){
			input = new BlocksIndexInput(index.getIndexFile());
			input.open();
			inputs.put(index, input);
			//System.out.println("Acquiring INPUT for "+index);
		}
		pattern.findIndexMatches(input, requestor, this.participant, this.scope, progressMonitor);
		executionTime += System.currentTimeMillis() - start;
		return COMPLETE;
	} catch(IOException e){
		return FAILED;
	} finally {
		monitor.exitRead(); // finished reading
	}
}
}
