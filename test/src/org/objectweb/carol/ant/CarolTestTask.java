/**
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: CarolTestTask.java,v 1.3 2005/02/08 22:51:39 el-vadimo Exp $
 * --------------------------------------------------------------------------
 */

package org.objectweb.carol.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;

public final class CarolTestTask extends Task {
    private final List configs;
    private String antfile;
    private String propDestination;
    private String propSource;
    private File propDestDir;
    private File propSourceDir;

    public CarolTestTask() {
        configs = new LinkedList();
    }

    public void setAntfile(String antfile) {
        this.antfile = antfile;
    }

    public void setPropDestination(String propDestination) {
        this.propDestination = propDestination;
    }

    public void setPropSource(String propSource) {
        this.propSource = propSource;
    }

    public CarolProtocols createCarolProtocols() {
        CarolProtocols config = new CarolProtocols();
        configs.add(config);
        return config;
    }

    private void checkSettings() throws BuildException {
        if (antfile == null) {
            throw new BuildException("antfile is not set");
        }
        if (propDestination == null) {
            throw new BuildException("propDestination is not set");
        }
        if (propSource == null) {
            throw new BuildException("propSource is not set");
        }
        if (configs.size() == 0) {
            throw new BuildException("no nested config elements found");
        }
    }

    public void execute() throws BuildException {
        checkSettings();

        propDestDir = new File(getProject().getBaseDir(), propDestination);
        assertIsDirectory("propDestination", propDestDir);

        propSourceDir = new File(propSource);
        assertIsDirectory("propSource", propSourceDir);

        for (Iterator ii=configs.iterator(); ii.hasNext(); ) {
            CarolProtocols config = (CarolProtocols) ii.next();
            if (config.getProto2() == null) {
                executeSingle(config.getProto1());
            } else {
                // executeMulti(ant, config.getProto1(), config.getProto2());
                throw new Error("not implemented");
            }
        }
    }

    private Ant newAnt() {
        Ant ant = new Ant();
        ant.setProject(getProject());
        ant.setOwningTarget(getOwningTarget());
        ant.setAntfile(antfile);
        return ant;
    }

    private static void assertIsDirectory(String propName, File dir) {
        if (!dir.exists()) {
            System.err.println("dir: " + dir);
            throw new BuildException
                (propName + " " + dir + " does not exist");
        }
        if (!dir.isDirectory()) {
            throw new BuildException
                (propName + " " + dir + " is not a directory");
        }
    }

    private void executeSingle(String proto) throws BuildException {
        // these are passed on to the Ant <target> task
        Properties targetProps = loadProperties(proto);
        final File clientPropsFile = new File(propDestDir, "client1.properties");
        String clientPropsFilename = clientPropsFile.getAbsolutePath();
        targetProps.setProperty("client.properties.file.name1", clientPropsFilename);
        targetProps.setProperty("server.properties.file.name", clientPropsFilename);

        int nVariations = CarolProtocols.JRMP.equals(proto) ? 2 : 1;

        // in the case of JRMP, we need to test two variations:
        // "jrmp 1.1" and "jrmp 1.2"
        for (int ii=1; ii<=nVariations; ii++) {

            // We make two iterations.  During the first one, we start a name
            // server externally.  During the second one, we let CAROL start
            // the name server on its own.
            for (int jj=0; jj<2; jj++) {

                Properties clientProps = new Properties();
                clientProps.setProperty("carol.protocols", proto);
                clientProps.setProperty("carol.start.ns", jj==0 ? "true" : "false");

                Ant ant = newAnt();

                for (Enumeration props=targetProps.propertyNames(); props.hasMoreElements(); ) {
                    String propName = (String) props.nextElement();

                    if (propName.startsWith("carol.")) {
                        clientProps.setProperty(propName, targetProps.getProperty(propName));
                    } else {
                        Property antProp = ant.createProperty();
                        antProp.setName(propName);
                        String propValue = alter(proto, ii, propName, targetProps.getProperty(propName));
                        antProp.setValue(propValue);
                    }
                }
                Property antProp = ant.createProperty();
                StringBuffer testname = new StringBuffer(proto);

                if (CarolProtocols.JRMP.equals(proto)) {
                    testname.append("1.").append(ii);
                }

                if (jj == 1) {
                    testname.append(".nons");
                }

                antProp.setName("test.name");
                antProp.setValue(testname.toString());

                try {
                    FileOutputStream os = new FileOutputStream(clientPropsFile);
                    clientProps.store(os, null);
                    os.close();
                    System.err.println("wrote " + clientPropsFile);
                } catch (IOException ex) {
                    throw new BuildException
                        ("couldn't create " + clientPropsFile, ex);
                }
                ant.execute();
            }
        }
    }

    private static String alter(String proto, int variation, String name, String value) {
        if (!CarolProtocols.JRMP.equals(proto)) {
            return value;
        }
        String suffix = variation == 1 ? "1.1" : "1.2";
        if (name.startsWith("stub.jar.name")) {
            return value + suffix + ".jar";
        }

        if (name.startsWith("test.name")) {
            return value + suffix;
        }

        return value;
    }


    private Properties loadProperties(String proto) throws BuildException {
        String filename = proto + ".properties";
        File propFile = new File(propSourceDir, filename);
        InputStream is;
        try {
            is = new FileInputStream(propFile);
        } catch (FileNotFoundException ex) {
            throw new BuildException(filename + " not found", ex);
        }

        Properties props = new Properties();

        try {
            props.load(is);
        } catch (IOException ex) {
            throw new BuildException("couldn't load " + filename, ex);
        } finally {
            try { is.close(); }
            catch (IOException ex) { ; }
        }
        return props;
    }
}
