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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class CompletionOnAnnotationOfType extends TypeDeclaration {
	public CompletionOnAnnotationOfType(CompilationResult compilationResult, Annotation annotation){
		super(compilationResult);
		this.sourceEnd = annotation.sourceEnd;
		this.sourceStart = annotation.sourceEnd;
		this.name = CharOperation.NO_CHAR;
		this.annotations = new Annotation[]{annotation};
	}
	
	public StringBuffer print(int indent, StringBuffer output) {
		return this.annotations[0].print(indent, output);
	}
}
