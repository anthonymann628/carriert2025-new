package com.agilegeodata.carriertrack.android.utils;

import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;
import com.agilegeodata.carriertrack.android.database.DBHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/*
 * File maniuplation utilities
 */
@SuppressWarnings("unchecked")
public class FileUtils{
	private static final String TAG = FileUtils.class.getSimpleName();
	private static final Logger logger = LoggerFactory.getLogger(GlobalConstants.CARRIERTRACK_LOGGER);

	/**
	 * Helper for determining if a file exists on a path
	 * @param path
	 * @return boolean
	 */
	public static boolean doesFileExist(String path){
		boolean exists = false;
		File f = new File(path);
		exists = f.exists();

		return exists;
	}

	/**
	 * Helper for saving a file at a path
	 * @param myBytes, filePath, fileName
	 * @return void
	 */
	public static boolean saveFile(byte[] myBytes, String filePath, String fileName) throws FileNotFoundException{
		File parent = new File(filePath);

		if(parent != null && !parent.exists()){
			parent.mkdirs();
		}

		File outputFile = new File(filePath + fileName);
		FileOutputStream out = new FileOutputStream(outputFile);

		try{
			out.write(myBytes);
		}
		catch(Exception e){
			return false;
		}
		finally{
			try{
				out.close();
			}
			catch(IOException e){
			}
		}

		return true;
	}

	public static void copyFile(InputStream in, String directory, String filename) throws Exception{
//			logger.info("STARTED");
//			logger.info("directory = " + directory);
//			logger.info("filename = " + filename);

		File parent = new File(directory);

		if(parent != null && !parent.exists()){
			//logger.info("MAKING PARENT DIRS FOR " + directory);
			parent.mkdirs();
		}

		File outputFile = new File(directory + filename);
//			logger.info("OUTPUT FILE " + directory + filename + " EXISTS = " + outputFile.exists());
		if(!outputFile.exists()){
			outputFile.createNewFile();
//				logger.info("CREATED NEW OUTPUT FILE FOR " + directory + filename);
		}

//			logger.info("File saved to : " + outputFile.getCanonicalPath());

		FileOutputStream fos = new FileOutputStream(outputFile);
		try{
			byte[] buf = new byte[1024];
			int i = 0;

			while((i = in.read(buf)) != -1){
				fos.write(buf, 0, i);
			}
		}
		catch(Exception e){
			logger.error("Exception", e);
			throw e;
		}
		finally{
			if(in != null){
				in.close();
			}

			if(fos != null){
				fos.close();
			}
		}

//			logger.info("outputFile.length = " + outputFile.length());
	}

	public static void deleteFile(String fileName) throws Exception{
		try{
			File file = new File(fileName);
			file.delete();
		}
		catch(Exception e){
			throw e;
		}
	}

	public static boolean deleteEmptyDirectory(File path){
		if(path.exists()){
			return (path.delete());
		}
		else{
			return false;
		}
	}

	public static boolean deleteDirectory(File path){
		if(path.exists()){
			File[] files = path.listFiles();

			for(int i = 0; i < files.length; i++){
				if(files[i].isDirectory()){
					deleteDirectory(files[i]);
				}
				else{
					files[i].delete();
				}
			}
		}

		return (path.delete());
	}

	public static void deflateContentSingleFileNew(String filename, String destFileName) throws IOException{
//		logger.info("STARTED");
//		logger.info("filename = " + filename);
//		logger.info("destFileName = " + destFileName);

		byte[] buffer = new byte[1024];

		try{
//			logger.info("opening file " + filename);
			ZipFile zip = new ZipFile(new File(filename), ZipFile.OPEN_READ);

			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
//			logger.info("entries.toString() = " + zip.entries());

			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();

				if(entry.isDirectory()){
//					logger.info("entry.isDirectory() = " + entry.toString());
					continue;
				}

				File theZipFile = new File(filename);
				String parentDir = "";

				try{
					parentDir = theZipFile.getParentFile().getCanonicalPath();
				}
				catch(Exception e){
					logger.error("EXCEPTION : " + e.getMessage());
				}

//				logger.info("zip file parent dir = " + parentDir);

				File parent = new File(parentDir + entry.getName()).getParentFile();
				if(parent != null && !parent.exists()){
					parent.mkdirs();
				}

				InputStream in = zip.getInputStream(entry);
//				logger.info("zip file destFileName = " + destFileName);
				FileOutputStream out = new FileOutputStream(destFileName);

				for(int len; (len = in.read(buffer)) >= 0; ){
					out.write(buffer, 0, len);
//					logger.info("zip file wrote " + len + " bytes");
				}

				out.flush();

				in.close();
				out.close();

				break;
			}

			zip.close();
		}
		catch(ZipException e1){
			logger.error("Exception", e1);

			throw e1;
		}
		catch(IOException e){
			logger.error("Exception" + filename, e);
		}
	}

	/*
	 * deflateContentSingleFile(String saveDir, String filename, String destFileName)
	 * Will stop processing zip file after the first file is found....
	 */
	@SuppressWarnings("unchecked")
	public static void deflateContentSingleFile(String saveDir, String filename, String destFileName) throws IOException{
//			logger.info(">>>>deflateContentSingleFile() : STARTED");
//			logger.info(">>>>deflateContentSingleFile() : saveDir = " + saveDir);
//			logger.info(">>>>deflateContentSingleFile() : filename = " + filename);
//			logger.info(">>>>deflateContentSingleFile() : destFileName = " + destFileName);

		filename = saveDir + filename;
		byte[] buffer = new byte[1024];

		try{
//			logger.info(">>>>deflateContentSingleFile() : opening file " + filename);
			ZipFile zip = new ZipFile(new File(filename), ZipFile.OPEN_READ);

			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();

			while(entries.hasMoreElements()){
				ZipEntry entry = entries.nextElement();

				if(entry.isDirectory()){
					continue;
				}

				File parent = new File(saveDir + entry.getName()).getParentFile();
				if(parent != null && !parent.exists()){
					parent.mkdirs();
				}

				InputStream in = zip.getInputStream(entry);
				FileOutputStream out = new FileOutputStream(saveDir + destFileName);

				for(int len; (len = in.read(buffer)) >= 0; ){
					out.write(buffer, 0, len);
				}
				out.flush();

				in.close();
				out.close();
				break;
			}
			zip.close();
		}
		catch(ZipException e1){
			logger.error("ZipException", e1);

			throw e1;
		}
		catch(IOException e){
			logger.error("IOException" + filename + "'", e);
		}
	}

	public static long getFileSize(String fileName){
		File file = new File(fileName);

		return file.length();
	}

	public static byte[] readFileToByteArray(String fileName){
		File file = new File(fileName);
		byte[] fileBArray = new byte[(int) file.length()];
		FileInputStream fis = null;

		try{
			fis = new FileInputStream(file);
			fis.read(fileBArray);
		}
		catch(FileNotFoundException e){
			return null;

		}
		catch(IOException e){
			return null;
		}

		return fileBArray;
	}

	static public String getAppDirectoryForSavedFiles(){
		String dir = "";
		String sdcard = "";

		try{
			sdcard = CTApp.getCustomAppContext().getExternalFilesDir(null).getCanonicalPath();
			dir = sdcard + "/savedfiles/";
		}
		catch(Exception e){
			logger.error("EXCEPTION : " + e.getMessage());
		}

		return dir;
	}

	static public String getAppDirectoryForFiles(){
		String dir = "";
		String sdcard = "";

		try{
			dir = CTApp.getCustomAppContext().getExternalFilesDir(null).getCanonicalPath() + "/";
		}
		catch(Exception e){
			logger.error("EXCEPTION : " + e.getMessage());
		}

		return dir;
	}

	static public String getAppDirectoryForLogFiles(){
		String dir = "";
		String sdcard = "";

		try{
			sdcard = CTApp.getCustomAppContext().getExternalFilesDir(null).getCanonicalPath();
			dir = sdcard + "/logfiles/";
		}
		catch(Exception e){
			logger.error("EXCEPTION : " + e.getMessage());
		}

		return dir;
	}

	static public String getAppDirectoryForDownloadFiles(){
		String dir = "";
		String sdcard = "";

		try{
			sdcard = CTApp.getCustomAppContext().getExternalFilesDir(null).getCanonicalPath();
			dir = sdcard + "/savedfiles/";
		}
		catch(Exception e){
			logger.error("EXCEPTION : " + e.getMessage());
		}

		return dir;
	}

	static public String getAppDirectoryForDataBaseFiles(){
		DBHelper db = DBHelper.getInstance();
		String dir = DBHelper.DB_PATH + "/";

		return dir;
	}
}