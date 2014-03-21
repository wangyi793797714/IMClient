package broadcast;

import java.util.List;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import vo.Myself;
import vo.Friends;
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
                List<Myself> onlineUsers = adapter.getDataSource();
                for (int i = 0; i < onlineUsers.size(); i++) {
                    if (user.getChannelId() == onlineUsers.get(i).getChannelId()) {
                        Myself u = onlineUsers.get(i);
                        onlineUsers.get(i).setOnline(true);
                        adapter.refresh();
                        // 好友上线，保存好友在线信息
                        Friends friend = new Friends();
                        friend.setChannelId(u.getChannelId());
                        friend.setName(u.getName());
                        friend.setOnline(true);
                        db.update(friend, "channelId = "+u.getChannelId());
                        return;
                    }
                }
            }
            // 下线
            else {
                List<Myself> onlineUsers = adapter.getDataSource();
                for (int i = 0; i < onlineUsers.size(); i++) {
                    if (user.getChannelId() == onlineUsers.get(i).getChannelId()) {
                        Myself u = onlineUsers.get(i);
                        onlineUsers.get(i).setOnline(false);
                        adapter.refresh();
                        // 好友下线，删除sd中保存的在线信息
                        Friends friend = new Friends();
                        friend.setChannelId(u.getChannelId());
                        friend.setName(u.getName());
                        friend.setOnline(false);
                        db.update(friend, "channelId = "+u.getChannelId());
                        return;
                    }
                }
            }
        }
    }
}