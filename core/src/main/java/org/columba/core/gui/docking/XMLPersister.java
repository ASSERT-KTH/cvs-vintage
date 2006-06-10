package org.columba.core.gui.docking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.flexdock.docking.DockingPort;
import org.flexdock.docking.state.LayoutNode;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.perspective.persist.xml.ISerializer;
import org.flexdock.perspective.persist.xml.SerializerRegistry;
import org.flexdock.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLPersister {

	private DocumentBuilder createDocumentBuilder() throws PersistenceException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            return documentBuilder;
        } catch (ParserConfigurationException ex) {
            throw new PersistenceException("Unable to create document builder", ex);
        }
    }
	
	public boolean store(OutputStream os, DockingPort dockingPort) throws IOException, PersistenceException {
        DocumentBuilder documentBuilder = createDocumentBuilder();
        Document document = documentBuilder.newDocument();
        
        ISerializer serializer = SerializerRegistry.getSerializer(LayoutNode.class);
        Element layoutNodeElement = serializer.serialize(document, dockingPort.exportLayout());

        document.appendChild(layoutNodeElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // the indent-number attribute causes an IllegalArgumentException under 1.4 
        if(Utilities.JAVA_1_5) {
        	transformerFactory.setAttribute("indent-number", new Integer(4));
        }

        try {
            Transformer transformer = transformerFactory.newTransformer();
            // this property is ignored under java 1.5.
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new OutputStreamWriter(os));
            
            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
            throw new PersistenceException("Unable to serialize perspectiveModel", ex);
        } catch (TransformerException ex) {
            throw new PersistenceException("Unable to serialize perspectiveModel", ex);
        }

        return true;
    }
	
	public void load(InputStream is, DockingPort dockingPort) throws IOException, PersistenceException {
        try {
            InputSource inputSource = new InputSource(is);
            DocumentBuilder documentBuilder = createDocumentBuilder();
            Document document = documentBuilder.parse(inputSource);

            ISerializer layoutNodeSerializer = SerializerRegistry.getSerializer(LayoutNode.class);
            LayoutNode layoutNode = (LayoutNode) layoutNodeSerializer.deserialize(document.getDocumentElement());
            dockingPort.importLayout(layoutNode);

        } catch (SAXException ex) {
            throw new PersistenceException("Unable to deserialize perspectiveModel from xml", ex);
        }
    }
	
}
