package org.eclipse.jdt.internal.core.index.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.index.*;

/**
 * An indexedFile associates a number to a document path, and document properties. 
 * It is what we add into an index, and the result of a query.
 */

public class IndexedFile implements IQueryResult {
	protected String path;
	protected int fileNumber;
	protected static final String INFO_BEGIN= "("; //$NON-NLS-1$
	protected static final String INFO_END= ")"; //$NON-NLS-1$
	protected static final String INFO_SEPARATOR= ","; //$NON-NLS-1$
	protected static final String INFO_VALUE_SEPARATOR= ":"; //$NON-NLS-1$
	protected static final int MAX_PROPERTIES_SIZE= 2 * 1024;
	protected Hashtable properties;
	protected int propertiesSize= 2 * (INFO_BEGIN.length() + INFO_END.length());

	public IndexedFile(String pathOrInfo, int fileNum) {
		if (fileNum < 1)
			throw new IllegalArgumentException();
		this.fileNumber= fileNum;
		properties= new Hashtable();
		int dp= pathOrInfo.indexOf(INFO_BEGIN);
		if (dp == -1)
			path= pathOrInfo;
		else {
			String fileInfo= pathOrInfo;
			path= fileInfo.substring(0, dp);
			String props= fileInfo.substring(dp, fileInfo.length());
			StringTokenizer t= new StringTokenizer(props.substring(1, props.length() - 1), INFO_SEPARATOR);
			while (t.hasMoreTokens()) {
				String g= t.nextToken();
				try {
					int dpt= g.indexOf(INFO_VALUE_SEPARATOR);
					setProperty(g.substring(0, dpt), g.substring(dpt + 1, g.length()));
				} catch (Exception e) {
				}
			}
		}
	}
	public IndexedFile(IDocument document, int fileNum) {
		if (fileNum < 1)
			throw new IllegalArgumentException();
		this.path= document.getName();
		this.fileNumber= fileNum;
		properties= new Hashtable();
		computeProperties(document);
	}
	protected void computeProperties(IDocument document) {
		for (Enumeration e= document.getPropertyNames(); e.hasMoreElements();) {
			String property= (String) e.nextElement();
			setProperty(property, document.getProperty(property));
		}
	}
	/**
	 * Returns the path represented by pathString converted back to a path relative to the local file system.
	 *
	 * @parame pathString the path to convert:
	 * <ul>
	 *		<li>an absolute IPath (relative to the workspace root) if the path represents a resource in the 
	 *			workspace
	 *		<li>a relative IPath (relative to the workspace root) followed by JAR_FILE_ENTRY_SEPARATOR
	 *			followed by an absolute path (relative to the jar) if the path represents a .class file in
	 *			an internal jar
	 *		<li>an absolute path (relative to the file system) followed by JAR_FILE_ENTRY_SEPARATOR
	 *			followed by an absolute path (relative to the jar) if the path represents a .class file in
	 *			an external jar
	 * </ul>
	 * @return the converted path:
	 * <ul>
	 *		<li>the original pathString if the path represents a resource in the workspace
	 *		<li>an absolute path (relative to the file system) followed by JAR_FILE_ENTRY_SEPARATOR
	 *			followed by an absolute path (relative to the jar) if the path represents a .class file in
	 *			an external or internal jar
	 * </ul>
	 */
	public static String convertPath(String pathString) {
		int index = pathString.indexOf(JarFileEntryDocument.JAR_FILE_ENTRY_SEPARATOR);
		if (index == -1)
			return pathString;
			
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Path jarPath = new Path(pathString.substring(0, index));
		if (!jarPath.isAbsolute()) {
			return jarPath.makeAbsolute().toString() + pathString.substring(index, pathString.length());
		} else {
			return jarPath.toOSString() + pathString.substring(index, pathString.length());
		}
	}
	/**
	 * Returns the size of the indexedFile.
	 */
	public int footprint() {
		//object+ 2 slots + size of the string
		return 8 + (2 * 4) + (28 + path.length() * 2);
	}
	/**
	 * Returns the file number.
	 */
	public int getFileNumber() {
		return fileNumber;
	}
	/**
	 * Returns the path.
	 */
	public String getPath() {
		return path;
	}
	public String getProperty(String propertyName) {
		return (String) properties.get(propertyName);
		;
	}
	/**
	 * getPropertyNames method comment.
	 */
	public Enumeration getPropertyNames() {
		return properties.keys();
	}
	public String propertiesToString() {
		if (properties.isEmpty())
			return ""; //$NON-NLS-1$
		StringBuffer prop= new StringBuffer(INFO_BEGIN);
		for (Enumeration e= getPropertyNames(); e.hasMoreElements();) {
			String property= (String) e.nextElement();
			String value= getProperty(property);
			prop.append(property);
			prop.append(INFO_VALUE_SEPARATOR);
			prop.append(value);
			if (e.hasMoreElements())
				prop.append(INFO_SEPARATOR);
		}
		prop.append(INFO_END);
		return prop.toString();
	}
	/**
	 * Sets the file number.
	 */
	public void setFileNumber(int fileNumber) {
		this.fileNumber= fileNumber;
	}
	/**
	 * getPropertyNames method comment.
	 */
	public void setProperty(String propertyName, String value) {
		propertiesSize += (INFO_SEPARATOR.length() + propertyName.length() + INFO_VALUE_SEPARATOR.length() + value.length()) * 2;
		if (propertiesSize < MAX_PROPERTIES_SIZE)
			properties.put(propertyName, value);
	}
	public String toString() {
		return "IndexedFile(" + fileNumber + ": " + path + ")"; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
	}
}
