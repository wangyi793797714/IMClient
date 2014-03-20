package application;

import java.util.Stack;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class IMApplication extends Application {
    
    public Stack<BroadcastReceiver> stack;
    
    public static IMApplication APP;

    @Override
    public void onCreate() {
        super.onCreate();
        stack = new Stack<BroadcastReceiver>();
    }

    public synchronized void reReceiver(BroadcastReceiver receiver,IntentFilter filter) {
        stack.add(receiver);
        registerReceiver(receiver, filter);
    }
    
    public static void set(IMApplication app) {
        APP = app;
    }
    
    public void closeReceiver(){
        for (BroadcastReceiver receiver:stack) {
            unregisterReceiver(receiver);
        }
        stack.clear();
    }
    
}
