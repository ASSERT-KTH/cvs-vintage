package org.eclipse.jdt.internal.compiler.ast;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class AbstractVariableDeclaration extends Statement {
	public int modifiers;

	public TypeReference type;
	public Expression initialization;

	public char[] name;
	public int declarationEnd;
	public int declarationSourceStart;
	public int declarationSourceEnd;
	public int modifiersSourceStart;
	public AbstractVariableDeclaration() {
	}
	public abstract String name();
	
	public String toString(int tab) {

		String s = tabString(tab);
		if (modifiers != AccDefault) {
			s += modifiersString(modifiers);
		}
		s += type.toString(0) + " " + new String(name()); //$NON-NLS-1$
		if (initialization != null)
			s += " = " + initialization.toStringExpression(tab); //$NON-NLS-1$
		return s;
	}
}