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
package org.eclipse.jdt.internal.core.index.impl;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.index.IDocument;

/**
 * An <code>JarFileDocument</code> represents an jar file.
 */

public class JarFileDocument implements IDocument {
	protected IFile file;
	/**
	 * JarFileDocument constructor comment.
	 */
	public JarFileDocument(IFile file) {
		this.file = file;
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
	 */
	public byte[] getByteContent() {
		return null;
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getCharContent()
	 */
	public char[] getCharContent() {
		return null;
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getEncoding()
	 */
	public String getEncoding() {
		return null;
	}

	public File getFile() {
		IPath location = file.getLocation();
		if (location == null) {
			return null;
		} else {
			return location.toFile();
		}
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getName()
	 */
	public String getName() {
		return file.getFullPath().toString();
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
	 */
	public String getStringContent() {
		return null;
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getType()
	 */
	public String getType() {
		String extension= file.getFileExtension();
		if (extension == null)
			return ""; //$NON-NLS-1$
		return extension;
	}
}
