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
package org.eclipse.jdt.internal.core;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Responsible for resolving types inside a compilation unit being reconciled,
 * reporting the discovered problems to a given IProblemRequestor.
 */
public class CompilationUnitProblemFinder extends Compiler {

	/**
	 * Answer a new CompilationUnitVisitor using the given name environment and compiler options.
	 * The environment and options will be in effect for the lifetime of the compiler.
	 * When the compiler is run, compilation results are sent to the given requestor.
	 *
	 *  @param environment org.eclipse.jdt.internal.compiler.api.env.INameEnvironment
	 *      Environment used by the compiler in order to resolve type and package
	 *      names. The name environment implements the actual connection of the compiler
	 *      to the outside world (e.g. in batch mode the name environment is performing
	 *      pure file accesses, reuse previous build state or connection to repositories).
	 *      Note: the name environment is responsible for implementing the actual classpath
	 *            rules.
	 *
	 *  @param policy org.eclipse.jdt.internal.compiler.api.problem.IErrorHandlingPolicy
	 *      Configurable part for problem handling, allowing the compiler client to
	 *      specify the rules for handling problems (stop on first error or accumulate
	 *      them all) and at the same time perform some actions such as opening a dialog
	 *      in UI when compiling interactively.
	 *      @see org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies
	 * 
	 *	@param settings The settings to use for the resolution.
	 *      
	 *  @param requestor org.eclipse.jdt.internal.compiler.api.ICompilerRequestor
	 *      Component which will receive and persist all compilation results and is intended
	 *      to consume them as they are produced. Typically, in a batch compiler, it is 
	 *      responsible for writing out the actual .class files to the file system.
	 *      @see org.eclipse.jdt.internal.compiler.CompilationResult
	 *
	 *  @param problemFactory org.eclipse.jdt.internal.compiler.api.problem.IProblemFactory
	 *      Factory used inside the compiler to create problem descriptors. It allows the
	 *      compiler client to supply its own representation of compilation problems in
	 *      order to avoid object conversions. Note that the factory is not supposed
	 *      to accumulate the created problems, the compiler will gather them all and hand
	 *      them back as part of the compilation unit result.
	 */
	protected CompilationUnitProblemFinder(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory) {

		super(environment, policy, settings, requestor, problemFactory, true);
	}

	/**
	 * Add additional source types
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		// ensure to jump back to toplevel type for first one (could be a member)
//		while (sourceTypes[0].getEnclosingType() != null)
//			sourceTypes[0] = sourceTypes[0].getEnclosingType();

		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), 1, 1, this.options.maxProblemsPerUnit);

		// need to hold onto this
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,//sourceTypes[0] is always toplevel here
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE // need member types
				| SourceTypeConverter.FIELD_INITIALIZATION, // need field initialization
				this.lookupEnvironment.problemReporter,
				result);

		if (unit != null) {
			this.lookupEnvironment.buildTypeBindings(unit, accessRestriction);
			this.lookupEnvironment.completeTypeBindings(unit);
		}
	}

	/*
	 *  Low-level API performing the actual compilation
	 */
	protected static IErrorHandlingPolicy getHandlingPolicy() {
		return DefaultErrorHandlingPolicies.proceedWithAllProblems();
	}

	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	protected static ICompilerRequestor getRequestor() {
		return new ICompilerRequestor() {
			public void acceptResult(CompilationResult compilationResult) {
				// default requestor doesn't handle compilation results back
			}
		};
	}

	public static CompilationUnitDeclaration process(
		CompilationUnitDeclaration unit,
		ICompilationUnit unitElement, 
		char[] contents,
		Parser parser,
		WorkingCopyOwner workingCopyOwner,
		IProblemRequestor problemRequestor,
		boolean resetEnvironment,
		IProgressMonitor monitor)
		throws JavaModelException {

		JavaProject project = (JavaProject) unitElement.getJavaProject();
		CancelableNameEnvironment environment = null;
		CancelableProblemFactory problemFactory = null;
		CompilationUnitProblemFinder problemFinder = null;
		try {
			environment = new CancelableNameEnvironment(project, workingCopyOwner, monitor);
			problemFactory = new CancelableProblemFactory(monitor);
			problemFinder = new CompilationUnitProblemFinder(
				environment,
				getHandlingPolicy(),
				project.getOptions(true),
				getRequestor(),
				problemFactory);
			if (parser != null) {
				problemFinder.parser = parser;
			}
			PackageFragment packageFragment = (PackageFragment)unitElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
			char[][] expectedPackageName = null;
			if (packageFragment != null){
				expectedPackageName = Util.toCharArrays(packageFragment.names);
			}
			if (unit == null) {
				unit = problemFinder.resolve(
					new BasicCompilationUnit(
						contents,
						expectedPackageName,
						unitElement.getPath().toString(),
						unitElement),
					true, // verify methods
					true, // analyze code
					true); // generate code
			} else {
				problemFinder.resolve(
					unit,
					null, // no need for source
					true, // verify methods
					true, // analyze code
					true); // generate code
			}
			reportProblems(unit, problemRequestor, monitor);
			if (NameLookup.VERBOSE)
				System.out.println(Thread.currentThread() + " TIME SPENT in NameLoopkup#seekTypesInSourcePackage: " + environment.nameLookup.timeSpentInSeekTypesInSourcePackage + "ms");  //$NON-NLS-1$ //$NON-NLS-2$
			return unit;
		} catch (OperationCanceledException e) {
			throw e;
		} catch(RuntimeException e) { 
			// avoid breaking other tools due to internal compiler failure (40334)
			Util.log(e, "Exception occurred during problem detection: "); //$NON-NLS-1$ 
			throw new JavaModelException(e, IJavaModelStatusConstants.COMPILER_FAILURE);
		} finally {
			if (environment != null)
				environment.monitor = null; // don't hold a reference to this external object
			if (problemFactory != null)
				problemFactory.monitor = null; // don't hold a reference to this external object
			// NB: unit.cleanUp() is done by caller
			if (problemFinder != null && resetEnvironment)
				problemFinder.lookupEnvironment.reset();			
		}
	}

	public static CompilationUnitDeclaration process(
		ICompilationUnit unitElement, 
		char[] contents,
		WorkingCopyOwner workingCopyOwner,
		IProblemRequestor problemRequestor,
		boolean resetEnvironment,
		IProgressMonitor monitor)
		throws JavaModelException {
			
		return process(null/*no CompilationUnitDeclaration*/, unitElement, contents, null/*use default Parser*/, workingCopyOwner, problemRequestor, resetEnvironment, monitor);
	}

	
	private static void reportProblems(CompilationUnitDeclaration unit, IProblemRequestor problemRequestor, IProgressMonitor monitor) {
		CompilationResult unitResult = unit.compilationResult;
		IProblem[] problems = unitResult.getAllProblems();
		for (int i = 0, problemLength = problems == null ? 0 : problems.length; i < problemLength; i++) {
			if (JavaModelManager.VERBOSE){
				System.out.println("PROBLEM FOUND while reconciling : "+problems[i].getMessage());//$NON-NLS-1$
			}
			if (monitor != null && monitor.isCanceled()) break;
			problemRequestor.acceptProblem(problems[i]);				
		}
	}

	/* (non-Javadoc)
	 * Fix for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=60689.
	 * @see org.eclipse.jdt.internal.compiler.Compiler#initializeParser()
	 */
	public void initializeParser() {
		this.parser = new CommentRecorderParser(this.problemReporter, this.options.parseLiteralExpressionsAsConstants);
	}
}	

