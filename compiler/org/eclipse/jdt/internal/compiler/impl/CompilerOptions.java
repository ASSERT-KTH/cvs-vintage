package org.eclipse.jdt.internal.compiler.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Locale;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompilerOptions implements ConfigurableProblems, ProblemIrritants, ProblemReasons, ProblemSeverities {

	// class file output
	// these are the bits used to buld a mask to know which debug 
	// attributes should be included in the .class file
	// By default only lines and source attributes are generated.
	public static final int Source = 1; // SourceFileAttribute
	public static final int Lines = 2; // LineNumberAttribute
	public static final int Vars = 4; // LocalVariableTableAttribute

	public int produceDebugAttributes = Lines | Source;

	// default severity level for handlers
	public int errorThreshold = UnreachableCode | ImportProblem;
	public int warningThreshold =
		ParsingOptionalError | 
		MethodWithConstructorName | OverriddenPackageDefaultMethod |
		UsingDeprecatedAPI | MaskedCatchBlock |
		UnusedLocalVariable | UnusedArgument |
		TemporaryWarning;

	// target JDK 1.1 or 1.2
	public static final int JDK1_1 = 0;
	public static final int JDK1_2 = 1;
	public int targetJDK = JDK1_1; // default generates for JVM1.1

	// print what unit is being processed
	public boolean verbose = false;
	// indicates if reference info is desired
	public boolean produceReferenceInfo = true;
	// indicates if unused/optimizable local variables need to be preserved (debugging purpose)
	public boolean preserveAllLocalVariables = false;
	// indicates whether literal expressions are inlined at parse-time or not
	public boolean parseLiteralExpressionsAsConstants = true;

	// exception raised for unresolved compile errors
	public String runtimeExceptionNameForCompileError = "java.lang.Error";

	// toggle private access emulation for 1.2 (constr. accessor has extra arg on constructor) or 1.3 (make private constructor default access when access needed)
	public boolean isPrivateConstructorAccessChangingVisibility = false; // by default, follows 1.2
/** 
 * Initializing the compiler options with defaults
 */
public CompilerOptions(){
}
/** 
 * Initializing the compiler options with external settings
 */
public CompilerOptions(ConfigurableOption[] settings){
	if (settings == null) return;
	// filter options which are related to the compiler component
	String componentName = Compiler.class.getName();
	for (int i = 0, max = settings.length; i < max; i++){
		if (settings[i].getComponentName().equals(componentName)){
			this.setOption(settings[i]);
		}
	}
}
/**
 * Returns all the options of the compiler to be shown by the UI
 *
 * @param locale java.util.Locale
 * @return org.eclipse.jdt.internal.compiler.ConfigurableOption[]
 */
public ConfigurableOption[] getConfigurableOptions(Locale locale) {
	String componentName = Compiler.class.getName();
	return new ConfigurableOption[] {
		new ConfigurableOption(
			componentName,
			"debug.vars", 
			locale, 
			(produceDebugAttributes & Vars) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"debug.lines", 
			locale, 
			(produceDebugAttributes & Lines) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"debug.source", 
			locale, 
			(produceDebugAttributes & Source) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"debug.preserveAllLocals", 
			locale, 
			preserveAllLocalVariables ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalError.unReachableCode", 
			locale, 
			(errorThreshold & UnreachableCode) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalError.importProblem", 
			locale, 
			(errorThreshold & ImportProblem) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalWarning.methodWithConstructorName", 
			locale, 
			(warningThreshold & MethodWithConstructorName) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalWarning.overridingPackageDefaultMethod", 
			locale, 
			(warningThreshold & OverriddenPackageDefaultMethod) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalWarning.deprecated", 
			locale, 
			(warningThreshold & UsingDeprecatedAPI) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalWarning.maskedCatchBlock", 
			locale, 
			(warningThreshold & MaskedCatchBlock) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalWarning.unusedLocalVariable", 
			locale, 
			(warningThreshold & UnusedLocalVariable) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"optionalWarning.unusedArgument", 
			locale, 
			(warningThreshold & UnusedArgument) != 0 ? 0 : 1), 
		new ConfigurableOption(
			componentName,
			"binaryCompatibility.targetJDK", 
			locale, 
			targetJDK), 
		new ConfigurableOption(
			componentName,
			"optionalWarning.accessEmulation", 
			locale, 
			(warningThreshold & AccessEmulation) != 0 ? 0 : 1),
		new ConfigurableOption(
			componentName,
			"optionalWarning.nonExternalizedString", 
			locale, 
			(warningThreshold & NonExternalizedString) != 0 ? 0 : 1)
		}; 
}
public int getDebugAttributesMask() {
	return this.produceDebugAttributes;
}
public int getTargetJDK() {
	return this.targetJDK;
}
public void handleAccessEmulationAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= AccessEmulation;
	} else {
		warningThreshold &= ~AccessEmulation;
	}
}
public void handleDeprecationUseAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= UsingDeprecatedAPI;
	} else {
		warningThreshold &= ~UsingDeprecatedAPI;
	}
}
public void handleImportProblemAsError(boolean flag) {
	if (flag) {
		errorThreshold |= ImportProblem;
		warningThreshold &= ~ImportProblem;
	} else {
		errorThreshold &= ~ImportProblem;
		warningThreshold |= ImportProblem;
	}
}
public void handleMaskedCatchBlockAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= MaskedCatchBlock;
	} else {
		warningThreshold &= ~MaskedCatchBlock;
	}
}
public void handleMethodWithConstructorNameAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= MethodWithConstructorName;
	} else {
		warningThreshold &= ~MethodWithConstructorName;
	}
}
public void handleObsoleteLiteralAsError(boolean flag) {
	if (flag) {
		errorThreshold |= ParsingOptionalError;
		warningThreshold &= ~ParsingOptionalError;
	} else {
		errorThreshold &= ~ParsingOptionalError;
		warningThreshold |= ParsingOptionalError;
	}
}
public void handleOverriddenPackageDefaultMethodAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= OverriddenPackageDefaultMethod;
	} else {
		warningThreshold &= ~OverriddenPackageDefaultMethod;
	}
}
public void handleUnreachableCodeAsError(boolean flag) {
	if (flag) {
		errorThreshold |= UnreachableCode;
		warningThreshold &= ~UnreachableCode;
	} else {
		errorThreshold &= ~UnreachableCode;
		warningThreshold |= UnreachableCode;
	}	
}
public void handleUnusedArgumentAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= UnusedArgument;
	} else {
		warningThreshold &= ~UnusedArgument;
	}
}
public void handleUnusedLocalVariableAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= UnusedLocalVariable;
	} else {
		warningThreshold &= ~UnusedLocalVariable;
	}
}
public boolean isAccessEmulationHandledAsWarning() {
	return (warningThreshold & AccessEmulation) != 0;
}
public boolean isDeprecationUseHandledAsWarning() {
	return (warningThreshold & UsingDeprecatedAPI) != 0;
}
public boolean isImportProblemHandledAsError() {
	return (errorThreshold & ImportProblem) != 0;
}
public boolean isMaskedCatchBlockHandledAsWarning() {
	return (warningThreshold & MaskedCatchBlock) != 0;
}
public boolean isMethodWithConstructorNameHandledAsWarning() {
	return (warningThreshold & MethodWithConstructorName) != 0;
}
public boolean isObsoleteLiteralAsHandledError() {
	return (errorThreshold & ParsingOptionalError) != 0;
}
public boolean isOverriddenPackageDefaultMethodHandledAsWarning() {
	return (warningThreshold & OverriddenPackageDefaultMethod) != 0;
}
public boolean isPreservingAllLocalVariables() {
	return this.preserveAllLocalVariables ;
}
public boolean isPrivateConstructorAccessChangingVisibility() {
	return isPrivateConstructorAccessChangingVisibility;
}
public boolean isUnreachableCodeHandledAsError() {
	return (errorThreshold & UnreachableCode) != 0;
}
public boolean isUnusedArgumentHandledAsWarning() {
	return (warningThreshold & UnusedArgument) != 0;
}
public boolean isUnusedLocalVariableHandledAsWarning() {
	return (warningThreshold & UnusedLocalVariable) != 0;
}
public void preserveAllLocalVariables(boolean flag) {
	this.preserveAllLocalVariables = flag;
}
public void privateConstructorAccessChangesVisibility(boolean flag) {
	isPrivateConstructorAccessChangingVisibility = flag;
}
public void produceDebugAttributes(int mask) {
	this.produceDebugAttributes = mask;
}
public void produceReferenceInfo(boolean flag) {
	this.produceReferenceInfo = flag;
}
public void setErrorThreshold(int errorMask) {
	this.errorThreshold = errorMask;
}
/**
 * Change the value of the option corresponding to the option number
 *
 * @param optionNumber <CODE>int</CODE>
 * @param setting.getCurrentValueIndex() <CODE>int</CODE>
 */
void setOption(ConfigurableOption setting) {
	
	switch (setting.getID()) {
		case 1 : // Local variable table attribute
			if (setting.getCurrentValueIndex() == 0) {
				// set the debug flag with Vars.
				produceDebugAttributes |= Vars;
			} else {
				produceDebugAttributes &= ~Vars;
			}
			break;
		case 2 : // Line number attribute
			if (setting.getCurrentValueIndex() == 0) {
				// set the debug flag with Lines
				produceDebugAttributes |= Lines;
			} else {
				produceDebugAttributes &= ~Lines;
			}
			break;
		case 3 : // source file attribute
			if (setting.getCurrentValueIndex() == 0) {
				// set the debug flag with Source.
				produceDebugAttributes |= Source;
			} else {
				produceDebugAttributes &= ~Source;
			}
			break;
		case 4 : // preserveAllLocals flag
			preserveAllLocalVariables(setting.getCurrentValueIndex() == 0);
			break;
		case 5 : // unreachable code reported as error
			handleUnreachableCodeAsError(setting.getCurrentValueIndex() == 0);
			break;
		case 6 : // invalid import
			handleImportProblemAsError(setting.getCurrentValueIndex() == 0);
			break;
		case 7 : // methods with constructor name
			handleMethodWithConstructorNameAsWarning(setting.getCurrentValueIndex() == 0);
			break;
		case 8 : // overridden package default method
			handleOverriddenPackageDefaultMethodAsWarning(setting.getCurrentValueIndex() == 0);
			break;
		case 9 : // use of deprecated API
			handleDeprecationUseAsWarning(setting.getCurrentValueIndex() == 0);
			break;
		case 10 : // catch block hidden by another one
			handleMaskedCatchBlockAsWarning(setting.getCurrentValueIndex() == 0);
			break;
		case 11 : // local variable not used
			handleUnusedLocalVariableAsWarning(setting.getCurrentValueIndex() == 0);
			break;
		case 12 : // argument not used
			handleUnusedArgumentAsWarning(setting.getCurrentValueIndex() == 0);
			break;
		case 13 : // temporary warning
			if (setting.getCurrentValueIndex() == 0) {
				warningThreshold |= TemporaryWarning;
			}
			break;
		case 14 : // target JDK
			setTargetJDK(setting.getCurrentValueIndex() == 0 ? JDK1_1 : JDK1_2);
			break;
		case 15: // synthetic access emulation
			handleAccessEmulationAsWarning(setting.getCurrentValueIndex() == 0);
			break;
		case 16: // non externalized string literal
			handleNonExternalizedStringLiteralAsWarning(setting.getCurrentValueIndex() == 0);
			break;
	}
}
public void setTargetJDK(int vmID) {
	this.targetJDK = vmID;
}
public void setVerboseMode(boolean flag) {
	this.verbose = flag;
}
public void setWarningThreshold(int warningMask) {
	this.warningThreshold = warningMask;
}
public String toString() {

	StringBuffer buf = new StringBuffer("CompilerOptions:");
	if ((produceDebugAttributes & Vars) != 0){
		buf.append("\n-local variables debug attributes: ON");
	} else {
		buf.append("\n-local variables debug attributes: OFF");
	}
	if ((produceDebugAttributes & Lines) != 0){
		buf.append("\n-line number debug attributes: ON");
	} else {
		buf.append("\n-line number debug attributes: OFF");
	}
	if ((produceDebugAttributes & Source) != 0){
		buf.append("\n-source debug attributes: ON");
	} else {
		buf.append("\n-source debug attributes: OFF");
	}
	if (preserveAllLocalVariables){
		buf.append("\n-preserve all local variables: ON");
	} else {
		buf.append("\n-preserve all local variables: OFF");
	}
	if ((errorThreshold & UnreachableCode) != 0){
		buf.append("\n-unreachable code: ERROR");
	} else {
		if ((warningThreshold & UnreachableCode) != 0){
			buf.append("\n-unreachable code: WARNING");
		} else {
			buf.append("\n-unreachable code: IGNORE");
		}
	}
	if ((errorThreshold & ImportProblem) != 0){
		buf.append("\n-import problem: ERROR");
	} else {
		if ((warningThreshold & ImportProblem) != 0){
			buf.append("\n-import problem: WARNING");
		} else {
			buf.append("\n-import problem: IGNORE");
		}
	}
	if ((errorThreshold & MethodWithConstructorName) != 0){
		buf.append("\n-method with constructor name: ERROR");		
	} else {
		if ((warningThreshold & MethodWithConstructorName) != 0){
			buf.append("\n-method with constructor name: WARNING");
		} else {
			buf.append("\n-method with constructor name: IGNORE");
		}
	}
	if ((errorThreshold & OverriddenPackageDefaultMethod) != 0){
		buf.append("\n-overridden package default method: ERROR");
	} else {
		if ((warningThreshold & OverriddenPackageDefaultMethod) != 0){
			buf.append("\n-overridden package default method: WARNING");
		} else {
			buf.append("\n-overridden package default method: IGNORE");
		}
	}
	if ((errorThreshold & UsingDeprecatedAPI) != 0){
		buf.append("\n-deprecation: ERROR");
	} else {
		if ((warningThreshold & UsingDeprecatedAPI) != 0){
			buf.append("\n-deprecation: WARNING");
		} else {
			buf.append("\n-deprecation: IGNORE");
		}
	}
	if ((errorThreshold & MaskedCatchBlock) != 0){
		buf.append("\n-masked catch block: ERROR");
	} else {
		if ((warningThreshold & MaskedCatchBlock) != 0){
			buf.append("\n-masked catch block: WARNING");
		} else {
			buf.append("\n-masked catch block: IGNORE");
		}
	}
	if ((errorThreshold & UnusedLocalVariable) != 0){
		buf.append("\n-unused local variable: ERROR");
	} else {
		if ((warningThreshold & UnusedLocalVariable) != 0){
			buf.append("\n-unused local variable: WARNING");
		} else {
			buf.append("\n-unused local variable: IGNORE");
		}
	}
	if ((errorThreshold & UnusedArgument) != 0){
		buf.append("\n-unused parameter: ERROR");
	} else {
		if ((warningThreshold & UnusedArgument) != 0){
			buf.append("\n-unused parameter: WARNING");
		} else {
			buf.append("\n-unused parameter: IGNORE");
		}
	}
	if ((errorThreshold & AccessEmulation) != 0){
		buf.append("\n-synthetic access emulation: ERROR");
	} else {
		if ((warningThreshold & AccessEmulation) != 0){
			buf.append("\n-synthetic access emulation: WARNING");
		} else {
			buf.append("\n-synthetic access emulation: IGNORE");
		}
	}
	if ((errorThreshold & NonExternalizedString) != 0){
		buf.append("\n-non externalized string: ERROR");
	} else {
		if ((warningThreshold & NonExternalizedString) != 0){
			buf.append("\n-non externalized string: WARNING");
		} else {
			buf.append("\n-non externalized string: IGNORE");
		}
	}
	switch(targetJDK){
		case JDK1_1 :
			buf.append("\n-target JDK: 1.1");
			break;
		case JDK1_2 :
			buf.append("\n-target JDK: 1.2");
	}
	buf.append("\n-verbose : " + (verbose ? "ON" : "OFF"));
	buf.append("\n-produce reference info : " + (produceReferenceInfo ? "ON" : "OFF"));
	buf.append("\n-parse literal expressions as constants : " + (parseLiteralExpressionsAsConstants ? "ON" : "OFF"));
	buf.append("\n-runtime exception name for compile error : " + runtimeExceptionNameForCompileError);
	return buf.toString();
}

public void handleNonExternalizedStringLiteralAsWarning(boolean flag) {
	if (flag) {
		warningThreshold |= NonExternalizedString;
	} else {
		warningThreshold &= ~NonExternalizedString;
	}
}

public boolean isNonExternalizedStringLiteralHandledAsWarning() {
	return (warningThreshold & NonExternalizedString) != 0;
}
}
