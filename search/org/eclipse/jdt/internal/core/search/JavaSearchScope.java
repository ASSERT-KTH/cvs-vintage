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
package org.eclipse.jdt.internal.core.search;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A Java-specific scope for searching relative to one or more java elements.
 */
public class JavaSearchScope extends AbstractSearchScope {
	
	private ArrayList elements;

	/* The paths of the resources in this search scope 
	   (or the classpath entries' paths 
	   if the resources are projects) */
	private IPath[] paths;
	private boolean[] pathWithSubFolders;
	private int pathsCount;
	
	private IPath[] enclosingProjectsAndJars;
	
public JavaSearchScope() {
	this.initialize();
	
	//disabled for now as this could be expensive
	//JavaModelManager.getJavaModelManager().rememberScope(this);
}
	
private void addEnclosingProjectOrJar(IPath path) {
	int length = this.enclosingProjectsAndJars.length;
	for (int i = 0; i < length; i++) {
		if (this.enclosingProjectsAndJars[i].equals(path)) return;
	}
	System.arraycopy(
		this.enclosingProjectsAndJars,
		0,
		this.enclosingProjectsAndJars = new IPath[length+1],
		0,
		length);
	this.enclosingProjectsAndJars[length] = path;
}

public void add(JavaProject javaProject, int includeMask, HashSet visitedProjects) throws JavaModelException {
	IProject project = javaProject.getProject();
	if (!project.isAccessible() || !visitedProjects.add(project)) return;

	this.addEnclosingProjectOrJar(project.getFullPath());

	IClasspathEntry[] entries = javaProject.getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
	IJavaModel model = javaProject.getJavaModel();
	for (int i = 0, length = entries.length; i < length; i++) {
		IClasspathEntry entry = entries[i];
		switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
				IClasspathEntry rawEntry = null;
				JavaModelManager.PerProjectInfo perProjectInfo = javaProject.getPerProjectInfo();
				if (perProjectInfo != null && perProjectInfo.resolvedPathToRawEntries != null) {
					rawEntry = (IClasspathEntry) perProjectInfo.resolvedPathToRawEntries.get(entry.getPath());
				}
				if (rawEntry == null) break;
				switch (rawEntry.getEntryKind()) {
					case IClasspathEntry.CPE_LIBRARY:
					case IClasspathEntry.CPE_VARIABLE:
						if ((includeMask & APPLICATION_LIBRARIES) != 0) {
							IPath path = entry.getPath();
							add(path, true);
							addEnclosingProjectOrJar(path);
						}
						break;
					case IClasspathEntry.CPE_CONTAINER:
						IClasspathContainer container = JavaCore.getClasspathContainer(rawEntry.getPath(), javaProject);
						if (container == null) break;
						if ((container.getKind() == IClasspathContainer.K_APPLICATION && (includeMask & APPLICATION_LIBRARIES) != 0)
								|| (includeMask & SYSTEM_LIBRARIES) != 0) {
							IPath path = entry.getPath();
							add(path, true);
							addEnclosingProjectOrJar(path);
						}
						break;
				}
				break;
			case IClasspathEntry.CPE_PROJECT:
				if ((includeMask & REFERENCED_PROJECTS) != 0) {
					add((JavaProject) model.getJavaProject(entry.getPath().lastSegment()), includeMask, visitedProjects);
				}
				break;
			case IClasspathEntry.CPE_SOURCE:
				if ((includeMask & SOURCES) != 0) {
					add(entry.getPath(), true);
				}
				break;
		}
	}
}
public void add(IJavaElement element) throws JavaModelException {
	IPackageFragmentRoot root = null;
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			// a workspace sope should be used
			break; 
		case IJavaElement.JAVA_PROJECT:
			int includeMask = SOURCES | APPLICATION_LIBRARIES | SYSTEM_LIBRARIES;
			this.add((JavaProject)element, includeMask, new HashSet(2));
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			root = (IPackageFragmentRoot)element;
			this.add(root.getPath(), true);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			root = (IPackageFragmentRoot)element.getParent();
			if (root.isArchive()) {
				String relativePath = Util.concatWith(((PackageFragment) element).names, '/');
				this.add(root.getPath().append(new Path(relativePath)), false);
			} else {
				IResource resource = element.getResource();
				if (resource != null && resource.isAccessible()) {
					this.add(resource.getFullPath(), false);
				}
			}
			break;
		default:
			// remember sub-cu (or sub-class file) java elements
			if (element instanceof IMember) {
				if (this.elements == null) {
					this.elements = new ArrayList();
				}
				this.elements.add(element);
			}
			this.add(this.fullPath(element), true);
			
			// find package fragment root including this java element
			IJavaElement parent = element.getParent();
			while (parent != null && !(parent instanceof IPackageFragmentRoot)) {
				parent = parent.getParent();
			}
			if (parent instanceof IPackageFragmentRoot) {
				root = (IPackageFragmentRoot)parent;
			}
	}
	
	if (root != null) {
		if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
			this.addEnclosingProjectOrJar(root.getPath());
		} else {
			this.addEnclosingProjectOrJar(root.getJavaProject().getProject().getFullPath());
		}
	}
}

/**
 * Adds the given path to this search scope. Remember if subfolders need to be included as well.
 */
private void add(IPath path, boolean withSubFolders) {
	if (this.paths.length == this.pathsCount) {
		System.arraycopy(
			this.paths,
			0,
			this.paths = new IPath[this.pathsCount * 2],
			0,
			this.pathsCount);
		System.arraycopy(
			this.pathWithSubFolders,
			0,
			this.pathWithSubFolders = new boolean[this.pathsCount * 2],
			0,
			this.pathsCount);
	}
	this.paths[this.pathsCount] = path;
	this.pathWithSubFolders[this.pathsCount++] = withSubFolders; 
}

/* (non-Javadoc)
 * @see IJavaSearchScope#encloses(String)
 */
public boolean encloses(String resourcePathString) {
	IPath resourcePath;
	int separatorIndex = resourcePathString.indexOf(JAR_FILE_ENTRY_SEPARATOR);
	if (separatorIndex != -1) {
		resourcePath = 
			new Path(resourcePathString.substring(0, separatorIndex)).
				append(new Path(resourcePathString.substring(separatorIndex+1)));
	} else {
			resourcePath = new Path(resourcePathString);
	}
	return this.encloses(resourcePath);
}

/**
 * Returns whether this search scope encloses the given path.
 */
private boolean encloses(IPath path) {
	for (int i = 0; i < this.pathsCount; i++) {
		if (this.pathWithSubFolders[i]) {
			if (this.paths[i].isPrefixOf(path)) {
				return true;
			}
		} else {
			// if not looking at subfolders, this scope encloses the given path 
			// if this path is a direct child of the scope's ressource
			// or if this path is the scope's resource (see bug 13919 Declaration for package not found if scope is not project)
			IPath scopePath = this.paths[i];
			if (scopePath.isPrefixOf(path) 
				&& ((scopePath.segmentCount() == path.segmentCount() - 1)
					|| (scopePath.segmentCount() == path.segmentCount()))) {
				return true;
			}
		}
	}
	return false;
}

/* (non-Javadoc)
 * @see IJavaSearchScope#encloses(IJavaElement)
 */
public boolean encloses(IJavaElement element) {
	if (this.elements != null) {
		for (int i = 0, length = this.elements.size(); i < length; i++) {
			IJavaElement scopeElement = (IJavaElement)this.elements.get(i);
			IJavaElement searchedElement = element;
			while (searchedElement != null) {
				if (searchedElement.equals(scopeElement))
					return true;
				searchedElement = searchedElement.getParent();
			}
		}
		return false;
	}
	return this.encloses(this.fullPath(element));
}

/* (non-Javadoc)
 * @see IJavaSearchScope#enclosingProjectsAndJars()
 */
public IPath[] enclosingProjectsAndJars() {
	return this.enclosingProjectsAndJars;
}
private IPath fullPath(IJavaElement element) {
	if (element instanceof IPackageFragmentRoot) {
		return ((IPackageFragmentRoot)element).getPath();
	}
	IJavaElement parent = element.getParent();
	IPath parentPath = parent == null ? null : this.fullPath(parent);
	IPath childPath;
	if (element instanceof PackageFragment) {
		String relativePath = Util.concatWith(((PackageFragment) element).names, '/');
		childPath = new Path(relativePath);
	} else if (element instanceof IOpenable) {
		childPath = new Path(element.getElementName());
	} else {
		return parentPath;
	}
	return parentPath == null ? childPath : parentPath.append(childPath);
}

protected void initialize() {
	this.paths = new IPath[1];
	this.pathWithSubFolders = new boolean[1];
	this.pathsCount = 0;
	this.enclosingProjectsAndJars = new IPath[0];
}
/*
 * @see AbstractSearchScope#processDelta(IJavaElementDelta)
 */
public void processDelta(IJavaElementDelta delta) {
	switch (delta.getKind()) {
		case IJavaElementDelta.CHANGED:
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++) {
				IJavaElementDelta child = children[i];
				this.processDelta(child);
			}
			break;
		case IJavaElementDelta.REMOVED:
			IJavaElement element = delta.getElement();
			if (this.encloses(element)) {
				if (this.elements != null) {
					this.elements.remove(element);
				} 
				IPath path = null;
				switch (element.getElementType()) {
					case IJavaElement.JAVA_PROJECT:
						path = ((IJavaProject)element).getProject().getFullPath();
					case IJavaElement.PACKAGE_FRAGMENT_ROOT:
						if (path == null) {
							path = ((IPackageFragmentRoot)element).getPath();
						}
						int toRemove = -1;
						for (int i = 0; i < this.pathsCount; i++) {
							if (this.paths[i].equals(path)) {
								toRemove = i;
								break;
							}
						}
						if (toRemove != -1) {
							int last = this.pathsCount-1;
							if (toRemove != last) {
								this.paths[toRemove] = this.paths[last];
								this.pathWithSubFolders[toRemove] = this.pathWithSubFolders[last];
							}
							this.pathsCount--;
						}
				}
			}
			break;
	}
}
public String toString() {
	StringBuffer result = new StringBuffer("JavaSearchScope on "); //$NON-NLS-1$
	if (this.elements != null) {
		result.append("["); //$NON-NLS-1$
		for (int i = 0, length = this.elements.size(); i < length; i++) {
			JavaElement element = (JavaElement)this.elements.get(i);
			result.append("\n\t"); //$NON-NLS-1$
			result.append(element.toStringWithAncestors());
		}
		result.append("\n]"); //$NON-NLS-1$
	} else {
		if (this.pathsCount == 0) {
			result.append("[empty scope]"); //$NON-NLS-1$
		} else {
			result.append("["); //$NON-NLS-1$
			for (int i = 0; i < this.pathsCount; i++) {
				IPath path = this.paths[i];
				result.append("\n\t"); //$NON-NLS-1$
				result.append(path.toString());
			}
			result.append("\n]"); //$NON-NLS-1$
		}
	}
	return result.toString();
}

}
