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
package org.eclipse.jdt.internal.core.jdom;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.jdom.IDOMCompilationUnit;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.core.jdom.IDOMType;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;
/**
 * DOMCompilation unit provides an implementation of IDOMCompilationUnit.
 *
 * @see IDOMCompilationUnit
 * @see DOMNode
 */
class DOMCompilationUnit extends DOMNode implements IDOMCompilationUnit, SuffixConstants {

	/**
	 * The comment and/or whitespace preceding the
	 * first document fragment in this compilation
	 * unit.
	 */
	protected String fHeader;
/**
 * Creates a new empty COMPILATION_UNIT document fragment.
 */
DOMCompilationUnit() {
	fHeader=""; //$NON-NLS-1$
}
/**
 * Creates a new COMPILATION_UNIT on the given range of the document.
 *
 * @param document - the document containing this node's original contents
 * @param sourceRange - a two element array of integers describing the
 *		entire inclusive source range of this node within its document.
 * 		A compilation unit's source range is the entire document - 
 *		the first integer is zero, and the second integer is the position
 *		of the last character in the document.
 */
DOMCompilationUnit(char[] document, int[] sourceRange) {
	super(document, sourceRange, null, new int[]{-1, -1});
	fHeader = ""; //$NON-NLS-1$
}
/**
 * @see DOMNode#appendContents(CharArrayBuffer)
 */
protected void appendFragmentedContents(CharArrayBuffer buffer) {
	buffer.append(getHeader());
	appendContentsOfChildren(buffer);
}
/**
 * @see IDOMNode#canHaveChildren()
 */
public boolean canHaveChildren() {
	return true;
}
/**
 * @see IDOMCompilationUnit#getCompilationUnit
 */
public ICompilationUnit getCompilationUnit(IPackageFragment parent, WorkingCopyOwner owner) throws IllegalArgumentException {
	return parent.getCompilationUnit(getName(), owner);
}
/**
 * @see IDOMCompilationUnit#getHeader()
 */
public String getHeader() {
	return fHeader;
}
/**
 * @see IDOMNode#getJavaElement
 */
public IJavaElement getJavaElement(IJavaElement parent) throws IllegalArgumentException {
	if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
		return getCompilationUnit((IPackageFragment)parent, DefaultWorkingCopyOwner.PRIMARY);
	} else {
		throw new IllegalArgumentException(Util.bind("element.illegalParent")); //$NON-NLS-1$
	}
}
/**
 * @see IDOMCompilationUnit#getName()
 */
public String getName() { 
	IDOMType topLevelType= null;
	IDOMType firstType= null;
	IDOMNode child= fFirstChild;
	while (child != null) {
		if (child.getNodeType() == IDOMNode.TYPE) {
			IDOMType type= (IDOMType)child;
			if (firstType == null) {
				firstType= type;
			}
			if (Flags.isPublic(type.getFlags())) {
				topLevelType= type;
				break;
			}
		}
		child= child.getNextNode();
	}
	if (topLevelType == null) {
		topLevelType= firstType;
	}
	if (topLevelType != null) {
		return topLevelType.getName() + SUFFIX_STRING_java;
	} else {
		return null;
	}
}
/**
 * @see IDOMNode#getNodeType()
 */
public int getNodeType() {
	return IDOMNode.COMPILATION_UNIT;
}
/**
 * Sets the header
 */
protected void initalizeHeader() {
	DOMNode child = (DOMNode)getFirstChild();
	if (child != null) {
		int childStart = child.getStartPosition();
		if (childStart > 1) {
			setHeader(new String(CharOperation.subarray(fDocument, 0, childStart)));
		}
	}
}
/**
 * @see IDOMNode#isAllowableChild(IDOMNode)
 */
public boolean isAllowableChild(IDOMNode node) {
	if (node != null) {
		int type= node.getNodeType();
		return type == IDOMNode.PACKAGE || type == IDOMNode.IMPORT || type == IDOMNode.TYPE; 
	} else {
		return false;
	}
	
}
/**
 * @see DOMNode
 */
protected DOMNode newDOMNode() {
	return new DOMCompilationUnit();
}
/**
 * Normalizes this <code>DOMNode</code>'s source positions to include whitespace preceeding
 * the node on the line on which the node starts, and all whitespace after the node up to
 * the next node's start
 */
void normalize(ILineStartFinder finder) {
	super.normalize(finder);
	initalizeHeader();
}
/**
 * @see IDOMCompilationUnit@setHeader(String)
 */
public void setHeader(String comment) {
	fHeader= comment;
	fragment();
}
/**
 * @see IDOMCompilationUnit#setName(String)
 */
public void setName(String name) {
	// nothing to do
}
/**
 * @see DOMNode#shareContents(DOMNode)
 */
protected void shareContents(DOMNode node) {
	super.shareContents(node);
	fHeader= ((DOMCompilationUnit)node).fHeader;
}
/**
 * @see IDOMNode#toString()
 */
public String toString() {
	return "COMPILATION_UNIT: " + getName(); //$NON-NLS-1$
}
}
