package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.internal.codeassist.ICompletionRequestor;
import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.core.*;

public class CompletionRequestorWrapper implements ICompletionRequestor {
	static final char[] ARG = "arg".toCharArray();  //$NON-NLS-1$
	
	ICodeCompletionRequestor clientRequestor;
	INameLookup nameLookup;
	
public CompletionRequestorWrapper(ICodeCompletionRequestor clientRequestor, INameLookup nameLookup){
	this.clientRequestor = clientRequestor;
	this.nameLookup = nameLookup;
}
/**
 * See ICompletionRequestor
 */
public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd) {

	this.clientRequestor.acceptClass(packageName, className, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptError(IProblem error) {

	if (true) return; // work-around PR 1GD9RLP: ITPJCORE:WIN2000 - Code assist is slow
	try {
		IMarker marker = ResourcesPlugin.getWorkspace().getRoot().createMarker(IJavaModelMarker.TRANSIENT_PROBLEM);
		marker.setAttribute(IJavaModelMarker.ID, error.getID());
		marker.setAttribute(IMarker.CHAR_START, error.getSourceStart());
		marker.setAttribute(IMarker.CHAR_END, error.getSourceEnd() + 1);
		marker.setAttribute(IMarker.LINE_NUMBER, error.getSourceLineNumber());
		//marker.setAttribute(IMarker.LOCATION, "#" + error.getSourceLineNumber());
		marker.setAttribute(IMarker.MESSAGE, error.getMessage());
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

		this.clientRequestor.acceptError(marker);

	} catch(CoreException e){
	}
}
/**
 * See ICompletionRequestor
 */
public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	this.clientRequestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptInterface(char[] packageName, char[] interfaceName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	this.clientRequestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart, completionEnd);
	}
/**
 * See ICompletionRequestor
 */
public void acceptKeyword(char[] keywordName, int completionStart, int completionEnd) {
	this.clientRequestor.acceptKeyword(keywordName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptLabel(char[] labelName, int completionStart, int completionEnd) {
	this.clientRequestor.acceptLabel(labelName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptLocalVariable(char[] name, char[] typePackageName, char[] typeName, int modifiers, int completionStart, int completionEnd) {
	this.clientRequestor.acceptLocalVariable(name, typePackageName, typeName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	if(parameterNames == null)
		parameterNames = findMethodParameterNames(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);

	this.clientRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptMethodDeclaration(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	if(parameterNames == null) {
		int length = parameterTypeNames.length;
		
		parameterNames = findMethodParameterNames(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);
		
		StringBuffer completion = new StringBuffer(completionName.length);
			
		int start = 0;
		int end = CharOperation.indexOf('%', completionName);

		completion.append(CharOperation.subarray(completionName, start, end));
		
		for(int i = 0 ; i < length ; i++){
			completion.append(parameterNames[i]);
			start = end + 1;
			end = CharOperation.indexOf('%', completionName, start);
			if(end > -1){
				completion.append(CharOperation.subarray(completionName, start, end));
			} else {
				completion.append(CharOperation.subarray(completionName, start, completionName.length));
			}
		}
		
		completionName = completion.toString().toCharArray();
	}	
	this.clientRequestor.acceptMethodDeclaration(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptModifier(char[] modifierName, int completionStart, int completionEnd) {
	this.clientRequestor.acceptModifier(modifierName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptPackage(char[] packageName, char[] completionName, int completionStart, int completionEnd) {
	this.clientRequestor.acceptPackage(packageName, completionName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptType(char[] packageName, char[] typeName, char[] completionName, int completionStart, int completionEnd) {
	this.clientRequestor.acceptType(packageName, typeName, completionName, completionStart, completionEnd);
}

private char[][] findMethodParameterNames(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames){
	char[][] parameterNames = null;
	int length = parameterTypeNames.length;
	
	char[] typeName = CharOperation.concat(declaringTypePackageName,declaringTypeName,'.');
	IType type = nameLookup.findType(new String(typeName),false,INameLookup.ACCEPT_CLASSES &NameLookup.ACCEPT_INTERFACES);
	if(type instanceof BinaryType){
		String[] args = new String[length];
		for(int i = 0;	i< length ; i++){
			char[] parameterType = CharOperation.concat(parameterPackageNames[i],parameterTypeNames[i],'.');
			args[i] = Signature.createTypeSignature(parameterType,true);
		}
		IMethod method = type.getMethod(new String(selector),args);
		try{
			parameterNames = new char[length][];
			String[] params = method.getParameterNames();
			for(int i = 0;	i< length ; i++){
				parameterNames[i] = params[i].toCharArray();
			}
		} catch(JavaModelException e){
			parameterNames = null;
		}
			
	}
	// default parameters name
	if(parameterNames == null) {
		parameterNames = new char[length][];
		for (int i = 0; i < length; i++) {
			parameterNames[i] = CharOperation.concat(ARG, String.valueOf(i).toCharArray());
		}
	}
	return parameterNames;
}
}
