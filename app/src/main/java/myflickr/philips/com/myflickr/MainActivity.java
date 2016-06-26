package myflickr.philips.com.myflickr;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;

import myflickr.philips.com.myflickr.view.UserInterfaceHandler;

public class MainActivity extends AppCompatActivity {
    public final String LAST_IMAGE = "lastImage";
    public UserInterfaceHandler mUserInterfaceHandler;

    // UI
    private Button pDownloadPhotos;
    private Gallery pGalleryObject;
    private ImageView pImageView;
    private EditText pEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init UI Handler
        mUserInterfaceHandler = new UserInterfaceHandler();

        pDownloadPhotos = (Button) findViewById(R.id.searchbutton);
        pEditText = (EditText) findViewById(R.id.editText);
        pGalleryObject = (Gallery) findViewById(R.id.gallery);
        pImageView = (ImageView) findViewById(R.id.imageView);

        mUserInterfaceHandler.SetGalleryObject (pGalleryObject);
        mUserInterfaceHandler.SetImageViewObject(pImageView);
        // Click on thumbnail
        pGalleryObject.setOnItemClickListener(onThumbClickListener);
        // Click on search
        pDownloadPhotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (pGalleryObject.getAdapter() != null) {
                    mUserInterfaceHandler.SetAdapter ();
                }

                if(v!=null) {
                    InputMethodManager inputManager =
                            (InputMethodManager) getApplicationContext().
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                new Thread(getMetadata).start();
            }
        });



    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Saving index of selected item in Gallery
        outState.putInt(LAST_IMAGE, pGalleryObject.getSelectedItemPosition());
        super.onSaveInstanceState(outState);

    }

    /**
     * Runnable to get metadata from Flickr API
     */
    Runnable getMetadata = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            String tag = pEditText.getText().toString().trim();
            if (tag != null && tag.length() >= 3)
                mUserInterfaceHandler.SearchImagesByTag (getApplicationContext(), tag);
                //FlickrHandler.searchImagesByTag(mViewController, getApplicationContext(), tag);
        }
    };

    AdapterView.OnItemClickListener onThumbClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            // Get large image of selected thumbnail
            mUserInterfaceHandler.GetLargePhoto (position);
        }
    };
}
