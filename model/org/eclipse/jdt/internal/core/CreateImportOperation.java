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

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.IDocument;

/**
 * <p>This operation adds an import declaration to an existing compilation unit.
 * If the compilation unit already includes the specified import declaration,
 * the import is not generated (it does not generate duplicates).
 * Note that it is valid to specify both a single-type import and an on-demand import
 * for the same package, for example <code>"java.io.File"</code> and
 * <code>"java.io.*"</code>, in which case both are preserved since the semantics
 * of this are not the same as just importing <code>"java.io.*"</code>.
 * Importing <code>"java.lang.*"</code>, or the package in which the compilation unit
 * is defined, are not treated as special cases.  If they are specified, they are
 * included in the result.
 *
 * <p>Required Attributes:<ul>
 *  <li>Compilation unit
 *  <li>Import name - the name of the import to add to the
 *      compilation unit. For example: <code>"java.io.File"</code> or <code>"java.awt.*"</code>
 * </ul>
 */
public class CreateImportOperation extends CreateElementInCUOperation {

	/**
	 * The name of the import to be created.
	 */
	protected String importName;
/**
 * When executed, this operation will add an import to the given compilation unit.
 */
public CreateImportOperation(String importName, ICompilationUnit parentElement) {
	super(parentElement);
	this.importName = importName;
}
protected StructuralPropertyDescriptor getChildPropertyDescriptor(ASTNode parent) {
	return CompilationUnit.IMPORTS_PROPERTY;
}
protected ASTNode generateElementAST(ASTRewrite rewriter, IDocument document, ICompilationUnit cu) throws JavaModelException {
	// ensure no duplicate
	Iterator imports = this.cuAST.imports().iterator();
	while (imports.hasNext()) {
		ImportDeclaration importDeclaration = (ImportDeclaration) imports.next();
		if (this.importName.equals(importDeclaration.getName().getFullyQualifiedName())) {
			//no new import was generated
			this.creationOccurred = false;
			return null;
		}
	}
	
	AST ast = this.cuAST.getAST();
	ImportDeclaration importDeclaration = ast.newImportDeclaration();
	Name name = ast.newName(new String[] {this.importName});
	importDeclaration.setName(name);
	return importDeclaration;
}
/**
 * @see CreateElementInCUOperation#generateResultHandle
 */
protected IJavaElement generateResultHandle() {
	return getCompilationUnit().getImport(this.importName);
}
/**
 * @see CreateElementInCUOperation#getMainTaskName()
 */
public String getMainTaskName(){
	return Util.bind("operation.createImportsProgress"); //$NON-NLS-1$
}
/**
 * Sets the correct position for the new import:<ul>
 * <li> after the last import
 * <li> if no imports, before the first type
 * <li> if no type, after the package statement
 * <li> and if no package statement - first thing in the CU
 */
protected void initializeDefaultPosition() {
	try {
		ICompilationUnit cu = getCompilationUnit();
		IImportDeclaration[] imports = cu.getImports();
		if (imports.length > 0) {
			createAfter(imports[imports.length - 1]);
			return;
		}
		IType[] types = cu.getTypes();
		if (types.length > 0) {
			createBefore(types[0]);
			return;
		}
		IJavaElement[] children = cu.getChildren();
		//look for the package declaration
		for (int i = 0; i < children.length; i++) {
			if (children[i].getElementType() == IJavaElement.PACKAGE_DECLARATION) {
				createAfter(children[i]);
				return;
			}
		}
	} catch (JavaModelException e) {
		// cu doesn't exit: ignore
	}
}
/**
 * Possible failures: <ul>
 *  <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the operation is
 * 		<code>null</code>.
 *  <li>INVALID_NAME - not a valid import declaration name.
 * </ul>
 * @see IJavaModelStatus
 * @see JavaConventions
 */
public IJavaModelStatus verify() {
	IJavaModelStatus status = super.verify();
	if (!status.isOK()) {
		return status;
	}
	if (JavaConventions.validateImportDeclaration(this.importName).getSeverity() == IStatus.ERROR) {
		return new JavaModelStatus(IJavaModelStatusConstants.INVALID_NAME, this.importName);
	}
	return JavaModelStatus.VERIFIED_OK;
}
}
