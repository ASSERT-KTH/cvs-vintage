package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * This class is used by <code>JavaModelManager</code> to convert
 * <code>IResourceDelta</code>s into <code>IJavaElementDelta</code>s.
 * It also does some processing on the <code>JavaElement</code>s involved
 * (e.g. closing them or updating classpaths).
 */
public class DeltaProcessor {
	

	
	/**
	 * The <code>JavaElementDelta</code> corresponding to the <code>IResourceDelta</code> being translated.
	 */
	protected JavaElementDelta fCurrentDelta;

	protected IndexManager indexManager =
		JavaModelManager.ENABLE_INDEXING ? new IndexManager() : null;
		
	/* A table from IPath (from a classpath entry) to IJavaProject */
	HashMap roots;
	
	/* A table from IPath (from a classpath entry) to HashSet of IJavaProject
	 * Used when an IPath corresponds to more than one root */
	HashMap otherRoots;
	
	/* The java element that was last created (see createElement(IResource). 
	 * This is used as a stack of java elements (using getParent() to pop it, and 
	 * using the various get*(...) to push it. */
	Openable currentElement;
	
	HashSet projectsToUpdate = new HashSet();

	public static boolean VERBOSE = false;

	/**
	 * Adds the given child handle to its parent's cache of children. 
	 */
	protected void addToParentInfo(Openable child) {

		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			try {
				JavaElementInfo info = parent.getElementInfo();
				info.addChild(child);
			} catch (JavaModelException e) {
				// do nothing - we already checked if open
			}
		}
	}



	/**
	 * Check whether the updated file is affecting some of the properties of a given project (like
	 * its classpath persisted as a file).
	 * 
	 * NOTE: It can induce resource changes, and cannot be called during POST_CHANGE notification.
	 *
	 */
	public static void checkProjectPropertyFileUpdate(
		IResourceDelta delta,
		IJavaElement parent) {

		IResource resource = delta.getResource();
		IJavaElement element = JavaCore.create(resource);

		boolean processChildren = false;

		switch (resource.getType()) {

			case IResource.ROOT :
				processChildren = true;
				break;
			case IResource.PROJECT :
				try {
					if (((IProject) resource).hasNature(JavaCore.NATURE_ID)) {
						processChildren = true;
					}
				} catch (CoreException e) {
				}
				break;
			case IResource.FILE :
				if (parent.getElementType() == IJavaElement.JAVA_PROJECT) {
					IFile file = (IFile) resource;
					JavaProject project = (JavaProject) parent;

					/* check classpath property file change */
					QualifiedName classpathProp;
					if (file.getName().equals(
							project.computeSharedPropertyFileName(
								classpathProp = project.getClasspathPropertyName()))) {

						switch (delta.getKind()) {
							case IResourceDelta.REMOVED : // recreate one based on in-memory path
								try {
									project.saveClasspath(false);
								} catch (JavaModelException e) {
								}
								break;
							case IResourceDelta.CHANGED :
								if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
									break; // only consider content change
							case IResourceDelta.ADDED :
								// check if any actual difference
								try {
									// force to (re)read the property file
									String fileClasspathString = project.getSharedProperty(classpathProp);
									if (fileClasspathString == null)
										break; // did not find the file
									IClasspathEntry[] fileEntries = project.readPaths(fileClasspathString);
									if (fileEntries == null)
										break; // could not read, ignore 
									if (project.isClasspathEqualsTo(fileEntries))
										break;

									// will force an update of the classpath/output location based on the file information
									// extract out the output location
									IPath outputLocation = null;
									if (fileEntries != null && fileEntries.length > 0) {
										IClasspathEntry entry = fileEntries[fileEntries.length - 1];
										if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
											outputLocation = entry.getPath();
											IClasspathEntry[] copy = new IClasspathEntry[fileEntries.length - 1];
											System.arraycopy(fileEntries, 0, copy, 0, copy.length);
											fileEntries = copy;
										}
									}
									// restore output location				
									if (outputLocation == null) {
										outputLocation = SetClasspathOperation.ReuseOutputLocation;
									}
									try {
										project.setRawClasspath(fileEntries, outputLocation, null, true, false, project.getExpandedClasspath(true));
									} catch (JavaModelException e) {
									}
								} catch (IOException e) {
									break;
								} catch (RuntimeException e) {
									break;
								} catch (CoreException e) {
									break;
								}

						}
					}
				}
				break;
		}
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++) {
				checkProjectPropertyFileUpdate(children[i], element);
			}
		}
	}

	/**
	 * Closes the given element, which removes it from the cache of open elements.
	 */
	protected static void close(Openable element) {

		try {
			element.close();
		} catch (JavaModelException e) {
			// do nothing
		}
	}

	/**
	 * Traverse an existing delta and close the affected compilation units.
	 */
	protected void closeAffectedElements(IResourceDelta delta) {

		Openable element = (Openable) JavaCore.create(delta.getResource());
		boolean processChildren = true;
		if (element != null) {
			int flags = delta.getFlags();
			switch (element.getElementType()) {
				case IJavaElement.CLASS_FILE :
				case IJavaElement.COMPILATION_UNIT :
					processChildren = false;
					switch (delta.getKind()) {
						case IResourceDelta.ADDED :
							break;
						case IResourceDelta.CHANGED :
							if ((flags & IResourceDelta.CONTENT) != 0) {
								try {
									element.close();
								} catch (JavaModelException e) {
								}
							}
							break;
						case IResourceDelta.REMOVED :
							try {
								element.close();
							} catch (JavaModelException e) {
							}
					}
			}
		}
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++) {
				closeAffectedElements(children[i]);
			}
		}
	}

	/**
	 * Generic processing for elements with changed contents:<ul>
	 * <li>The element is closed such that any subsequent accesses will re-open
	 * the element reflecting its new structure.
	 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
	 * </ul>
	 */
	protected void contentChanged(Openable element, IResourceDelta delta) {

		close(element);
		fCurrentDelta.changed(element, IJavaElementDelta.F_CONTENT);
	}

	/**
	 * Creates the openables corresponding to this resource.
	 * Returns null if none was found.
	 */
	protected Openable createElement(IResource resource, int elementType) {
		if (resource == null) return null;
		
		// find the element that encloses the resource
		IPath path = resource.getFullPath();
		this.popUntilPrefixOf(path);
		
		IJavaElement element = null;
		if (this.currentElement != null) {
			switch (elementType) {
				case IJavaElement.JAVA_PROJECT:
					element = JavaCore.create((IProject)resource);
					break;
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
					element = this.currentElement.getJavaProject().getPackageFragmentRoot(resource);
					break;
				case IJavaElement.PACKAGE_FRAGMENT:
					IPackageFragmentRoot root = this.currentElement.getPackageFragmentRoot();
					if (root != null && !Util.conflictsWithOutputLocation(path, (JavaProject)root.getJavaProject())) {
						IPath pkgPath = path.removeFirstSegments(root.getPath().segmentCount());
						String pkg = Util.packageName(pkgPath);
						if (pkg == null) return null;
						element = root.getPackageFragment(pkg);
					}
					break;
				case IJavaElement.COMPILATION_UNIT:
					IPackageFragment pkgFragment = null;
					switch (this.currentElement.getElementType()) {
						case IJavaElement.PACKAGE_FRAGMENT:
							pkgFragment = (IPackageFragment)this.currentElement;
							break;
						case IJavaElement.COMPILATION_UNIT:
						case IJavaElement.CLASS_FILE:
							pkgFragment = (IPackageFragment)this.currentElement.getParent();
							break;
					}
					if (pkgFragment != null) {
						String fileName = path.lastSegment();
						if (!Util.isValidCompilationUnitName(fileName)) return null;
						element = pkgFragment.getCompilationUnit(fileName);
					}
					break;
				case IJavaElement.CLASS_FILE:
					pkgFragment = null;
					switch (this.currentElement.getElementType()) {
						case IJavaElement.PACKAGE_FRAGMENT:
							pkgFragment = (IPackageFragment)this.currentElement;
							break;
						case IJavaElement.COMPILATION_UNIT:
						case IJavaElement.CLASS_FILE:
							pkgFragment = (IPackageFragment)this.currentElement.getParent();
							break;
					}
					if (pkgFragment != null) {
						String fileName = path.lastSegment();
						if (!Util.isValidClassFileName(fileName)) return null;
						element = pkgFragment.getClassFile(fileName);
					}
					break;
			}
		}
		if (element == null) {
			element = JavaCore.create(resource);
		}
		this.currentElement = (Openable)element;
		return this.currentElement;
	}

	/**
	 * Processing for an element that has been added:<ul>
	 * <li>If the element is a project, do nothing, and do not process
	 * children, as when a project is created it does not yet have any
	 * natures - specifically a java nature.
	 * <li>If the elemet is not a project, process it as added (see
	 * <code>basicElementAdded</code>.
	 * </ul>
	 */
	protected void elementAdded(Openable element, IResourceDelta delta) {
		int elementType = element.getElementType();
		if (elementType == IJavaElement.JAVA_PROJECT) {
			// project add is handled by JavaProject.configure() because
			// when a project is created, it does not yet have a java nature
			if (hasJavaNature(delta.getResource())) {
				addToParentInfo(element);
				fCurrentDelta.added(element);
			}
		} else {
			addToParentInfo(element);
			fCurrentDelta.added(element);
			
			switch (elementType) {
				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
					// when a root is added, and is on the classpath, the project must be updated
					this.projectsToUpdate.add(element.getJavaProject());
					element = (Openable)((IPackageFragmentRoot)element).getPackageFragment("");//$NON-NLS-1$
					// don't break as subpackages must be added too
				case IJavaElement.PACKAGE_FRAGMENT :
					// get rid of namelookup since it holds onto obsolete cached info 
					JavaProject project = (JavaProject) element.getJavaProject();
					try {
						project.getJavaProjectElementInfo().setNameLookup(null);
					} catch (JavaModelException e) {
					}
					// add subpackages
					PackageFragmentRoot root = element.getPackageFragmentRoot();
					String name = element.getElementName();
					IResourceDelta[] children = delta.getAffectedChildren();
					for (int i = 0, length = children.length; i < length; i++) {
						IResourceDelta child = children[i];
						IResource resource = child.getResource();
						if (resource instanceof IFolder) {
							String subpkgName = 
								name.length() == 0 ? 
									resource.getName() : 
									name + "." + resource.getName(); //$NON-NLS-1$
							Openable subpkg = (Openable)root.getPackageFragment(subpkgName);
							this.updateIndex(subpkg, child);
							this.elementAdded(subpkg, child);
						}
					}
					break;
			}
		}
	}

	/**
	 * Processing for the closing of an element - there are two cases:<ul>
	 * <li>when a project is closed (in the platform sense), the
	 * 		JavaModel reports this as if the JavaProject has been removed.
	 * <li>otherwise, the JavaModel reports this
	 *		as a the element being closed (CHANGED + F_CLOSED).
	 * </ul>
	 * <p>In both cases, the children of the element are not processed. When
	 * a resource is closed, the platform reports all children as removed. This
	 * would effectively delete the classpath if we processed children.
	 */
	protected void elementClosed(Openable element, IResourceDelta delta) {

		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			// treat project closing as removal
			elementRemoved(element, delta);
		} else {
			removeFromParentInfo(element);
			close(element);
			fCurrentDelta.closed(element);
		}
	}

	/**
	 * Processing for the opening of an element - there are two cases:<ul>
	 * <li>when a project is opened (in the platform sense), the
	 * 		JavaModel reports this as if the JavaProject has been added.
	 * <li>otherwise, the JavaModel reports this
	 *		as a the element being opened (CHANGED + F_CLOSED).
	 * </ul>
	 */
	protected void elementOpened(Openable element, IResourceDelta delta) {

		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			// treat project opening as addition
			if (hasJavaNature(delta.getResource())) {
				elementAdded(element, delta);
			}
		} else {
			addToParentInfo(element);
			fCurrentDelta.opened(element);
		}
	}

	/**
	 * Generic processing for a removed element:<ul>
	 * <li>Close the element, removing its structure from the cache
	 * <li>Remove the element from its parent's cache of children
	 * <li>Add a REMOVED entry in the delta
	 * </ul>
	 */
	protected void elementRemoved(Openable element, IResourceDelta delta) {
		
		close(element);
		removeFromParentInfo(element);
		fCurrentDelta.removed(element);

		switch (element.getElementType()) {
			case IJavaElement.JAVA_MODEL :
				element.getJavaModelManager().getIndexManager().reset();
				element.getJavaModelManager().fModelInfo = null;
				break;
			case IJavaElement.JAVA_PROJECT :
				JavaModelManager.getJavaModelManager().removePerProjectInfo(
					(JavaProject) element);
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT :
				this.projectsToUpdate.add(element.getJavaProject());
				element = (Openable)((IPackageFragmentRoot)element).getPackageFragment("");//$NON-NLS-1$
				// don't break so that subpackages are also removed
			case IJavaElement.PACKAGE_FRAGMENT :
				//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
				JavaProject project = (JavaProject) element.getJavaProject();
				try {
					project.getJavaProjectElementInfo().setNameLookup(null);
				} catch (JavaModelException e) {
				}
				// remove subpackages
				PackageFragmentRoot root = element.getPackageFragmentRoot();
				String name = element.getElementName();
				IResourceDelta[] children = delta.getAffectedChildren();
				for (int i = 0, length = children.length; i < length; i++) {
					IResourceDelta child = children[i];
					IResource resource = child.getResource();
					if (resource instanceof IFolder) {
						String subpkgName = 
							name.length() == 0 ? 
								resource.getName() : 
								name + "." + resource.getName(); //$NON-NLS-1$
						Openable subpkg = (Openable)root.getPackageFragment(subpkgName);
						this.updateIndex(subpkg, child);
						this.elementRemoved(subpkg, child);
					}
				}
				break;
		}
	}

	/**
	 * Filters the generated <code>JavaElementDelta</code>s to remove those
	 * which should not be fired (because they don't represent a real change
	 * in the Java Model).
	 */
	protected IJavaElementDelta[] filterRealDeltas(IJavaElementDelta[] deltas) {

		IJavaElementDelta[] realDeltas = new IJavaElementDelta[deltas.length];
		int index = 0;
		for (int i = 0; i < deltas.length; i++) {
			IJavaElementDelta delta = deltas[i];
			if (delta == null) {
				continue;
			}
			if (delta.getAffectedChildren().length > 0
				|| delta.getKind() != IJavaElementDelta.CHANGED
				|| (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_OPENED) != 0) {

				realDeltas[index++] = delta;
			}
		}
		IJavaElementDelta[] result = new IJavaElementDelta[index];
		if (result.length > 0) {
			System.arraycopy(realDeltas, 0, result, 0, result.length);
		}
		return result;
	}

	/**
	 * Returns true if the given resource is contained in an open project
	 * with a java nature, otherwise false.
	 */
	protected boolean hasJavaNature(IResource resource) {

		// ensure the project has a java nature (if open)
		IProject project = resource.getProject();
		if (project.isOpen()) {
			try {
				return project.hasNature(JavaCore.NATURE_ID);
			} catch (CoreException e) {
				// do nothing
			}
		}
		return false;
	}
private void initializeRoots() {
	this.roots = new HashMap();
	this.otherRoots = new HashMap();
	IJavaProject[] projects;
	try {
		projects = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProjects();
	} catch (JavaModelException e) {
		// nothing can be done
		return;
	}
	for (int i = 0, length = projects.length; i < length; i++) {
		IJavaProject project = projects[i];
		IClasspathEntry[] classpath;
		try {
			classpath = project.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			// continue with next project
			continue;
		}
		for (int j= 0, classpathLength = classpath.length; j < classpathLength; j++) {
			IClasspathEntry entry = classpath[j];
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) continue;
			IPath path = entry.getPath();
			if (this.roots.get(path) == null) {
				this.roots.put(path, project);
			} else {
				HashSet set = (HashSet)this.otherRoots.get(path);
				if (set == null) {
					set = new HashSet();
					this.otherRoots.put(path, set);
				}
				set.add(project);
			}
		}
	}
}

	/**
	 * Returns true if the given resource is considered open (in the
	 * platform sense), otherwise false.
	 */
	protected boolean isOpen(IResource resource) {

		IProject project = resource.getProject();
		if (project == null) {
			return true; // workspace is always open
		} else {
			return project.isOpen();
		}
	}

	/**
	 * Generic processing for elements with changed contents:<ul>
	 * <li>The element is closed such that any subsequent accesses will re-open
	 * the element reflecting its new structure.
	 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
	 * </ul>
	 */
	protected void nonJavaResourcesChanged(Openable element, IResourceDelta delta)
		throws JavaModelException {

		// reset non-java resources if element was open
		if (element.isOpen()) {
			JavaElementInfo info = element.getElementInfo();
			switch (element.getElementType()) {
				case IJavaElement.JAVA_PROJECT :
					IResource resource = delta.getResource();
					((JavaProjectElementInfo) info).setNonJavaResources(null);
	
					// if a package fragment root is the project, clear it too
					PackageFragmentRoot projectRoot =
						(PackageFragmentRoot) ((JavaProject) element).getPackageFragmentRoot(
							new Path(IPackageFragment.DEFAULT_PACKAGE_NAME));
					if (projectRoot.isOpen()) {
						((PackageFragmentRootInfo) projectRoot.getElementInfo()).setNonJavaResources(
							null);
					}
					break;
				case IJavaElement.PACKAGE_FRAGMENT :
					 ((PackageFragmentInfo) info).setNonJavaResources(null);
					break;
				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
					 ((PackageFragmentRootInfo) info).setNonJavaResources(null);
			}
		}

		JavaElementDelta elementDelta = fCurrentDelta.find(element);
		if (elementDelta == null) {
			fCurrentDelta.changed(element, IJavaElementDelta.F_CONTENT);
			elementDelta = fCurrentDelta.find(element);
		}
		elementDelta.addResourceDelta(delta);
	}
	private void popUntilPrefixOf(IPath path) {
		while (this.currentElement != null) {
			IPath currentElementPath = null;
			if (this.currentElement instanceof IPackageFragmentRoot) {
				currentElementPath = ((IPackageFragmentRoot)this.currentElement).getPath();
			} else {
				IResource currentElementResource = null;
				try {
					currentElementResource = this.currentElement.getUnderlyingResource();
				} catch (JavaModelException e) {
				}
				if (currentElementResource != null) {
					currentElementPath = currentElementResource.getFullPath();
				}
			}
			if (currentElementPath != null 
				&& currentElementPath.isPrefixOf(path)) {
					return;
			} else {
				this.currentElement = (Openable)this.currentElement.getParent();
			}
		}
	}

	/**
	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code> into
	 * the corresponding set of <code>IJavaElementDelta</code>, rooted in the
	 * relevant <code>JavaModel</code>s.
	 */
	public IJavaElementDelta[] processResourceDelta(IResourceDelta changes) {

		try {
			this.initializeRoots();
			
			// get the workspace delta, and start processing there.
			IResourceDelta[] deltas = changes.getAffectedChildren();
			IJavaElementDelta[] translatedDeltas = new JavaElementDelta[deltas.length];
			for (int i = 0; i < deltas.length; i++) {
				IResourceDelta delta = deltas[i];
				JavaModel model =
					JavaModelManager.getJavaModel(delta.getResource().getWorkspace());
				if (model != null) {
					fCurrentDelta = new JavaElementDelta(model);
					traverseDelta(delta, IJavaElement.JAVA_MODEL); // traverse delta
					translatedDeltas[i] = fCurrentDelta;
				}
			}
			
			// update package fragment roots of projects that were affected
			Iterator iterator = this.projectsToUpdate.iterator();
			while (iterator.hasNext()) {
				JavaProject project = (JavaProject)iterator.next();
				project.updatePackageFragmentRoots();
			}
	
			return filterRealDeltas(translatedDeltas);
		} finally {
			this.projectsToUpdate = new HashSet();
		}
	}
	
/*
 * Update the current delta (ie. add/remove/change the given element) and update the correponding index.
 * Returns whether the children of the given delta must be processed.
 */
private boolean updateCurrentDeltaAndIndex(IResourceDelta delta, int elementType) {
	Openable element;
	switch (delta.getKind()) {
		case IResourceDelta.ADDED :
			element = this.createElement(delta.getResource(), elementType);
			if (element == null) return false;
			updateIndex(element, delta);
			elementAdded(element, delta);
			return false;
		case IResourceDelta.REMOVED :
			element = this.createElement(delta.getResource(), elementType);
			if (element == null) return false;
			updateIndex(element, delta);
			elementRemoved(element, delta);
			return false;
		case IResourceDelta.CHANGED :
			int flags = delta.getFlags();
			if ((flags & IResourceDelta.CONTENT) != 0) {
				element = this.createElement(delta.getResource(), elementType);
				if (element == null) return false;
				updateIndex(element, delta);
				contentChanged(element, delta);
			} else if ((flags & IResourceDelta.OPEN) != 0) {
				element = this.createElement(delta.getResource(), elementType);
				if (element == null) return false;
				updateIndex(element, delta);
				if (isOpen(delta.getResource())) {
					elementOpened(element, delta);
				} else {
					elementClosed(element, delta);
				}
				return false; // when a project is open/closed don't process children
			}
			return true;
	}
	return true;
}

	/**
	 * Removes the given element from its parents cache of children. If the
	 * element does not have a parent, or the parent is not currently open,
	 * this has no effect. 
	 */
	protected void removeFromParentInfo(Openable child) {

		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			try {
				JavaElementInfo info = parent.getElementInfo();
				info.removeChild(child);
			} catch (JavaModelException e) {
				// do nothing - we already checked if open
			}
		}
	}

	/**
	 * Converts an <code>IResourceDelta</code> and its children into
	 * the corresponding <code>IJavaElementDelta</code>s.
	 * The classpath is passed along. If it is null, the resource
	 * is already known to be on the classpath, if it is UNKNOWN_CLASSPATH, then 
	 * it will be computed if the resource corresponds to a Java project.
	 * Return whether the delta corresponds to a resource on the classpath.
	 */
	protected boolean traverseDelta(IResourceDelta delta, int parentType) {

		IResource res = delta.getResource();
		
		// check if current resource is now on classpath and determine the element type
		boolean isOnClasspath = false;
		int elementType = -1;
		switch (parentType) {
			case IJavaElement.JAVA_MODEL:
			case IJavaElement.JAVA_PROJECT:
				isOnClasspath = this.isOnClasspath(res.getFullPath());
				if (isOnClasspath) {
					elementType = IJavaElement.PACKAGE_FRAGMENT_ROOT;
				} else {
					elementType = IJavaElement.JAVA_PROJECT; // not yet in a package fragment root
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT:
				isOnClasspath = true;
				if (res instanceof IFolder) {
					elementType = IJavaElement.PACKAGE_FRAGMENT;
				} else {
					String extension = res.getFileExtension();
					if ("java".equalsIgnoreCase(extension)) { //$NON-NLS-1$
						elementType = IJavaElement.COMPILATION_UNIT;
					} else if ("class".equalsIgnoreCase(extension)) { //$NON-NLS-1$
						elementType = IJavaElement.CLASS_FILE;
					}
				}
				break;
		}						
		
		boolean processChildren = true;
		if (isOnClasspath) {
			processChildren = this.updateCurrentDeltaAndIndex(delta, elementType);
		} else {
			if (res instanceof IProject) {
				try {
					if (this.isOpen(res) && !((IProject)res).hasNature(JavaCore.NATURE_ID)) return false; // non java project
				} catch (CoreException e) {
					return false;
				}
				processChildren = this.updateCurrentDeltaAndIndex(delta, elementType);
				if (delta.getKind() != IResourceDelta.CHANGED 
						|| (delta.getFlags() & IResourceDelta.OPEN) != 0) {
					return false; // don't go deeper for added, removed, opened or closed projects
				}
			} else {
				// not yet inside a package fragment root
				processChildren = true;
			}
		}
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			boolean oneChildOnClasspath = false;
			int length = children.length;
			IResourceDelta[] orphanChildren = new IResourceDelta[length];
			Openable element = null;
			for (int i = 0; i < length; i++) {
				IResourceDelta child = children[i];
				if (!traverseDelta(child, elementType)) {
					try {
						if (isOnClasspath) { 
							if (element == null) {
								element = this.createElement(res, elementType);
								if (element == null) continue;
							}
							// add child as non java resource if current element on classpath
							nonJavaResourcesChanged(element, child);
						} else {
							orphanChildren[i] = child;
						}
					} catch (JavaModelException e) {
					}
				} else {
					oneChildOnClasspath = true;
				}
			}
			if (oneChildOnClasspath || res instanceof IProject) {
				// add orphan children (case of non java resources under project)
				JavaProject project = (JavaProject)JavaCore.getJavaCore().create(res.getProject());
				if (project != null) {
					for (int i = 0; i < length; i++) {
						if (orphanChildren[i] != null) {
							try {
								nonJavaResourcesChanged(project, orphanChildren[i]);
							} catch (JavaModelException e) {
							}
						}
					}
				}
			} // else resource delta will be added by parent
			return isOnClasspath || oneChildOnClasspath;
		} else {
			return 
				isOnClasspath 
					&& (elementType == IJavaElement.COMPILATION_UNIT 
						|| elementType == IJavaElement.CLASS_FILE);
		}
	}
	
private boolean isOnClasspath(IPath path) {
	return this.roots.get(path) != null;
}

protected void updateIndex(Openable element, IResourceDelta delta) {

	try {		
		if (indexManager == null)
			return;

		switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT :
				switch (delta.getKind()) {
					case IResourceDelta.ADDED :
					case IResourceDelta.OPEN :
						indexManager.indexAll(element.getJavaProject().getProject());
						break;
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT :
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
					case IResourceDelta.CHANGED:
						if (element instanceof JarPackageFragmentRoot) {
							JarPackageFragmentRoot root = (JarPackageFragmentRoot)element;
							// index jar file only once (if the root is in its declaring project)
							if (root.getJavaProject().getProject().getFullPath().isPrefixOf(root.getPath())) {
								indexManager.indexJarFile(root.getPath(), root.getJavaProject().getElementName());
							}
						}
						break;
					case IResourceDelta.REMOVED:
						// keep index in case it is added back later in this session
						break;
				}
				// don't break as packages of the package fragment root can be indexed below
			case IJavaElement.PACKAGE_FRAGMENT :
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
					case IResourceDelta.REMOVED:
						IPackageFragment pkg = null;
						if (element instanceof IPackageFragmentRoot) {
							IPackageFragmentRoot root = (IPackageFragmentRoot)element;
							pkg = root.getPackageFragment(""); //$NON-NLS-1$
						} else {
							pkg = (IPackageFragment)element;
						}
						String name = pkg.getElementName();
						IResourceDelta[] children = delta.getAffectedChildren();
						for (int i = 0, length = children.length; i < length; i++) {
							IResourceDelta child = children[i];
							IResource resource = child.getResource();
							if (resource instanceof IFile) {
								String extension = resource.getFileExtension();
								if ("java".equalsIgnoreCase(extension)) { //$NON-NLS-1$
									Openable cu = (Openable)pkg.getCompilationUnit(resource.getName());
									this.updateIndex(cu, child);
								} else if ("class".equalsIgnoreCase(extension)) { //$NON-NLS-1$
									Openable classFile = (Openable)pkg.getClassFile(resource.getName());
									this.updateIndex(classFile, child);
								}
							}
						}
						break;
				}
				break;
			case IJavaElement.CLASS_FILE :
				IFile file = (IFile) element.getUnderlyingResource();
				IJavaProject project = element.getJavaProject();
				IResource binaryFolder;
				try {
					binaryFolder = element.getPackageFragmentRoot().getUnderlyingResource();
					// if the class file is part of the binary output, it has been created by
					// the java builder -> ignore
					if (binaryFolder.getFullPath().equals(project.getOutputLocation())) {
						break;
					}
				} catch (JavaModelException e) {
					break;
				}
				switch (delta.getKind()) {
					case IResourceDelta.CHANGED :
						// no need to index if the content has not changed
						if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
							break;
					case IResourceDelta.ADDED :
						if (file.isLocal(IResource.DEPTH_ZERO))
							indexManager.add(file, binaryFolder);
						break;
					case IResourceDelta.REMOVED :
						indexManager.remove(file.getFullPath().toString(), binaryFolder);
						break;
				}
				break;
			case IJavaElement.COMPILATION_UNIT :
				file = (IFile) delta.getResource();
				switch (delta.getKind()) {
					case IResourceDelta.CHANGED :
						// no need to index if the content has not changed
						if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
							break;
					case IResourceDelta.ADDED :
						if (file.isLocal(IResource.DEPTH_ZERO))
							indexManager.add(file, file.getProject());
						break;
					case IResourceDelta.REMOVED :
						indexManager.remove(file.getFullPath().toString(), file.getProject());
						break;
				}
		}
	} catch (CoreException e) {
		// ignore: index won't be updated
	}
}

}