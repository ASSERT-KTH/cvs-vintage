package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * @see IField
 */

/* package */ class SourceField extends Member implements IField {

/**
 * Constructs a handle to the field with the given name in the specified type. 
 */
protected SourceField(IType parent, String name) {
	super(FIELD, parent, name);
}
/**
 * @see JavaElement#equalsDOMNode
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	return (node.getNodeType() == IDOMNode.FIELD) && super.equalsDOMNode(node);
}
/**
 * @see IField
 */
public Object getConstant() throws JavaModelException {
	SourceFieldElementInfo info = (SourceFieldElementInfo) getElementInfo();
	return convertConstant(info.getConstant());
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_FIELD;
}
/**
 * @see IField
 */
public String getTypeSignature() throws JavaModelException {
	SourceFieldElementInfo info = (SourceFieldElementInfo) getElementInfo();
	return info.getTypeSignature();
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	if (info == null) {
		buffer.append(getElementName());
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		buffer.append(getElementName());
	} else {
		try {
			buffer.append(Signature.toString(this.getTypeSignature()));
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(this.getElementName());
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
