package org.dvlyyon.study.xml.dom4j;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;

public class XmlReader {
	    public static void main(String[] args) throws Exception
	    {
	        SAXReader saxReader = new SAXReader();

	        Document document = saxReader.read(new File("model/example.xml"));

	        // 获取根元素
	        Element root = document.getRootElement();
	        System.out.println("Root: " + root.getName());

	        // 获取所有子元素
	        List<Element> childList = root.elements();
	        System.out.println("total child count: " + childList.size());

	        // 获取特定名称的子元素
	        List<Element> objectList = root.elements("object");
	        System.out.println("object child: " + objectList.size());
	        
	        for (Element o:objectList) {
	        	System.out.println("object name:"+o.attributeValue("name"));
	        	List<Element> mList = o.elements("metaInfo");
	        	System.out.println("total metaInfo count:"+mList.size());
	        	List<Element> aList = o.elements("action");
	        	for (Element a:aList) {
	        		System.out.println("\taction name:"+a.attributeValue("name"));
	        		List<Element> amList = a.elements("metaInfo");
	        		System.out.println("\ttotal metaInfo count:"+amList.size());
	        		for (Element am:amList) {
	        			List<Element> miList = am.elements("metaItem");
	        			for (Element mi:miList) {
	        				System.out.format("\t\tmetaItem name=%1$s,value=%2$s%n",mi.attributeValue("name"),mi.attributeValue("value"));
	        			}
	        		}
	        		List<Element> atrList = a.elements("attribute");
	        		for (Element atr:atrList) {
	        			System.out.println("\tattribute name"+atr.attributeValue("name"));
	        			List<Element> atrmList = atr.elements("metaInfo");
	        			System.out.println("\ttotal metaInfo count:"+atrmList.size());
	        			Element atrm = atrmList.get(0);
	        			List<Element> atrmiList = atrm.elements("metaItem");
	        			for (Element atrmi:atrmiList) {
	        				System.out.format("\t\t\tmetaItem name=%1$s,value=%2$s%n",atrmi.attributeValue("name"),atrmi.attributeValue("value"));
	        			}
	        		}
	        	}
	        }
	        // 获取名字为指定名称的第一个子元素
	        Element firstWorldElement = root.element("object");
	        // 输出其属性
	        System.out.println("first object Attr: "
	                + firstWorldElement.attribute(0).getName() + "="
	                + firstWorldElement.attributeValue("name"));

	        System.out.println("迭代输出-----------------------");
	        // 迭代输出
	        for (Iterator iter = root.elementIterator(); iter.hasNext();)
	        {
	            Element e = (Element) iter.next();
	            System.out.println(e.attributeValue("name"));

	        }

	        System.out.println("用DOMReader-----------------------");
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // 注意要用完整类名
	        org.w3c.dom.Document document2 = db.parse(new File("model/example.xml"));

	        DOMReader domReader = new DOMReader();

	        // 将JAXP的Document转换为dom4j的Document
	        Document document3 = domReader.read(document2);

	        Element rootElement = document3.getRootElement();

	        System.out.println("Root: " + rootElement.getName());

	    }

	}
