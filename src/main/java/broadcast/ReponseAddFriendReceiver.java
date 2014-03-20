package broadcast;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import vo.AddFriendResponse;
import vo.Myself;
import vo.OnlineFriends;
import adapter.OnlineAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import config.Const;

public class ReponseAddFriendReceiver extends BroadcastReceiver {

    private Activity act;

    private OnlineAdapter adapter;

    public ReponseAddFriendReceiver(Activity act, OnlineAdapter adapter) {
        this.act = act;
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_ADDFRIEND_REPONSE.equals(intent.getAction())) {
            final AddFriendResponse resp = (AddFriendResponse) intent.getSerializableExtra("resp");
            if (resp.isAccept()) {
                Myself respFriend = resp.getRespFriend();
                respFriend.setOnline(true);
                if (resp.getResponseName() != null && resp.getResponseName().length() > 0) {
                    // 这里是请求方
                    Toast.makeText(act, resp.getResponseName() + "同意了我的好友请求", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // 这里是被请求方
                }
                adapter.addItem(respFriend, 0);
                // 新增好友成功，将好友视作上线的好友，保存到sd卡中
                FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
                OnlineFriends friend = new OnlineFriends();
                friend.setChannelId(resp.getRespFriend().getChannelId());
                friend.setName(resp.getRespFriend().getName());
                db.save(friend);
            } else {
                Toast.makeText(act, resp.getRespFriend().getName() + "拒绝了我的好友请求",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
