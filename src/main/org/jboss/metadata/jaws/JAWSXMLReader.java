package org.jboss.metadata.jaws;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.Types;

import org.xml.sax.*;
import com.sun.xml.parser.*;

import org.jboss.metadata.io.XMLReader;
import org.jboss.metadata.*;

public class JAWSXMLReader extends HandlerBase implements XMLReader {
    private JAWSServer server = null;
    private JAWSContainer container = null;
    private JAWSBean bean = null;
    private JAWSField field = null;
    private String currentElement;
    private String contents;

    public JAWSXMLReader() {
    }

    public String getFileName() {
        return "jaws.xml";
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
        if(name.equals("jaws")) {
            server = new JAWSServer();
            container = new JAWSContainer();
        } else if(name.equals("entity"))
            bean = new JAWSBean();
        else if(name.equals("cmp-field"))
            field = new JAWSField();
    }

    public void characters(char[] data, int start, int length) throws SAXException {
        if(contents == null)
            contents = new String(data, start, length);
        else
            contents += new String(data, start, length);
    }

    public void endElement(String name) throws SAXException {
        if(name.equals("entity")) {
            bean.setContainerMetaData((ContainerMetaData)container.clone());
            server.addBean(bean);
            bean = null;
        } else if(name.equals("cmp-field")) {
            bean.addField(field);
            field = null;
        } else if(name.equals("ejb-name"))
            bean.setName(contents);
        else if(name.equals("table-name"))
            bean.tableName = contents;
        else if(name.equals("create-table")) {
            bean.createTable = new Boolean(contents).booleanValue();
            System.out.println("Bean's createTable is "+bean.createTable+" based on input '"+contents+"'");
        } else if(name.equals("remove-table"))
            bean.removeTable = new Boolean(contents).booleanValue();
        else if(name.equals("tuned-updates"))
            bean.tunedUpdates = new Boolean(contents).booleanValue();
        else if(name.equals("read-only"))
            bean.readOnly = new Boolean(contents).booleanValue();
        else if(name.equals("time-out"))
            bean.timeOut = Integer.parseInt(contents);
        else if(name.equals("field-name"))
            field.setName(contents);
        else if(name.equals("column-name"))
            field.columnName = contents;
        else if(name.equals("sql-type")) {
            if(field != null)
                field.sqlType = contents;
        } else if(name.equals("jdbc-type")) {
            if(field != null)
                field.jdbcType = getJDBCType(contents);
        } else if(name.equals("datasource"))
            container.cmpDataSource = contents;
        else if(name.equals("type-mapping") && container.cmpDBType == null)
            container.cmpDBType = contents;
    }

    private int getJDBCType(String source) throws SAXException {
        try {
            Class cls = Types.class;
            Field field = cls.getField(source);
            return ((Number)field.get(null)).intValue();
        } catch(Exception e) {
            System.out.println("Unknown JDBC type '"+source+"'");
            throw new SAXException("Unknown JDBC type '"+source+"'");
        }
    }
}