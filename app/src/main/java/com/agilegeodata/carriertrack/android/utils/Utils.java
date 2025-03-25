package com.agilegeodata.carriertrack.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class Utils{
	public static final String TAG = Utils.class.getSimpleName();
	protected static Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	public static void showAlertMessage(Context context, String text){
		Toast toast = createToast(CTApp.getCustomAppContext(), text);
		toast.show();
	}

	public static String getDeviceId(Context ctx){

		//=== generate unique id
		SharedPreferences prefs = ctx.getSharedPreferences(GlobalConstants.DEFAULT_PREF_FILE, Context.MODE_PRIVATE);
		return prefs.getString(GlobalConstants.PREF_DEVICE_ID, null);
	}

	private static Toast createToast(Context context, String text){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.toast, null);
		//=== set the text in the view
		TextView message = view.findViewById(R.id.toast_message);
		message.setText(text);
		ImageView image = view.findViewById(R.id.toast_image);
		image.setImageResource(R.drawable.icon);

		Toast toast = new Toast(CTApp.getCustomAppContext());
		toast.setView(view);
		toast.setDuration(Toast.LENGTH_LONG);
		View toastView = toast.getView();
		toastView.setBackgroundResource(R.color.black);

		return toast;
	}

	public static List<NameValuePair> buildURLParamsPost(java.util.Map<String, String> query){
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		if(query == null){
			query = new HashMap<String, String>();
		}

		Set<Entry<String, String>> entries = query.entrySet();
		Iterator<Entry<String, String>> it = entries.iterator();

		//	logger.debug(TAG,"--------------Number of Keys: " + query.size());
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			String keyContent = key == null ? "NULL" : key.trim().isEmpty() ? "NULL" : key;
			String valueContent = value == null ? "NULL" : value.trim().isEmpty() ? "NULL" : value;
//			logger.debug("Utils.buildURLParametersPost() : Adding KEY: " + keyContent + " Value: " + valueContent);
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return nvps;
	}

	public static void logHttpHeaders(Header[] headers){
		//logger.debug(TAG,"logger.infong http headers: " + headers.length );
		for(int i = 0; i < headers.length; i++){
			Header h = headers[i];
			//logger.debug(TAG,"Header: " + h.getName() + " " + h.getValue());
		}
	}

	public static String inputStreamToString(InputStream stream){
		StringBuffer out = new StringBuffer();
		try{
			byte[] b = new byte[4096];

			for(int n; (n = stream.read(b)) != -1; ){
				out.append(new String(b, 0, n));
			}
		}
		catch(IOException e){
			logger.debug(TAG, "Exception processing input stream" + e);
		}
		if(out != null){
			logger.debug("Output stream is: " + out);
			return out.toString();
		}
		else{
			return null;
		}
	}
}
