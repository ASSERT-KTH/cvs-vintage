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
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an access to a member (field reference or message send) 
 * containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      bar().fred[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnMemberAccess:bar().fred>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnMemberAccess extends FieldReference {
	
	public CompletionOnMemberAccess(char[] source, long pos) {
		
		super(source, pos);
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {

		output.append("<CompleteOnMemberAccess:"); //$NON-NLS-1$
		return super.printExpression(0, output).append('>'); 
	}

	public TypeBinding resolveType(BlockScope scope) {
		
		this.receiverType = receiver.resolveType(scope);
		if (this.receiverType == null || this.receiverType.isBaseType())
			throw new CompletionNodeFound();
		else
			throw new CompletionNodeFound(this, this.receiverType, scope);
		// array types are passed along to find the length field
	}
}
