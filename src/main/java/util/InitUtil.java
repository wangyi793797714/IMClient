package util;

import java.io.File;

import android.content.Context;

public class InitUtil {
    public static boolean createFolder(Context context,String folderName) {
        return FileOperator.createFolder(folderPath(context,folderName));
    }

    public static void createFile(Context context,String fileName,String folderName) {
            FileOperator.createFile(folderPath(context,folderName) + fileName);
    }

    private static String folderPath(Context context,String folderName) {
        String folderPath = FileOperator.getPath(context) + File.separator + folderName
                + File.separator;
        return folderPath;
    }
    
    public static boolean createImageFolder(Context context,String folderName){
    	return FileOperator.createFolder(folderPath(context,folderName));
    }
}
