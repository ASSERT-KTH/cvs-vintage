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
 * $Id: CarolTestTask.java,v 1.5 2005/02/09 19:47:48 el-vadimo Exp $
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
    private String antfile;
    private String propDestination;
    private String propSource;
    private File propDestDir;
    private File propSourceDir;

    public void setAntfile(String antfile) {
        this.antfile = antfile;
    }

    public void setPropDestination(String propDestination) {
        this.propDestination = propDestination;
    }

    public void setPropSource(String propSource) {
        this.propSource = propSource;
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
    }

    public void execute() throws BuildException {
        checkSettings();

        propDestDir = new File(getProject().getBaseDir(), propDestination);
        assertIsDirectory("propDestination", propDestDir);

        propSourceDir = new File(propSource);
        assertIsDirectory("propSource", propSourceDir);


        // these are passed on to the Ant <target> task
        for (Iterator configs=Config.configurations(); configs.hasNext(); ) {
            Config config = (Config) configs.next();

            // XXX: for now, we only handle single-protocol tests
            if (config.getProto2() == null) {
                CarolProtocol proto = config.getProto1();

                Properties targetProps = loadProperties(proto.getName() + ".properties");
                final File clientPropsFile = new File(propDestDir, "client1.properties");
                String clientPropsFilename = clientPropsFile.getAbsolutePath();
                targetProps.setProperty("client.properties.file.name1", clientPropsFilename);
                targetProps.setProperty("server.properties.file.name", clientPropsFilename);

                Properties clientProps = new Properties();
                clientProps.setProperty("carol.protocols", proto.getName());
                clientProps.setProperty("carol.start.ns",
                                        config.usesExternallyStartedNS());

                Ant ant = newAnt();

                for (Enumeration props=targetProps.propertyNames(); props.hasMoreElements(); ) {
                    String propName = (String) props.nextElement();

                    if (propName.startsWith("carol.")) {
                        clientProps.setProperty(propName, targetProps.getProperty(propName));
                    } else {
                        Property antProp = ant.createProperty();
                        antProp.setName(propName);
                        String propValue = alter(proto, propName, targetProps.getProperty(propName));
                        antProp.setValue(propValue);
                    }
                }

                Property antProp = ant.createProperty();
                antProp.setName("test.name");
                antProp.setValue(config.toString());

                try {
                    FileOutputStream os = new FileOutputStream(clientPropsFile);
                    clientProps.store(os, null);
                    os.close();
                    System.err.println("wrote " + clientPropsFile);
                } catch (IOException ex) {
                    throw new BuildException
                        ("couldn't create " + clientPropsFile, ex);
                }
                System.out.println("executing " + config.toString());
                ant.execute();
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

    private static String alter(CarolProtocol proto, String name, String value) {
        if (proto != CarolProtocol.JRMP11 && proto != CarolProtocol.JRMP12) {
            return value;
        }
        if (!name.startsWith("stub.jar.name")) {
            return value;
        }
        String suffix = proto == CarolProtocol.JRMP11 ? "1.1" : "1.2";
        return value + suffix + ".jar";
    }


    private Properties loadProperties(String filename) throws BuildException {
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

    private static class Config {
        private final CarolProtocol proto1;
        private final CarolProtocol proto2;
        private final boolean usesExternallyStartedNS;

        private final static List configurations;

        static {
            configurations = new LinkedList();
            CarolProtocol[] protos =
                new CarolProtocol[] {CarolProtocol.IIOP,
                                     CarolProtocol.JEREMIE,
                                     CarolProtocol.JRMP11,
                                     CarolProtocol.JRMP12};

            boolean[] nsValues = new boolean[] {true, false};

            for (int nsIdx=0; nsIdx<nsValues.length; nsIdx++) {
                final boolean usesExternal = nsValues[nsIdx];

                for (int ii=0; ii<protos.length; ii++) {
                    configurations.add(new Config(protos[ii], null, usesExternal));
                }

                for (int ii=0; ii<protos.length-1; ii++) {
                    final CarolProtocol proto1 = protos[ii];

                    for (int jj=ii+1; jj<protos.length; jj++) {
                        final CarolProtocol proto2 = protos[jj];
                        configurations.add(new Config(proto1, proto2, usesExternal));
                    }
                }
            }
        }


        Config(CarolProtocol proto1, CarolProtocol proto2, boolean usesExternallyStartedNS) {
            if (proto1 == null) { throw new NullPointerException("proto1"); }

            this.proto1 = proto1;
            this.proto2 = proto2;
            this.usesExternallyStartedNS = usesExternallyStartedNS;
        }

        public CarolProtocol getProto1() {
            return proto1;
        }

        public CarolProtocol getProto2() {
            return proto2;
        }

        public String usesExternallyStartedNS() {
            return Boolean.valueOf(usesExternallyStartedNS).toString();
        }

        public static Iterator configurations() {
            System.err.println("configurations=" + configurations);
            return configurations.iterator();
        }

        public String toString() {
            StringBuffer sb = new StringBuffer(proto1.getNameVersion());
            if (proto2 != null) {
                sb.append('.').append(proto2.getNameVersion());
            }
            if (!usesExternallyStartedNS) {
                sb.append('.').append("nons");
            }
            return sb.toString();
        }
    }
}
