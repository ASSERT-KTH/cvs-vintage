/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;

/**
 * Compiler adapter for the JDT Compiler. 
 */
public class JDTCompilerAdapter extends DefaultCompilerAdapter {
	private static String compilerClass = "org.eclipse.jdt.internal.compiler.batch.Main"; //$NON-NLS-1$
	/**
	 * Performs a compile using the JDT batch compiler 
	 */
	public boolean execute() throws BuildException {
		attributes.log(Util.bind("ant.jdtadapter.info.usingJdtCompiler"), Project.MSG_VERBOSE); //$NON-NLS-1$
		Commandline cmd = setupJavacCommand();

		try {
			Class c = Class.forName(compilerClass);
			Method compile = c.getMethod("main", new Class[] { String[].class }); //$NON-NLS-1$
			compile.invoke(null, new Object[] { cmd.getArguments()});
		} catch (ClassNotFoundException cnfe) {
			throw new BuildException(Util.bind("ant.jdtadapter.error.missingJDTCompiler")); //$NON-NLS-1$
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
		return true;
	}
	
	
	protected Commandline setupJavacCommand() throws BuildException {
		Commandline cmd = new Commandline();
		
		/*
		 * This option is used to never exit at the end of the ant task. 
		 */
		cmd.createArgument().setValue("-noExit"); //$NON-NLS-1$

        Path classpath = new Path(project);

        /*
         * Eclipse compiler doesn't support bootclasspath dir (-bootclasspath).
         * It is emulated using the classpath. We add bootclasspath at the beginning of
         * the classpath.
         */
        if (bootclasspath != null && bootclasspath.size() != 0) {
            classpath.append(bootclasspath);
        } else {
            /*
             * No bootclasspath, we will add one throught the JRE_LIB variable
             */
			IPath jre_lib = JavaCore.getClasspathVariable("JRE_LIB"); //$NON-NLS-1$
			if (jre_lib == null) {
				throw new BuildException(Util.bind("ant.jdtadapter.error.missingJRELIB")); //$NON-NLS-1$
			}
			classpath.addExisting(new Path(null, jre_lib.toOSString()));
        }

        /*
         * Eclipse compiler doesn't support -extdirs.
         * It is emulated using the classpath. We add extdirs entries after the 
         * bootclasspath.
         */
        addExtdirsToClasspath(classpath);

		/*
		 * The java runtime is already handled, so we simply want to retrieve the
		 * ant runtime and the compile classpath.
		 */
		includeJavaRuntime = false;
        classpath.append(getCompileClasspath());

		/*
		 * Set the classpath for the Eclipse compiler.
		 */
		cmd.createArgument().setValue("-classpath"); //$NON-NLS-1$
		cmd.createArgument().setPath(classpath);

		/*
		 * Handle the nowarn option. If none, then we generate all warnings.
		 */		
        if (attributes.getNowarn()) {
            cmd.createArgument().setValue("-nowarn"); //$NON-NLS-1$
        } else {
			cmd.createArgument().setValue(
				"-warn:constructorName,packageDefaultMethod,maskedCatchBlocks,deprecation"); //$NON-NLS-1$
        }

		/*
		 * deprecation option.
		 */		
		if (deprecation) {
			cmd.createArgument().setValue("-deprecation"); //$NON-NLS-1$
		}

		/*
		 * destDir option.
		 */		
		if (destDir != null) {
			cmd.createArgument().setValue("-d"); //$NON-NLS-1$
			cmd.createArgument().setFile(destDir.getAbsoluteFile());
		}

		/*
		 * target option.
		 */		
		if (target != null) {
			cmd.createArgument().setValue("-target"); //$NON-NLS-1$
			cmd.createArgument().setValue(target);
		}

		/*
		 * debug option
		 */
		if (debug) {
			cmd.createArgument().setValue("-g"); //$NON-NLS-1$
		}

		/*
		 * verbose option
		 */
		if (verbose) {
			cmd.createArgument().setValue("-verbose"); //$NON-NLS-1$
			/*
			 * extra option allowed by the Eclipse compiler
			 */
			cmd.createArgument().setValue("-log"); //$NON-NLS-1$
			cmd.createArgument().setValue(destDir.getAbsolutePath() + ".log"); //$NON-NLS-1$
		}

		/*
		 * failnoerror option
		 */
		if (!attributes.getFailonerror()) {
			cmd.createArgument().setValue("-proceedOnError"); //$NON-NLS-1$
		}

		/*
		 * extra option allowed by the Eclipse compiler
		 */
		cmd.createArgument().setValue("-time"); //$NON-NLS-1$

		/*
		 * extra option allowed by the Eclipse compiler
		 */
		cmd.createArgument().setValue("-noImportError"); //$NON-NLS-1$

		/*
		 * source option
		 */
		String source = attributes.getSource();
        if (source != null) {
            cmd.createArgument().setValue("-source"); //$NON-NLS-1$
            cmd.createArgument().setValue(source);
        }

		/*
		 * encoding option
		 */
        if (encoding != null) {
            cmd.createArgument().setValue("-encoding"); //$NON-NLS-1$
            cmd.createArgument().setValue(encoding);
        }

		/*
		 * Eclipse compiler doesn't have a -sourcepath option. This is
		 * handled through the javac task that collects all source files in
		 * srcdir option.
		 */        
        logAndAddFilesToCompile(cmd);
		return cmd;
	}
}