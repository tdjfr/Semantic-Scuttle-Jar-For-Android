package uk.co.jarofgreen.semanticscuttlejar;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class APICall  {

	static InputStream callScuttleURL(String url, Context context) throws IOException {
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
		
        Authenticator.setDefault(new ScuttleAuthenticator(scuttleUsername, scuttlePassword));
        HttpURLConnection c = (HttpURLConnection)(new URL(scuttleURL+url).openConnection());
        c.setUseCaches(false);
        c.setConnectTimeout(1500);
        c.setReadTimeout(300);
        c.connect();
        InputStream is = c.getInputStream();
        return(is);
	}
	static class ScuttleAuthenticator extends Authenticator {
		private String username, password;
		
		public ScuttleAuthenticator(String user, String pass) {
			this.username = user;
			this.password = pass;
		}
		protected PasswordAuthentication getPasswordAuthentication() {
			return( new PasswordAuthentication(this.username, this.password.toCharArray()) );
		}
	}

}
