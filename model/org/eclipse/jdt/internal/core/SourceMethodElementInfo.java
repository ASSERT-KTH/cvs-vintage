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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;

/** 
 * Element info for IMethod elements. 
 */
public class SourceMethodElementInfo extends MemberElementInfo implements ISourceMethod {
	
	protected char[] selector;

	/**
	 * For a source method (that is, a method contained in a compilation unit)
	 * this is a collection of the names of the parameters for this method,
	 * in the order the parameters are delcared. For a binary method (that is, 
	 * a method declared in a binary type), these names are invented as
	 * "arg"i where i starts at 1. This is an empty array if this method
	 * has no parameters.
	 */
	protected char[][] argumentNames;

	/**
	 * Collection of type names for the arguments in this
	 * method, in the order they are declared. This is an empty
	 * array for a method with no arguments. A name is a simple
	 * name or a qualified, dot separated name.
	 * For example, Hashtable or java.util.Hashtable.
	 */
	protected char[][] argumentTypeNames;

	/**
	 * Return type name for this method. The return type of
	 * constructors is equivalent to void.
	 */
	protected char[] returnType;

	/**
	 * A collection of type names of the exceptions this
	 * method throws, or an empty collection if this method
	 * does not declare to throw any exceptions. A name is a simple
	 * name or a qualified, dot separated name.
	 * For example, Hashtable or java.util.Hashtable.
	 */
	protected char[][] exceptionTypes;

	/**
	 * Constructor flag.
	 */
	protected boolean isConstructor= false;
	
	/*
	 * The type parameters of this source type. Empty if none.
	 */
	protected ITypeParameter[] typeParameters = TypeParameter.NO_TYPE_PARAMETERS;
	
	/*
	 * The positions of a default member value of an annotation member.
	 * These are {-1, -1} if the method is not an annotation method.
	 * These are {0, -1} if the method is an annotation method with no default value.
	 * Otherwise these are the start and end (inclusive) of the expression representing the default value.
	 */
	protected int defaultValueStart;
	protected int defaultValueEnd;


public char[][] getArgumentNames() {
	return this.argumentNames;
}
public char[][] getArgumentTypeNames() {
	return this.argumentTypeNames;
}
public char[] getDefaultValueSource(char[] cuSource) {
	if (this.defaultValueStart == 0 && this.defaultValueEnd == 0) 
		return null;
	return CharOperation.subarray(cuSource, this.defaultValueStart, this.defaultValueEnd+1);
}
public char[][] getExceptionTypeNames() {
	return this.exceptionTypes;
}
public char[] getReturnTypeName() {
	return this.returnType;
}
public char[] getSelector() {
	return this.selector;
}
protected String getSignature() {

	String[] paramSignatures = new String[this.argumentTypeNames.length];
	for (int i = 0; i < this.argumentTypeNames.length; ++i) {
		paramSignatures[i] = Signature.createTypeSignature(this.argumentTypeNames[i], false);
	}
	return Signature.createMethodSignature(paramSignatures, Signature.createTypeSignature(this.returnType, false));
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
public boolean isConstructor() {
	return this.isConstructor;
}
public boolean isAnnotationMethod() {
	return this.defaultValueStart != -1;
}
protected void setArgumentNames(char[][] names) {
	this.argumentNames = names;
}
protected void setArgumentTypeNames(char[][] types) {
	this.argumentTypeNames = types;
}
protected void setConstructor(boolean isConstructor) {
	this.isConstructor = isConstructor;
}
protected void setExceptionTypeNames(char[][] types) {
	this.exceptionTypes = types;
}
protected void setReturnType(char[] type) {
	this.returnType = type;
}
}
