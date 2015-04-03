package util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import config.Const;


/**
 * 
 * @Des: TODO
 * @author Rhino 
 * @version V1.0 
 * @created  2014年6月4日 下午6:17:29
 */
@SuppressLint("CommitPrefEdits")
public class SpUtil {

    private SharedPreferences sp = null;

    private Editor edit = null;

    public SpUtil(SharedPreferences sp) {
        this.sp = sp;
        edit = sp.edit();
    }

    public SpUtil(Context context) {
        this(context.getSharedPreferences(Const.SP_NAME,
                Context.MODE_PRIVATE));
    }

    public void setValue(String key, boolean value) {
        edit.putBoolean(key, value);
        edit.commit();
    }

    public void setValue(String key, float value) {
        edit.putFloat(key, value);
        edit.commit();
    }

    public void setValue(String key, int value) {
        edit.putInt(key, value);
        edit.commit();
    }

    public void setValue(String key, long value) {
        edit.putLong(key, value);
        edit.commit();
    }

    public void setValue(String key, String value) {
        edit.putString(key, value);
        edit.commit();
    }

    public boolean getBoolValue(String key) {
        return sp.getBoolean(key, true);
    }

    public float getFloatValue(String key) {
        return sp.getFloat(key, 0);
    }

    public int getIntegerValue(String key) {
        return sp.getInt(key, 0);
    }

    public long getLongValue(String key) {
        return sp.getLong(key, 0);
    }

    public String getStringValue(String key) {
        return sp.getString(key, "");
    }

    public void remove(String key) {
        edit.remove(key);
        edit.commit();
    }

    public void clear() {
        edit.clear();
        edit.commit();
    }

}
