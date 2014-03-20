package broadcast;

import vo.AddFriendResponse;
import adapter.FriendAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import config.Const;

public class ReponseAddFriendReceiver extends BroadcastReceiver {

    private Activity act;

    private FriendAdapter adapter;

    public ReponseAddFriendReceiver(Activity act, FriendAdapter adapter) {
        this.act = act;
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_ADDFRIEND_REPONSE.equals(intent.getAction())) {
            final AddFriendResponse resp = (AddFriendResponse) intent.getSerializableExtra("resp");
            if (resp.isAccept()) {
                if (resp.getResponseName() != null && resp.getResponseName().length() > 0) {
                    // 这里是请求方
                    Toast.makeText(act, resp.getResponseName() + "同意了我的好友请求", Toast.LENGTH_SHORT)
                            .show();
                    adapter.addItem(resp.getRespFriend(), 0);
                } else {
                    // 这里是被请求方
                    adapter.addItem(resp.getRespFriend(), 0);
                }
            } else {
                Toast.makeText(act, resp.getRespFriend().getName() + "拒绝了我的好友请求",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
