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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.compiler.env.ISourceImport;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;
import org.eclipse.jdt.internal.compiler.env.ISourceType;

/** 
 * Element info for an IType element that originated from source. 
 */
public class SourceTypeElementInfo extends MemberElementInfo implements ISourceType {

	protected static final ISourceImport[] NO_IMPORTS = new ISourceImport[0];
	protected static final InitializerElementInfo[] NO_INITIALIZERS = new InitializerElementInfo[0];
	protected static final SourceField[] NO_FIELDS = new SourceField[0];
	protected static final SourceMethod[] NO_METHODS = new SourceMethod[0];
	protected static final SourceType[] NO_TYPES = new SourceType[0];
	/**
	 * The name of the superclass for this type. This name
	 * is fully qualified for binary types and is NOT
	 * fully qualified for source types.
	 */
	protected char[] superclassName;
	
	/**
	 * The names of the interfaces this type implements or
	 * extends. These names are fully qualified in the case
	 * of a binary type, and are NOT fully qualified in the
	 * case of a source type
	 */
	protected char[][] superInterfaceNames;
	
	/**
	 * The name of the source file this type is declared in.
	 */
	protected char[] sourceFileName;

	/**
	 * The name of the package this type is contained in.
	 */
	protected char[] packageName;

	/**
	 * The infos of the imports in this type's compilation unit
	 */
	private ISourceImport[] imports;
	
	/**
	 * Backpointer to my type handle - useful for translation
	 * from info to handle.
	 */
	protected IType handle = null;
	
	/*
	 * The type parameters of this source type. Empty if none.
	 */
	protected ITypeParameter[] typeParameters = TypeParameter.NO_TYPE_PARAMETERS;

/**
 * Returns the ISourceType that is the enclosing type for this
 * type, or <code>null</code> if this type is a top level type.
 */
public ISourceType getEnclosingType() {
	IJavaElement parent= this.handle.getParent();
	if (parent != null && parent.getElementType() == IJavaElement.TYPE) {
		try {
			return (ISourceType)((JavaElement)parent).getElementInfo();
		} catch (JavaModelException e) {
			return null;
		}
	} else {
		return null;
	}
}
/**
 * @see ISourceType
 */
public ISourceField[] getFields() {
	SourceField[] fieldHandles = getFieldHandles();
	int length = fieldHandles.length;
	ISourceField[] fields = new ISourceField[length];
	for (int i = 0; i < length; i++) {
		try {
			ISourceField field = (ISourceField) fieldHandles[i].getElementInfo();
			fields[i] = field;
		} catch (JavaModelException e) {
			// ignore
		}
	}
	return fields;
}
public SourceField[] getFieldHandles() {
	int length = this.children.length;
	if (length == 0) return NO_FIELDS;
	SourceField[] fields = new SourceField[length];
	int fieldIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof SourceField)
			fields[fieldIndex++] = (SourceField) child;
	}
	if (fieldIndex == 0) return NO_FIELDS;
	if (fieldIndex < length)
		System.arraycopy(fields, 0, fields = new SourceField[fieldIndex], 0, fieldIndex);
	return fields;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return this.sourceFileName;
}
/**
 * Returns the handle for this type info
 */
public IType getHandle() {
	return this.handle;
}
/**
 * @see ISourceType
 */
public ISourceImport[] getImports() {
	if (this.imports == null) {
		try {
			IImportDeclaration[] importDeclarations = this.handle.getCompilationUnit().getImports();
			int length = importDeclarations.length;
			if (length == 0) {
				this.imports = NO_IMPORTS;
			} else {
				ISourceImport[] sourceImports = new ISourceImport[length];
				for (int i = 0; i < length; i++) {
					sourceImports[i] = (ImportDeclarationElementInfo)((ImportDeclaration)importDeclarations[i]).getElementInfo();
				}
				this.imports = sourceImports; // only commit at the end, once completed (bug 36854)
			}
		} catch (JavaModelException e) {
			this.imports = NO_IMPORTS;
		}
	}
	return this.imports;
}
/*
 * Returns the InitializerElementInfos for this type.
 * Returns an empty array if none.
 */
public InitializerElementInfo[] getInitializers() {
	int length = this.children.length;
	if (length == 0) return NO_INITIALIZERS;
	InitializerElementInfo[] initializers = new InitializerElementInfo[length];
	int initializerIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof Initializer) {
			try {
				InitializerElementInfo initializer = (InitializerElementInfo)((Initializer)child).getElementInfo();
				initializers[initializerIndex++] = initializer;
			} catch (JavaModelException e) {
				// ignore
			}
		}
	}
	if (initializerIndex == 0) return NO_INITIALIZERS;
	System.arraycopy(initializers, 0, initializers = new InitializerElementInfo[initializerIndex], 0, initializerIndex);
	return initializers;
}
/**
 * @see ISourceType
 */
public char[][] getInterfaceNames() {
	if (this.handle.getElementName().length() == 0) { // if anonymous type
		return null;
	}
	return this.superInterfaceNames;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IGenericType#getKind()
 */
public int getKind() {
	if ((this.flags & IConstants.AccInterface) != 0) {
		if ((this.flags & IConstants.AccAnnotation) != 0)
			return IGenericType.ANNOTATION_TYPE_DECL;
		return IGenericType.INTERFACE_DECL;
	}
	if ((this.flags & IConstants.AccEnum) != 0) return IGenericType.ENUM_DECL;
	return IGenericType.CLASS_DECL;
}
/**
 * @see ISourceType
 */
public ISourceType[] getMemberTypes() {
	SourceType[] memberTypeHandles = getMemberTypeHandles();
	int length = memberTypeHandles.length;
	ISourceType[] memberTypes = new ISourceType[length];
	for (int i = 0; i < length; i++) {
		try {
			ISourceType type = (ISourceType) memberTypeHandles[i].getElementInfo();
			memberTypes[i] = type;
		} catch (JavaModelException e) {
			// ignore
		}
	}
	return memberTypes;
}
public SourceType[] getMemberTypeHandles() {
	int length = this.children.length;
	if (length == 0) return NO_TYPES;
	SourceType[] memberTypes = new SourceType[length];
	int typeIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof SourceType)
			memberTypes[typeIndex++] = (SourceType)child;
	}
	if (typeIndex == 0) return NO_TYPES;
	if (typeIndex < length)
		System.arraycopy(memberTypes, 0, memberTypes = new SourceType[typeIndex], 0, typeIndex);
	return memberTypes;
}
/**
 * @see ISourceType
 */
public ISourceMethod[] getMethods() {
	SourceMethod[] methodHandles = getMethodHandles();
	int length = methodHandles.length;
	ISourceMethod[] methods = new ISourceMethod[length];
	int methodIndex = 0;
	for (int i = 0; i < length; i++) {
		try {
			ISourceMethod method = (ISourceMethod) methodHandles[i].getElementInfo();
			methods[methodIndex++] = method;
		} catch (JavaModelException e) {
			// ignore
		}
	}
	return methods;
}
public SourceMethod[] getMethodHandles() {
	int length = this.children.length;
	if (length == 0) return NO_METHODS;
	SourceMethod[] methods = new SourceMethod[length];
	int methodIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof SourceMethod)
			methods[methodIndex++] = (SourceMethod) child;
	}
	if (methodIndex == 0) return NO_METHODS;
	if (methodIndex < length)
		System.arraycopy(methods, 0, methods = new SourceMethod[methodIndex], 0, methodIndex);
	return methods;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.ISourceType#getName()
 */
public char[] getName() {
	return this.handle.getElementName().toCharArray();
}
/**
 * @see ISourceType
 */
public char[] getPackageName() {
	return this.packageName;
}
/**
 * @see ISourceType
 */
public char[] getSuperclassName() {
	if (this.handle.getElementName().length() == 0) { // if anonymous type
		char[][] interfaceNames = this.superInterfaceNames;	
		if (interfaceNames != null && interfaceNames.length > 0) {
			return interfaceNames[0];
		}
	} 
	return this.superclassName;
}
public char[][][] getTypeParameterBounds() {
	int length = this.typeParameters.length;
	char[][][] typeParameterBounds = new char[length][][];
	for (int i = 0; i < length; i++) {
		try {
			TypeParameterElementInfo info = (TypeParameterElementInfo) ((JavaElement)this.typeParameters[i]).getElementInfo();
			typeParameterBounds[i] = info.bounds;
		} catch (JavaModelException e) {
			// type parameter does not exist: ignore
		}
	}
	return typeParameterBounds;
}
public char[][] getTypeParameterNames() {
	int length = this.typeParameters.length;
	if (length == 0) return CharOperation.NO_CHAR_CHAR;
	char[][] typeParameterNames = new char[length][];
	for (int i = 0; i < length; i++) {
		typeParameterNames[i] = this.typeParameters[i].getElementName().toCharArray();
	}
	return typeParameterNames;
}
/**
 * @see ISourceType
 */
public boolean isBinaryType() {
	return false;
}
/**
 * Sets the handle for this type info
 */
protected void setHandle(IType handle) {
	this.handle = handle;
}
/**
 * Sets the name of the package this type is declared in.
 */
protected void setPackageName(char[] name) {
	this.packageName= name;
}
/**
 * Sets the name of the source file this type is declared in.
 */
protected void setSourceFileName(char[] name) {
	this.sourceFileName= name;
}
/**
 * Sets the (unqualified) name of this type's superclass
 */
protected void setSuperclassName(char[] superclassName) {
	this.superclassName = superclassName;
}
/**
 * Sets the (unqualified) names of the interfaces this type implements or extends
 */
protected void setSuperInterfaceNames(char[][] superInterfaceNames) {
	this.superInterfaceNames = superInterfaceNames;
}
public String toString() {
	return "Info for " + this.handle.toString(); //$NON-NLS-1$
}
}
