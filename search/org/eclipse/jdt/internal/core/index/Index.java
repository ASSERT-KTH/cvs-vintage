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
package org.eclipse.jdt.internal.core.index;

import java.io.*;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.core.util.*;
import org.eclipse.jdt.internal.core.search.indexing.InternalSearchDocument;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchPattern;

/**
 * An <code>Index</code> maps document names to their referenced words in various categories.
 * 
 * Queries can search a single category or several at the same time.
 * 
 * Indexes are not synchronized structures and should only be queried/updated one at a time.
 */

public class Index {

public String printableName;
public ReadWriteMonitor monitor;

protected DiskIndex diskIndex;
protected MemoryIndex memoryIndex;

/**
 * Returns the path represented by pathString converted back to a path relative to the local file system.
 *
 * @param pathString the path to convert:
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
	int index = pathString.indexOf(IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR);
	if (index == -1) return pathString;
	
	Path jarPath = new Path(pathString.substring(0, index));
	return (jarPath.isAbsolute() ? jarPath.toOSString() : jarPath.makeAbsolute().toString())
		+ pathString.substring(index, pathString.length());
}
public static boolean isMatch(char[] pattern, char[] word, int matchRule) {
	if (pattern == null) return true;

	// need to mask some bits of pattern rule (bug 79790)
	switch(matchRule & JavaSearchPattern.MATCH_RULE_INDEX_MASK) {
		case SearchPattern.R_EXACT_MATCH :
			return CharOperation.equals(pattern, word, false);
		case SearchPattern.R_PREFIX_MATCH :
			return CharOperation.prefixEquals(pattern, word, false);
		case SearchPattern.R_PATTERN_MATCH :
			return CharOperation.match(pattern, word, false);
		case SearchPattern.R_EXACT_MATCH + SearchPattern.R_CASE_SENSITIVE :
			return CharOperation.equals(pattern, word);
		case SearchPattern.R_PREFIX_MATCH + SearchPattern.R_CASE_SENSITIVE :
			return CharOperation.prefixEquals(pattern, word);
		case SearchPattern.R_PATTERN_MATCH + SearchPattern.R_CASE_SENSITIVE :
			return CharOperation.match(pattern, word, true);
	}
	return false;
}


public Index(String fileName, String printableName, boolean reuseExistingFile) throws IOException {
	this.printableName = printableName;
	this.monitor = new ReadWriteMonitor();

	this.memoryIndex = new MemoryIndex();
	this.diskIndex = new DiskIndex(fileName);
	this.diskIndex.initialize(reuseExistingFile);
}
public void addIndexEntry(char[] category, char[] key, InternalSearchDocument document) {
	this.memoryIndex.addIndexEntry(category, key, document);
}
public File getIndexFile() {
	if (this.diskIndex == null) return null;

	return this.diskIndex.getIndexFile();
}
public boolean hasChanged() {
	return this.memoryIndex.hasChanged();
}
/**
 * Returns the entries containing the given key in a group of categories, or null if no matches are found.
 * The matchRule dictates whether its an exact, prefix or pattern match, as well as case sensitive or insensitive.
 * If the key is null then all entries in specified categories are returned.
 */
public EntryResult[] query(char[][] categories, char[] key, int matchRule) throws IOException {
	if (this.memoryIndex.shouldMerge() && monitor.exitReadEnterWrite()) {
		try {
			save();
		} finally {
			monitor.exitWriteEnterRead();
		}
	}

	HashtableOfObject results;
	if (this.memoryIndex.hasChanged()) {
		results = this.diskIndex.addQueryResults(categories, key, matchRule, this.memoryIndex);
		this.memoryIndex.addQueryResults(categories, key, matchRule, results);
	} else {
		results = this.diskIndex.addQueryResults(categories, key, matchRule, null);
	}
	if (results.elementSize == 0) return null;

	EntryResult[] entryResults = new EntryResult[results.elementSize];
	int count = 0;
	Object[] values = results.valueTable;
	for (int i = 0, l = values.length; i < l; i++) {
		EntryResult result = (EntryResult) values[i];
		if (result != null)
			entryResults[count++] = result;
	}
	return entryResults;
}
/**
 * Returns the document names that contain the given substring, if null then returns all of them.
 */
public String[] queryDocumentNames(String substring) throws IOException {
	SimpleSet results;
	if (this.memoryIndex.hasChanged()) {
		results = this.diskIndex.addDocumentNames(substring, this.memoryIndex);
		this.memoryIndex.addDocumentNames(substring, results);
	} else {
		results = this.diskIndex.addDocumentNames(substring, null);
	}
	if (results.elementSize == 0) return null;

	String[] documentNames = new String[results.elementSize];
	int count = 0;
	Object[] paths = results.values;
	for (int i = 0, l = paths.length; i < l; i++)
		if (paths[i] != null)
			documentNames[count++] = (String) paths[i];
	return documentNames;
}
public void remove(String documentName) {
	this.memoryIndex.remove(documentName);
}
public void save() throws IOException {
	// must own the write lock of the monitor
	if (!hasChanged()) return;

	int numberOfChanges = this.memoryIndex.docsToReferences.elementSize;
	this.diskIndex = this.diskIndex.mergeWith(this.memoryIndex);
	this.memoryIndex = new MemoryIndex();
	if (numberOfChanges > 1000)
		System.gc(); // reclaim space if the MemoryIndex was very BIG
}
public void startQuery() {
	if (this.diskIndex != null)
		this.diskIndex.startQuery();
}
public void stopQuery() {
	if (this.diskIndex != null)
		this.diskIndex.stopQuery();
}
public String toString() {
	if (this.printableName != null) return this.printableName;
	return super.toString();
}
}