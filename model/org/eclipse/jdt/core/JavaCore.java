package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.indexing.*;

/**
 * The plug-in runtime class for the Java model plug-in containing the core
 * (UI-free) support for Java projects.
 * <p>
 * Like all plug-in runtime classes (subclasses of <code>Plugin</code>), this
 * class is automatically instantiated by the platform when the plug-in gets
 * activated. Clients must not attempt to instantiate plug-in runtime classes
 * directly.
 * </p>
 * <p>
 * The single instance of this class can be accessed from any plug-in declaring
 * the Java model plug-in as a prerequisite via 
 * <code>JavaCore.getJavaCore()</code>. The Java model plug-in will be activated
 * automatically if not already active.
 * </p>
 */
public final class JavaCore extends Plugin implements IExecutableExtension {

	private static Plugin JAVA_CORE_PLUGIN = null; 
	/**
	 * The plug-in identifier of the Java core support
	 * (value <code>"org.eclipse.jdt.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.jdt.core" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java builder
	 * (value <code>"org.eclipse.jdt.core.javabuilder"</code>).
	 */
	public static final String BUILDER_ID = PLUGIN_ID + ".javabuilder" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java model
	 * (value <code>"org.eclipse.jdt.core.javamodel"</code>).
	 */
	public static final String MODEL_ID = PLUGIN_ID + ".javamodel" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java nature
	 * (value <code>"org.eclipse.jdt.core.javanature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * Java-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature
	 */
	public static final String NATURE_ID = PLUGIN_ID + ".javanature" ; //$NON-NLS-1$

	/**
	 * Name of the handle id attribute in a Java marker
	 */
	protected static final String ATT_HANDLE_ID =
		"org.eclipse.jdt.internal.core.JavaModelManager.handleId" ; //$NON-NLS-1$

	/**
	 * Creates the Java core plug-in.
	 */
	public JavaCore(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		JAVA_CORE_PLUGIN = this;
	}

	/**
	 * Adds the given listener for changes to Java elements.
	 * Has no effect if an identical listener is already registered.
	 *
	 * This listener will only be notified during the POST_CHANGE resource change notification
	 * and any reconcile operation (POST_RECONCILE).
	 * For finer control of the notification, use <code>addElementChangedListener(IElementChangedListener,int)</code>
	 * which allows to specify a different eventMask.
	 * 
	 * @see ElementChangeEvent
	 * @param listener the listener
	 */
	public static void addElementChangedListener(IElementChangedListener listener) {
		addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE | ElementChangedEvent.POST_RECONCILE);
	}

	/**
	 * Adds the given listener for changes to Java elements.
	 * Has no effect if an identical listener is already registered.
	 * After completion of this method, the given listener will be registered for exactly the
	 * the specified events.  If they were previously registered for other events, they
	 * will be deregistered.  
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to
	 * java elements in the model. The listener continues to receive 
	 * notifications until it is replaced or removed. 
	 * </p>
	 * <p>
	 * Listeners can listen for several types of event as defined in <code>ElementChangeEvent</code>.
	 * Clients are free to register for any number of event types however if they register
	 * for more than one, it is their responsibility to ensure they correctly handle the
	 * case where the same java element change shows up in multiple notifications.  
	 * Clients are guaranteed to receive only the events for which they are registered.
	 * </p>
	 * 
	 * @param listener the listener
	 * @param eventMask the bit-wise OR of all event types of interest to the listener
	 * @see IElementChangeListener
	 * @see ElementChangeEvent
	 * @see #removeElementChangeListener
	 *	@since 2.0
	 */
	public static void addElementChangedListener(IElementChangedListener listener, int eventMask) {
		JavaModelManager.getJavaModelManager().addElementChangedListener(listener, eventMask);
	}

	/**
	 * Configures the given marker attribute map for the given Java element.
	 * Used for markers which denote a Java element rather than a resource.
	 *
	 * @param attributes the mutable marker attribute map (key type: <code>String</code>,
	 *   value type: <code>String</code>)
	 * @param element the Java element for which the marker needs to be configured
	 */
	public static void addJavaElementMarkerAttributes(
		Map attributes,
		IJavaElement element) {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (attributes != null && element != null)
			attributes.put(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	
	/**
	 * Configures the given marker for the given Java element.
	 * Used for markers which denote a Java element rather than a resource.
	 *
	 * @param marker the marker to be configured
	 * @param element the Java element for which the marker needs to be configured
	 * @exception CoreException if the <code>IMarker.setAttribute</code> on the marker fails
	 */
	public void configureJavaElementMarker(IMarker marker, IJavaElement element)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (marker != null && element != null)
			marker.setAttribute(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	
	/**
	 * Returns the Java model element corresponding to the given handle identifier
	 * generated by <code>IJavaElement.getHandleIdentifier()</code>, or
	 * <code>null</code> if unable to create the associated element.
	 */
	public static IJavaElement create(String handleIdentifier) {
		if (handleIdentifier == null) {
			return null;
		}
		try {
			return JavaModelManager.getJavaModelManager().getHandleFromMemento(
				handleIdentifier);
		} catch (JavaModelException e) {
			return null;
		}
	}
	/**
	 * Returns the Java element corresponding to the given file, or
	 * <code>null</code> if unable to associate the given file
	 * with a Java element.
	 *
	 * <p>The file must be one of:<ul>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IFile file) {
		return JavaModelManager.create(file, null);
	}
	/**
	 * Returns the package fragment or package fragment root corresponding to the given folder, or
	 * <code>null</code> if unable to associate the given folder with a Java element.
	 * <p>
	 * Note that a package fragment root is returned rather than a default package.
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IFolder folder) {
		return JavaModelManager.create(folder, null);
	}
	/**
	 * Returns the Java project corresponding to the given project.
	 * <p>
	 * Creating a Java Project has the side effect of creating and opening all of the
	 * project's parents if they are not yet open.
	 * <p>
	 * Note that no check is done at this time on the existence or the java nature of this project.
	 */
	public static IJavaProject create(IProject project) {
		if (project == null) {
			return null;
		}
		JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
		return javaModel.getJavaProject(project);
	}
	/**
	 * Returns the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Java element.
	 * <p>
	 * The resource must be one of:<ul>
	 *	<li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *  <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
	 *			or <code>IPackageFragment</code></li>
	 *  <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IResource resource) {
		return JavaModelManager.create(resource, null);
	}
	/**
	 * Returns the Java model.
	 */
	public static IJavaModel create(IWorkspaceRoot root) {
		if (root == null) {
			return null;
		}
		return JavaModelManager.getJavaModelManager().getJavaModel();
	}
	/**
	 * Creates and returns a class file element for
	 * the given <code>.class</code> file. Returns <code>null</code> if unable
	 * to recognize the class file.
	 */
	public static IClassFile createClassFileFrom(IFile file) {
		return JavaModelManager.createClassFileFrom(file, null);
	}
	/**
	 * Creates and returns a compilation unit element for
	 * the given <code>.java</code> file. Returns <code>null</code> if unable
	 * to recognize the compilation unit.
	 */
	public static ICompilationUnit createCompilationUnitFrom(IFile file) {
		return JavaModelManager.createCompilationUnitFrom(file, null);
	}
	/**
	 * Creates and returns a handle for the given JAR file.
	 * The Java model associated with the JAR's project may be
	 * created as a side effect. 
	 * Returns <code>null</code> if unable to create a JAR package fragment root.
	 * (for example, if the JAR file represents a non-Java resource)
	 */
	public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile file) {
		return JavaModelManager.createJarPackageFragmentRootFrom(file, null);
	}

	/** 
	 * Answers the set of classpath entries corresponding to a given container for a specific project.
	 * Indeed, classpath containers can have a different meaning in different project, according to the behavior
	 * of the <code>ClasspathContainerInitializer</code> which got involved for resolving this container.
	 * In case this container path could not be resolved, then will answer <code>null</code>.
	 * <p>
	 * Both the container path and the project context are supposed to be non-null.
	 * <p>
	 * The set of entries associated with a classpath container may contain any of the following:
	 * <ul>
	 * <li> source entries (<code>CPE_SOURCE</code>) </li>
	 * <li> library entries (<code>CPE_LIBRARY</code>) </li>
	 * <li> project entries (<code>CPE_PROJECT</code>) </li>
	 * <li> variable entries (<code>CPE_VARIABLE</code>), note that these are not automatically resolved </li>
	 * </ul>
	 * A classpath container cannot reference further classpath containers.
	 * <p>
	 * @param containerPath - the name of the container which needs to be resolved
	 * @param changeScope - a specific project (IJavaProject) in which the container is being resolved
	 * 
	 * @exception JavaModelException if an exception occurred while resolving the container, or if the resolved container
	 *   contains illegal entries (further container entries or null entries).	 
	 * 
	 * @since 2.0
	 */
	public static IClasspathContainer getClasspathContainer(IPath containerPath, IJavaProject project) throws JavaModelException {

		Map projectContainers = (Map)JavaModelManager.Containers.get(project);
		if (projectContainers == null){
			projectContainers = new HashMap(1);
			JavaModelManager.Containers.put(project, projectContainers);
		}
		IClasspathContainer container = (IClasspathContainer)projectContainers.get(containerPath);

		if (container == JavaModelManager.ContainerInitializationInProgress) return null; // break cycle
		if (container == null){
			ClasspathContainerInitializer initializer = JavaModelManager.getClasspathContainerInitializer(containerPath.segment(0));
			if (initializer != null){
				projectContainers.put(containerPath, JavaModelManager.ContainerInitializationInProgress); // avoid initialization cycles
				boolean ok = false;
				try {
					initializer.initialize(containerPath, project);
					if (container != null){
						IClasspathEntry[] entries = container.getClasspathEntries();
						// validation - no nested classpath container
						if (entries != null){
							for (int i = 0; i < entries.length; i++){
								IClasspathEntry entry = entries[i];
								if (entry == null || entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER){
									throw new JavaModelException(
										new JavaModelStatus(
											IJavaModelStatusConstants.INVALID_CP_CONTAINER_ENTRY,
											containerPath.toString()));
								}
							}
						}
					}
					ok = true;
				} catch(CoreException e){
					throw new JavaModelException(e);
				} finally {
					if (!ok) JavaModelManager.Containers.put(project, null); // flush cache
				}
				if (container != null){
					projectContainers.put(containerPath, container);
				}
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.print("CPContainer INIT - after resolution: " + containerPath + " --> "); //$NON-NLS-2$//$NON-NLS-1$
					if (container != null){
						System.out.print("container: "+container.getDescription()+" {"); //$NON-NLS-2$//$NON-NLS-1$
						IClasspathEntry[] entries = container.getClasspathEntries();
						if (entries != null){
							for (int i = 0; i < entries.length; i++){
								if (i > 0) System.out.println(", ");//$NON-NLS-1$
								System.out.println(entries[i]);
							}
						}
						System.out.println("}");//$NON-NLS-1$
					} else {
						System.out.println("{unbound}");//$NON-NLS-1$
					}
				}
			}
		}
		return container;			
	}

	/**
	 * Returns the path held in the given classpath variable.
	 * Returns <node>null</code> if unable to bind.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Note that classpath variables can be contributed registered initializers for,
	 * using the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 *
	 * @param variableName the name of the classpath variable
	 * @return the path, or <code>null</code> if none 
	 * @see #setClasspathVariable
	 */
	public static IPath getClasspathVariable(String variableName) {

		IPath variablePath = (IPath) JavaModelManager.Variables.get(variableName);

		if (variablePath == JavaModelManager.VariableInitializationInProgress) return null; // break cycle
		
		if (variablePath == null){
			ClasspathVariableInitializer initializer = getClasspathVariableInitializer(variableName);
			if (initializer != null){
				JavaModelManager.Variables.put(variableName, JavaModelManager.VariableInitializationInProgress); // avoid initialization cycles
				initializer.initialize(variableName);
				variablePath = (IPath) JavaModelManager.Variables.get(variableName); // retry
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.println("CPVariable INIT - after initialization: " + variableName + " --> " + variablePath); //$NON-NLS-2$//$NON-NLS-1$
				}
			}
		}
		return variablePath;
	}

	/**
 	 * Retrieve the client classpath variable initializer registered for a given variable if any
 	 */
	private static ClasspathVariableInitializer getClasspathVariableInitializer(String variable){
		
		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null) return null;

		IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					IPluginDescriptor plugin = extension.getDeclaringPluginDescriptor();
					if (plugin.isPluginActivated()) {
						for(int j = 0; j < configElements.length; j++){
							try {
								String varAttribute = configElements[j].getAttribute("variable"); //$NON-NLS-1$
								if (variable.equals(varAttribute)) {
									if (JavaModelManager.CP_RESOLVE_VERBOSE) {
										System.out.println("CPVariable INIT - found initializer: "+variable+" --> " + configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
									}						
									Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
									if (execExt instanceof ClasspathVariableInitializer){
										return (ClasspathVariableInitializer)execExt;
									}
								}
							} catch(CoreException e){
							}
						}
					}
			}	
		}
		return null;
	}	
	
	/**
	 * Returns the names of all known classpath variables.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @return the list of classpath variable names
	 * @see #setClasspathVariable
	 */
	public static String[] getClasspathVariableNames() {
		int length = JavaModelManager.Variables.size();
		String[] result = new String[length];
		Iterator vars = JavaModelManager.Variables.keySet().iterator();
		int index = 0;
		while (vars.hasNext()) {
			result[index++] = (String) vars.next();
		}
		return result;
	}

	private static IPath getInstallLocation() {
		return new Path(getPlugin().getDescriptor().getInstallURL().getFile());
	}

	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 * Equivalent to <code>(JavaCore) getPlugin()</code>.
	 */
	public static JavaCore getJavaCore() {
		return (JavaCore) getPlugin();
	}
	/**
	 * Returns the <code>IJavaProject</code> associated with the
	 * given <code>IProject</code>, or <code>null</code> if the
	 * project does not have a Java nature.
	 */
	private IJavaProject getJavaProject(IProject project) {
		try {
			if (project.hasNature(NATURE_ID)) {
				JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
				if (model != null) {
					return model.getJavaProject(project);
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}
	
	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 */
	public static Plugin getPlugin() {
		return JAVA_CORE_PLUGIN;
	}

	/**
	 * This is a helper method which returns the resolved classpath entry denoted 
	 * by a given entry (if it is a variable entry). It is obtained by resolving the variable 
	 * reference in the first segment. Returns <node>null</code> if unable to resolve using 
	 * the following algorithm:
	 * <ul>
	 * <li> if variable segment cannot be resolved, returns <code>null</code></li>
	 * <li> finds a project, JAR or binary folder in the workspace at the resolved path location</li>
	 * <li> if none finds an external JAR file or folder outside the workspace at the resolved path location </li>
	 * <li> if none returns <code>null</code></li>
	 * </ul>
	 * <p>
	 * Variable source attachment path and root path are also resolved and recorded in the resulting classpath entry.
	 * <p>
	 * NOTE: This helper method does not handle classpath containers, for which should rather be used
	 * <code>JavaCore#getResolvedClasspathContainer(IPath, IJavaProject)</code>.
	 * <p>
	 * @return the resolved library or project classpath entry, or <code>null</code>
	 *   if the given variable entry could not be resolved to a valid classpath entry
	 */
	public static IClasspathEntry getResolvedClasspathEntry(IClasspathEntry entry) {

		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE)
			return entry;

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath resolvedPath = JavaCore.getResolvedVariablePath(entry.getPath());
		if (resolvedPath == null)
			return null;

		Object target = JavaModel.getTarget(workspaceRoot, resolvedPath, false);
		if (target == null)
			return null;

		// inside the workspace
		if (target instanceof IResource) {
			IResource resolvedResource = (IResource) target;
			if (resolvedResource != null) {
				switch (resolvedResource.getType()) {
					
					case IResource.PROJECT :  
						// internal project
						return JavaCore.newProjectEntry(resolvedPath, entry.isExported());
						
					case IResource.FILE : 
						String extension = resolvedResource.getFileExtension();
						if ("jar".equalsIgnoreCase(extension)  //$NON-NLS-1$
							 || "zip".equalsIgnoreCase(extension)) {  //$NON-NLS-1$
							// internal binary archive
							return JavaCore.newLibraryEntry(
									resolvedPath,
									getResolvedVariablePath(entry.getSourceAttachmentPath()),
									getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
									entry.isExported());
						}
						break;
						
					case IResource.FOLDER : 
						// internal binary folder
						return JavaCore.newLibraryEntry(
								resolvedPath,
								getResolvedVariablePath(entry.getSourceAttachmentPath()),
								getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
								entry.isExported());
				}
			}
		}
		// outside the workspace
		if (target instanceof File) {
			File externalFile = (File) target;
			if (externalFile.isFile()) {
				String fileName = externalFile.getName().toLowerCase();
				if (fileName.endsWith(".jar"  //$NON-NLS-1$
					) || fileName.endsWith(".zip"  //$NON-NLS-1$
					)) { // external binary archive
					return JavaCore.newLibraryEntry(
							resolvedPath,
							getResolvedVariablePath(entry.getSourceAttachmentPath()),
							getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
							entry.isExported());
				}
			} else { // external binary folder
				if (resolvedPath.isAbsolute()){
					return JavaCore.newLibraryEntry(
							resolvedPath,
							getResolvedVariablePath(entry.getSourceAttachmentPath()),
							getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
							entry.isExported());
				}
			}
		}
		return null;
	}


	/**
	 * Resolve a variable path (helper method)
	 */
	public static IPath getResolvedVariablePath(IPath variablePath) {

		if (variablePath == null)
			return null;
		int count = variablePath.segmentCount();
		if (count == 0)
			return null;

		// lookup variable	
		String variableName = variablePath.segment(0);
		IPath resolvedPath = JavaCore.getClasspathVariable(variableName);
		if (resolvedPath == null)
			return null;

		// append path suffix
		if (count > 1) {
			resolvedPath = resolvedPath.append(variablePath.removeFirstSegments(1));
		}
		return resolvedPath;
	}

	/**
	 * Returns whether the given marker references the given Java element.
	 * Used for markers which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param marker the marker
	 * @return <code>true</code> if the marker references the element
	 * @exception CoreException if the <code>IMarker.getAttribute</code> on the marker fails 	 
	 */
	public static boolean isReferencedBy(IJavaElement element, IMarker marker)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		return (
			element != null
				&& marker != null
				&& element.getHandleIdentifier().equals(marker.getAttribute(ATT_HANDLE_ID)));
	}

	/**
	 * Returns whether the given marker delta references the given Java element.
	 * Used for markers deltas which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param markerDelta the marker delta
	 * @return <code>true</code> if the marker delta references the element
	 * @exception CoreException if the  <code>IMarkerDelta.getAttribute</code> on the marker delta fails 	 
	 */
	public static boolean isReferencedBy(
		IJavaElement element,
		IMarkerDelta markerDelta)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		return element != null
			&& markerDelta != null
			&& element.getHandleIdentifier().equals(markerDelta.getAttribute(ATT_HANDLE_ID));
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_CONTAINER</code>
	 * for the given path. The path of the container will be used during resolution so as to map this
	 * container entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of libraries, projects and variable entries,
	 * which can be interpreted differently for each Java project where it is used.
	 * A classpath container entry can be resolved using <code>JavaCore#getResolvedClasspathContainer</code>,
	 * and updated with <code>JavaCore#classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be exactly formed of 2 segments, where: <ul>
	 * <li> the first segment is a unique ID identifying the target container, there must be a container initializer registered
	 * 	onto this ID through the extension point  "org.eclipse.jdt.core.classpathContainerInitializer". </li>
	 * <li> the second segment is a string which will be passed onto the initializer, and can be used as a clue during
	 * 	the resolution phase. </li>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 *    point="org.eclipse.jdt.core.classpathContainerInitializer">
	 *    <containerInitializer
	 *       id="MyProvidedJDK"
	 *       class="com.example.MyInitializer"/> 
	 * <p>
	 * Note that this operation does not attempt to validate classpath containers
	 * or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newContainerEntry(-,false)</code>.
	 * <p>
	 * @param containerPath - the path identifying the container, it must be formed of two
	 * 	segments
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getResolvedClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#classpathContainerChanged(IPath, IJavaElement, IProgressMonitor)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */
	public static IClasspathEntry newContainerEntry(IPath containerPath) {
			
		return newContainerEntry(containerPath, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_CONTAINER</code>
	 * for the given path. The path of the container will be used during resolution so as to map this
	 * container entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of libraries, projects and variable entries,
	 * which can be interpreted differently for each Java project where it is used.
	 * A classpath container entry can be resolved using <code>JavaCore#getResolvedClasspathContainer</code>,
	 * and updated with <code>JavaCore#classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be exactly formed of 2 segments, where: <ul>
	 * <li> the first segment is a unique ID identifying the target container, there must be a container initializer registered
	 * 	onto this ID through the extension point  "org.eclipse.jdt.core.classpathContainerInitializer". </li>
	 * <li> the second segment is a string which will be passed onto the initializer, and can be used as a clue during
	 * 	the initialization phase. </li>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 *    point="org.eclipse.jdt.core.classpathContainerInitializer">
	 *    <containerInitializer
	 *       id="MyProvidedJDK"
	 *       class="com.example.MyInitializer"/> 
	 * <p>
	 * Note that this operation does not attempt to validate classpath containers
	 * or access the resources at the given paths.
	 * <p>
	 * @param containerPath - the path identifying the container, it must be formed of two
	 * 	segments
	 * @param isExported - a boolean indicating whether this entry is contributed to dependent
	 *		projects in addition to the output location
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getResolvedClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#classpathContainerChanged(IPath, IJavaElement, IProgressMonitor)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */
	public static IClasspathEntry newContainerEntry(IPath containerPath, boolean isExported) {
			
		Assert.isTrue(
			containerPath != null && containerPath.segmentCount() == 2,
			Util.bind("classpath.illegalContainerPath" )); //$NON-NLS-1$
			
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_CONTAINER,
			containerPath,
			null,
			null,
			isExported);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_LIBRARY</code> for the 
	 * JAR or folder identified by the given absolute path. This specifies that all package fragments 
	 * within the root will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
	 * The target JAR or folder can either be defined internally to the workspace (absolute path relative
	 * to the workspace root) or externally to the workspace (absolute path in the file system).
	 * <p>
	 * e.g. Here are some examples of binary path usage<ul>
	 *	<li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code> - reference to an external JAR</li>
	 *	<li><code> "/Project/someLib.jar" </code> - reference to an internal JAR </li>
	 *	<li><code> "c:/classes/" </code> - reference to an external binary folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the 
	 * resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newLibraryEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @param sourceAttachmentPath the absolute path of the corresponding source archive, 
	 *    or <code>null</code> if none
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @return a new library classpath entry
	 * 
	 * @see #newLibraryEntry(IPath, IPath, IPath, boolean)
	 */
	public static IClasspathEntry newLibraryEntry(
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath) {
			
		return newLibraryEntry(path, sourceAttachmentPath, sourceAttachmentRootPath, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_LIBRARY</code> for the JAR or folder
	 * identified by the given absolute path. This specifies that all package fragments within the root 
	 * will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
	 * The target JAR or folder can either be defined internally to the workspace (absolute path relative
	 * to the workspace root) or externally to the workspace (absolute path in the file system).
	 *	<p>
	 * e.g. Here are some examples of binary path usage<ul>
	 *	<li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code> - reference to an external JAR</li>
	 *	<li><code> "/Project/someLib.jar" </code> - reference to an internal JAR </li>
	 *	<li><code> "c:/classes/" </code> - reference to an external binary folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the 
	 * resources at the given paths.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @param sourceAttachmentPath the absolute path of the corresponding source archive, 
	 *    or <code>null</code> if none
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new library classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newLibraryEntry(
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		boolean isExported) {
			
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
			
		return new ClasspathEntry(
			IPackageFragmentRoot.K_BINARY,
			IClasspathEntry.CPE_LIBRARY,
			JavaProject.canonicalizedPath(path),
			sourceAttachmentPath,
			sourceAttachmentRootPath,
			isExported);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
	 * contributes all its package fragment roots) or as binaries (when building, it contributes its 
	 * whole output location).
	 * <p>
	 * A project reference allows to indirect through another project, independently from its internal layout. 
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative to the workspace root.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newProjectEntry(_,false)</code>.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @return a new project classpath entry
	 * 
	 * @see JavaCore#newProjectEntry(IPath, boolean)
	 */
	public static IClasspathEntry newProjectEntry(IPath path) {
		return newProjectEntry(path, false);
	}
	
	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
	 * contributes all its package fragment roots) or as binaries (when building, it contributes its 
	 * whole output location).
	 * <p>
	 * A project reference allows to indirect through another project, independently from its internal layout. 
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative to the workspace root.
	 * <p>
	 * 
	 * @param path the absolute path of the prerequisite project
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new project classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newProjectEntry(IPath path, boolean isExported) {
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_PROJECT,
			path,
			null,
			null,
			isExported);
	}

	/**
	 * Returns a new empty region.
	 */
	public static IRegion newRegion() {
		return new Region();
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code> for the project's source folder 
	 * identified by the given absolute path. This specifies that all package fragments within the root will 
	 * have children of type <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the workspace root, e.g. <code>"/Project/src"</code>.
	 * </p>
	 * <p>
	 * A source entry is used to setup the internal source layout of a project, and cannot be used out of the
	 * context of the containing project (a source entry "Proj1/src" cannot be used on the classpath of Proj2).
	 * </p>
	 * <p>
	 * A particular source entry cannot be exported to other projects. All sources/binaries inside a project are
	 * contributed as a whole through a project entry (see <code>JavaCore.newProjectEntry</code>).
	 * </p>
	 * 
	 * @param path the absolute path of a source folder
	 * @return a new source classpath entry
	 */
	public static IClasspathEntry newSourceEntry(IPath path) {
		Assert.isTrue(
			path.isAbsolute(),
			Util.bind("classpath.needAbsolutePath" )); //$NON-NLS-1$
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_SOURCE,
			path,
			null,
			null,
			false);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_VARIABLE</code>
	 * for the given path. The first segment of the path is the name of a classpath variable.
	 * The trailing segments of the path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to other projects or libraries,
	 * depending on what the classpath variable is referring.
	 * <p>
	 *	It is possible to register an automatic initializer (<code>ClasspathVariableInitializer</code>)
	 * which will be invoked through the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * After resolution, a classpath variable entry may either correspond to a project or a library entry. </li>	 
	 * <p>
	 * e.g. Here are some examples of variable path usage<ul>
	 * <li><"JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "c:/jars/jdtcore.jar". The resoved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "/Project_JDTCORE". The resoved classpath entry is denoting the project "/Project_JDTCORE"</li>
	 * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
	 *      is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newVariableEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param variablePath the path of the binary archive; first segment is the
	 *   name of a classpath variable
	 * @param variableSourceAttachmentPath the path of the corresponding source archive, 
	 *    or <code>null</code> if none; if present, the first segment is the
	 *    name of a classpath variable (not necessarily the same variable
	 *    as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @return a new library classpath entry
	 * 
	 * @see JavaCore#newVariableEntry(IPath, IPath, IPath, boolean)
	 */
	public static IClasspathEntry newVariableEntry(
		IPath variablePath,
		IPath variableSourceAttachmentPath,
		IPath sourceAttachmentRootPath) {
		Assert.isTrue(
			variablePath != null && variablePath.segmentCount() >= 1,
			Util.bind("classpath.illegalVariablePath" )); //$NON-NLS-1$
		return newVariableEntry(variablePath, variableSourceAttachmentPath, sourceAttachmentRootPath, false);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_VARIABLE</code>
	 * for the given path. The first segment of the path is the name of a classpath variable.
	 * The trailing segments of the path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to other projects or libraries,
	 * depending on what the classpath variable is referring.
	 * <p>
	 *	It is possible to register an automatic initializer (<code>ClasspathVariableInitializer</code>)
	 * which will be invoked through the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * After resolution, a classpath variable entry may either correspond to a project or a library entry. </li>	 
	 * <p>
	 * e.g. Here are some examples of variable path usage<ul>
	 * <li><"JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "c:/jars/jdtcore.jar". The resoved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "/Project_JDTCORE". The resoved classpath entry is denoting the project "/Project_JDTCORE"</li>
	 * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
	 *      is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 *
	 * @param variablePath the path of the binary archive; first segment is the
	 *   name of a classpath variable
	 * @param variableSourceAttachmentPath the path of the corresponding source archive, 
	 *    or <code>null</code> if none; if present, the first segment is the
	 *    name of a classpath variable (not necessarily the same variable
	 *    as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new variable classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newVariableEntry(
		IPath variablePath,
		IPath variableSourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		boolean isExported) {
			
		Assert.isTrue(
			variablePath != null && variablePath.segmentCount() >= 1,
			Util.bind("classpath.illegalVariablePath" )); //$NON-NLS-1$
			
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_VARIABLE,
			variablePath,
			variableSourceAttachmentPath,
			sourceAttachmentRootPath,
			isExported);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @see #setClasspathVariable
	 *
	 * @deprecated - use version with extra IProgressMonitor
	 */
	public static void removeClasspathVariable(String variableName) {
		removeClasspathVariable(variableName, null);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param monitor the progress monitor to report progress
	 * @see #setClasspathVariable
	 */
	public static void removeClasspathVariable(
		String variableName,
		IProgressMonitor monitor) {

		try {
			updateVariableValues(new String[]{ variableName}, new IPath[]{ null }, monitor);
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Removes the given element changed listener.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 */
	public static void removeElementChangedListener(IElementChangedListener listener) {
		JavaModelManager.getJavaModelManager().removeElementChangedListener(listener);
	}

	/** TOFIX
	 * Notification of a classpath container change. This notification must be performed
	 * by client code which did register some classpath container initializers, whenever 
	 * changes in container need to be reflected onto the JavaModel.
	 * <p>
	 * In reaction to this notification, the JavaModel will be updated to reflect the new
	 * state of the updated container. Note that the update can be scoped to either
	 * a given project or the entire Java model according to the affectedElement argument.
	 * This is symetric to container resolution which enables project specific resolution.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath container states are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * When notifying a container change, the corresponding container initializer should
	 * in turn expect to be activated so as to resolve the updated container path.
	 * 
	 * @param containerPath - the name of the container which is being updated
	 * @param affectedElement - the scope of the change, either a specific project (IJavaProject)
	 *     or the entire JavaModel (IJavaModel).
	 * @param monitor a monitor to report progress
	 * 
	 * @see #getClasspathContainer(IPath, IJavaProject)
	 * @since 2.0
	 */
	public static void setClasspathContainer(IPath containerPath, IJavaProject[] affectedProjects, IClasspathContainer[] newContainers, IProgressMonitor monitor) throws JavaModelException {

		if (monitor != null && monitor.isCanceled()) return;

		int projectLength = affectedProjects.length;
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IClasspathEntry[][] oldResolvedPaths = new IClasspathEntry[projectLength][];

		// filter out unmodified project containers
		int remaining = 0;
		for (int i = 0; i < projectLength; i++){

			if (monitor != null && monitor.isCanceled()) return;

			IJavaProject affectedProject = affectedProjects[i];
			IClasspathContainer newContainer = newContainers[i];
			
			IClasspathEntry[] rawClasspath = affectedProject.getRawClasspath();
			boolean found = false;
			for (int j = 0, cpLength = rawClasspath.length; j <cpLength; j++) {
				IClasspathEntry entry = rawClasspath[j];
				if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().equals(containerPath)){
					found = true;
					break;
				}
			}
			if (!found){
				affectedProjects[i] = null; // filter out this project - does not reference the container path
				continue;
			}
			Map perProjectContainers = (Map)JavaModelManager.Containers.get(affectedProject);
			if (perProjectContainers == null){
				perProjectContainers = new HashMap();
				JavaModelManager.Containers.put(affectedProject, perProjectContainers);
			} else {
				IClasspathContainer oldContainer = (IClasspathContainer) perProjectContainers.get(containerPath);
				if (oldContainer != null && oldContainer.equals(newContainers[i])){
					affectedProjects[i] = null; // filter out this project - container did not change
					continue;
				}
			}
			remaining++;
			oldResolvedPaths[i] = affectedProject.getResolvedClasspath(true);
			perProjectContainers.put(containerPath, newContainer);
		}
		
		if (remaining == 0) return;
		
		// trigger model refresh
		boolean wasFiring = manager.isFiring();
		int count = 0;
		try {
			if (wasFiring)
				manager.stopDeltas();
				
			for(int i = 0; i < projectLength; i++){

				if (monitor != null && monitor.isCanceled()) return;

				JavaProject affectedProject = (JavaProject)affectedProjects[i];
				if (affectedProject == null) continue; // was filtered out
				
				if (++count == remaining) { // re-enable firing for the last operation
					if (wasFiring) {
						wasFiring = false;
						manager.startDeltas();
					}
				}
			
				// force a refresh of the affected project (will compute deltas)
				affectedProject.setRawClasspath(
						affectedProject.getRawClasspath(),
						SetClasspathOperation.ReuseOutputLocation,
						monitor,
						true,
						affectedProject.getWorkspace().isAutoBuilding(),
						oldResolvedPaths[i],
						remaining == 1); // no individual cycle check if more than 1 project
			}
			if (remaining > 1){
				try {
					// use workspace runnable so as to allow marker creation - workaround bug 14733
					ResourcesPlugin.getWorkspace().run(
						new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								JavaProject.updateAllCycleMarkers(); // update them all at once
							}
						}, 
						monitor);					
				} catch(CoreException e){
					throw new JavaModelException(e);
				}
			}
		} finally {
			if (wasFiring) {
				manager.startDeltas();
				// in case of exception traversing, deltas may be fired only in the next #fire() iteration
			}
		}
					
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must have at least one segment.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @see #getClasspathVariable
	 *
	 * @deprecated - use API with IProgressMonitor
	 */
	public static void setClasspathVariable(String variableName, IPath path)
		throws JavaModelException {

		setClasspathVariable(variableName, path, null);
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must not be null.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @param monitor a monitor to report progress
	 * @see #getClasspathVariable
	 */
	public static void setClasspathVariable(
		String variableName,
		IPath path,
		IProgressMonitor monitor)
		throws JavaModelException {

		Assert.isTrue(path != null, Util.bind("classpath.nullVariablePath" )); //$NON-NLS-1$
		setClasspathVariables(new String[]{variableName}, new IPath[]{ path }, monitor);
	}

	/**
	 * Sets the values of all the given classpath variables at once.
	 * Null paths can be used to request corresponding variable removal.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 * 
	 * @param variableNames - an array of names for the updated classpath variables
	 * @param variablePaths - an array of path updates for the modified classpath variables (null
	 *       meaning that the corresponding value will be removed
	 * @param monitor a monitor to report progress
	 * @see #getClasspathVariable
	 * @since 2.0
	 */
	public static void setClasspathVariables(
		String[] variableNames,
		IPath[] paths,
		IProgressMonitor monitor)
		throws JavaModelException {

		Assert.isTrue(variableNames.length == paths.length, Util.bind("classpath.mismatchNamePath" )); //$NON-NLS-1$
		updateVariableValues(variableNames, paths, monitor);
	}

	/* (non-Javadoc)
	 * Method declared on IExecutableExtension.
	 * Record any necessary initialization data from the plugin.
	 */
	public void setInitializationData(
		IConfigurationElement cfig,
		String propertyName,
		Object data)
		throws CoreException {
	}

	/**
	 * Shutdown the JavaCore plugin
	 * <p>
	 * De-registers the JavaModelManager as a resource changed listener and save participant.
	 * <p>
	 * @see Plugin#shutdown
	 */
	public void shutdown() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(JavaModelManager.getJavaModelManager());
		workspace.removeSaveParticipant(this);

		((JavaModelManager) JavaModelManager.getJavaModelManager()).shutdown();
	}

	/**
	 * Initiate the background indexing process.
	 * This should be deferred after the plugin activation.
	 */
	private void startIndexing() {

		JavaModelManager.getJavaModelManager().getIndexManager().reset();
	}

	/**
	 * Startup of the JavaCore plugin
	 * <p>
	 * Registers the JavaModelManager as a resource changed listener and save participant.
	 * Starts the background indexing, and restore saved classpath variable values.
	 * <p>
	 * @see Plugin#startup
	 */
	public void startup() {
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			manager.configurePluginDebugOptions();
			manager.loadVariables();
			IWorkspace workspace = ResourcesPlugin.getWorkspace();

			// need to initialize workspace now since a query may be done before indexing starts
			manager.getIndexManager().workspace = workspace;

			workspace.addResourceChangeListener(
				manager,
				IResourceChangeEvent.PRE_AUTO_BUILD
					| IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE);

			startIndexing();
			workspace.addSaveParticipant(this, manager);
			
		} catch (CoreException e) {
		} catch (RuntimeException e) {
			manager.shutdown();
			throw e;
		}
	}


	/**
	 * Internal updating of a variable values (null path meaning removal), allowing to change multiple variable values at once.
	 */
	private static void updateVariableValues(
		String[] variableNames,
		IPath[] variablePaths,
		IProgressMonitor monitor) throws JavaModelException {

		if (monitor != null && monitor.isCanceled()) return;
		
		boolean mayChangeProjectDependencies = false;
		int varLength = variableNames.length;
		
		// gather classpath information for updating
		HashMap affectedProjects = new HashMap(5);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IJavaModel model = manager.getJavaModel();

		// filter out unmodified variables
		int discardCount = 0;
		for (int i = 0; i < varLength; i++){
			IPath oldPath = (IPath)JavaModelManager.Variables.get(variableNames[i]);
			if (oldPath != null && oldPath.equals(variablePaths[i])){
				variableNames[i] = null;
				discardCount++;
			}
		}
		if (discardCount > 0){
			if (discardCount == varLength) return;
			int changedLength = varLength - discardCount;
			String[] changedVariableNames = new String[changedLength];
			IPath[] changedVariablePaths = new IPath[changedLength];
			for (int i = 0, index = 0; i < varLength; i++){
				if (variableNames[i] != null){
					changedVariableNames[index] = variableNames[i];
					changedVariablePaths[index] = variablePaths[i];
					index++;
				}
			}
			variableNames = changedVariableNames;
			variablePaths = changedVariablePaths;
			varLength = changedLength;
		}
		
		if (monitor != null && monitor.isCanceled()) return;

		if (model != null) {
			IJavaProject[] projects = model.getJavaProjects();
			nextProject : for (int i = 0, projectLength = projects.length; i < projectLength; i++){
				IJavaProject project = projects[i];
						
				// check to see if any of the modified variables is present on the classpath
				IClasspathEntry[] classpath = project.getRawClasspath();
				for (int j = 0, cpLength = classpath.length; j < cpLength; j++){
					
					IClasspathEntry entry = classpath[j];
					for (int k = 0; k < varLength; k++){

						String variableName = variableNames[k];						
						if (entry.getEntryKind() ==  IClasspathEntry.CPE_VARIABLE){

							if (entry.getPath().segment(0).equals(variableName)){
								affectedProjects.put(project, ((JavaProject)project).getResolvedClasspath(true));
								
								// also check whether it will be necessary to update proj references and cycle markers
								if (!mayChangeProjectDependencies && entry.getPath().segmentCount() ==  1){
									IPath oldPath = (IPath)JavaModelManager.Variables.get(variableName);
									if (oldPath != null && oldPath.segmentCount() == 1) {
										mayChangeProjectDependencies = true;
									} else {
										IPath newPath = variablePaths[k];
										if (newPath != null && newPath.segmentCount() == 1) {
											mayChangeProjectDependencies = true;
										}
									}
								}
								continue nextProject;
							}
							IPath sourcePath, sourceRootPath;
							if (((sourcePath = entry.getSourceAttachmentPath()) != null	&& sourcePath.segment(0).equals(variableName))
								|| ((sourceRootPath = entry.getSourceAttachmentRootPath()) != null	&& sourceRootPath.segment(0).equals(variableName))) {

								affectedProjects.put(project, ((JavaProject)project).getResolvedClasspath(true));
								continue nextProject;
							}
						}												
					}
				}
			}
		}
		// update variables
		for (int i = 0; i < varLength; i++){
			IPath path = variablePaths[i];
			if (path == null) {
				JavaModelManager.Variables.remove(variableNames[i]);
			} else {
				// new variable value is assigned
				JavaModelManager.Variables.put(variableNames[i], path);
			}
		}
				
		// update affected project classpaths
		int size = affectedProjects.size();
		if (!affectedProjects.isEmpty()) {
			boolean wasFiring = manager.isFiring();
			try {
				if (wasFiring)
					manager.stopDeltas();
				// propagate classpath change
				Iterator projectsToUpdate = affectedProjects.keySet().iterator();
				while (projectsToUpdate.hasNext()) {

					if (monitor != null && monitor.isCanceled()) return;

					JavaProject project = (JavaProject) projectsToUpdate.next();
					
					if (!projectsToUpdate.hasNext()) {
						// re-enable firing for the last operation
						if (wasFiring) {
							wasFiring = false;
							manager.startDeltas();
						}
					}
					project
						.setRawClasspath(
							project.getRawClasspath(),
							SetClasspathOperation.ReuseOutputLocation,
							monitor,
							true,
							project.getWorkspace().isAutoBuilding(),
							// force build if in auto build mode
							(IClasspathEntry[]) affectedProjects.get(project),
							size == 1 && mayChangeProjectDependencies); // no individual check if more than 1 project to update
				}
				if (size > 1 && mayChangeProjectDependencies){
					try {
						// TOFIX
						// use workspace runnable so as to allow marker creation - workaround bug 14733
						ResourcesPlugin.getWorkspace().run(
							new IWorkspaceRunnable() {
								public void run(IProgressMonitor monitor) throws CoreException {
									JavaProject.updateAllCycleMarkers(); // update them all at once
								}
							}, 
							monitor);					
					} catch(CoreException e){
						throw new JavaModelException(e);
					}
				}
			} finally {
				if (wasFiring) {
					manager.startDeltas();
					// in case of exception traversing, deltas may be fired only in the next #fire() iteration
				}
			}
		}
	}
	
	/**
	 * Sets the current table of options. All and only the options explicitly included in the given table 
	 * are remembered; all previous option settings are forgotten, including ones not explicitly
	 * mentioned.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param newOptions the new options (key type: <code>String</code>; value type: <code>String</code>),
	 *   or <code>null</code> to reset all options to their default values
	 * @see JavaCore#getDefaultOptions
	 */
	public static void setOptions(Hashtable newOptions) {
		if (newOptions == null){
			JavaModelManager.Options = getDefaultOptions();
		} else {
			JavaModelManager.Options = (Hashtable)newOptions.clone();
		}
	}

	/**
	 * Returns the table of the current options. Initially, all options have their default values,
	 * and this method returns a table that includes all known options.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @return table of current settings of all options 
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions
	 */
	public static Hashtable getOptions() {
		return (Hashtable)JavaModelManager.Options.clone();
	}

	/**
	 * Returns a table of all known configurable options with their default values.
	 * These options allow to configure the behavior of the underlying components.
	 * The client may safely use the result as a template that they can modify and
	 * then pass to <code>setOptions</code>.
	 * 
	 * Note: more options might be added in further releases.
	 * </pre>
	 * RECOGNIZED OPTIONS:
	 *  COMPILER / Generating Local Variable Debug Attribute
 	 *    When generated, this attribute will enable local variable names 
	 *    to be displayed in debugger, only in place where variables are 
	 *    definitely assigned (.class file is then bigger)
	 *     - option id:			"org.eclipse.jdt.core.compiler.debug.localVariable"
	 *     - possible values:	{ "generate", "do not generate" }
	 *     - default:			"generate"
	 *
	 *  COMPILER / Generating Line Number Debug Attribute 
	 *    When generated, this attribute will enable source code highlighting in debugger 
	 *    (.class file is then bigger).
	 *     - option id:			"org.eclipse.jdt.core.compiler.debug.lineNumber"
	 *     - possible values:	{ "generate", "do not generate" }
	 *     - default:			"generate"
	 *		
	 *  COMPILER / Generating Source Debug Attribute 
	 *    When generated, this attribute will enable the debugger to present the 
	 *    corresponding source code.
	 *     - option id:			"org.eclipse.jdt.core.compiler.debug.sourceFile"
	 *     - possible values:	{ "generate", "do not generate" }
	 *     - default:			"generate"
	 *		
	 *  COMPILER / Preserving Unused Local Variables
	 *    Unless requested to preserve unused local variables (i.e. never read), the 
	 *    compiler will optimize them out, potentially altering debugging
	 *     - option id:			"org.eclipse.jdt.core.compiler.codegen.unusedLocal"
	 *     - possible values:	{ "preserve", "optimize out" }
	 *     - default:			"preserve"
	 * 
	 *  COMPILER / Defining Target Java Platform
	 *    For binary compatibility reason, .class files can be tagged to with certain VM versions and later.
	 *    Note that "1.4" target require to toggle compliance mode to "1.4" too.
	 *     - option id:			"org.eclipse.jdt.core.compiler.codegen.targetPlatform"
	 *     - possible values:	{ "1.1", "1.2", "1.3", "1.4" }
	 *     - default:			"1.1"
	 *
	 *	COMPILER / Reporting Unreachable Code
	 *    Unreachable code can optionally be reported as an error, warning or simply 
	 *    ignored. The bytecode generation will always optimized it out.
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.unreachableCode"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"error"
	 *
	 *	COMPILER / Reporting Invalid Import
	 *    An import statement that cannot be resolved might optionally be reported 
	 *    as an error, as a warning or ignored.
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.invalidImport"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"error"
	 *
	 *	COMPILER / Reporting Attempt to Override Package-Default Method
	 *    A package default method is not visible in a different package, and thus 
	 *    cannot be overriden. When enabling this option, the compiler will signal 
	 *    such scenarii either as an error or a warning.
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"warning"
	 *
	 *  COMPILER / Reporting Method With Constructor Name
	 *    Naming a method with a constructor name is generally considered poor 
	 *    style programming. When enabling this option, the compiler will signal such 
	 *    scenarii either as an error or a warning.
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.methodWithConstructorName"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"warning"
	 *
	 *  COMPILER / Reporting Deprecation
	 *    When enabled, the compiler will signal use of deprecated API either as an 
	 *    error or a warning.
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.deprecation"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"warning"
	 *
	 *	COMPILER / Reporting Hidden Catch Block
	 *    Locally to a try statement, some catch blocks may hide others , e.g.
	 *      try {	throw new java.io.CharConversionException();
	 *      } catch (java.io.CharConversionException e) {
	 *      } catch (java.io.IOException e) {}. 
	 *    When enabling this option, the compiler will issue an error or a warning for hidden 
	 *    catch blocks corresponding to checked exceptions
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"warning"
	 *
	 *  COMPILER / Reporting Unused Local
	 *    When enabled, the compiler will issue an error or a warning for unused local 
	 *    variables (i.e. variables never read from)
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.unusedLocal"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"ignore"
	 *
	 *	COMPILER / Reporting Unused Parameter
	 *    When enabled, the compiler will issue an error or a warning for unused method 
	 *    parameters (i.e. parameters never read from)
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.unusedParameter"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"ignore"
	 *
	 *	COMPILER / Reporting Synthetic Access Emulation
	 *    When enabled, the compiler will issue an error or a warning whenever it emulates 
	 *    access to a non-accessible member of an enclosing type. Such access can have
	 *    performance implications.
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"ignore"
	 *
	 * COMPILER / Reporting Non-Externalized String Literal
	 *    When enabled, the compiler will issue an error or a warning for non externalized 
	 *    String literal (i.e. non tagged with //$NON-NLS-<n>$). 
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"ignore"
	 * 
	 * COMPILER / Reporting Usage of 'assert' Identifier
	 *    When enabled, the compiler will issue an error or a warning whenever 'assert' is 
	 *    used as an identifier (reserved keyword in 1.4)
	 *     - option id:			"org.eclipse.jdt.core.compiler.problem.assertIdentifier"
	 *     - possible values:	{ "error", "warning", "ignore" }
	 *     - default:			"ignore"
	 * 
	 * COMPILER / Setting Source Compatibility Mode
	 *    Specify whether source is 1.3 or 1.4 compatible. From 1.4 on, 'assert' is a keyword
	 *    reserved for assertion support. Also note, than when toggling to 1.4 mode, the target VM
	 *   level should be set to "1.4" and the compliance mode should be "1.4".
	 *     - option id:			"org.eclipse.jdt.core.compiler.source"
	 *     - possible values:	{ "1.3", "1.4" }
	 *     - default:			"1.3"
	 * 
	 * COMPILER / Setting Compliance Level
	 *    Select the compliance level for the compiler. In "1.3" mode, source and target settings
	 *    should not go beyond "1.3" level.
	 *     - option id:			"org.eclipse.jdt.core.compiler.compliance"
	 *     - possible values:	{ "1.3", "1.4" }
	 *     - default:			"1.3"
	 * 
	 * BUILDER / Specifying Filters for Resource Copying Control
	 *    Allow to specify some filters to control the resource copy process.
	 *   Note: no trimming of the names is performed, the list should thus not contain any superfluous
	 *              character.
	 *     - option id:			"org.eclipse.jdt.core.builder.resourceCopyExclusionFilter"
	 *     - possible values:	{ "<name>[,<name>]* } where <name> is a file name pattern (only * wild-cards allowed)
	 *     - default:			""
	 * 
	 * BUILDER / Abort  if Invalid Classpath
	 *    Allow to toggle the builder to abort if the classpath is invalid
	 *     - option id:			"org.eclipse.jdt.core.builder.invalidClasspath"
	 *     - possible values:	{ "abort", "ignore" }
	 *     - default:			"ignore"
	 * 
	 *	JAVACORE / Computing Project Build Order
	 *    Indicate whether JavaCore should enforce the project build order to be based on
	 *    the classpath prerequisite chain. When requesting to compute, this takes over
	 *    the platform default order (based on project references).
	 *     - option id:			"org.eclipse.jdt.core.computeJavaBuildOrder"
	 *     - possible values:	{ "compute", "ignore" }
	 *     - default:			"ignore"	 
	 * 
	 * JAVACORE / Specify Default Source Encoding Format
	 *    Select the encoding format for compiled sources.
	 *     - option id:			"org.eclipse.jdt.core.encoding"
	 *     - possible values:	{ "" for platform default, or any of the supported encoding name}.
	 *     - default:			""
	 * 
	 *	FORMATTER / Inserting New Line Before Opening Brace
	 *    When Insert, a new line is inserted before an opening brace, otherwise nothing
	 *    is inserted
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.openingBrace"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"do not insert"
	 * 
	 *	FORMATTER / Inserting New Line Inside Control Statement
	 *    When Insert, a new line is inserted between } and following else, catch, finally
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.controlStatement"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"do not insert"
	 * 
	 *	FORMATTER / Clearing Blank Lines
	 *    When Clear all, all blank lines are removed. When Preserve one, only one is kept
	 *    and all others removed.
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.clearAll"
	 *     - possible values:	{ "clear all", "preserve one" }
	 *     - default:			"preserve one"
	 * 
	 *	FORMATTER / Inserting New Line Between Else/If 
	 *    When Insert, a blank line is inserted between an else and an if when they are 
	 *    contiguous. When choosing to not insert, else-if will be kept on the same
	 *    line when possible.
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.elseIf"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"do not insert"
	 * 
	 *	FORMATTER / Inserting New Line In Empty Block
	 *    When insert, a line break is inserted between contiguous { and }, if } is not followed
	 *    by a keyword.
	 *     - option id:			"org.eclipse.jdt.core.formatter.newline.emptyBlock"
	 *     - possible values:	{ "insert", "do not insert" }
	 *     - default:			"insert"
	 * 
	 *	FORMATTER / Splitting Lines Exceeding Length
	 *    Enable splitting of long lines (exceeding the configurable length). Length of 0 will
	 *    disable line splitting
	 *     - option id:			"org.eclipse.jdt.core.formatter.lineSplit"
	 *     - possible values:	"<n>", where n is zero or a positive integer
	 *     - default:			"80"
	 * 
	 *	FORMATTER / Compacting Assignment
	 *    Assignments can be formatted asymmetrically, e.g. 'int x= 2;', when Normal, a space
	 *    is inserted before the assignment operator
	 *     - option id:			"org.eclipse.jdt.core.formatter.style.assignment"
	 *     - possible values:	{ "compact", "normal" }
	 *     - default:			"normal"
	 * 
	 *	FORMATTER / Defining Indentation Character
	 *    Either choose to indent with tab characters or spaces
	 *     - option id:			"org.eclipse.jdt.core.formatter.tabulation.char"
	 *     - possible values:	{ "tab", "space" }
	 *     - default:			"tab"
	 * 
	 *	FORMATTER / Defining Space Indentation Length
	 *    When using spaces, set the amount of space characters to use for each 
	 *    indentation mark.
	 *     - option id:			"org.eclipse.jdt.core.formatter.tabulation.size"
	 *     - possible values:	"<n>", where n is a positive integer
	 *     - default:			"4"
	 * 
	 *	CODEASSIST / Activate Visibility Sensitive Completion
	 *    When active, completion doesn't show that you can not see
	 *    (eg. you can not see private methods of a super class).
	 *     - option id:			"org.eclipse.jdt.core.codeComplete.visibilityCheck"
	 *     - possible values:	{ "enabled", "disabled" }
	 *     - default:			"disabled"
	 * 
	 *	CODEASSIST / Automatic Qualification of Implicit Members
	 *    When active, completion automatically qualifies completion on implicit
	 *    field references and message expressions.
	 *     - option id:			"org.eclipse.jdt.core.codeComplete.forceImplicitQualification"
	 *     - possible values:	{ "enabled", "disabled" }
	 *     - default:			"disabled"
	 * </pre>
	 * 
	 * @return a mutable table containing the default settings of all known options
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see #setOptions
	 */
 	public static Hashtable getDefaultOptions(){
	
		Hashtable defaultOptions = new Hashtable(10);
	
		// Compiler settings
		defaultOptions.put("org.eclipse.jdt.core.compiler.debug.localVariable", "generate"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.debug.lineNumber", "generate"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.debug.sourceFile", "generate"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.codegen.unusedLocal", "preserve"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.1"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.unreachableCode", "error"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.invalidImport", "error"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod", "warning"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.methodWithConstructorName", "warning"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.deprecation", "warning"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock", "warning"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.unusedLocal", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.unusedParameter", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.problem.assertIdentifier", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.source", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$

		// Builder settings
		defaultOptions.put("org.eclipse.jdt.core.builder.resourceCopyExclusionFilter", ""); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.builder.invalidClasspath", "ignore");  //$NON-NLS-1$ //$NON-NLS-2$
		
		// JavaCore settings
		defaultOptions.put("org.eclipse.jdt.core.computeJavaBuildOrder", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.encoding", ""); //$NON-NLS-1$ //$NON-NLS-2$
	
		// Formatter settings
		defaultOptions.put("org.eclipse.jdt.core.formatter.newline.openingBrace", "do not insert"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.newline.controlStatement", "do not insert"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.newline.clearAll", "preserve one"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.newline.elseIf", "do not insert"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.newline.emptyBlock", "insert"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.lineSplit", "80"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.style.assignment", "normal"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.tabulation.char", "tab"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.formatter.tabulation.size", "4"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// CodeAssist settings
		defaultOptions.put("org.eclipse.jdt.core.codeComplete.visibilityCheck", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
		defaultOptions.put("org.eclipse.jdt.core.codeComplete.forceImplicitQualification", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return defaultOptions;
	}


	/**
	 * Names of recognized configurable options
	 */
	
	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_LOCAL_VARIABLE_ATTR = PLUGIN_ID + ".compiler.debug.localVariable"; //$NON-NLS-1$
		// possible values are GENERATE or DO_NOT_GENERATE (default is DO_NOT_GENERATE)
		
	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_LINE_NUMBER_ATTR = PLUGIN_ID + ".compiler.debug.lineNumber"; //$NON-NLS-1$
		// possible values are  GENERATE or DO_NOT_GENERATE (default is GENERATE)
		
	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_SOURCE_FILE_ATTR = PLUGIN_ID + ".compiler.debug.sourceFile"; //$NON-NLS-1$
		// possible values are  GENERATE or DO_NOT_GENERATE (default is GENERATE)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_CODEGEN_UNUSED_LOCAL = PLUGIN_ID + ".compiler.codegen.unusedLocal"; //$NON-NLS-1$
		// possible values are PRESERVE or OPTIMIZE_OUT	(default is OPTIMIZE_OUT)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_CODEGEN_TARGET_PLATFORM = PLUGIN_ID + ".compiler.codegen.targetPlatform"; //$NON-NLS-1$
		// possible values are VERSION_1_1 or VERSION_1_2	(default is VERSION_1_1)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_UNREACHABLE_CODE = PLUGIN_ID + ".compiler.problem.unreachableCode"; //$NON-NLS-1$
		// possible values are ERROR or WARNING	(default is ERROR)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_INVALID_IMPORT = PLUGIN_ID + ".compiler.problem.invalidImport"; //$NON-NLS-1$
		// possible values are ERROR or WARNING	(default is ERROR)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD = PLUGIN_ID + ".compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$
		// possible values are WARNING or IGNORE (default is WARNING)
		
	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME = PLUGIN_ID + ".compiler.problem.methodWithConstructorName"; //$NON-NLS-1$
		// possible values are WARNING or IGNORE (default is WARNING)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_DEPRECATION = PLUGIN_ID + ".compiler.problem.deprecation"; //$NON-NLS-1$
		// possible values are WARNING or IGNORE (default is WARNING)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_HIDDEN_CATCH_BLOCK = PLUGIN_ID + ".compiler.problem.hiddenCatchBlock"; //$NON-NLS-1$
		// possible values are WARNING or IGNORE (default is WARNING)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_UNUSED_LOCAL = PLUGIN_ID + ".compiler.problem.unusedLocal"; //$NON-NLS-1$
		// possible values are WARNING or IGNORE (default is WARNING)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER = PLUGIN_ID + ".compiler.problem.unusedParameter"; //$NON-NLS-1$
		// possible values are WARNING or IGNORE (default is WARNING)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPILER_PB_SYNTHETIC_ACCESS_EMULATION = PLUGIN_ID + ".compiler.problem.syntheticAccessEmulation"; //$NON-NLS-1$
		// possible values are WARNING or IGNORE (default is IGNORE)

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String CORE_JAVA_BUILD_ORDER = PLUGIN_ID + ".computeJavaBuildOrder"; //$NON-NLS-1$
		// possible values are COMPUTE or IGNORE (default is COMPUTE)

	/**
	 * Possible values for configurable options
	 */
	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String GENERATE = "generate"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String DO_NOT_GENERATE = "do not generate"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String PRESERVE = "preserve"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String OPTIMIZE_OUT = "optimize out"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String ERROR = "error"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String WARNING = "warning"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String IGNORE = "ignore"; //$NON-NLS-1$

	/** 
	 * @deprecated - use string value directly
	 * @see JavaCore#getDefaultOptions
	 */
	public static final String COMPUTE = "compute"; //$NON-NLS-1$
}