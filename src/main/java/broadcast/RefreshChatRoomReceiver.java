package broadcast;

import util.Util;
import vo.FriendBody;
import adapter.MyExpandAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import config.Const;

public class RefreshChatRoomReceiver extends BroadcastReceiver {

    private MyExpandAdapter adapter;

    public RefreshChatRoomReceiver(MyExpandAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_ADD_FRIENDTOCHAT.equals(intent.getAction())) {
            FriendBody newData = (FriendBody) intent.getSerializableExtra("newFriend");
            adapter.refreshRoom(newData.getRoom().getGrouppTag(), newData.getRoom());
        }
    }
}