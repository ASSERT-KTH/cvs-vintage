package org.jboss.metadata.ejbjar;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import com.sun.xml.parser.*;

import org.jboss.metadata.io.XMLReader;
import org.jboss.metadata.*;

public class EJBXMLReader extends HandlerBase implements XMLReader {
    private EJBServer server = null;
    private EJBContainer container = null;
    private EJBBean bean = null;
    private MethodHolder method = null;
    private String currentElement;
    private String contents;
    private Vector methods = new Vector();
    private ClassLoader loader;

    public EJBXMLReader() {
    }

    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public String getFileName() {
        return "ejb-jar.xml";
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
        if(name.equals("ejb-jar"))
            server = new EJBServer();
        else if(name.equals("entity") || name.equals("session")) {
            bean = new EJBBean();
            container = new EJBContainer();
        } else if(name.equals("method"))
            method = new MethodHolder();
        else if(name.equals("container-transaction"))
            methods.clear();

    }

    public void characters(char[] data, int start, int length) throws SAXException {
        if(contents == null)
            contents = new String(data, start, length);
        else
            contents += new String(data, start, length);
    }

    public void endElement(String name) throws SAXException {
        if(name.equals("entity") || name.equals("session")) {
            bean.setContainerMetaData(container);
            container = null;
            server.addBean(bean);
            bean = null;
        } else if(name.equals("ejb-name")) {
            if(bean != null)
                bean.setName(contents);
            else if(method != null)
                method.ejbName = contents;
            // otherwise, a container transaction bean reference
        } else if(name.equals("method")) {
            methods.add(method.method);
            if(method.isHome) {
                try {
                    EJBBean bean = (EJBBean)server.getBean(method.ejbName);
                    bean.addHomeMethod(method.method);
                    method = null;
                } catch(IllegalArgumentException e) {// a Home?
                    System.out.println("Couldn't find bean '"+method.ejbName+"' to add home method '"+method.method.getName()+"' to!");
                }
            } else {
                try {
                    EJBBean bean = (EJBBean)server.getBean(method.ejbName);
                    bean.addMethod(method.method);
                    method = null;
                } catch(IllegalArgumentException e) {// a Home?
                    System.out.println("Couldn't find bean '"+method.ejbName+"' to add method '"+method.method.getName()+"' to!");
                }
            }
        } else if(name.equals("description")) {
            if(bean != null)
                bean.description = contents;
            // otherwise, a container transaction description
        } else if(name.equals("display-name")) {
            if (bean != null) {
                bean.displayName = contents;
            } else {
             // TODO:
             // set the display name in the ejb-jar file
            }
        } else if(name.equals("home"))
            try {
                bean.homeClass = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to locate class '"+contents+"'");
            }
        else if(name.equals("remote"))
            try {
                bean.remoteClass = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to locate class '"+contents+"'");
            }
        else if(name.equals("ejb-class"))
            try {
                bean.implementationClass = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to locate class '"+contents+"'");
            }
        else if(name.equals("prim-key-class"))
            try {
                if(MetaDataFactory.primitives.containsKey(contents))
                    bean.primaryKeyClass = (Class)MetaDataFactory.primitives.get(contents);
                else
                    bean.primaryKeyClass = loadClass(contents);
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to locate class '"+contents+"'");
            }
        else if(name.equals("persistence-type"))
            bean.persistanceType = contents;
        else if(name.equals("reentrant"))
            bean.reentrant = new Boolean(contents).booleanValue();
        else if(name.equals("field-name")) {
            EJBField field = new EJBField();
            field.setName(contents);
            field.isCMP = true;
            bean.addField(field);
        } else if(name.equals("method-name"))
            method.method.setName(contents);
        else if(name.equals("method-param")) {
            LinkedList list = new LinkedList(Arrays.asList(method.method.getParameterTypes()));
            try {
                if(MetaDataFactory.primitives.containsKey(contents))
                    list.add(MetaDataFactory.primitives.get(contents));
                else
                    list.add(loadClass(contents));
                method.method.setParameterTypes((Class[])list.toArray(new Class[list.size()]));
            } catch(ClassNotFoundException e) {
                throw new SAXException("Unable to locate class '"+contents+"'");
            }
        } else if(name.equals("method-intf"))
            method.isHome = contents.equalsIgnoreCase("Home");
        else if(name.equals("trans-attribute")) {
            byte value;
            if(contents.equals("Required"))
                value = EJBMethod.TX_REQUIRED;
            else if(contents.equals("RequiresNew"))
                value = EJBMethod.TX_REQUIRES_NEW;
            else if(contents.equals("Supports"))
                value = EJBMethod.TX_SUPPORTS;
            else if(contents.equals("Never"))
                value = EJBMethod.TX_NEVER;
            else if(contents.equals("NotSupported"))
                value = EJBMethod.TX_NOT_SUPPORTED;
            else if(contents.equals("Mandatory"))
                value = EJBMethod.TX_MANDATORY;
            else throw new SAXException("Unknown transaction type '"+contents+"'");
            for(int i=0; i<methods.size(); i++)
                ((EJBMethod)methods.get(i)).transactionAttribute = value;
        }
    }

    private Class loadClass(String name) throws ClassNotFoundException {
        if(loader == null)
            return Class.forName(name);
        else
            return loader.loadClass(name);
    }

    private class MethodHolder {
        EJBMethod method = new EJBMethod();
        String ejbName;
        boolean isHome;
    }
}
