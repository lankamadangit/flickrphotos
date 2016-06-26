package myflickr.philips.com.myflickr.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;

import java.util.ArrayList;

import myflickr.philips.com.myflickr.controller.FlickrHandler;
import myflickr.philips.com.myflickr.model.ImageRepository;
import myflickr.philips.com.myflickr.R;

/**
 * Created by lankamadan on 26-06-2016.
 */
public class UserInterfaceHandler {
    public final String LAST_IMAGE = "lastImage";
    public ImageAdapter mImageAdapter;
    private ArrayList<ImageRepository> pImageList;

    // UI
    private Gallery pGalleryObject;
    private ImageView pImageView;
    private EditText pEditText;
    public ViewController mViewController = new ViewController();

    public void GetLargePhoto (int position) {
        new GetLargePhotoThread(pImageList.get(position), mViewController).start();
    }

    public void SetGalleryObject (Gallery galleryObject)
    {
        pGalleryObject = galleryObject;
    }

    public void SetImageViewObject (ImageView imageView)
    {
        pImageView = imageView;
    }

    public void SearchImagesByTag (Context ctx, String tag)
    {
        mViewController.setmAppContext(ctx);
        FlickrHandler.searchImagesByTag(mViewController, ctx, tag);
    }

    public void SetAdapter ()
    {
        mImageAdapter.imageRepository = new ArrayList<ImageRepository>();
        pGalleryObject.setAdapter(mImageAdapter);
        pImageView.setVisibility(View.INVISIBLE);
    }

    /**
     *
     * Actual picture download
     */
    public class GetLargePhotoThread extends Thread {
        ImageRepository ic;
        ViewController uih;

        public GetLargePhotoThread(ImageRepository ic, ViewController uih) {
            this.ic = ic;
            this.uih = uih;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (ic.getPhoto() == null) {
                ic.setPhoto(FlickrHandler.getImage(ic));
            }
            Bitmap bmp = ic.getPhoto();
            if (ic.getPhoto() != null) {
                Message msg = Message.obtain(uih, ViewController.ID_SHOW_IMAGE);
                msg.obj = bmp;
                uih.sendMessage(msg);
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private int defaultItemBackground;
        private ArrayList<ImageRepository> imageRepository;

        public ArrayList<ImageRepository> getImageRepository() {
            return imageRepository;
        }

        public void setImageContener(ArrayList<ImageRepository> imageRepository) {
            this.imageRepository = imageRepository;
        }

        public ImageAdapter(Context c, ArrayList<ImageRepository> imageRepository) {
            mContext = c;
            this.imageRepository = imageRepository;
            TypedArray styleAttrs = c.obtainStyledAttributes(R.styleable.PicGallery);
            styleAttrs.getResourceId(R.styleable.PicGallery_android_galleryItemBackground, 0);
            defaultItemBackground = styleAttrs.getResourceId(R.styleable.PicGallery_android_galleryItemBackground, 0);
            styleAttrs.recycle();
        }

        public int getCount() {
            return imageRepository.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            if (imageRepository.get(position).getThumb() != null) {
                i.setImageBitmap(imageRepository.get(position).getThumb());
                i.setLayoutParams(new Gallery.LayoutParams(600, 600));
                i.setBackgroundResource(defaultItemBackground);
            } else
                i.setImageDrawable(mContext.getResources().getDrawable(android.R.color.black));
            return i;
        }

    }

    public class ViewController extends Handler {
        public static final int ID_METADATA_DOWNLOADED = 0;
        public static final int ID_SHOW_IMAGE = 1;
        public static final int ID_UPDATE_ADAPTER = 2;
        public Context mAppContext;

        public Context getmAppContext() {
            return mAppContext;
        }

        public void setmAppContext(Context mAppContext) {
            this.mAppContext = mAppContext;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_METADATA_DOWNLOADED:
                    // Set of information required to download thumbnails is
                    // available now
                    if (msg.obj != null) {
                        pImageList = (ArrayList<ImageRepository>) msg.obj;
                        mImageAdapter = new ImageAdapter(getmAppContext(), pImageList);
                        pGalleryObject.setAdapter(mImageAdapter);
                        for (int i = 0; i < mImageAdapter.getCount(); i++) {
                            new FlickrHandler.GetThumbnailsThread(mViewController, mImageAdapter.getImageRepository().get(i)).start();
                        }
                    }
                    break;
                case ID_SHOW_IMAGE:
                    // Display large image
                    if (msg.obj != null) {
                        pImageView.setImageBitmap((Bitmap) msg.obj);
                        pImageView.setVisibility(View.VISIBLE);
                    }
                    break;
                case ID_UPDATE_ADAPTER:
                    // Update adapter with thumbnails
                    if(mImageAdapter != null) {
                        mImageAdapter.notifyDataSetChanged();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
