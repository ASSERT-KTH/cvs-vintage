package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 

import org.eclipse.jdt.internal.compiler.parser.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalSymbols;

import org.eclipse.jdt.internal.compiler.util.CharOperation;

import java.util.StringTokenizer;

import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.core.runtime.*;
import java.io.File;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.core.resources.*;

/**
 * Provides methods for checking Java-specific conventions such as name syntax.
 * <p>
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public final class JavaConventions {
	private final static char fgDot= '.';
	private final static String fgJAVA= "JAVA"; //$NON-NLS-1$
/**
 * Not instantiable.
 */
private JavaConventions() {}
/**
 * Returns whether the given package fragment root paths are considered
 * to overlap.
 * <p>
 * Two root paths overlap if one is a prefix of the other, or they point to
 * the same location. However, a JAR is allowed to be nested in a root.
 *
 */
public static boolean isOverlappingRoots(IPath rootPath1, IPath rootPath2) {
	if (rootPath1 == null || rootPath2 == null) {
		return false;
	}
	String extension1 = rootPath1.getFileExtension();
	String extension2 = rootPath2.getFileExtension();
	String jarExtension = "JAR"; //$NON-NLS-1$
	String zipExtension = "ZIP"; //$NON-NLS-1$
	if (extension1 != null && (extension1.equalsIgnoreCase(jarExtension) || extension1.equalsIgnoreCase(zipExtension))) {
		return false;
	} 
	if (extension2 != null && (extension2.equalsIgnoreCase(jarExtension) || extension2.equalsIgnoreCase(zipExtension))) {
		return false;
	}
	return rootPath1.isPrefixOf(rootPath2) || rootPath2.isPrefixOf(rootPath1);
}
/**
 * Returns the current identifier extracted by the scanner (ie. without unicodes)
 * from the given id.
 * Returns <code>null</code> if the id was not valid.
 */
private static char[] scannedIdentifier(String id) {
	if (id == null) {
		return null;
	}
	String trimmed = id.trim();
	if (!trimmed.equals(id)) {
		return null;
	}
	try {
		Scanner scanner = new Scanner();
		scanner.setSourceBuffer(id.toCharArray());
		int token = scanner.getNextToken();
		char[] currentIdentifier;
		try {
			currentIdentifier = scanner.getCurrentIdentifierSource();
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
		int nextToken= scanner.getNextToken();
		if (token == TerminalSymbols.TokenNameIdentifier 
			&& nextToken == TerminalSymbols.TokenNameEOF
			&& scanner.startPosition == scanner.source.length) { // to handle case where we had an ArrayIndexOutOfBoundsException 
															     // while reading the last token
			return currentIdentifier;
		} else {
			return null;
		}
	}
	catch (InvalidInputException e) {
		return null;
	}
}
/**
 * Validate the given compilation unit name.
 * A compilation unit name must obey the following rules:
 * <ul>
 * <li> it must not be null
 * <li> it must include the <code>".java"</code> suffix
 * <li> its prefix must be a valid identifier
 * </ul>
 * </p>
 * @param name the name of a compilation unit
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given name is valid as a compilation unit name, otherwise a status 
 *		object indicating what is wrong with the name
 */
public static IStatus validateCompilationUnitName(String name) {
	if (name == null) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.unit.nullName"), null); //$NON-NLS-1$
	}
	String extension;
	String identifier;
	int index;
	index = name.indexOf('.');
	if (index == -1) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.unit.notJavaName"), null); //$NON-NLS-1$
	}
	identifier = name.substring(0, index);
	extension = name.substring(index + 1);
	IStatus status = validateIdentifier(identifier);
	if (!status.isOK()) {
		return status;
	}
	if (!Util.isJavaFileName(name)) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.unit.notJavaName"), null); //$NON-NLS-1$
	}
	return new Status(IStatus.OK, JavaCore.PLUGIN_ID, -1, "OK", null); //$NON-NLS-1$
}
/**
 * Validate the given field name.
 * <p>
 * Syntax of a field name corresponds to VariableDeclaratorId (JLS2 8.3).
 * For example, <code>"x"</code>.
 *
 * @param name the name of a field
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given name is valid as a field name, otherwise a status 
 *		object indicating what is wrong with the name
 */
public static IStatus validateFieldName(String name) {
	return validateIdentifier(name);
}
/**
 * Validate the given Java identifier.
 * The identifier must have the same spelling as a Java keyword,
 * boolean literal (<code>"true"</code>, <code>"false"</code>), or null literal (<code>"null"</code>).
 * See section 3.8 of the <em>Java Language Specification, Second Edition</em> (JLS2).
 * A valid identifier can act as a simple type name, method name or field name.
 *
 * @param id the Java identifier
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given identifier is a valid Java idetifier, otherwise a status 
 *		object indicating what is wrong with the identifier
 */
public static IStatus validateIdentifier(String id) {
	if (scannedIdentifier(id) != null) {
		return new Status(IStatus.OK, JavaCore.PLUGIN_ID, -1, "OK", null); //$NON-NLS-1$
	} else {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.illegalIdentifier", id), null); //$NON-NLS-1$
	}
}
/**
 * Validate the given import declaration name.
 * <p>
 * The name of an import corresponds to a fully qualified type name
 * or an on-demand package name as defined by ImportDeclaration (JLS2 7.5).
 * For example, <code>"java.util.*"</code> or <code>"java.util.Hashtable"</code>.
 *
 * @param name the import declaration
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given name is valid as an import declaration, otherwise a status 
 *		object indicating what is wrong with the name
 */
public static IStatus validateImportDeclaration(String name) {
	if (name == null || name.length() == 0) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.import.nullImport"), null); //$NON-NLS-1$
	} 
	if (name.charAt(name.length() - 1) == '*') {
		if (name.charAt(name.length() - 2) == '.') {
			return validatePackageName(name.substring(0, name.length() - 2));
		} else {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.import.unqualifiedImport"), null); //$NON-NLS-1$
		}
	}
	return validatePackageName(name);
}
/**
 * Validate the given Java type name, either simple or qualified.
 * For example, <code>"java.lang.Object"</code>, or <code>"Object"</code>.
 * <p>
 *
 * @param name the name of a type
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given name is valid as a Java type name, 
 *      a status with code <code>IStatus.WARNING</code>
 *		indicating why the given name is discouraged, 
 *      otherwise a status object indicating what is wrong with 
 *      the name
 */
public static IStatus validateJavaTypeName(String name) {
	if (name == null) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.nullName"), null); //$NON-NLS-1$
	}
	String trimmed = name.trim();
	if (!name.equals(trimmed)) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.nameWithBlanks"), null); //$NON-NLS-1$
	}
	int index = name.lastIndexOf('.');
	char[] scannedID;
	if (index == -1) {
		// simple name
		scannedID = scannedIdentifier(name);
	} else {
		// qualified name
		String pkg = name.substring(0, index).trim();
		IStatus status = validatePackageName(pkg);
		if (!status.isOK()) {
			return status;
		}
		String type = name.substring(index + 1).trim();
		scannedID = scannedIdentifier(type);
	}

	if (scannedID != null) {
		if (CharOperation.contains('$', scannedID)) {
			return new Status(IStatus.WARNING, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.dollarName"), null); //$NON-NLS-1$
		}
		if ((scannedID.length > 0 && Character.isLowerCase(scannedID[0]))) {
			return new Status(IStatus.WARNING, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.lowercaseName"), null); //$NON-NLS-1$
		}
		return new Status(IStatus.OK, JavaCore.PLUGIN_ID, -1, "OK", null); //$NON-NLS-1$
	} else {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.type.invalidName", name), null); //$NON-NLS-1$
	}
}
/**
 * Validate the given method name.
 * The special names "&lt;init&gt;" and "&lt;clinit&gt;" are not valid.
 * <p>
 * The syntax for a method  name is defined by Identifer
 * of MethodDeclarator (JLS2 8.4). For example "println".
 *
 * @param name the name of a method
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given name is valid as a method name, otherwise a status 
 *		object indicating what is wrong with the name
 */
public static IStatus validateMethodName(String name) {
	return validateIdentifier(name);
}
/**
 * Validate the given package name.
 * <p>
 * The syntax of a package name corresponds to PackageName as
 * defined by PackageDeclaration (JLS2 7.4). For example, <code>"java.lang"</code>.
 *
 * @param name the name of a package
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given name is valid as a package name, otherwise a status 
 *		object indicating what is wrong with the name
 */
public static IStatus validatePackageName(String name) {
	if (name == null) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.nullName"), null); //$NON-NLS-1$
	}
	int length;
	if ((length = name.length()) == 0) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.emptyName"), null); //$NON-NLS-1$
	}
	if (name.charAt(0) == fgDot || name.charAt(length-1) == fgDot) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.dotName"), null); //$NON-NLS-1$
	}
	if (Character.isWhitespace(name.charAt(0)) || Character.isWhitespace(name.charAt(name.length() - 1))) {
		return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.nameWithBlanks"), null);; //$NON-NLS-1$
	}
	int dot = 0;
	while (dot != -1 && dot < length-1) {
		if ((dot = name.indexOf(fgDot, dot+1)) != -1 && dot < length-1 && name.charAt(dot+1) == fgDot) {
			return new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("convention.package.consecutiveDotsName"), null); //$NON-NLS-1$
			}
	}
	StringTokenizer st = new StringTokenizer(name, new String(new char[] {fgDot}));
	while (st.hasMoreTokens()) {
		String typeName = st.nextToken();
		typeName = typeName.trim(); // grammar allows spaces
		IStatus status = validateIdentifier(typeName);
		if (!status.isOK()) {
			return status;
		}
	}
	return new Status(IStatus.OK, JavaCore.PLUGIN_ID, -1, "OK", null); //$NON-NLS-1$
}

/**
 * Validate the given classpath and output location.
 * - Source folders cannot be nested inside the binary output, and reciprocally. They can coincidate.
 * - Source folders cannot be nested in each other.
 * - Output location must be nested inside project.
 
 * @param classpath a given classpath
 * @param outputLocation a given output location
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given classpath and output location are compatible, otherwise a status 
 *		object indicating what is wrong with the classpath or output location
 */
public static IJavaModelStatus validateClasspath(IJavaProject javaProject, IClasspathEntry[] classpath, IPath outputLocation) {

	IProject project = javaProject.getProject();
	IPath projectPath= project.getFullPath();

	/* validate output location */
	if (outputLocation == null) {
		return new JavaModelStatus(IJavaModelStatusConstants.NULL_PATH);
	}
	if (outputLocation.isAbsolute()) {
		if (!projectPath.isPrefixOf(outputLocation)) {
			return new JavaModelStatus(IJavaModelStatusConstants.PATH_OUTSIDE_PROJECT, javaProject, outputLocation.toString());
		}
	} else {
		return new JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH, outputLocation);
	}

		
		
	// check if any source entries coincidates with binary output - in which case nesting inside output is legal
	boolean allowNestingInOutput = false;
	boolean hasSource = false;
	for (int i = 0 ; i < classpath.length; i++) {
		if (classpath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) hasSource = true;
		if (classpath[i].getPath().equals(outputLocation)){
			allowNestingInOutput = true;
			break;
		}
	}
	if (!hasSource) allowNestingInOutput = true; // if no source, then allowed
	
	// check all entries
	for (int i = 0 ; i < classpath.length; i++) {
		IClasspathEntry entry = classpath[i];
		IPath entryPath = entry.getPath();

		// no further check if entry coincidates with project or output location
		if (entryPath.equals(projectPath)) continue;
		if (entryPath.equals(outputLocation)) continue;
		
		// prevent nesting source entries in each other
		if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE){
			for (int j = 0; j < classpath.length; j++){
				IClasspathEntry otherEntry = classpath[j];
				if (entry != otherEntry && otherEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE){
					if (entryPath.isPrefixOf(otherEntry.getPath())){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestSourceFolderInSource",entryPath.toString(), otherEntry.getPath().toString())); //$NON-NLS-1$
					}
				}
			}
		}
		// prevent nesting output location inside entry
		if (entryPath.isPrefixOf(outputLocation)) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestSourceFolderInOutput",entryPath.toString(), outputLocation.toString())); //$NON-NLS-1$
		}

		// prevent nesting entry inside output location - when distinct from project or a source folder
		if (!allowNestingInOutput && outputLocation.isPrefixOf(entryPath)) {
			return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.cannotNestOuputInSourceFolder", outputLocation.toString(), entryPath.toString())); //$NON-NLS-1$
		}
	}
	return JavaModelStatus.VERIFIED_OK;	
}

	/**
	 * Returns a message describing the problem related to this classpath entry if any, or null if entry is fine 
	 * (i.e. if the given classpath entry denotes a valid element to be referenced onto a classpath).
	 */
	public static IJavaModelStatus validateClasspathEntry(IJavaProject javaProject, IClasspathEntry entry, boolean checkSourceAttachment){
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();			
		IPath path = entry.getPath();
		
		switch(entry.getEntryKind()){

			// variable entry check
			case IClasspathEntry.CPE_VARIABLE :
				if (path != null && path.segmentCount() >= 1){
					entry = JavaCore.getResolvedClasspathEntry(entry);
					if (entry == null){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundVariablePath", path.toString())); //$NON-NLS-1$
					}
					return validateClasspathEntry(javaProject, entry, checkSourceAttachment);
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalVariablePath", path.toString()));					 //$NON-NLS-1$
				}

			// library entry check
			case IClasspathEntry.CPE_LIBRARY :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IPath sourceAttachment = entry.getSourceAttachmentPath();
					Object target = JavaModel.getTarget(workspaceRoot, path, true);
					if (target instanceof IResource){
						IResource resolvedResource = (IResource) target;
						switch(resolvedResource.getType()){
							case IResource.FILE :
								String extension = resolvedResource.getFileExtension();
								if ("jar".equalsIgnoreCase(extension) || "zip".equalsIgnoreCase(extension)){ // internal binary archive //$NON-NLS-2$ //$NON-NLS-1$
									if (checkSourceAttachment 
										&& sourceAttachment != null
										&& !sourceAttachment.isEmpty()
										&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
										return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", sourceAttachment.toString())); //$NON-NLS-1$
									}
								}
								break;
							case IResource.FOLDER :	// internal binary folder
								if (checkSourceAttachment 
									&& sourceAttachment != null 
									&& !sourceAttachment.isEmpty()
									&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
									return  new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", sourceAttachment.toString())); //$NON-NLS-1$
								}
						}
					} else if (target instanceof File){
						if (checkSourceAttachment 
							&& sourceAttachment != null 
							&& !sourceAttachment.isEmpty()
							&& JavaModel.getTarget(workspaceRoot, sourceAttachment, true) == null){
							return  new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceAttachment", sourceAttachment.toString())); //$NON-NLS-1$
						}
					} else {
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundLibrary", path.toString())); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalLibraryPath", path.toString())); //$NON-NLS-1$
				}
				break;

			// project entry check
			case IClasspathEntry.CPE_PROJECT :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IProject project = workspaceRoot.getProject(path.segment(0));
					try {
						if (!project.exists() || !project.hasNature(JavaCore.NATURE_ID)){
							return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundProject", path.segment(0).toString())); //$NON-NLS-1$
						}
					} catch (CoreException e){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundProject", path.segment(0).toString())); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalProjectPath", path.segment(0).toString())); //$NON-NLS-1$
				}
				break;

			// project source folder
			case IClasspathEntry.CPE_SOURCE :
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IPath projectPath= javaProject.getProject().getFullPath();
					if (!projectPath.isPrefixOf(path) || JavaModel.getTarget(workspaceRoot, path, true) == null){
						return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.unboundSourceFolder", path.toString())); //$NON-NLS-1$
					}
				} else {
					return new JavaModelStatus(IJavaModelStatusConstants.INVALID_CLASSPATH, Util.bind("classpath.illegalSourceFolderPath", path.toString())); //$NON-NLS-1$
				}
				break;
		}
	return JavaModelStatus.VERIFIED_OK;		
}
}
