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
package org.eclipse.jdt.internal.core.search.matching;

import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.ClasspathJar;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;

/*
 * A name environment based on the classpath of a Java project.
 */
public class JavaSearchNameEnvironment implements INameEnvironment, SuffixConstants {
	
	ClasspathLocation[] locations;
	
public JavaSearchNameEnvironment(IJavaProject javaProject) {
	computeClasspathLocations(javaProject.getProject().getWorkspace().getRoot(), (JavaProject) javaProject);
}

public void cleanup() {
	for (int i = 0, length = this.locations.length; i < length; i++) {
		this.locations[i].cleanup();
	}
}

private void computeClasspathLocations(IWorkspaceRoot workspaceRoot, JavaProject javaProject) {

	String encoding = null;
	IPackageFragmentRoot[] roots = null;
	try {
		roots = javaProject.getAllPackageFragmentRoots();
	} catch (JavaModelException e) {
		// project doesn't exist
		this.locations = new ClasspathLocation[0];
		return;
	}
	int length = roots.length;
	ClasspathLocation[] cpLocations = new ClasspathLocation[length];
	int index = 0;
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0; i < length; i++) {
		IPackageFragmentRoot root = roots[i];
		IPath path = root.getPath();
		try {
			if (root.isArchive()) {
				ZipFile zipFile = manager.getZipFile(path);
				cpLocations[index++] = new ClasspathJar(zipFile);
			} else {
				Object target = JavaModel.getTarget(workspaceRoot, path, false);
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					if (encoding == null) {
						encoding = javaProject.getOption(JavaCore.CORE_ENCODING, true);
					}
					cpLocations[index++] = new ClasspathSourceDirectory((IContainer)target, encoding);
				} else {
					cpLocations[index++] = ClasspathLocation.forBinaryFolder((IContainer) target, false);
				}
			}
		} catch (CoreException e1) {
			// problem opening zip file or getting root kind
			// consider root corrupt and ignore
			// just resize cpLocations
			System.arraycopy(cpLocations, 0, cpLocations = new ClasspathLocation[cpLocations.length-1], 0, index);
		}
	}
	this.locations = cpLocations;
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName) {
	String 
		binaryFileName = null, qBinaryFileName = null, 
		sourceFileName = null, qSourceFileName = null, 
		qPackageName = null;
	for (int i = 0, length = this.locations.length; i < length; i++) {
		ClasspathLocation location = this.locations[i];
		NameEnvironmentAnswer answer;
		if (location instanceof ClasspathSourceDirectory) {
			if (sourceFileName == null) {
				qSourceFileName = qualifiedTypeName + SUFFIX_STRING_java;
				sourceFileName = qSourceFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qSourceFileName.length() - typeName.length - 5; // size of ".java"
					qPackageName =  qSourceFileName.substring(0, typeNameStart - 1);
					sourceFileName = qSourceFileName.substring(typeNameStart);
				}
			}
			answer = location.findClass(
				sourceFileName,
				qPackageName,
				qSourceFileName);
		} else {
			if (binaryFileName == null) {
				qBinaryFileName = qualifiedTypeName + SUFFIX_STRING_class;
				binaryFileName = qBinaryFileName;
				qPackageName =  ""; //$NON-NLS-1$
				if (qualifiedTypeName.length() > typeName.length) {
					int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
					qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
					binaryFileName = qBinaryFileName.substring(typeNameStart);
				}
			}
			answer = 
				location.findClass(
					binaryFileName, 
					qPackageName, 
					qBinaryFileName);
		}
		if (answer != null) return answer;
	}
	return null;
}

public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName);
	return null;
}

public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1]);
	return null;
}

public boolean isPackage(char[][] compoundName, char[] packageName) {
	return isPackage(new String(CharOperation.concatWith(compoundName, packageName, '/')));
}

public boolean isPackage(String qualifiedPackageName) {
	for (int i = 0, length = this.locations.length; i < length; i++)
		if (this.locations[i].isPackage(qualifiedPackageName))
			return true;
	return false;
}

}
