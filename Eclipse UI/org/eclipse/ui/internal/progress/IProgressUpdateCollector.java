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
package org.eclipse.ui.internal.progress;


/**
 * The IProgressUpdateCollector is the interface that content providers
 * conform to in order that the ProgressViewUpdater can talk to various
 * types of content provider.
 */
public interface IProgressUpdateCollector {
	
	/**
	 * Refresh the viewer.
	 */
	void refresh();
	
	/**
	 * Refresh the elements.
	 * @param elements
	 */
	void refresh(Object[] elements);
	
	/**
	 * Add the elements.
	 * @param elements Array of JobTreeElement
	 */
	void add(Object[] elements);
	
	/**
	 * Remove the elements.
	 * @param elements Array of JobTreeElement
	 */
	void remove(Object[] elements);

}
