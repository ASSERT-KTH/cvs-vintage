/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathSourceJar extends ClasspathJar {
	private String encoding;

	public ClasspathSourceJar(File file, boolean closeZipFileAtEnd, AccessRuleSet accessRuleSet, String encoding) {
		super(file, closeZipFileAtEnd, accessRuleSet);
		this.encoding = encoding;
	}

	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
		if (!isPackage(qualifiedPackageName)) 
			return null; // most common case

		ZipEntry sourceEntry = this.zipFile.getEntry(qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6)  + SUFFIX_STRING_java);
		if (sourceEntry != null) {
			try {
				return new NameEnvironmentAnswer(new CompilationUnit(Util.getInputStreamAsCharArray(this.zipFile.getInputStream(sourceEntry), -1, this.encoding),
						qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6)  + SUFFIX_STRING_java, this.encoding),
						fetchAccessRestriction(qualifiedBinaryFileName));
			} catch (IOException e) {
				// treat as if source file is missing
			}
		}
		return null;
	}
}
