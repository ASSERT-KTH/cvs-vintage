package org.jboss.metadata.jboss;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import com.sun.xml.parser.*;

import org.jboss.metadata.io.XMLReader;
import org.jboss.metadata.*;

public class JBossXMLReader extends HandlerBase implements XMLReader {
    private JBossServer server = null;
    private JBossContainer container = null;
    private JBossBean bean = null;
    private String currentElement;
    private String contents;
    private Vector containers;
    private ClassLoader loader;

    public JBossXMLReader() {
        containers = new Vector();
    }

    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public String getFileName() {
        return "jboss.xml";
    }

    public ServerMetaData readXML(Reader input) throws IOException {
        com.sun.xml.parser.Parser parser = new com.sun.xml.parser.Parser();
        parser.setDocumentHandler(this);
        InputSource is = new InputSource(input);
        try {
            parser.parse(is);
        } catch(SAXException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        return server;
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String name, AttributeList atts) throws SAXException {
        currentElement = name;
        contents = null;
        if(name.equals("jboss")) {
            containers.clear();
            server = new JBossServer();
        } else if(name.equals("container-configuration")) {
            container = new JBossContainer();
            try {
                container.configurationClass = loadClass(atts.getValue("configuration-class"));
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to load class '"+atts.getValue("configuration-class")+"'.");
            }
        } else if(name.equals("entity") || name.equals("session"))
            bean = new JBossBean();
    }

    public void characters(char[] data, int start, int length) throws SAXException {
        if(contents == null)
            contents = new String(data, start, length);
        else
            contents += new String(data, start, length);
    }

    public void endElement(String name) throws SAXException {
        if(name.equals("container-configuration")) {
            containers.add(container);
            container = null;
        } else if(name.equals("session") || name.equals("entity")) {
            server.addBean(bean);
            bean = null;
        } else if(name.equals("ejb-name"))
            bean.setName(contents);
        else if(name.equals("configuration-name")) {
            for(int i=0; i<containers.size(); i++) {
                JBossContainer cont = (JBossContainer)containers.elementAt(i);
                if(cont.name.equals(contents)) {
                    bean.setContainerMetaData((ContainerMetaData)cont.clone());
                    break;
                }
            }
        } else if(name.equals("container-name"))
            container.name = contents;
        else if(name.equals("call-logging"))
            container.callLogging = new Boolean(contents).booleanValue();
        else if(name.equals("container-invoker"))
            try {
                if(contents != null && contents.length() > 0)
                    container.containerInvoker = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to load class '"+contents+"'.");
            }
        else if(name.equals("instance-pool"))
            try {
                if(contents != null && contents.length() > 0)
                    container.instancePool = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to load class '"+contents+"'.");
            }
        else if(name.equals("instance-cache"))
            try {
                if(contents != null && contents.length() > 0)
                    container.instanceCache = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to load class '"+contents+"'.");
            }
        else if(name.equals("persistence-manager"))
            try {
                if(contents != null && contents.length() > 0)
                    container.persistenceManager = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to load class '"+contents+"'.");
            }
        else if(name.equals("transaction-manager"))
            try {
                if(contents != null && contents.length() > 0)
                    container.transactionManager = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to load class '"+contents+"'.");
            }
        else if(name.equals("Optmized"))
            container.invokerOptimized = new Boolean(contents).booleanValue();
        else if(name.equals("MaximumSize"))
            container.poolMaximum = Integer.parseInt(contents);
        else if(name.equals("MinimumSize"))
            container.poolMinimum = Integer.parseInt(contents);
        else if(name.equals("commit-option"))
            container.commitOption = contents.charAt(0);
    }

    private Class loadClass(String name) throws ClassNotFoundException {
        if(loader == null)
            return Class.forName(name);
        else
            return loader.loadClass(name);
    }
}