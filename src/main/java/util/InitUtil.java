package util;

import java.io.File;

import config.Const;
import android.content.Context;

public class InitUtil {
    public static boolean createFolder(Context context) {
        return FileOperator.createFolder(folderPath(context));
    }

    public static void createFile(Context context,String fileName) {
            FileOperator.createFile(folderPath(context) + fileName);
    }

    private static String folderPath(Context context) {
        String folderPath = FileOperator.getPath(context) + File.separator + Const.DB_FOLDER_NAME
                + File.separator;
        return folderPath;
    }
}
