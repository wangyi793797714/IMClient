package util;

import java.io.File;
import java.io.IOException;

import config.Const;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

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
    
    public static String getDbPath(Context context){
        String folderPath = FileOperator.getPath(context) + File.separator + Const.DB_FOLDER_NAME
                + File.separator;
        return folderPath+Const.DB__NAME;
    }
}
