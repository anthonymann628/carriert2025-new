package com.agilegeodata.carriertrack.android.services;

import android.app.IntentService;
import android.content.Intent;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;

/*Deprecated
IntentService is subject to all the background execution limits imposed with Android 8.0 (API level 26).
Consider using androidx. work. WorkManager instead.
*/
public class CleanupIntentService extends IntentService{
	public static final String TAG = CleanupIntentService.class.getSimpleName();

	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	public CleanupIntentService(){
		super("CleanupIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent){
		logger.info("STARTED CLEANUP SERVICE");

		String savePath = FileUtils.getAppDirectoryForSavedFiles();
		//logger.info("savePath = " + savePath);

		//=== Get the list of files in the directory
		File dir = new File(savePath);
		FilenameFilter filter = new FilenameFilter(){

			public boolean accept(File dir, String filename){
				return (filename.endsWith(".jpg") ||
						filename.endsWith(".zip") ||
						filename.endsWith(".txt") ||
						filename.endsWith(".db"));
			}

		};

		//logger.info("dir.isDirectory() = " + dir.isDirectory());
		if(dir.isDirectory()){
			File[] files = dir.listFiles(filter);
			//logger.info("files.length = " + files.length);

			//=== Loop through all files
			for(File f : files){
				// Get the last modified date in milliseconds since 1970
				Long lastModified = f.lastModified();

				//=== Delete file if older than 10 days
				if(lastModified + GlobalConstants.MAX_FILE_AGE < System.currentTimeMillis()){
					//logger.info("deletefile = " + String.format("%s", f.getName()));
					f.delete();
				}
			}
		}
		logger.info("ENDED CLEANUP SERVICE");
	}
}
