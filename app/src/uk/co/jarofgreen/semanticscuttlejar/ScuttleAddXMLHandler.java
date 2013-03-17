package uk.co.jarofgreen.semanticscuttlejar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class ScuttleAddXMLHandler extends DefaultHandler {

	private String result = "";

	public String getResult() {
		return this.result;
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if( localName.equalsIgnoreCase("result") ) {
			this.result = attributes.getValue("code");
		}
	}

}
