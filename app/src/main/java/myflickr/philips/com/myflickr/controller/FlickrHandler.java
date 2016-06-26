package myflickr.philips.com.myflickr.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import myflickr.philips.com.myflickr.model.ImageRepository;
import myflickr.philips.com.myflickr.view.UserInterfaceHandler.ViewController;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;

import myflickr.philips.com.myflickr.model.ConnectionHandler;

/**
 * Created by lankamadan on 26-06-2016.
 */
public class FlickrHandler {
	// String to create Flickr API urls
	private static final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?method=";
	private static final String FLICKR_PHOTOS_SEARCH_STRING = "flickr.photos.search";
	private static final String FLICKR_GET_SIZES_STRING = "flickr.photos.getSizes";
	private static final int FLICKR_PHOTOS_SEARCH_ID = 1;
	private static final int FLICKR_GET_SIZES_ID = 2;
	
	//You can set here your API_KEY
	private static final String APIKEY_SEARCH_STRING = "&api_key=e427c6442578cc8a209328ee63b473a3";
	
	private static final String TAGS_STRING = "&tags=";
	private static final String PHOTO_ID_STRING = "&photo_id=";
	private static final String FORMAT_STRING = "&format=json";
	public static final int PHOTO_THUMB = 111;
	public static final int PHOTO_LARGE = 222;

	public static ViewController uihandler;

	private static String createURL(int methodId, String parameter) {
		String method_type = "";
		String url = null;
		switch (methodId) {
		case FLICKR_PHOTOS_SEARCH_ID:
			method_type = FLICKR_PHOTOS_SEARCH_STRING;
			url = FLICKR_BASE_URL + method_type + APIKEY_SEARCH_STRING + TAGS_STRING + parameter + FORMAT_STRING +"&media=photos";
			break;
		case FLICKR_GET_SIZES_ID:
			method_type = FLICKR_GET_SIZES_STRING;
			url = FLICKR_BASE_URL + method_type + PHOTO_ID_STRING + parameter + APIKEY_SEARCH_STRING + FORMAT_STRING;
			break;
		}
		return url;
	}

	public static Bitmap getImage(ImageRepository imgCon) {
		Bitmap bm = null;
		try {
			URL aURL = new URL(imgCon.getLargeURL());
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (Exception e) {
			Log.e("FlickrHandler", e.getMessage());
		}
		return bm;
	}

	public static void getThumbnails(ArrayList<ImageRepository> imgCon, ViewController uih) {
		for (int i = 0; i < imgCon.size(); i++)
			new GetThumbnailsThread(uih, imgCon.get(i)).start();
	}

	public static Bitmap getThumbnail(ImageRepository imgCon) {
		Bitmap bm = null;
		try {
			URL aURL = new URL(imgCon.getThumbURL());
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (Exception e) {
			Log.e("FlickrHandler", e.getMessage());
		}
		return bm;
	}

	// http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg
	public static void getImageURLS(ImageRepository imgCon) {
		String url = createURL(FLICKR_GET_SIZES_ID, imgCon.getId());
		ByteArrayOutputStream baos = ConnectionHandler.readBytes(url);
		String json = baos.toString();
		try {
			JSONObject root = new JSONObject(json.replace("jsonFlickrApi(", "").replace(")", ""));
			JSONObject sizes = root.getJSONObject("sizes");
			JSONArray size = sizes.getJSONArray("size");
			for (int i = 0; i < size.length(); i++) {
				JSONObject image = size.getJSONObject(i);
				if (image.getString("label").equals("Square")) {
					imgCon.setThumbURL(image.getString("source"));
				} else if (image.getString("label").equals("Medium")) {
					imgCon.setLargeURL(image.getString("source"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static class GetThumbnailsThread extends Thread {
		ViewController uih;
		ImageRepository imgContener;

		public GetThumbnailsThread(ViewController uih, ImageRepository imgCon) {
			this.uih = uih;
			this.imgContener = imgCon;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			imgContener.setThumb(getThumbnail(imgContener));
			if (imgContener.getThumb() != null) {
				Message msg = Message.obtain(uih, ViewController.ID_UPDATE_ADAPTER);
				uih.sendMessage(msg);

			}
		}

	}

	public static ArrayList<ImageRepository> searchImagesByTag(ViewController uih, Context ctx, String tag) {
		uihandler = uih;
		String url = createURL(FLICKR_PHOTOS_SEARCH_ID, tag);
		ArrayList<ImageRepository> tmp = new ArrayList<ImageRepository>();
		String jsonString = null;
		try {
			if (ConnectionHandler.isOnline(ctx)) {
				ByteArrayOutputStream baos = ConnectionHandler.readBytes(url);
				jsonString = baos.toString();
			}
			try {
				JSONObject root = new JSONObject(jsonString.replace("jsonFlickrApi(", "").replace(")", ""));
				JSONObject photos = root.getJSONObject("photos");
				JSONArray imageJSONArray = photos.getJSONArray("photo");
				for (int i = 0; i < imageJSONArray.length(); i++) {
					JSONObject item = imageJSONArray.getJSONObject(i);
					ImageRepository imgCon = new ImageRepository(item.getString("id"), item.getString("owner"), item.getString("secret"), item.getString("server"),
							item.getString("farm"));
					imgCon.setPosition(i);
					tmp.add(imgCon);
				}
				Message msg = Message.obtain(uih, ViewController.ID_METADATA_DOWNLOADED);
				msg.obj = tmp;
				uih.sendMessage(msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NullPointerException nue) {
			nue.printStackTrace();
		}

		return tmp;
	}

}
