/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;

public class FileFinder {
	private static final int INITIAL_SIZE = 10;
	public String[] resultFiles = new String[INITIAL_SIZE];
	public int count = 0;
public void find(File f, String pattern, boolean verbose) {
	if (verbose) {
		System.out.println(Main.bind("scanning.start",f.getAbsolutePath())); //$NON-NLS-1$
	}
	find0(f, pattern, verbose);
	System.arraycopy(this.resultFiles, 0, (this.resultFiles = new String[this.count]), 0, this.count);
}
public void find0(File f, String pattern, boolean verbose) {
	if (f.isDirectory()) {
		String[] files = f.list();
		if (files == null) return;
		for (int i = 0, max = files.length; i < max; i++) {
			File current = new File(f, files[i]);
			if (current.isDirectory()) {
				find0(current, pattern, verbose);
			} else {
				if (current.getName().toUpperCase().endsWith(pattern)) {
					int length;
					if ((length = this.resultFiles.length) == this.count) {
						System.arraycopy(this.resultFiles, 0, (this.resultFiles = new String[length * 2]), 0, length);
					}
					this.resultFiles[this.count++] = current.getAbsolutePath();
					if (verbose && (this.count % 100) == 0)
						System.out.print('.');
				}
			}
		}
	}
}
}
