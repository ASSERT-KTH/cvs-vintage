/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Internal class.
 * @since 3.1
 */
class BindingKey {
	 BindingKeyScanner scanner;
	 char[][] compoundName;
	 
	 BindingKey(char[] key) {
	 	this.scanner = new BindingKeyScanner(key);
	 	reset();
	 }
	 
	 BindingKey(String key) {
	 	this(key.toCharArray());
	 }
	 
	 /*
	  * If not already cached, computes and cache the compound name (pkg name + top level name) of this key.
	  * Returns the package name if key is a pkg key.
	  * Returns an empty array if malformed.
	  * This key's scanner should be at the start of the key if first call.
	  */
	 char[][] compoundName() {
	 	if (this.compoundName == null) {
		 	if (this.scanner.token != BindingKeyScanner.PACKAGE) {
		 		this.compoundName = CharOperation.NO_CHAR_CHAR; // malformed
		 	} else {
			 	char[][] pkg = CharOperation.splitOn('.', this.scanner.getTokenSource());
			 	if (this.scanner.isAtTypeEnd()) {
			 		this.compoundName = pkg;
			 	} else {
				 	int token = this.scanner.nextToken();
				 	switch (token) {
				 		case BindingKeyScanner.TYPE:
					 		char[] simpleName = this.scanner.getTokenSource();
					 		this.compoundName = CharOperation.arrayConcat(pkg, simpleName);
					 		break;
					 	case BindingKeyScanner.ARRAY: // case of base type with array dimension
					 		this.compoundName = pkg;
					 		break;
					 	default:
					 		this.compoundName = this.compoundName = CharOperation.NO_CHAR_CHAR; // malformed
				 	}
			 	}
		 	}
	 	}
	 	return this.compoundName;
	 }
	 
	 /*
	  * Finds the compilation unit declaration corresponding to the key in the given lookup environment.
	  * Returns null if no compilation unit declaration could be found.
	  */
	 CompilationUnitDeclaration getCompilationUnitDeclaration(LookupEnvironment lookupEnvironment) {
		char[][] compundName = compoundName();
		if (compundName.length == 0) return null;
		ReferenceBinding binding = lookupEnvironment.getType(compundName);
		if (!(binding instanceof SourceTypeBinding)) return null;
		return ((SourceTypeBinding) binding).scope.compilationUnitScope().referenceContext;
	 }
	 
	 Binding getCompilerBinding(CompilationUnitResolver resolver) {
		CompilationUnitDeclaration parsedUnit = getCompilationUnitDeclaration(resolver.lookupEnvironment);
		if (parsedUnit != null) {
			char[] fileName = parsedUnit.compilationResult.getFileName();
			if (resolver.requestedKeys.containsKey(fileName) || resolver.requestedSources.containsKey(fileName))
				throw new RuntimeException("Key is part of a file that is being requested already"); //$NON-NLS-1$
			resolver.process(parsedUnit, resolver.totalUnits+1);
		}
		return getCompilerBinding(parsedUnit, resolver);
	 }
	 
	 /*
	  * Returns the compiler binding corresponding to this key.
	  * This key's scanner must be after the top level type. Returns null otherwise.
	  */
	 Binding getCompilerBinding(CompilationUnitDeclaration parsedUnit, CompilationUnitResolver resolver) {
	 	switch (this.scanner.token) {
	 		case BindingKeyScanner.PACKAGE:
	 			if (this.compoundName.length > 0) {
		 			TypeBinding baseTypeBinding = Scope.getBaseType(this.compoundName[this.compoundName.length-1]);
		 			if (baseTypeBinding != null) // case of base type
	 					return baseTypeBinding;
	 			}
	 			return new PackageBinding(this.compoundName, null, resolver.lookupEnvironment);
	 		case BindingKeyScanner.TYPE:
	 			if (parsedUnit == null) 
	 				return getBinaryBinding(resolver);
	 			char[] typeName = this.compoundName[this.compoundName.length-1];
	 			SourceTypeBinding binding = getTypeBinding(parsedUnit, parsedUnit.types, typeName);
	 			switch (this.scanner.token) {
	 				case BindingKeyScanner.PACKAGE:
					case BindingKeyScanner.END:
	 					return binding;
	 				case BindingKeyScanner.ARRAY:
	 					return getArrayBinding(binding, resolver);
	 				case BindingKeyScanner.FIELD:
	 					return getFieldBinding(binding);
	 				case BindingKeyScanner.METHOD:
	 					return getMethodBinding(binding, resolver);
	 			}
	 			break;
	 		case BindingKeyScanner.ARRAY:
	 			if (this.compoundName.length > 0) {
		 			TypeBinding baseTypeBinding = Scope.getBaseType(this.compoundName[this.compoundName.length-1]);
		 			if (baseTypeBinding != null)
	 					return getArrayBinding(baseTypeBinding, resolver);
	 			}
 				break;
	 	}
	 	return null;
	 }
	 
	 Binding getArrayBinding(TypeBinding binding, CompilationUnitResolver resolver) {
		char[] tokenSource = this.scanner.getTokenSource();
		int dimension = tokenSource.length / 2;
		return resolver.lookupEnvironment.createArrayType(binding, dimension);
	}
	 
	 Binding getBinaryBinding(CompilationUnitResolver resolver) {
		TypeBinding binding = resolver.lookupEnvironment.getType(this.compoundName);
	 	if (this.scanner.nextToken() == BindingKeyScanner.ARRAY)
			return getArrayBinding(binding, resolver);
	 	else
	 		return binding;
	}

	FieldBinding getFieldBinding(SourceTypeBinding typeBinding) {
	 	FieldBinding[] fields = typeBinding.fields;
	 	if (fields == null) return null;
	 	char[] fieldName = this.scanner.getTokenSource();
	 	for (int i = 0, length = fields.length; i < length; i++) {
			FieldBinding field = fields[i];
			if (CharOperation.equals(fieldName, field.name)) 
				return field;
		}
	 	return null;
	 }
	 
	 /*
	  * Returns the string that this binding key wraps.
	  */
	 String getKey() {
	 	return new String(this.scanner.source);
	 }
	 
	 MethodBinding getMethodBinding(SourceTypeBinding typeBinding, CompilationUnitResolver resolver) {
	 	MethodBinding[] methods = typeBinding.methods;
	 	if (methods == null) return null;
	 	char[] selector = this.scanner.getTokenSource();
	 	ArrayList parameterList = new ArrayList();
	 	do {
	 		reset();
	 		Binding parameterBinding = getCompilerBinding(resolver);
	 		if (parameterBinding == null) break;
	 		parameterList.add(parameterBinding);
	 	} while (this.scanner.token != BindingKeyScanner.END);
	 	int parameterLength = parameterList.size();
	 	TypeBinding[] parameters = new TypeBinding[parameterLength];
	 	parameterList.toArray(parameters);
	 	nextMethod: for (int i = 0, methodLength = methods.length; i < methodLength; i++) {
			MethodBinding method = methods[i];
			if (CharOperation.equals(selector, method.selector) || (selector.length == 0 && method.isConstructor())) {
				TypeBinding[] methodParameters = method.parameters;
				if (methodParameters == null || methodParameters.length != parameterLength)
					continue nextMethod;
				for (int j = 0; j < parameterLength; j++) {
					if (methodParameters[j] != parameters[j])
						continue nextMethod;
				}
				return method;
			}
		}
	 	return null;
	 }
	 
	 SourceTypeBinding getTypeBinding(CompilationUnitDeclaration parsedUnit, TypeDeclaration[] types, char[] typeName) {
	 	if (Character.isDigit(typeName[0])) {
	 		// anonymous or local type
	 		int nextToken = this.scanner.nextToken();
	 		while (nextToken == BindingKeyScanner.TYPE) 
	 			nextToken = this.scanner.nextToken();
	 		typeName = nextToken == BindingKeyScanner.END ? this.scanner.source : CharOperation.subarray(this.scanner.source, 0, this.scanner.start-1);
	 		LocalTypeBinding[] localTypeBindings  = parsedUnit.localTypes;
	 		for (int i = 0; i < parsedUnit.localTypeCount; i++)
	 			if (CharOperation.equals(typeName, localTypeBindings[i].constantPoolName()))
	 				return localTypeBindings[i];
	 		return null;
	 	} else {
	 		// member type
		 	if (types == null) return null;
			for (int i = 0, length = types.length; i < length; i++) {
				TypeDeclaration declaration = types[i];
				if (CharOperation.equals(typeName, declaration.name)) {
					if (this.scanner.nextToken() == BindingKeyScanner.TYPE)
						return getTypeBinding(parsedUnit, declaration.memberTypes, this.scanner.getTokenSource());
					else
						return declaration.binding;
				}
			}
	 	}
		return null;
	 }
	 
	 void reset() {
	 	this.compoundName = null;
	 	if (this.scanner.isAtTypeEnd())
		 	this.scanner.nextToken();
	 }
	 
	 public String toString() {
		return getKey();
	}
}
