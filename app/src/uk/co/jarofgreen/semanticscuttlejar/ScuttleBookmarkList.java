package uk.co.jarofgreen.semanticscuttlejar;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import uk.co.jarofgreen.semanticscuttlejar.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.xml.sax.helpers.DefaultHandler;
import java.net.URLEncoder;


public class ScuttleBookmarkList extends ListActivity {
	private String filterTag;
	
	private abstract class BackgroundTask extends AsyncTask<String, Void, List<HashMap<String, String>>> {
		protected AlertDialog alertLoading;
		protected String errorMsg = "";
	}

	private class RetrieveTags extends BackgroundTask {

		protected void onPreExecute() {
	        AlertDialog.Builder builder = new AlertDialog.Builder(ScuttleBookmarkList.this);
	        builder.setMessage(getString(R.string.tagslist_loading));
	        alertLoading = builder.create();
	        alertLoading.show();
		}
		protected void onPostExecute(List<HashMap<String, String>> tags) {
			alertLoading.dismiss();
			if( tags != null ) {
				displayTagChooser(tags);
			}
			else {
				Toast.makeText(ScuttleBookmarkList.this, this.errorMsg, Toast.LENGTH_LONG).show();
			}
		}
		protected List<HashMap<String, String>> doInBackground(String... args) {
			try {
				DefaultHandler handler = new ScuttleTagXMLHandler();
				handler = APICall.parseScuttleURL("api/tags_get.php", ScuttleBookmarkList.this, handler);
				return(((ScuttleTagXMLHandler) handler).getTags());
			} catch( ScuttleAPIException sae ) {
				this.errorMsg = sae.getMessage();
				return null;
			}
		}
	}
	private class RetrieveBookmarks extends BackgroundTask {
		
		protected void onPreExecute() {
	        AlertDialog.Builder builder = new AlertDialog.Builder(ScuttleBookmarkList.this);
	        builder.setMessage(getString(R.string.postlist_loading));
	        alertLoading = builder.create();
	        alertLoading.show();
		}
		protected void onPostExecute(List<HashMap<String, String>> bmarks) {
			alertLoading.dismiss();
			if( bmarks != null ) {
				displayBookmarks(bmarks);
			}
			else {
				Toast.makeText(ScuttleBookmarkList.this, this.errorMsg, Toast.LENGTH_LONG).show();
			}
		}
		protected List<HashMap<String, String>> doInBackground(String... args) {
			try {
				String url = "api/posts_all.php";
				// If a valid tag has been specified by the user then we only 
				// get bookmarks with that tag.
				if( filterTag != null && !filterTag.equals("") && !filterTag.equals(getResources().getString(R.string.tagslist_notags)) ) {
					url += "?tag="+filterTag;
				}
				DefaultHandler handler = new ScuttleBookmarkXMLHandler();
				handler = APICall.parseScuttleURL(url, ScuttleBookmarkList.this, handler);
				return (((ScuttleBookmarkXMLHandler) handler).getBookmarks());
			} catch( ScuttleAPIException sae ) {
				this.errorMsg = sae.getMessage();
				return null;
			}
		}
	}

	private void loadBookmarks() {
		(new RetrieveBookmarks()).execute();
	}
	private void loadTags() {
		(new RetrieveTags()).execute();
	}
	private void displayTagChooser(List<HashMap<String, String>> tags) {
		// Create the dialog.
		final Dialog dlgTagList = new Dialog(this);
		dlgTagList.setContentView(R.layout.dialog_taglist);
		dlgTagList.setTitle(getString(R.string.tagslist_title));
		dlgTagList.setCancelable(true);
		// Sort the tags alphabetically.
		Collections.sort(tags, new Comparator<HashMap<String, String>>() {
			public int compare(HashMap<String, String> object1, HashMap<String, String> object2) {
				return( object1.get("tag").compareTo(object2.get("tag")) );
			}
		});
		// Add an item at the beginning of the list that will show all bookmarks.
		HashMap<String, String> notags = new HashMap<String, String>();
		notags.put("tag", getString(R.string.tagslist_notags));
		tags.add(0, notags);
		// Create the list in the ListView from the tags.
    	SimpleAdapter adapter = new SimpleAdapter(this, 
    			tags, 
    			R.layout.tag_list_item,
    			new String[] { "tag" }, 
    			new int[] { R.id.taglist_tag });
		ListView lv = (ListView)dlgTagList.findViewById(R.id.listview_tags);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String tag = ((TextView)view.findViewById(R.id.taglist_tag)).getText().toString();
				dlgTagList.dismiss();
				ScuttleBookmarkList.this.filterTag = tag;
				loadBookmarks();
			}
		});
		dlgTagList.show();
	}
	private void displayBookmarks(List<HashMap<String, String>> bookmarks) {
    	SimpleAdapter adapter = new SimpleAdapter(this, 
        			bookmarks, 
        			R.layout.bookmark_list_item,
        			new String[] { "description", "href" }, 
        			new int[] { R.id.bookmark_description, R.id.bookmark_url });
        setListAdapter(adapter);
        
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(ScuttleBookmarkList.this);
        		String[] options = {getString(R.string.open), getString(R.string.share), getString(R.string.delete)};
        		final class BookmarkClick implements DialogInterface.OnClickListener {
        			
        			View view;
        			
        			public void onClick(DialogInterface dialog, int which) {
        				final String url = ((TextView)view.findViewById(R.id.bookmark_url)).getText().toString();
        	    		String description = ((TextView)view.findViewById(R.id.bookmark_description)).getText().toString();
        				if (which == 0) {
	        	    		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	        	    		startActivity(i);
	        	    	}
	        	    	if (which == 1) {
	        	    		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
	        	    		sharingIntent.setType("text/plain");
	        	    		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, description);
	        	    		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
	        	    		startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
	        	    	}
                        // Delete URL
	        	    	if (which == 2) {
                            AlertDialog dialogDelete = new AlertDialog.Builder(ScuttleBookmarkList.this).create();
                            dialogDelete.setTitle(getString(R.string.delbookmark_title));
                            dialogDelete.setMessage(getString(R.string.delbookmark_msg));
                            dialogDelete.setCancelable(false);
                            dialogDelete.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.delbookmark_yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int buttonId) {
                                }
                            });
                            dialogDelete.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.delbookmark_no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int buttonId) {
                                }
                            });
                            dialogDelete.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogDelete.show();
	        	    	}
	        	    }
        		}
        		BookmarkClick bookmarkClick = new BookmarkClick();
        		bookmarkClick.view = view;
        		builder.setTitle(getString(R.string.choose_action));
        		builder.setItems(options, bookmarkClick);

    	        AlertDialog actionChooser = builder.create();
    	        actionChooser.show();
        	}
        });
	}
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
		String scuttleURL = prefs.getString("url", "");
		if ( scuttleURL.equals("") ) {
			startActivity(new Intent(this, ScuttlePreferences.class));
		}
		else {
			this.loadBookmarks();
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater i = getMenuInflater();
        i.inflate(R.menu.list_menu, menu);
        return(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch( item.getItemId() ) {
    	case R.id.listmenu_preferences:
    		startActivity(new Intent(this, ScuttlePreferences.class));
    		return(true);
    	case R.id.listmenu_add:
    		startActivity(new Intent(this, ScuttleAddBookmark.class));
    		return(true);    		
    	case R.id.listmenu_refresh:
    		this.loadBookmarks();
    		return(true);
    	case R.id.listmenu_tags:
    		this.loadTags();
    		return(true);
    	}
    	return(super.onOptionsItemSelected(item));
    }

}
