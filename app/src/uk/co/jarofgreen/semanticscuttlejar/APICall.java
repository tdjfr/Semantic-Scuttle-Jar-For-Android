package uk.co.jarofgreen.semanticscuttlejar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.util.Base64;

import org.xml.sax.helpers.DefaultHandler;

import uk.co.jarofgreen.semanticscuttlejar.ScuttleAPIException;


public class APICall  {

	static HttpURLConnection callScuttleURL(String url, Context context) throws IOException {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		String scuttleURL = prefs.getString("url", "");
		if( scuttleURL.length() > 0 ) {
			String last = scuttleURL.substring(scuttleURL.length()-1);
			if( !last.equals("/") ) {
				scuttleURL += "/";
			}
			
			String http = scuttleURL.substring(0,7).toLowerCase();
			String https = scuttleURL.substring(0,8).toLowerCase();
			if (http.compareTo("http://") != 0 && https.compareTo("https://") != 0) {
				scuttleURL = "http://"+scuttleURL;
			}
		}
		
		String scuttleUsername = prefs.getString("username", "");
		String scuttlePassword = prefs.getString("password", "");
		String acceptAllSSLCerts = prefs.getString("acceptAllSSLCerts", "no");
		
		String authentication = scuttleUsername+":"+scuttlePassword;
		String encodedAuthentication = Base64.encodeToString(authentication.getBytes(), Base64.NO_WRAP);

		HttpURLConnection c = (HttpURLConnection)(new URL(scuttleURL+url).openConnection());
		c.setRequestProperty("Authorization", "Basic "+encodedAuthentication);
		c.setUseCaches(false);
		c.setConnectTimeout(2000);
		c.setReadTimeout(2000);

		if (acceptAllSSLCerts.compareTo("yes") == 0) {
			try {
				TrustModifier.relaxHostChecking(c);
			} catch (KeyStoreException e) {
				//
			} catch (KeyManagementException e) {
				//
			} catch (NoSuchAlgorithmException e) {
				//
			}
		}
		c.connect();
		return c;
	}

	static DefaultHandler parseScuttleURL(String url, Context context, DefaultHandler handler) throws ScuttleAPIException {
		try {
			HttpURLConnection c = APICall.callScuttleURL(url, context);
			if (c.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new ScuttleAPIException(context.getString(R.string.error_authentication));
			}
			InputStream is = c.getInputStream();
			String charset = APICall.getConnectionCharset(c);
			Xml.Encoding parse_encoding = Xml.Encoding.ISO_8859_1;
			if ("UTF-8".equals(charset.toUpperCase())) {
				parse_encoding = Xml.Encoding.UTF_8;
			}
			Xml.parse(is, parse_encoding, handler);
		}
		catch( ScuttleAPIException sae ) {
			throw sae;
		}
		catch( SocketTimeoutException ste ) {
			throw new ScuttleAPIException(context.getString(R.string.error_timeout));
		} catch( FileNotFoundException fnfe ) {
			throw new ScuttleAPIException(context.getString(R.string.error_filenotfound));
		} catch( IOException ioe ) {
			throw new ScuttleAPIException("ioe:"+ioe.getMessage());
		} catch( Exception e ) {
			throw new ScuttleAPIException("e:"+e.getMessage());
		}
		return handler;
	}

	static String getConnectionCharset(HttpURLConnection connection) {
		String contentType = connection.getContentType();
		String[] values = contentType.split(";");
		String charset = "";
		for (String value : values) {
			value = value.trim();
			if (value.toLowerCase().startsWith("charset=")) {
				charset = value.substring("charset=".length());
			}
		}
		if ("".equals(charset)) {
			charset = "UTF-8";
		}
		return charset;
	}
}
