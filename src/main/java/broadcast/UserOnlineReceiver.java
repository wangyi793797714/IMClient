package broadcast;

import java.util.List;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import vo.Myself;
import vo.OnlineFriends;
import adapter.OnlineAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import config.Const;

public class UserOnlineReceiver extends BroadcastReceiver {

    private OnlineAdapter adapter;

    private Activity act;

    public UserOnlineReceiver(OnlineAdapter adapter, Activity act) {
        this.adapter = adapter;
        this.act = act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_ON_OR_OFF_LINE.equals(intent.getAction())) {
            Myself user = (Myself) intent.getSerializableExtra("user");
            // 上线
            FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
            if (user.getName() != null) {
                OnlineFriends on = new OnlineFriends();
                on.setChannelId(user.getChannelId());
                on.setName(user.getName());
                db.save(on);
                adapter.addItem(user, adapter.getCount());
            }
            // 下线
            else {
                List<Myself> onlineUsers = adapter.getDataSource();
                for (int i = 0; i < onlineUsers.size(); i++) {
                    if (user.getChannelId() == onlineUsers.get(i).getChannelId()) {
                        Myself u =onlineUsers.get(i);
                        adapter.remove(onlineUsers.get(i));
                        //TODO用户下线，删除
                        db.deleteByWhere(OnlineFriends.class, "channelId = "+u.getChannelId());
                        return;
                    }
                }
            }
        }
    }
}