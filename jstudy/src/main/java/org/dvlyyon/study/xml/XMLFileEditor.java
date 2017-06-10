package org.dvlyyon.study.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class XMLFileEditor {
	private List<Node> filteredOutObjects 	= new ArrayList<Node>();
	private Document doc;
	
	protected abstract boolean isRequiredObject(Node object);
	
	protected abstract void modifyObject(Node object);
	
	protected void printFilteredOutObjects(List<Node> objects) {
		System.out.println("The objects are filtered out:\n");
		for (Node object:objects) {
			System.out.println("\t"+getNameAttribute(object));
		}
	}
	
	public Node createElement(String name) {
		return doc.createElement(name);
	}
	
	protected String getNameAttribute(Node node) {
		return ((Element)node).getAttribute("name");
	}
	
	public void update(String fileName) {
	       try {
	            DocumentBuilderFactory docFactory = DocumentBuilderFactory
	                    .newInstance();
	            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	            doc = docBuilder.parse(fileName);

	            // Get the root element
	            Node data= doc.getFirstChild();

	            NodeList objList = doc.getElementsByTagName("object");
	            
	            for (int i=0; i<objList.getLength(); i++) {
	            	Node object = objList.item(i);
	            	if (isRequiredObject(object)) {
	            		System.out.println("modify object "+getNameAttribute(object));
	            		modifyObject(object);
	            	} else {
	            		filteredOutObjects.add(object);
	            	}
	            }
	            printFilteredOutObjects(filteredOutObjects);
	            
	            // write the content into xml file
	            TransformerFactory transformerFactory = TransformerFactory
	                    .newInstance();
	            Transformer transformer = transformerFactory.newTransformer();
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	            DOMSource source = new DOMSource(doc);
	            StringWriter sw = new StringWriter();
	            StreamResult result = new StreamResult(sw);
	            transformer.transform(source, result);

	            final OutputFormat format = OutputFormat.createPrettyPrint();
	            final org.dom4j.Document document = DocumentHelper.parseText(sw.toString());
	            FileWriter file = new FileWriter(fileName+".new.xml");
	            final XMLWriter writer = new XMLWriter(file, format);
	            writer.write(document);
	            
	            System.out.println("Done");
	     	} catch (ParserConfigurationException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (TransformerException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (SAXException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	}

    public static void main(String argv[]) {
    	XMLFileEditor editor = new AddValidateAttributeEditor();
    	editor.update("model/DCICli.xml");
    }

}