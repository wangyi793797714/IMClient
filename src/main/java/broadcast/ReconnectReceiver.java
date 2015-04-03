package broadcast;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import vo.Myself;
import adapter.OnlineAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import aysntask.FetchOnlineUserTask;
import config.Const;

/**
 * 
 * @Des: 重连通知
 * @author Rhino 
 * @version V1.0 
 * @created  2015年4月3日 下午3:14:34
 */
public class ReconnectReceiver extends BroadcastReceiver {

    private OnlineAdapter adapter;

    private Activity act;

    public ReconnectReceiver(OnlineAdapter adapter, Activity act) {
        this.adapter = adapter;
        this.act = act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_RECONNECT.equals(intent.getAction())) {
            //第一步所有头像置灰，执行重连
        	adapter.offLine();
        	FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
            new FetchOnlineUserTask(act, adapter).execute(db.findAll(Myself.class).get(0));
        }
    }
}