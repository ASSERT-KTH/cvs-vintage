package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Adapter of the requestor interface <code>ICompletionRequestor</code>.
 *
 * @see ICompletionRequestor
 * @since 2.0
 */
public class CompletionRequestorAdapter implements ICompletionRequestor {

	/*
	 * @see ICompletionRequestor#acceptAnonymousType(char[], char[], char[][], char[][], char[][], char[], int, int, int)
	 */
	public void acceptAnonymousType(
		char[] superTypePackageName,
		char[] superTypeName,
		char[][] parameterPackageNames,
		char[][] parameterTypeNames,
		char[][] parameterNames,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptClass(char[], char[], char[], int, int, int)
	 */
	public void acceptClass(
		char[] packageName,
		char[] className,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptError(IProblem)
	 */
	public void acceptError(IProblem error) {
	}

	/*
	 * @see ICompletionRequestor#acceptField(char[], char[], char[], char[], char[], char[], int, int, int)
	 */
	public void acceptField(
		char[] declaringTypePackageName,
		char[] declaringTypeName,
		char[] name,
		char[] typePackageName,
		char[] typeName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptInterface(char[], char[], char[], int, int, int)
	 */
	public void acceptInterface(
		char[] packageName,
		char[] interfaceName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptKeyword(char[], int, int)
	 */
	public void acceptKeyword(
		char[] keywordName,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptLabel(char[], int, int)
	 */
	public void acceptLabel(
		char[] labelName,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptLocalVariable(char[], char[], char[], int, int, int)
	 */
	public void acceptLocalVariable(
		char[] name,
		char[] typePackageName,
		char[] typeName,
		int modifiers,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptMethod(char[], char[], char[], char[][], char[][], char[][], char[], char[], char[], int, int, int)
	 */
	public void acceptMethod(
		char[] declaringTypePackageName,
		char[] declaringTypeName,
		char[] selector,
		char[][] parameterPackageNames,
		char[][] parameterTypeNames,
		char[][] parameterNames,
		char[] returnTypePackageName,
		char[] returnTypeName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptMethodDeclaration(char[], char[], char[], char[][], char[][], char[][], char[], char[], char[], int, int, int)
	 */
	public void acceptMethodDeclaration(
		char[] declaringTypePackageName,
		char[] declaringTypeName,
		char[] selector,
		char[][] parameterPackageNames,
		char[][] parameterTypeNames,
		char[][] parameterNames,
		char[] returnTypePackageName,
		char[] returnTypeName,
		char[] completionName,
		int modifiers,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptModifier(char[], int, int)
	 */
	public void acceptModifier(
		char[] modifierName,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptPackage(char[], char[], int, int)
	 */
	public void acceptPackage(
		char[] packageName,
		char[] completionName,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptType(char[], char[], char[], int, int)
	 */
	public void acceptType(
		char[] packageName,
		char[] typeName,
		char[] completionName,
		int completionStart,
		int completionEnd,
		int relevance) {
	}

	/*
	 * @see ICompletionRequestor#acceptVariableName(char[], char[], char[], char[], int, int)
	 */
	public void acceptVariableName(
		char[] typePackageName,
		char[] typeName,
		char[] name,
		char[] completionName,
		int completionStart,
		int completionEnd,
		int relevance) {
	}
}
