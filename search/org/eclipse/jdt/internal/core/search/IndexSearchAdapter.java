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
package org.eclipse.jdt.internal.core.search;

public class IndexSearchAdapter implements IIndexSearchRequestor {
/**
 * @see IIndexSearchRequestor
 */
public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptConstructorDeclaration(String resourcePath, char[] typeName, int parameterCount) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptConstructorReference(String resourcePath, char[] typeName, int parameterCount) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptFieldDeclaration(String resourcePath, char[] fieldName) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptFieldReference(String resourcePath, char[] fieldName) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptMethodDeclaration(String resourcePath, char[] methodName, int parameterCount) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptMethodReference(String resourcePath, char[] methodName, int parameterCount) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptPackageReference(String resourcePath, char[] packageName) {
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptSuperTypeReference(String resourcePath, char[] qualification, char[] typeName, char[] enclosingTypeName, char classOrInterface, char[] superQualification, char[] superTypeName, char superClassOrInterface, int modifiers){
	// implements interface method
}
/**
 * @see IIndexSearchRequestor
 */
public void acceptTypeReference(String resourcePath, char[] typeName) {
	// implements interface method
}
}
