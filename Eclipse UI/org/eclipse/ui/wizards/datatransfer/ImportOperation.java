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
package org.eclipse.ui.wizards.datatransfer;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import java.io.*;
import java.util.*;

/**
 * An operation which does the actual work of copying objects from the local file
 * system into the workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ImportOperation extends WorkspaceModifyOperation {
	private static final int POLICY_DEFAULT = 0;
	private static final int POLICY_SKIP_CHILDREN = 1;
	private static final int POLICY_FORCE_OVERWRITE = 2;
	
	private Object source;
	private IPath destinationPath;
	private IContainer destinationContainer;
	private List selectedFiles;
	private List rejectedFiles; 
	private IImportStructureProvider provider;
	private IProgressMonitor	monitor;
	protected IOverwriteQuery overwriteCallback;
	private Shell context;
	private List errorTable = new ArrayList();
	private boolean createContainerStructure = true;
	
	//The constants for the overwrite 3 state
	private static final int OVERWRITE_NOT_SET = 0;
	private static final int OVERWRITE_NONE = 1;	
	private static final int OVERWRITE_ALL = 2;	
	private int overwriteState = OVERWRITE_NOT_SET;
/**
 * Creates a new operation that recursively imports the entire contents of the
 * specified root file system object.
 * <p>
 * The <code>source</code> parameter represents the root file system object to 
 * import. All contents of this object are imported. Valid types for this parameter
 * are determined by the supplied <code>IImportStructureProvider</code>.
 * </p>
 * <p>
 * The <code>provider</code> parameter allows this operation to deal with the
 * source object in an abstract way. This operation calls methods on the provider
 * and the provider in turn calls specific methods on the source object.
 * </p>
 *  <p>
 * The default import behavior is to recreate the complete container structure
 * for the contents of the root file system object in their destination. 
 * If <code>setCreateContainerStructure</code> is set to false then the container 
 * structure created is relative to the root file system object.
 * </p>
 * 
 * @param containerPath the full path of the destination container within the
 *   workspace
 * @param source the root file system object to import
 * @param provider the file system structure provider to use
 * @param overwriteImplementor the overwrite strategy to use
 */
public ImportOperation(IPath containerPath, Object source, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor) {
	super();
	this.destinationPath = containerPath;
	this.source = source;
	this.provider = provider;
	overwriteCallback = overwriteImplementor;
}
/**
 * Creates a new operation that imports specific file system objects.
 * In this usage context, the specified source file system object is used by the
 * operation solely to determine the destination container structure of the file system
 * objects being imported.
 * <p>
 * The <code>source</code> parameter represents the root file system object to 
 * import. Valid types for this parameter are determined by the supplied 
 * <code>IImportStructureProvider</code>. The contents of the source which
 * are to be imported are specified in the <code>filesToImport</code>
 * parameter.
 * </p>
 * <p>
 * The <code>provider</code> parameter allows this operation to deal with the
 * source object in an abstract way. This operation calls methods on the provider
 * and the provider in turn calls specific methods on the source object.
 * </p>
 * <p>
 * The <code>filesToImport</code> parameter specifies what contents of the root
 * file system object are to be imported.
 * </p>
 * <p>
 * The default import behavior is to recreate the complete container structure
 * for the file system objects in their destination. If <code>setCreateContainerStructure</code>
 * is set to <code>false</code>, then the container structure created for each of 
 * the file system objects is relative to the supplied root file system object.
 * </p>
 *
 * @param containerPath the full path of the destination container within the
 *   workspace
 * @param source the root file system object to import from
 * @param provider the file system structure provider to use
 * @param overwriteImplementor the overwrite strategy to use
 * @param filesToImport the list of file system objects to be imported
 *  (element type: <code>Object</code>)
 */
public ImportOperation(IPath containerPath, Object source, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor, List filesToImport) {
	this(containerPath, source, provider, overwriteImplementor);
	setFilesToImport(filesToImport);
}
/**
 * Creates a new operation that imports specific file system objects.
 * <p>
 * The <code>provider</code> parameter allows this operation to deal with the
 * source object in an abstract way. This operation calls methods on the provider
 * and the provider in turn calls specific methods on the source object.
 * </p>
 * <p>
 * The <code>filesToImport</code> parameter specifies what file system objects 
 * are to be imported.
 * </p>
 * <p>
 * The default import behavior is to recreate the complete container structure
 * for the file system objects in their destination. If <code>setCreateContainerStructure</code>
 * is set to <code>false</code>, then no container structure is created for each of 
 * the file system objects.
 * </p>
 *
 * @param containerPath the full path of the destination container within the
 *   workspace
 * @param provider the file system structure provider to use
 * @param overwriteImplementor the overwrite strategy to use
 * @param filesToImport the list of file system objects to be imported
 *  (element type: <code>Object</code>)
 */
public ImportOperation(IPath containerPath, IImportStructureProvider provider, IOverwriteQuery overwriteImplementor, List filesToImport) {
	this(containerPath, null, provider, overwriteImplementor);
	setFilesToImport(filesToImport);
}
/**
 * Prompts if existing resources should be overwritten. Recursively collects
 * existing read-only files to overwrite and resources that should not be
 * overwritten.
 * 
 * @param destinationPath destination path to check for existing files
 * @param sources file system objects that may exist in the destination
 * @param noOverwrite files that were selected to be skipped (don't overwrite).
 * 	object type IPath
 * @param overwriteReadonly the collected existing read-only files to overwrite.
 * 	object type IPath
 */
void collectExistingReadonlyFiles(IPath destinationPath, List sources, ArrayList noOverwrite, ArrayList overwriteReadonly, int policy) {
	IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
	Iterator sourceIter = sources.iterator();
	IPath sourceRootPath = null;
	
	if (this.source != null) {
		sourceRootPath = new Path(provider.getFullPath(this.source));
	}
	while (sourceIter.hasNext()) {
		Object source = sourceIter.next();
		IPath sourcePath = new Path(provider.getFullPath(source));
		IPath newDestinationPath;
		IResource newDestination;
			
		if (sourceRootPath == null) {
			newDestinationPath = destinationPath.append(provider.getLabel(source));
		} else {
			int prefixLength = sourcePath.matchingFirstSegments(sourceRootPath);
			IPath relativeSourcePath = sourcePath.removeFirstSegments(prefixLength); 
			newDestinationPath = this.destinationPath.append(relativeSourcePath);
		}		
		newDestination = workspaceRoot.findMember(newDestinationPath);
		if (newDestination == null)
			continue;

		IFolder folder = getFolder(newDestination);
		if (folder != null) {
			if (policy != POLICY_FORCE_OVERWRITE){			
				if(this.overwriteState == OVERWRITE_NONE || !queryOverwrite(newDestinationPath)) {
					noOverwrite.add(folder);
					continue;
				}
			}			
			if (provider.isFolder(source))			
				collectExistingReadonlyFiles(
					newDestinationPath, 
					provider.getChildren(source), 
					noOverwrite, 
					overwriteReadonly, 
					POLICY_FORCE_OVERWRITE);
		}			
		else {
			IFile file = getFile(newDestination);
			
			if (file != null) {
				if (!queryOverwriteFile(file, policy))
					noOverwrite.add(file.getFullPath());
				else if (file.isReadOnly())
					overwriteReadonly.add(file);
			}	
		}
	}
} 
/**
 * Creates the folders that appear in the specified resource path.
 * These folders are created relative to the destination container.
 *
 * @param path the relative path of the resource
 * @return the container resource coresponding to the given path
 * @exception CoreException if this method failed
 */
IContainer createContainersFor(IPath path) throws CoreException {

	IContainer currentFolder = (IContainer) destinationContainer;
	
	int segmentCount = path.segmentCount();
	
	//No containers to create
	if(segmentCount == 0)
		return currentFolder;
	
	//Needs to be handles differently at the root
	if(currentFolder.getType() == IResource.ROOT)
		return createFromRoot(path);

	
	for (int i = 0; i < segmentCount; i++) {
		currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
		if (!currentFolder.exists()) 
			((IFolder) currentFolder).create(false,true,null);
	}

	return currentFolder;
}

/**
 * Creates the folders that appear in the specified resource path
 * assuming that the destinationContainer begins at the root. Do not create projects.
 *
 * @param path the relative path of the resource
 * @return the container resource coresponding to the given path
 * @exception CoreException if this method failed
 */
private IContainer createFromRoot(IPath path) throws CoreException {
		
	int segmentCount = path.segmentCount();
	
	//Assume the project exists 
	IContainer currentFolder = 
		((IWorkspaceRoot) destinationContainer).getProject(path.segment(0));
	
	for (int i = 1; i < segmentCount; i++) {
		currentFolder = currentFolder.getFolder(new Path(path.segment(i)));
		if (!currentFolder.exists()) 
			((IFolder) currentFolder).create(false,true,null);
	}

	return currentFolder;
}


/**
 * Deletes the given resource. If the resource fails to be deleted, adds a
 * status object to the list to be returned by <code>getResult</code>.
 *
 * @param resource the resource
 */
void deleteResource(IResource resource) {
	try {
		resource.delete(IResource.KEEP_HISTORY, null);
	} catch (CoreException e) {
		errorTable.add(e.getStatus());
	}
}
/* (non-Javadoc)
 * Method declared on WorkbenchModifyOperation.
 * Imports the specified file system objects from the file system.
 */
protected void execute(IProgressMonitor progressMonitor) {
	
	monitor = progressMonitor;

	try {
		if (selectedFiles == null) {
			//Set the amount to 1000 as we have no idea of how long this will take
			monitor.beginTask(DataTransferMessages.getString("DataTransfer.importTask"),1000); //$NON-NLS-1$
			ContainerGenerator generator = new ContainerGenerator(destinationPath);
			monitor.worked(30);
			validateFiles(Arrays.asList(new Object[] {source}));
			monitor.worked(50);
			destinationContainer = generator.generateContainer(new SubProgressMonitor(monitor, 50));
			importRecursivelyFrom(source, POLICY_DEFAULT);
			//Be sure it finishes
			monitor.worked(90);
		} else {
			// Choose twice the selected files size to take folders into account
			int creationCount = selectedFiles.size();
			monitor.beginTask(DataTransferMessages.getString("DataTransfer.importTask"),creationCount + 100); //$NON-NLS-1$
			ContainerGenerator generator = new ContainerGenerator(destinationPath);
			monitor.worked(30);
			validateFiles(selectedFiles);		
			monitor.worked(50);
			destinationContainer = generator.generateContainer(new SubProgressMonitor(monitor, 50));
			importFileSystemObjects(selectedFiles);
			monitor.done();
		}
	}
	catch (CoreException e) {
		errorTable.add(e.getStatus());
	}
	finally {
		monitor.done();
	}
}
/**
 * Returns the container resource that the passed file system object should be
 * imported into.
 *
 * @param fileSystemObject the file system object being imported
 * @return the container resource that the passed file system object should be
 *     imported into
 * @exception CoreException if this method failed
 */
IContainer getDestinationContainerFor(Object fileSystemObject) throws CoreException {
	IPath pathname = new Path(provider.getFullPath(fileSystemObject));
	
	if (createContainerStructure)
		return createContainersFor(pathname.removeLastSegments(1));
	else {
		if (source == fileSystemObject)
			return null;
		IPath sourcePath = new Path(provider.getFullPath(source));
		IPath destContainerPath = pathname.removeLastSegments(1);
		IPath relativePath = destContainerPath.removeFirstSegments(sourcePath.segmentCount()).setDevice(null);
		return createContainersFor(relativePath);
	}
}
/**
 * Returns the resource either casted to or adapted to an IFile. 
 * 
 * @param resource resource to cast/adapt
 * @return the resource either casted to or adapted to an IFile.
 * 	<code>null</code> if the resource does not adapt to IFile 
 */
IFile getFile(IResource resource) {
	if (resource instanceof IFile) {
		return (IFile) resource;
	}
	if (resource instanceof IAdaptable) {
		return (IFile) ((IAdaptable) resource).getAdapter(IFile.class);
	}
	return null;
}
/**
 * Returns the resource either casted to or adapted to an IFolder. 
 * 
 * @param resource resource to cast/adapt
 * @return the resource either casted to or adapted to an IFolder.
 * 	<code>null</code> if the resource does not adapt to IFolder 
 */
IFolder getFolder(IResource resource) {
	if (resource instanceof IFolder) {
		return (IFolder) resource;
	}
	if (resource instanceof IAdaptable) {
		return (IFolder) ((IAdaptable) resource).getAdapter(IFolder.class);
	}
	return null;
}
/**
 * Returns the rejected files based on the given multi status.
 *  
 * @param multiStatus multi status to use to determine file rejection
 * @param files source files
 * @return list of rejected files as absolute paths. Object type IPath.
 */
ArrayList getRejectedFiles(IStatus multiStatus, IFile[] files) {
	ArrayList rejectedFiles = new ArrayList();

	IStatus[] status = multiStatus.getChildren();
	for (int i = 0; i < status.length; i++) {
		if (status[i].isOK() == false) {
			rejectedFiles.add(files[i].getFullPath());
		}
	}
	return rejectedFiles;
}
/**
 * Returns the status of the import operation.
 * If there were any errors, the result is a status object containing
 * individual status objects for each error.
 * If there were no errors, the result is a status object with error code <code>OK</code>.
 *
 * @return the status
 */
public IStatus getStatus() {
	IStatus[] errors = new IStatus[errorTable.size()];
	errorTable.toArray(errors);
	return new MultiStatus(
		PlatformUI.PLUGIN_ID, 
		IStatus.OK, 
		errors,
		DataTransferMessages.getString("ImportOperation.importProblems"),  //$NON-NLS-1$
		null);
}
/**
 * Imports the specified file system object into the workspace.
 * If the import fails, adds a status object to the list to be returned by
 * <code>getResult</code>.
 *
 * @param fileObject the file system object to be imported
 * @param policy determines how the file object is imported
 */
void importFile(Object fileObject, int policy) {
	IContainer containerResource;
	try {
		containerResource = getDestinationContainerFor(fileObject);
	} catch (CoreException e) {
		IStatus coreStatus = e.getStatus();		
		String newMessage = DataTransferMessages.format("ImportOperation.coreImportError", new Object [] {fileObject, coreStatus.getMessage()}); //$NON-NLS-1$
		IStatus status = new Status(coreStatus.getSeverity(), coreStatus.getPlugin(), coreStatus.getCode(), newMessage, null);
		errorTable.add(status);
		return;
	}
		
	String fileObjectPath = provider.getFullPath(fileObject);
	monitor.subTask(fileObjectPath);
	IFile targetResource = containerResource.getFile(new Path(provider.getLabel(fileObject)));
	monitor.worked(1);
	
	if (rejectedFiles.contains(targetResource.getFullPath()))
		return;

	// ensure that the source and target are not the same
	IPath targetPath = targetResource.getLocation();
	// Use Files for comparison to avoid platform specific case issues
	if (targetPath != null && 
		(targetPath.toFile().equals(new File(fileObjectPath)))) {
		errorTable.add(
			new Status(
				IStatus.ERROR,
				PlatformUI.PLUGIN_ID,
				0,
				DataTransferMessages.format(
					"ImportOperation.targetSameAsSourceError", //$NON-NLS-1$
					new Object [] {fileObjectPath}), 
				null));
		return;		
	}

	InputStream contentStream = provider.getContents(fileObject);
	if (contentStream == null) {
		errorTable.add(
			new Status(
				IStatus.ERROR,
				PlatformUI.PLUGIN_ID,
				0,
				DataTransferMessages.format("ImportOperation.openStreamError", new Object [] {fileObjectPath}), //$NON-NLS-1$
				null));
		return;
	}
	
	try {
		if (targetResource.exists())
			targetResource.setContents(contentStream, IResource.KEEP_HISTORY, null);
		else
			targetResource.create(contentStream, false, null);
	} catch (CoreException e) {
		errorTable.add(e.getStatus());
	} finally {
		try {
			contentStream.close();
		} catch (IOException e) {
			errorTable.add(
				new Status(
					IStatus.ERROR,
					PlatformUI.PLUGIN_ID,
					0,
					DataTransferMessages.format("ImportOperation.closeStreamError", new Object [] {fileObjectPath}),  //$NON-NLS-1$
					e));
		}
	}
}
/**
 * Imports the specified file system objects into the workspace.
 * If the import fails, adds a status object to the list to be returned by
 * <code>getStatus</code>.
 *
 * @param filesToImport the list of file system objects to import
 *   (element type: <code>Object</code>)
 * @exception OperationCanceledException if canceled
 */
void importFileSystemObjects(List filesToImport) {
	Iterator filesEnum = filesToImport.iterator();
	while (filesEnum.hasNext()) {
		Object fileSystemObject = filesEnum.next();
		if (source == null) {
			// We just import what we are given into the destination
			IPath sourcePath = new Path(provider.getFullPath(fileSystemObject)).removeLastSegments(1);
			if (provider.isFolder(fileSystemObject) && sourcePath.isEmpty()) {
				// If we don't have a parent then we have selected the
				// file systems root. Roots can't copied (at least not
				// under windows).
				errorTable.add(
					new Status(
						IStatus.INFO,
						PlatformUI.PLUGIN_ID,
						0,
						DataTransferMessages.getString("ImportOperation.cannotCopy"), //$NON-NLS-1$
						null));
				continue;
			}
			source = sourcePath.toFile();
		}
		importRecursivelyFrom(fileSystemObject, POLICY_DEFAULT);
	}
}
/**
 * Imports the specified file system container object into the workspace.
 * If the import fails, adds a status object to the list to be returned by
 * <code>getResult</code>.
 *
 * @param fileObject the file system container object to be imported
 * @param policy determines how the folder object and children are imported
 * @return the policy to use to import the folder's children
 */
int importFolder(Object folderObject, int policy) {
	IContainer containerResource;
	try {
		containerResource = getDestinationContainerFor(folderObject);
	} catch (CoreException e) {
		errorTable.add(e.getStatus());
		return policy;
	}

	if (containerResource == null)
		return policy;

	monitor.subTask(provider.getFullPath(folderObject));
	IWorkspace workspace = destinationContainer.getWorkspace();
	IPath containerPath = containerResource.getFullPath();
	IPath resourcePath = containerPath.append(provider.getLabel(folderObject));

	// Do not attempt the import if the resource path is unchanged. This may happen
	// when importing from a zip file.
	if (resourcePath.equals(containerPath))
		return policy;

	if (workspace.getRoot().exists(resourcePath)) {
		if (rejectedFiles.contains(resourcePath))
			return POLICY_SKIP_CHILDREN;

		return POLICY_FORCE_OVERWRITE;
	}
		
	try {
		workspace.getRoot().getFolder(resourcePath).create(
			false,
			true,
			null);
	} catch (CoreException e) {
		errorTable.add(e.getStatus());
	}

	return policy;
}
/**
 * Imports the specified file system object recursively into the workspace.
 * If the import fails, adds a status object to the list to be returned by
 * <code>getStatus</code>.
 *
 * @param fileSystemObject the file system object to be imported
 * @param policy determines how the file system object and children are imported
 * @exception OperationCanceledException if canceled
 */
void importRecursivelyFrom(Object fileSystemObject, int policy) {
	if (monitor.isCanceled())
		throw new OperationCanceledException();
	
	if (!provider.isFolder(fileSystemObject)) {
		importFile(fileSystemObject, policy);
		return;
	}

	int childPolicy = importFolder(fileSystemObject, policy);
	if (childPolicy != POLICY_SKIP_CHILDREN) {
		Iterator children = provider.getChildren(fileSystemObject).iterator();
		while (children.hasNext())
			importRecursivelyFrom(children.next(), childPolicy);
	}
}
/**
 * Queries the user whether the resource with the specified path should be
 * overwritten by a file system object that is being imported.
 * 
 * @param path the workspace path of the resource that needs to be overwritten
 * @return <code>true</code> to overwrite, <code>false</code> to not overwrite
 * @exception OperationCanceledException if canceled
 */
boolean queryOverwrite(IPath resourcePath) throws OperationCanceledException {
	String overwriteAnswer = overwriteCallback.queryOverwrite(resourcePath.makeRelative().toString());

	if (overwriteAnswer.equals(IOverwriteQuery.CANCEL))
		throw new OperationCanceledException(DataTransferMessages.getString("DataTransfer.emptyString")); //$NON-NLS-1$
				
	if (overwriteAnswer.equals(IOverwriteQuery.NO)) {
		return false;
	}
	
	if (overwriteAnswer.equals(IOverwriteQuery.NO_ALL)){
		this.overwriteState = OVERWRITE_NONE;
		return false;
	}
			
	if (overwriteAnswer.equals(IOverwriteQuery.ALL))
		this.overwriteState = OVERWRITE_ALL;

	return true;
}
/**
 * Returns whether the given file should be overwritten.
 *
 * @param targetFile the file to ask to overwrite 
 * @param policy determines if the user is queried for overwrite 
 * @return <code>true</code> if the file should be overwritten, and
 * 	<code>false</code> if not.
 */
boolean queryOverwriteFile(IFile targetFile, int policy) {
	//If force overwrite is on don't bother
	if (policy != POLICY_FORCE_OVERWRITE){
		if(this.overwriteState == OVERWRITE_NOT_SET && 
			!queryOverwrite(targetFile.getFullPath()))
				return false;
		if(this.overwriteState == OVERWRITE_NONE)
			return false;	
	}
	return true;
}
/**
 * Sets the context for use by the VCM provider to prompt the user
 * for check-out of files.
 * 
 * @param shell context for use by the VCM provider to prompt user
 * 	for check-out. The user will not be prompted if set to <code>null</code>.
 * @see IWorkspace.validateEdit
 * @since 2.1
 */
public void setContext(Shell shell) {
	context = shell;
}
/**
 * Sets whether the containment structures that are implied from the full paths
 * of file system objects being imported should be duplicated in the workbench.
 *
 * @param value <code>true</code> if containers should be created, and
 *  <code>false</code> otherwise
 */
public void setCreateContainerStructure(boolean value) {
	createContainerStructure = value;
}
/**
 * Sets the file system objects to import.
 *
 * @param filesToImport the list of file system objects to be imported
 *   (element type: <code>Object</code>)
 */
public void setFilesToImport(List filesToImport) {
	this.selectedFiles = filesToImport;
}
/**
 * Sets whether imported file system objects should automatically overwrite
 * existing workbench resources when a conflict occurs.
 *
 * @param value <code>true</code> to automatically overwrite, and 
 *   <code>false</code> otherwise
 */
public void setOverwriteResources(boolean value) {
	if(value)
		this.overwriteState = OVERWRITE_ALL;
}
/**
 * Validates that the given source resources can be copied to the 
 * destination as decided by the VCM provider.
 * 
 * @param existingFiles existing files to validate
 * @return list of rejected files as absolute paths. Object type IPath.
 */
ArrayList validateEdit(List existingFiles) {
	ArrayList rejectedFiles = new ArrayList();
		
	if (existingFiles.size() > 0) {
		IFile[] files = (IFile[]) existingFiles.toArray(new IFile[existingFiles.size()]);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateEdit(files, context);
			
		if (status.isMultiStatus())
			rejectedFiles = getRejectedFiles(status, files);
		else if (!status.isOK())
			throw new OperationCanceledException(DataTransferMessages.getString("DataTransfer.emptyString")); //$NON-NLS-1$
	}
	return rejectedFiles;
}
/**
 * Validates the given file system objects.
 * The user is prompted to overwrite existing files.
 * Existing read-only files are validated with the VCM provider.
 * 
 * @param sourceFiles files to validate
 */
void validateFiles(List sourceFiles) {
	ArrayList noOverwrite = new ArrayList();
	ArrayList overwriteReadonly = new ArrayList();
	
	collectExistingReadonlyFiles(destinationPath, sourceFiles, noOverwrite, overwriteReadonly, POLICY_DEFAULT);
	rejectedFiles = validateEdit(overwriteReadonly);
	rejectedFiles.addAll(noOverwrite);
}
}
