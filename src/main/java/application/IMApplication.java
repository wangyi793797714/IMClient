package application;

import java.util.Stack;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class IMApplication extends Application {

    private Stack<BroadcastReceiver> broadcastStack;

    private Stack<Activity> activityStack;

    public static IMApplication APP;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastStack = new Stack<BroadcastReceiver>();
        activityStack = new Stack<Activity>();
    }

    public synchronized void reReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        broadcastStack.add(receiver);
        registerReceiver(receiver, filter);
    }

    public synchronized void addActivity(Activity act) {
        activityStack.add(act);
    }

    public static void set(IMApplication app) {
        APP = app;
    }

    public void closeReceiver() {
        for (BroadcastReceiver receiver : broadcastStack) {
            unregisterReceiver(receiver);
        }
        broadcastStack.clear();
    }

    public void finishAct() {
        for (Activity act : activityStack) {
            act.finish();
        }
        activityStack.clear();
        System.exit(0);
    }
}
