/*
 * Copyright (C) 2002-2003, Simon Nieuviarts
 */
/***
 * Jonathan: an Open Distributed Processing Environment 
 * Copyright (C) 1999 France Telecom R&D
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Release: 2.0
 *
 * Contact: jonathan@objectweb.org
 *
 * Author: Kathleen Milsted
 *
 * with contributions from:
 *   Francois Horn
 *   Bruno Dumant
 *   Vincent Sheffer 
 * 
 */
package org.objectweb.carol.cmi.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * This is a utility class containing useful static methods for the stub
 * compiler.
 */
public class Utils {
    static void compileFile(Compiler cCtx, String fullFileName)
        throws CompilerException {
        String command =
            cCtx.getCompiler()
                + " -d "
                + cCtx.getDestDir()
                + " "
                + fullFileName;

        String classpath = "";
        if (cCtx.getClassPath() != null) {
            classpath =
                cCtx.getClassPath()
                    + System.getProperty("path.separator", "")
                    + System.getProperty("java.class.path", "");
        } else {
            classpath = System.getProperty("java.class.path", "");
        }

        String[] env = new String[] { "CLASSPATH=" + classpath };

        Process proc;
        try {
            proc = Runtime.getRuntime().exec(command, env);
        } catch (IOException e) {
            throw new CompilerException("While compiling " + fullFileName, e);
        }

        Thread stdoutThread =
            new Thread(
                new RunnableStreamListener(
                    new BufferedReader(
                        new InputStreamReader(proc.getInputStream())),
                    System.out),
                "stdout listener for " + cCtx.getCompiler());
        stdoutThread.start();

        Thread stderrThread =
            new Thread(
                new RunnableStreamListener(
                    new BufferedReader(
                        new InputStreamReader(proc.getErrorStream())),
                    System.err),
                "stderr listener for " + cCtx.getCompiler());
        stderrThread.start();
        int n;
        try {
            n = proc.waitFor();
        } catch (InterruptedException e1) {
            throw new CompilerException("While compiling " + fullFileName, e1);
        }
        if (n != 0) {
            throw new CompilerException("Compilation of " + fullFileName + " ended abnormally with code " + n);
        }
    }
}

class RunnableStreamListener implements Runnable {

    BufferedReader is;
    PrintStream ps;

    RunnableStreamListener(BufferedReader istream, PrintStream pstream) {
        is = istream;
        ps = pstream;
    }

    public void run() {
        String line;
        try {
            while ((line = is.readLine()) != null)
                ps.println(line);
        } catch (IOException e) {
            ps.println(e.toString());
        }
    }
}
