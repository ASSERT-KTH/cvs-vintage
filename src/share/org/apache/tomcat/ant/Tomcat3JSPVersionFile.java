/*   
 *  Copyright 2002-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.regexp.RegexpMatcher;
import org.apache.tools.ant.util.regexp.RegexpMatcherFactory;

/**
 * Task to create version files used by Tomcat to determine the
 * appropriate class to load for a JSP.
 *
 * This task can accept the following attributes:
 * <ul>
 * <li>srcdir
 * <li>regexpclasspath
 * <li>regexpclasspathref
 * </ul>
 * <b>srcdir</b> is required.
 * <p>
 * When this task executes, it will scan the files in srcdir which have
 * the form <i>name_nnn.class</i>.  For each class, if a corresponding
 * version file of the form <i>name.ver</i> does not exist, it is created
 * with version <i>nnn</i>.  If the version file exists, this task verifies that
 * it contains the correct version <i>nnn</i>.  If not, a new version file is 
 * created.
 *
 * <p>This task uses a regular expression library.  If one is not found in
 * the ant classpath, this task will attempt to load both the Ant regexp
 * bridge (optional.jar) and the Jakarta regular expression matcher 
 * (jakarta-regexp) from the specified regexpclasspath.
 *
 * <p>Use this task with the Tomcat3Precompiler to create the appropriate
 * files to pre-populate the Tomcat work directory.
 *
 * @author Keith Wannamaker <a href="mailto:Keith@Apache.org">Keith@Apache.org</a>
 *
 * @version $Revision: 1.3 $
 *
 * @since Ant 1.6
 *
 */
public class Tomcat3JSPVersionFile extends Task {
    private File srcdir;
    private Path regexpClasspath;
    private RegexpMatcherFactory factory = new RegexpMatcherFactory();

    /** Setter for srcdir */
    public void setSrcdir(String srcdir) {
        this.srcdir = new File(srcdir);
    }

    /** Setter for regexpclasspath */
    public void setRegexpClasspath(Path cp) {
        if (regexpClasspath == null) {
            regexpClasspath = cp;
        } else {
            regexpClasspath.append(cp);
        }
    }

    /**
     * Support nested regexpclasspath elements
     */
    public Path createRegexpClasspath() {
        if (regexpClasspath == null) {
            regexpClasspath = new Path(project);
        }
        return regexpClasspath.createPath();
    }

    /**
     * Add classpath reference 
     */
    public void setRegexpClasspathRef(Reference r) {
        createRegexpClasspath().setRefid(r);
    }

    /** Execute the task */
    public void execute() throws BuildException {
        if (srcdir == null) {
            throw new BuildException("srcdir attribute required", location);
        }
        DirectoryScanner ds = new DirectoryScanner();
        ds.setIncludes(new String [] {"**_?*.class"});
        ds.setBasedir(srcdir);
        ds.setCaseSensitive(true);
        ds.scan();
        String[] files = ds.getIncludedFiles();
        int count = 0;
        for (int i = 0; i < files.length; i++) {
            RegexpMatcher rm = loadRegexpMatcher();
            rm.setPattern("(.*)_(\\d*).class");
            if (rm.matches(files[i])) {
                Vector components = rm.getGroups(files[i]);
                String base = (String) components.elementAt(1);
                String strVersion = (String) components.elementAt(2);
                int version = new Integer(strVersion).intValue();
                File verFile = new File(srcdir, base + ".ver");
                try {
                    if (verFile.exists()) {
                        if (readVersionFile(verFile) == version) {
                            continue;
                        }
                    }
                    log("Creating verion file " + verFile.getAbsolutePath(), 
                       Project.MSG_VERBOSE);
                    count++;
                    writeVersionFile(verFile, version);
                } catch (IOException ioe) {
                    throw new BuildException(ioe);
                }
            }
        }
        log("Created " + count + " version file" + ((count != 1) ? "s" : ""));
    }

    /**
     * Read Tomcat 3 version file
     */
    private int readVersionFile(File verFile) throws IOException {
        FileInputStream stream = new FileInputStream(verFile);
        try {
            return (int) stream.read();
        } finally {
            stream.close();
        }
    }

    /**
     * Write Tomcat 3 version file
     */
    private void writeVersionFile(File verFile, int version) throws IOException {
        FileOutputStream stream = new FileOutputStream(verFile);
        try {
          stream.write(version);
        } finally {
          stream.close();
        }
    }

    /**
     * Load regexp matcher
     */
    private RegexpMatcher loadRegexpMatcher() throws BuildException {
        RegexpMatcher rm;
        /* First try to load from factory's classloader */
        try {    
          rm = factory.newRegexpMatcher();
          log("Loaded RegexpMatcher from factory", Project.MSG_DEBUG);
          return rm;
        } catch (BuildException be) {
          ;
        }
        /* Now try to load the Jakara regexp jar from a specified classpath */
        try {
          log("Loading RegexpMatcher from " + regexpClasspath, Project.MSG_DEBUG);
          AntClassLoader loader = new AntClassLoader(getProject(), regexpClasspath);
          Class implClass = loader.findClass(DEFAULT_REGEXP_CLASS);
          return (RegexpMatcher) implClass.newInstance();
        } catch (Throwable t) {
          throw new BuildException(t);
        }
    }

    protected String DEFAULT_REGEXP_CLASS =
         "org.apache.tools.ant.util.regexp.JakartaRegexpMatcher";
}

