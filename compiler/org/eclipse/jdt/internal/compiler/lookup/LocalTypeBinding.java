/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.util.Util;

public final class LocalTypeBinding extends NestedTypeBinding {
	final static char[] LocalTypePrefix = { '$', 'L', 'o', 'c', 'a', 'l', '$' };

	private InnerEmulationDependency[] dependents;
	ArrayBinding[] localArrayBindings; // used to cache array bindings of various dimensions for this local type

public LocalTypeBinding(ClassScope scope, SourceTypeBinding enclosingType) {
	super(
		new char[][] {CharOperation.concat(LocalTypePrefix, scope.referenceContext.name)},
		scope,
		enclosingType);

	if (this.sourceName == AnonymousLocalTypeDeclaration.ANONYMOUS_EMPTY_NAME)
		this.tagBits |= AnonymousTypeMask;
	else
		this.tagBits |= LocalTypeMask;
}
/* Record a dependency onto a source target type which may be altered
* by the end of the innerclass emulation. Later on, we will revisit
* all its dependents so as to update them (see updateInnerEmulationDependents()).
*/

public void addInnerEmulationDependent(BlockScope scope, boolean wasEnclosingInstanceSupplied) {
	int index;
	if (dependents == null) {
		index = 0;
		dependents = new InnerEmulationDependency[1];
	} else {
		index = dependents.length;
		for (int i = 0; i < index; i++)
			if (dependents[i].scope == scope)
				return; // already stored
		System.arraycopy(dependents, 0, (dependents = new InnerEmulationDependency[index + 1]), 0, index);
	}
	dependents[index] = new InnerEmulationDependency(scope, wasEnclosingInstanceSupplied);
	//  System.out.println("Adding dependency: "+ new String(scope.enclosingType().readableName()) + " --> " + new String(this.readableName()));
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] constantPoolName() /* java/lang/Object */ {
	return constantPoolName;
}

ArrayBinding createArrayType(int dimensionCount) {
	if (localArrayBindings == null) {
		localArrayBindings = new ArrayBinding[] {new ArrayBinding(this, dimensionCount)};
		return localArrayBindings[0];
	}

	// find the cached array binding for this dimensionCount (if any)
	int length = localArrayBindings.length;
	for (int i = 0; i < length; i++)
		if (localArrayBindings[i].dimensions == dimensionCount)
			return localArrayBindings[i];

	// no matching array
	System.arraycopy(localArrayBindings, 0, localArrayBindings = new ArrayBinding[length + 1], 0, length); 
	return localArrayBindings[length] = new ArrayBinding(this, dimensionCount);
}

public char[] readableName() {
	if (isAnonymousType()) {
		if (superInterfaces == NoSuperInterfaces)
			return ("<"+Util.bind("binding.subclass",new String(superclass.readableName())) + ">").toCharArray(); //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
		else
			return ("<"+Util.bind("binding.implementation",new String(superInterfaces[0].readableName())) + ">").toCharArray();			 //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
	} else if (isMemberType()) {
		return CharOperation.concat(enclosingType().readableName(), sourceName, '.');
	} else {
		return sourceName;
	}
}

public char[] shortReadableName() {
	if (isAnonymousType()) {
		if (superInterfaces == NoSuperInterfaces)
			return ("<"+Util.bind("binding.subclass",new String(superclass.shortReadableName())) + ">").toCharArray(); //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
		else
			return ("<"+Util.bind("binding.implementation",new String(superInterfaces[0].shortReadableName())) + ">").toCharArray();			 //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
	} else if (isMemberType()) {
		return CharOperation.concat(enclosingType().shortReadableName(), sourceName, '.');
	} else {
		return sourceName;
	}
}

// Record that the type is a local member type
public void setAsMemberType() {
	tagBits |= MemberTypeMask;
}

public void setConstantPoolName(char[] computedConstantPoolName) /* java/lang/Object */ {
	this.constantPoolName = computedConstantPoolName;
}

public char[] sourceName() {
	if (isAnonymousType()) {
		//return readableName();
		if (superInterfaces == NoSuperInterfaces)
			return ("<"+Util.bind("binding.subclass",new String(superclass.sourceName())) + ">").toCharArray(); //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
		else
			return ("<"+Util.bind("binding.implementation",new String(superInterfaces[0].sourceName())) + ">").toCharArray();			 //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
			
	} else
		return sourceName;
}
public String toString() {
	if (isAnonymousType())
		return "Anonymous type : " + super.toString(); //$NON-NLS-1$
	if (isMemberType())
		return "Local member type : " + new String(sourceName()) + " " + super.toString(); //$NON-NLS-2$ //$NON-NLS-1$
	return "Local type : " + new String(sourceName()) + " " + super.toString(); //$NON-NLS-2$ //$NON-NLS-1$
}
/* Trigger the dependency mechanism forcing the innerclass emulation
* to be propagated to all dependent source types.
*/

public void updateInnerEmulationDependents() {
	if (dependents != null) {
		for (int i = 0; i < dependents.length; i++) {
			InnerEmulationDependency dependency = dependents[i];
			// System.out.println("Updating " + new String(this.readableName()) + " --> " + new String(dependency.scope.enclosingType().readableName()));
			dependency.scope.propagateInnerEmulation(this, dependency.wasEnclosingInstanceSupplied);
		}
	}
}
}
