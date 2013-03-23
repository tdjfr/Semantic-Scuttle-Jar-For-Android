package uk.co.jarofgreen.semanticscuttlejar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.xml.sax.helpers.DefaultHandler;

import uk.co.jarofgreen.semanticscuttlejar.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ScuttleAddBookmark extends Activity {

	private class SaveBookmark extends AsyncTask<String, Void, Boolean> {
		private String errorMsg = "";

		protected void onPreExecute() {
		}
		protected void onPostExecute(Boolean success) {
			String msg = "";
			if( success == false ) {
				msg = "Error saving bookmark: " + this.errorMsg;
			}
			else {
				msg = "Bookmark saved";
			}
			Toast.makeText(ScuttleAddBookmark.this, msg, Toast.LENGTH_SHORT).show();
		}
		protected Boolean doInBackground(String... args) {
			String url = args[0];
			String desc = args[1];
			String tags = args[2];
			String status = args[3];
			try {
				DefaultHandler handler = new ScuttleAddXMLHandler();
				String apiURL = "api/posts_add.php?url="+URLEncoder.encode(url, "UTF-8")+
					"&description="+URLEncoder.encode(desc, "UTF-8")+
					"&tags="+URLEncoder.encode(tags, "UTF-8")+
					"&status="+URLEncoder.encode(status, "UTF-8");
				handler = APICall.parseScuttleURL(apiURL, ScuttleAddBookmark.this, handler);
			} catch( ScuttleAPIException sae ) {
				this.errorMsg = sae.getMessage();
				return false;
			} catch (UnsupportedEncodingException e) {
				this.errorMsg = "e:"+e.getMessage();
				return false;
			}
			return true;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addbookmark);
		
		// Setup the Privacy (status) spinner.
		Spinner spinner = (Spinner) findViewById(R.id.addbookmark_status);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.status_options, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		// Get the extras data passed in with the intent.
		Bundle b = this.getIntent().getExtras();
		if (b != null) {
			// Set the text fields to the values of the passed in data.
			EditText txtUrl = (EditText)findViewById(R.id.addbookmark_url);
			txtUrl.setText(b.getCharSequence(Intent.EXTRA_TEXT));
			EditText txtDesc = (EditText)findViewById(R.id.addbookmark_description);
			txtDesc.setText(b.getString(Intent.EXTRA_SUBJECT));
		}
		// Handle when the user presses the save button.
		Button btnSave = (Button)findViewById(R.id.addbookmark_btnsave);
		btnSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText fieldUrl = (EditText)findViewById(R.id.addbookmark_url);
				String strUrl = fieldUrl.getText().toString();
				EditText fieldDesc = (EditText)findViewById(R.id.addbookmark_description); 
				String strDesc = fieldDesc.getText().toString();
				String strTags = ((EditText)findViewById(R.id.addbookmark_tags)).getText().toString();
				String strStatus = ((String)((Spinner)findViewById(R.id.addbookmark_status)).getSelectedItem());
				
				if (strUrl.trim().equals("")) { 
					fieldUrl.setError("URL is required");
				}
				if (strDesc.trim().equals("")) { 
					fieldDesc.setError("Description is required");
				}
				else {
					(new SaveBookmark()).execute(strUrl, strDesc, strTags, strStatus);
					finish();
				}
			}
		});
	}
}
