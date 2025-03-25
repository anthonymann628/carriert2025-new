package com.agilegeodata.carriertrack.android.database;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;

public class DBUpdate{
	public static final String TAG = DBUpdate.class.getSimpleName();

	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	public HashMap<Integer, Integer> executeDatabaseUpdatesZipNew(InputStream data, String filePathName){
		logger.info("START");
//        logger.info(">>>>executeDatabaseUpdatesZipNew() : filePathName = " + filePathName);

		//int count = DBHelper.fetchCountByQuery("SELECT count(*) from addressdetailproducts");
		//logger.error("ADDRESS DETAIL PRODUCT COUNT = " + count);

		int errorCode = GlobalConstants.ERROR_CODE_NONE;

		String unzippedFilePathName = filePathName.replace(GlobalConstants.DBUPDATE_ZIPFILENAME, GlobalConstants.DBUPDATE_FILENAME);
//        logger.info("unzippedFilePathName = " + unzippedFilePathName);

		try{
//            logger.info("CALLING FileUtils.deflateContentSingleFileNew()");
			FileUtils.deflateContentSingleFileNew(filePathName, unzippedFilePathName);

			long len = FileUtils.getFileSize(unzippedFilePathName);
//            logger.info("fileName " + unzippedFilePathName + " length is " + len);

			boolean isFirst = true;
			try{
				// Open the file that is the first
				// command line parameter
				FileInputStream fstream = new FileInputStream(unzippedFilePathName);

				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String userInput;

				//=== Read File Line By Line
//                logger.debug("############### READING THE DOWNLOAD FILE LINE BY LINE #############");

				while((userInput = br.readLine()) != null){
//                    logger.info(unzippedFilePathName + "############### READLINE = " + userInput);
					if(isFirst){
						//======LOOKS LIKE ->^*^85^*^
						String uI = userInput.substring(3, userInput.length() - 3);
						logger.info("################# IS FIRST - dataLength = " + uI);
						isFirst = false;
						int dataLength = Integer.valueOf(uI);
						logger.info("############### IS FIRST - Stream Len: " + len + " Reported Len: " + dataLength);

						if(dataLength != len){
							logger.info("############# IS FIRST - Stream Len: " + len + " Reported Len: " + dataLength);

							errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_BAD_RESPONSE;
							logger.info("################# IS FIRST ERROR - ERROR_CODE_DOWNLOAD_BAD_RESPONSE");
							break;
						}
					}
					else{
//                        logger.info("CALLING  DBHelper.doExecQuery() WITH COMMAND -> " + userInput);
						if(userInput.length() > 0 && !userInput.equals("\n")){
//							logger.debug("DBUpdate : SQL line = " + userInput);
							DBHelper.getInstance().doExecQuery_Common(userInput);
						}
					}
				}

				//=== Close the input stream
				in.close();
				//int count2 = DBHelper.fetchCountByQuery("SELECT count(*) from addressdetailproducts");
				//logger.error("ADDRESS DETAIL PRODUCT COUNT = " + count2);
			}
			catch(Exception e){// Catch exception if any
				errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_DATABASE_LOADERROR;
				logger.error("EXCEPTION", e);
			}
		}
		catch(Exception e){
			errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_DATABASE_LOADERROR;
			logger.error("EXCEPTION", e);
		}

		if(errorCode == GlobalConstants.ERROR_CODE_NONE){
			logger.info("END");

			return DBHelper.getInstance().fetchRouteCountByJobDetailsId_Common();
		}
		else{
			logger.info("END");

			return null;
		}
	}

	public HashMap<Integer, Integer> executeDatabaseUpdatesZip(InputStream data, String saveDir){
//        logger.info(">>>>executeDatabaseUpdatesZip() : START");
//        logger.info(">>>>executeDatabaseUpdatesZip() : dir to save is " + saveDir);

		//int count = DBHelper.fetchCountByQuery("SELECT count(*) from addressdetailproducts");
		//logger.error("ADDRESS DETAIL PRODUCT COUNT 2 = " + count);

		int errorCode = GlobalConstants.ERROR_CODE_NONE;

		try{
			long datetime = Calendar.getInstance().getTimeInMillis();

			String fileName = saveDir + datetime + "_" + GlobalConstants.DBUPDATE_FILENAME;
//            logger.info(">>>>executeDatabaseUpdatesZip() : fileName is " + fileName);

			FileUtils.copyFile(data, saveDir, datetime + "_" + GlobalConstants.DBUPDATE_ZIPFILENAME);

			FileUtils.deflateContentSingleFile(saveDir, datetime + "_"
														+ GlobalConstants.DBUPDATE_ZIPFILENAME, datetime + "_" + GlobalConstants.DBUPDATE_FILENAME);

			long len = FileUtils.getFileSize(fileName);
//            logger.info(">>>>executeDatabaseUpdatesZip() : fileName " + fileName + " length is " + len);

			boolean isFirst = true;
			try{
				//=== Open the file that is the first
				//=== command line parameter
				FileInputStream fstream = new FileInputStream(fileName);

				//=== Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String userInput;

				//=== Read File Line By Line
				while((userInput = br.readLine()) != null){
					if(isFirst){
						String uI = userInput.substring(3, userInput.length() - 3);
						isFirst = false;
						int dataLength = Integer.valueOf(uI);

						if(dataLength != len){
//                            logger.info(">>>>executeDatabaseUpdatesZip() : Stream Len: " + len						+ " Reported Len: " + dataLength);

							errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_BAD_RESPONSE;
							break;
						}
					}
					else{
//                        logger.info(">>>>executeDatabaseUpdatesZip() : Stream Len: ");
						DBHelper.getInstance().doExecQuery_Common(userInput);
					}
				}

				//=== Close the input stream
				in.close();
				//int count2 = DBHelper.fetchCountByQuery("SELECT count(*) from addressdetailproducts");
				//logger.error("ADDRESS DETAIL PRODUCT COUNT 2 = " + count2);
			}
			catch(Exception e){// Catch exception if any
				errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_DATABASE_LOADERROR;
				logger.error("1 : ", e);
			}
		}
		catch(Exception e){// Catch exception if any
			errorCode = GlobalConstants.ERROR_CODE_DOWNLOAD_DATABASE_LOADERROR;
			logger.error("2 : ", e);
		}

		if(errorCode == GlobalConstants.ERROR_CODE_NONE){
			logger.info("END");

			return DBHelper.getInstance().fetchRouteCountByJobDetailsId_Common();
		}
		else{
			logger.info("END");

			return null;
		}
	}
}
