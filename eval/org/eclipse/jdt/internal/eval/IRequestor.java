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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;

/**
 * A callback interface for receiving code snippet evaluation results.
 */
public interface IRequestor {
/**
 * @see org.eclipse.jdt.core.eval.ICodeSnippetRequestor
 */
boolean acceptClassFiles(ClassFile[] classFiles, char[] codeSnippetClassName);
/**
 * @see org.eclipse.jdt.core.eval.ICodeSnippetRequestor
 */
void acceptProblem(IProblem problem, char[] fragmentSource, int fragmentKind);
}
