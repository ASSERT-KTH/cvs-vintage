package org.eclipse.jdt.internal.compiler.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Converter from source element type to parsed compilation unit.
 *
 * Limitation:
 * | The source element field does not carry any information for its constant part, thus
 * | the converted parse tree will not include any field initializations.
 * | Therefore, any binary produced by compiling against converted source elements will
 * | not take advantage of remote field constant inlining.
 * | Given the intended purpose of the conversion is to resolve references, this is not
 * | a problem.
 *
 */

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class SourceTypeConverter implements CompilerModifiers {
/*
 * Convert a source element type into a parsed type declaration
 *
 * Can optionally ignore fields & methods
 *
 * @deprecated - should use the other API with one extra boolean
 */
public static CompilationUnitDeclaration buildCompilationUnit(
	ISourceType sourceType,
	boolean needFieldsAndMethods,
	ProblemReporter problemReporter,
	CompilationResult compilationResult) {

	return buildCompilationUnit(sourceType, needFieldsAndMethods, true, problemReporter, compilationResult);
}
/*
 * Convert a source element type into a parsed type declaration
 *
 * Can optionally ignore fields & methods or member types
 */
public static CompilationUnitDeclaration buildCompilationUnit(
	ISourceType sourceType,
	boolean needFieldsAndMethods,
	boolean needMemberTypes,
	ProblemReporter problemReporter,
	CompilationResult compilationResult) {

	if (sourceType.getName() == null) return null; // do a basic test that the sourceType is valid

	CompilationUnitDeclaration compilationUnit = 
		new CompilationUnitDeclaration(problemReporter, compilationResult, 0); // not filled at this point

	/* only positions available */
	int start = sourceType.getNameSourceStart();
	int end = sourceType.getNameSourceEnd();
		
	/* convert package and imports */
	if (sourceType.getPackageName() != null && sourceType.getPackageName().length > 0) // if its null then it is defined in the default package
		compilationUnit.currentPackage = createImportReference(sourceType.getPackageName(), start, end);
	char[][] importNames = sourceType.getImports();
	int importCount = importNames == null ? 0 : importNames.length;
	compilationUnit.imports = new ImportReference[importCount];
	for (int i = 0; i < importCount; i++)
		compilationUnit.imports[i] = createImportReference(importNames[i], start, end);
	/* convert type */
	compilationUnit.types = new TypeDeclaration[] {convert(sourceType, needFieldsAndMethods, needMemberTypes)}; 

	return compilationUnit;
}
/*
 * Convert a field source element into a parsed field declaration
 */

private static FieldDeclaration convert(ISourceField sourceField) {

	FieldDeclaration field = new FieldDeclaration();

	int start = sourceField.getNameSourceStart();
	int end = sourceField.getNameSourceEnd();

	field.name = sourceField.getName();
	field.sourceStart = start;
	field.sourceEnd = end;
	field.type = createTypeReference(sourceField.getTypeName(), start, end);
	field.declarationSourceStart = sourceField.getDeclarationSourceStart();
	field.declarationSourceEnd = sourceField.getDeclarationSourceEnd();
	field.modifiers = sourceField.getModifiers();
	
	/* conversion of field constant: if not present, then cannot generate binary against 
		converted parse nodes */
	/*
	if (field.modifiers & AccFinal){
		char[] initializationSource = sourceField.getInitializationSource();
	}
	*/
	return field;
}
/*
 * Convert a method source element into a parsed method/constructor declaration 
 */
private static AbstractMethodDeclaration convert(ISourceMethod sourceMethod) {

	AbstractMethodDeclaration method;

	/* only source positions available */
	int start = sourceMethod.getNameSourceStart();
	int end = sourceMethod.getNameSourceEnd();

	if (sourceMethod.isConstructor()) {
		ConstructorDeclaration decl = new ConstructorDeclaration();
		decl.isDefaultConstructor = false;
		method = decl;
	} else {
		MethodDeclaration decl = new MethodDeclaration();
		/* convert return type */
		decl.returnType = 
			createTypeReference(sourceMethod.getReturnTypeName(), start, end); 
		method = decl;
	}
	method.selector = sourceMethod.getSelector();
	method.modifiers = sourceMethod.getModifiers();
	method.sourceStart = start;
	method.sourceEnd = end;
	method.declarationSourceStart = sourceMethod.getDeclarationSourceStart();
	method.declarationSourceEnd = sourceMethod.getDeclarationSourceEnd();

	/* convert arguments */
	char[][] argumentTypeNames = sourceMethod.getArgumentTypeNames();
	char[][] argumentNames = sourceMethod.getArgumentNames();
	int argumentCount = argumentTypeNames == null ? 0 : argumentTypeNames.length;
	long position = (long) start << 32 + end;
	method.arguments = new Argument[argumentCount];
	for (int i = 0; i < argumentCount; i++) {
		method.arguments[i] = 
			new Argument(
				argumentNames[i], 
				position, 
				createTypeReference(argumentTypeNames[i], start, end), 
				AccDefault); // do not care whether was final or not
	}

	/* convert thrown exceptions */
	char[][] exceptionTypeNames = sourceMethod.getExceptionTypeNames();
	int exceptionCount = exceptionTypeNames == null ? 0 : exceptionTypeNames.length;
	method.thrownExceptions = new TypeReference[exceptionCount];
	for (int i = 0; i < exceptionCount; i++) {
		method.thrownExceptions[i] = 
			createTypeReference(exceptionTypeNames[i], start, end); 
	}
	return method;
}
/*
 * Convert a source element type into a parsed type declaration
 *
 * Can optionally ignore fields & methods
 */
private static TypeDeclaration convert(ISourceType sourceType, boolean needFieldsAndMethods, boolean needMemberTypes) {

	/* create type declaration - can be member type */
	TypeDeclaration type;
	if (sourceType.getEnclosingType() == null){
		type = new TypeDeclaration();
	} else {
		type = new MemberTypeDeclaration();
	}
	type.name = sourceType.getName();
	int start, end; // only positions available
	type.sourceStart = start = sourceType.getNameSourceStart();
	type.sourceEnd = end = sourceType.getNameSourceEnd();
	type.modifiers = sourceType.getModifiers();
	type.declarationSourceStart = sourceType.getDeclarationSourceStart();
	type.declarationSourceEnd = sourceType.getDeclarationSourceEnd();

	/* set superclass and superinterfaces */
	if (sourceType.getSuperclassName() != null)
		type.superclass = createTypeReference(sourceType.getSuperclassName(), start, end);
	char[][] interfaceNames = sourceType.getInterfaceNames();
	int interfaceCount = interfaceNames == null ? 0 : interfaceNames.length;
	type.superInterfaces = new TypeReference[interfaceCount];
	for (int i = 0; i < interfaceCount; i++){
		type.superInterfaces[i] = createTypeReference(interfaceNames[i], start, end);
	}	
	/* convert member types */
	if (needMemberTypes){
		ISourceType[] sourceMemberTypes = sourceType.getMemberTypes();
		int sourceMemberTypeCount = sourceMemberTypes == null ? 0 : sourceMemberTypes.length;
		type.memberTypes = new MemberTypeDeclaration[sourceMemberTypeCount];
		for (int i = 0; i < sourceMemberTypeCount; i++) {
			type.memberTypes[i] = (MemberTypeDeclaration) convert(sourceMemberTypes[i], needFieldsAndMethods, true);
		}
	}
	/* convert fields and methods */
	if (needFieldsAndMethods) {
		/* convert fields */
		ISourceField[] sourceFields = sourceType.getFields();
		int sourceFieldCount = sourceFields == null ? 0 : sourceFields.length;
		type.fields = new FieldDeclaration[sourceFieldCount];
		for (int i = 0; i < sourceFieldCount; i++) {
			type.fields[i] = convert(sourceFields[i]);
		}

		/* convert methods - need to add default constructor if necessary */
		ISourceMethod[] sourceMethods = sourceType.getMethods();
		int sourceMethodCount = sourceMethods == null ? 0 : sourceMethods.length;

		/* source type has a constructor ?           */
		/* by default, we assume that one is needed. */
		int neededCount = 1;
		for (int i = 0; i < sourceMethodCount; i++) {
			if (sourceMethods[i].isConstructor()) {
				neededCount = 0; // Does not need the extra constructor since one constructor already exists.
				break;
			}
		}
		type.methods = 
			new AbstractMethodDeclaration[sourceMethodCount + neededCount]; 
		if (neededCount != 0){ // add default constructor in first position
			type.methods[0] = type.createsInternalConstructor(false, false);
		}
		for (int i = 0; i < sourceMethodCount; i++) {
			type.methods[neededCount + i] = convert(sourceMethods[i]);
		}
	}
	return type;
}
/*
 * Build an import reference from an import name, e.g. java.lang.*
 */
private static ImportReference createImportReference(
	char[] importName, 
	int start, 
	int end) {

	/* count identifiers */
	int max = importName.length;
	int identCount = 0;
	for (int i = 0; i < max; i++) {
		if (importName[i] == '.') identCount++;
	}
	/* import on demand? */
	boolean onDemand = importName[max-1] == '*';
	if (!onDemand) identCount++; // one more ident than dots

	long[] positions = new long[identCount];
	long position = (long) start << 32 + end;
	for (int i = 0; i < identCount; i++){
		positions[i] = position;
	}
	return new ImportReference( 
		CharOperation.splitOn('.', importName, 0, max - (onDemand ? 3 : 1)),
		positions,
		onDemand);
}
/*
 * Build a type reference from a readable name, e.g. java.lang.Object[][]
 */
private static TypeReference createTypeReference(
	char[] typeSignature, 
	int start, 
	int end) {

	/* count identifiers and dimensions */
	int max = typeSignature.length;
	int dimStart = max;
	int dim = 0;
	int identCount = 1;
	for (int i = 0; i < max; i++) {
		switch (typeSignature[i]) {
			case '[' :
				if (dim == 0)
					dimStart = i;
				dim++;
				break;
			case '.' :
				identCount++;
				break;
		}
	}
	/* rebuild identifiers and dimensions */
	if (identCount == 1) { // simple type reference
		if (dim == 0) {
			return new SingleTypeReference(typeSignature, (long)start << 32  + end);
		} else {
			char[] identifier = new char[dimStart];
			System.arraycopy(typeSignature, 0, identifier, 0, dimStart);
			return new ArrayTypeReference(identifier, dim, (long)start << 32  + end);
		}
	} else { // qualified type reference
		long[] positions = new long[identCount];
		long pos = (long)start << 32  + end;
		for(int i = 0; i < identCount; i++){
			positions[i] = pos;
		}
		char[][] identifiers = CharOperation.splitOn('.', typeSignature, 0, dimStart - 1);
		if (dim == 0){
			return new QualifiedTypeReference(identifiers, positions);
		} else {
			return new ArrayQualifiedTypeReference(identifiers, dim, positions);			
		}
	}
}
}
