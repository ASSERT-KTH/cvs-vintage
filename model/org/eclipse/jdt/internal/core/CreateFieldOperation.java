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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.IDocument;

/**
 * <p>This operation creates a field declaration in a type.
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing Type
 *  <li>The source code for the declaration. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateFieldOperation extends CreateTypeMemberOperation {
/**
 * When executed, this operation will create a field with the given name
 * in the given type with the specified source.
 *
 * <p>By default the new field is positioned after the last existing field
 * declaration, or as the first member in the type if there are no
 * field declarations.
 */
public CreateFieldOperation(IType parentElement, String source, boolean force) {
	super(parentElement, source, force);
}
protected ASTNode generateElementAST(ASTRewrite rewriter, IDocument document, ICompilationUnit cu) throws JavaModelException {
	ASTNode node = super.generateElementAST(rewriter, document, cu);
	if (node.getNodeType() != ASTNode.FIELD_DECLARATION)
		throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
	return node;
}
/**
 * @see CreateElementInCUOperation#generateResultHandle
 */
protected IJavaElement generateResultHandle() {
	return getType().getField(getASTNodeName());
}
/**
 * @see CreateElementInCUOperation#getMainTaskName()
 */
public String getMainTaskName(){
	return Util.bind("operation.createFieldProgress"); //$NON-NLS-1$
}
/**
 * By default the new field is positioned after the last existing field
 * declaration, or as the first member in the type if there are no
 * field declarations.
 */
protected void initializeDefaultPosition() {
	IType parentElement = getType();
	try {
		IJavaElement[] elements = parentElement.getFields();
		if (elements != null && elements.length > 0) {
			createAfter(elements[elements.length - 1]);
		} else {
			elements = parentElement.getChildren();
			if (elements != null && elements.length > 0) {
				createBefore(elements[0]);
			}
		}
	} catch (JavaModelException e) {
		// type doesn't exist: ignore
	}
}
/**
 * @see CreateTypeMemberOperation#verifyNameCollision
 */
protected IJavaModelStatus verifyNameCollision() {
	if (this.createdNode != null) {
		IType type= getType();
		String fieldName = getASTNodeName();
		if (type.getField(fieldName).exists()) {
			return new JavaModelStatus(
				IJavaModelStatusConstants.NAME_COLLISION, 
				Util.bind("status.nameCollision", fieldName)); //$NON-NLS-1$
		}
	}
	return JavaModelStatus.VERIFIED_OK;
}
private String getASTNodeName() {
	VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((FieldDeclaration) this.createdNode).fragments().iterator().next();
	return fragment.getName().getIdentifier();
}
protected SimpleName rename(ASTNode node, SimpleName newName) {
	VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((FieldDeclaration) node).fragments().iterator().next();
	SimpleName oldName = fragment.getName();
	fragment.setName(newName);
	return oldName;
}
}
