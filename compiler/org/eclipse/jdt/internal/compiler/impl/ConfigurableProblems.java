package org.eclipse.jdt.internal.compiler.impl;

public interface ConfigurableProblems {
	final int UnreachableCode = 0x100;
	final int ParsingOptionalError = 0x200;
	final int ImportProblem = 0x400;
	final int MethodWithConstructorName = 0x1000;
	final int OverriddenPackageDefaultMethod = 0x2000;
	final int UsingDeprecatedAPI = 0x4000;
	final int MaskedCatchBlock = 0x8000;
	final int UnusedLocalVariable = 0x10000;
	final int UnusedArgument = 0x20000;
	final int TemporaryWarning = 0x40000;
	final int AccessEmulation = 0x80000;
	final int NonExternalizedString = 0x100000;
}
