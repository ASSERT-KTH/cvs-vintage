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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ArrayTypeReference extends SingleTypeReference {
	public int dimensions;
/**
 * ArrayTypeReference constructor comment.
 * @param source char[]
 * @param dim int
 * @param pos int
 */
public ArrayTypeReference(char[] source, int dim, long pos) {
	super(source, pos);
	dimensions = dim ;
}
public ArrayTypeReference(char[] source, TypeBinding tb, int dim, long pos) {
	super(source, tb, pos);
	dimensions = dim ;}
public int dimensions() {
	return dimensions;
}
public TypeBinding getTypeBinding(Scope scope) {
	if (this.resolvedType != null)
		return this.resolvedType;
	if (dimensions > 255) {
		scope.problemReporter().tooManyDimensions(this);
	}
	return scope.createArray(scope.getType(token), dimensions);

}
public String toStringExpression(int tab){

	String s = super.toStringExpression(tab)  ;
	if (dimensions == 1 ) return s + "[]" ; //$NON-NLS-1$
	for (int i=1 ; i <= dimensions ; i++)
		s = s + "[]" ; //$NON-NLS-1$
	return s ;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
}
