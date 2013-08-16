package uk.co.jarofgreen.semanticscuttlejar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Integer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ScuttleBookmarkXMLHandler extends DefaultHandler {
	private List<HashMap<String, String>> bookmarks = new ArrayList<HashMap<String, String>>();
	
	public List<HashMap<String, String>> getBookmarks() {
		return(this.bookmarks);
	}
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		if( localName.equalsIgnoreCase("post") ) {
			HashMap<String, String> curBookmark = new HashMap<String, String>();
			curBookmark.put("href", attributes.getValue("href"));
            		switch(Integer.valueOf(attributes.getValue("status"))) {
				case 1:
					curBookmark.put("description", "S / "+attributes.getValue("description"));
					break;
				case 2:
					curBookmark.put("description", "P / "+attributes.getValue("description"));
					break;
				default:
					curBookmark.put("description", attributes.getValue("description"));
					break;
                        }
			curBookmark.put("hash", attributes.getValue("hash"));
			curBookmark.put("tag", attributes.getValue("tag"));
			curBookmark.put("time", attributes.getValue("time"));
			curBookmark.put("status", attributes.getValue("status"));
			this.bookmarks.add(curBookmark);

		}
	}
}
