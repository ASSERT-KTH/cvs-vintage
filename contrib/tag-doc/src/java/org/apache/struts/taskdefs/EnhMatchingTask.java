/*
 * $Header: /tmp/cvs-vintage/struts/contrib/tag-doc/src/java/org/apache/struts/taskdefs/EnhMatchingTask.java,v 1.3 2004/02/29 22:18:42 martinc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/02/29 22:18:42 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.struts.taskdefs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;

/** * ENHanced-MatchingTask - enhances the MatchingTask by allowing * the subclasses to act as a FileSet container as well as allowing * the implicit FileSet. * * @version $Revision: 1.3 $ $Date: 2004/02/29 22:18:42 $
 */

public abstract class EnhMatchingTask extends MatchingTask {
	/** Base directory for implicit FileSet */
	protected File dir;
	/** List of embeded filesets */
	protected List filesets = new ArrayList();

	/**
	 * Set the base directory for the implicit FileSet
	 * @param base directory for implicit FileSet
	 */
	public void setDir(File dir) {
		this.dir = dir;
	}
	/**
	 * Adds an embeded FileSet for this task.
	 * @param the FileSet to add
	 */
	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}

	/**
	 * Returns the combined list of Files, from both the
	 * implicit and embeded FileSets.
	 * @return list of File objects
	 */
	protected List getFiles() {
		List files = new ArrayList();
		if (dir != null) {
			fileset.setDir(dir);
			files.addAll(getFiles(fileset));
		}
		Iterator iter = filesets.iterator();
		while (iter.hasNext()) {
			files.addAll(getFiles((FileSet) iter.next()));
		}
		return files;
	}
	private List getFiles(FileSet fs) {
		List files = new ArrayList();
		DirectoryScanner ds = fs.getDirectoryScanner(fs.getProject());
		File dir = ds.getBasedir();
		String[] filenames = ds.getIncludedFiles();
		for (int i = 0; i < filenames.length; i++) {
			File file = new File(dir, filenames[i]);
			files.add(file);
		}
		return files;
	}
}