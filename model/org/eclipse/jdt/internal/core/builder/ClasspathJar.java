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
package org.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

class ClasspathJar extends ClasspathLocation {

String zipFilename; // keep for equals
ZipFile zipFile;
SimpleLookupTable packageCache;	

ClasspathJar(String zipFilename) {
	this.zipFilename = zipFilename;
	this.zipFile = null;
	this.packageCache = null;
}

void cleanup() {
	if (zipFile != null) {
		try { zipFile.close(); } catch(IOException e) {}
		this.zipFile = null;
	}
	this.packageCache = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJar)) return false;

	return zipFilename.equals(((ClasspathJar) o).zipFilename);
} 

NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	try {
		ClassFileReader reader = ClassFileReader.read(zipFile, qualifiedBinaryFileName);
		if (reader != null) return new NameEnvironmentAnswer(reader);
	} catch (Exception e) {} // treat as if class file is missing
	return null;
}

boolean isPackage(String qualifiedPackageName) {
	if (packageCache != null)
		return packageCache.containsKey(qualifiedPackageName);

	this.packageCache = new SimpleLookupTable(41);
	packageCache.put("", ""); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		this.zipFile = new ZipFile(zipFilename);
	} catch(IOException e) {
		return false;
	}

	nextEntry : for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();

		// add the package name & all of its parent packages
		int last = fileName.lastIndexOf('/');
		while (last > 0) {
			// extract the package name
			String packageName = fileName.substring(0, last);
			if (packageCache.containsKey(packageName))
				continue nextEntry;
			packageCache.put(packageName, packageName);
			last = packageName.lastIndexOf('/');
		}
	}
	return packageCache.containsKey(qualifiedPackageName);
}

public String toString() {
	return "Classpath jar file " + zipFilename; //$NON-NLS-1$
}
}