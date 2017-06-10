package org.dvlyyon.study.xml.dom4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

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
		
	protected String getNameAttribute(Node node) {
		
		return ((Element)node).attribute("name").getValue();
	}
	
	public void update(String fileName) {
		try {
			File inputFile = new File(fileName);
			SAXReader reader = new SAXReader();
			Document document = reader.read( inputFile );

			Element rootElement = document.getRootElement();
			List<Element> objList = rootElement.elements("object");

			for (int i=0; i<objList.size(); i++) {
				Node object = objList.get(i);
				if (isRequiredObject(object)) {
					System.out.println("modify object "+getNameAttribute(object));
					modifyObject(object);
				} else {
					filteredOutObjects.add(object);
				}
			}
			printFilteredOutObjects(filteredOutObjects);


			final OutputFormat format = OutputFormat.createPrettyPrint();
			FileWriter file = new FileWriter(fileName+".new.xml");
			FileOutputStream fileS = new FileOutputStream(fileName+".new.xml");
			final XMLWriter writer = new XMLWriter(file, format);
			writer.write(document);
			writer.flush();
			System.out.println("Done");
	      } catch (DocumentException e) {
	          e.printStackTrace();
	       } catch (UnsupportedEncodingException e) {         
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