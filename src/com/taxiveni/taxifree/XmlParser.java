package com.taxiveni.taxifree;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class XmlParser {
	
	private final String TAG = getClass().getSimpleName();
	
	public GeoCodeResult parseXmlResponse(String response) {
		
		try {
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(response)));
			
			NodeList resultNodes = doc.getElementsByTagName("Result");			
			Node resultNode = resultNodes.item(0);			
			NodeList attrsList = resultNode.getChildNodes();
			
			GeoCodeResult result = new GeoCodeResult();
			
			for (int i=0; i < attrsList.getLength(); i++) {
				
				Node node = attrsList.item(i);				
				Node firstChild = node.getFirstChild();
				
				if ("line1".equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
					result.line1 = firstChild.getNodeValue();
				}
				if ("line2".equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
					result.line2 = firstChild.getNodeValue();
				}
				if ("line3".equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
					result.line3 = firstChild.getNodeValue();
				}
				if ("line4".equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
					result.line4 = firstChild.getNodeValue();
				}
			}
			
			Log.d(TAG, result.toString());
			
			return result;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

}
