package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import config.Const;

public class FileOperator {

	public static boolean delSdcardFile(String target) {
		File file = new File(target);
		if (file.exists()) {
			if (!file.isDirectory()) {
				return file.delete();
			}
			return false;
		} else {
			return true;
		}
	}

	public static boolean createFolder(String name) {
		File folder = new File(name);
		if (folder.exists()) {
			return false;
		} else {
			return folder.mkdirs();
		}
	}

	public static boolean createFile(String name) {
		if (TextUtils.isEmpty(name)) {
			return false;
		}
		File file = new File(name);
		try {
			return file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String getPath(Context context) {

		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		File mf = null;
		if (sdCardExist) {
			mf = Environment.getExternalStorageDirectory();
		} else {
			mf = context.getFilesDir();
		}
		return mf.toString();
	}

	public static String getDbPath(Context context) {
		String folderPath = FileOperator.getPath(context) + File.separator
				+ Const.DB_FOLDER_NAME + File.separator;
		return folderPath + Const.DB__NAME;
	}

	public static String getLocalImageFolderPath(Context context) {
		String folderPath = FileOperator.getPath(context) + File.separator
				+ Const.LOCAL__IMAGE_FOLDER + File.separator;
		return folderPath;
	}

	public static String getLocalVoiceFolderPath(Context context) {
		String folderPath = FileOperator.getPath(context) + File.separator
				+ Const.LOCAL__VOICE_FOLDER + File.separator;
		return folderPath;
	}

	public static void saveImage2Sd(Context context, Bitmap bm, String fileName) {
		try {
			File myCaptureFile = new File(getLocalImageFolderPath(context)
					+ fileName);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(myCaptureFile));
			bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveVoice2Sd(Context context, String voice,
			String fileName) {
		try {
			File voiceFile = new File(getLocalVoiceFolderPath(context)
					+ fileName);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(voiceFile));
			byte bytes[] = Base64.decode(voice.getBytes(), Base64.DEFAULT);
			bos.write(bytes);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
