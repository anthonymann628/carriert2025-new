package com.agilegeodata.carriertrack.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.agilegeodata.carriertrack.android.R;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.utils.FileUtils;
import com.agilegeodata.carriertrack.android.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/*
 *  Displays the custom log file
 *  Used for benchmarking the details
 */
public class LogFileViewer extends Activity{
	public static final String TAG = LogFileViewer.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	private WebView myWebView;
	private String mFileName;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//logger.debug("Displaying log file viewer");

		setContentView(R.layout.logfileviewer);
		myWebView = findViewById(R.id.webView);
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.getSettings().setSupportZoom(true);
		myWebView.getSettings().setUseWideViewPort(true);
		myWebView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);

		myWebView.setInitialScale(31);
		myWebView.getSettings().setBuiltInZoomControls(true);

		// load the first item
		loadHTML(false);

		Button deleteButton = findViewById(R.id.btnDelete);
		deleteButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//============sync with logger path
				try{
					String logfilePath = FileUtils.getAppDirectoryForLogFiles() + GlobalConstants.LOGGER_FILENAME + ".txt";
					FileUtils.deleteFile(logfilePath);
					loadHTML(false);
				}
				catch(Exception e){
					Utils.showAlertMessage(LogFileViewer.this, getResources().getString(R.string.errorUnableToDelete, e));
				}
			}
		});

		Button refreshButton = findViewById(R.id.btnRefresh);
		refreshButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Utils.showAlertMessage(LogFileViewer.this, getResources().getString(R.string.loadingLogFile));
				loadHTML(false);
			}

		});

		Utils.showAlertMessage(LogFileViewer.this, getResources().getString(R.string.loadingLogFile));
	}

	private void loadHTML(boolean blank){
		StringBuffer out = new StringBuffer();
		out.append("<html>" + "<head>" + "</head>" + "<body>");

		if(blank){
			out.append("No Log File");
		}
		else{
			try{
				//============sync with logger path
				//logger.debug("Reading: " + filePath);
				String logfilePath = FileUtils.getAppDirectoryForLogFiles() + GlobalConstants.LOGGER_FILENAME + ".txt";
				FileInputStream fstream = new FileInputStream(logfilePath);

				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String line = null;

				while((line = br.readLine()) != null){
					out.append(line + "<br><br>");
				}

				//Close the input stream
				in.close();
				br.close();

				//logger.debug("log is: " + out.toString());
			}
			catch(Exception e){//Catch exception if any
				out.append("No Log File");
				logger.debug("Exception: ", e);
			}
		}

		out.append("</body></html>");
		myWebView.loadData(out.toString(), "text/html", "UTF-8");
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		System.gc();
	}
}