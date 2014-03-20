package broadcast;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import vo.ChatRoom;
import vo.RoomChild;
import adapter.MyExpandAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import config.Const;

public class ReqestCreateChatRoomReceiver extends BroadcastReceiver {

    private MyExpandAdapter adapter;

    private Activity act;

    public ReqestCreateChatRoomReceiver(MyExpandAdapter adapter, Activity act) {
        this.adapter = adapter;
        this.act = act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_CREATE_CHAT_ROOM.equals(intent.getAction())) {
            final ChatRoom room = (ChatRoom) intent.getSerializableExtra("room");
            final FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
            adapter.addRoom(room);
            db.save(room);
            for (RoomChild child : room.getChildDatas()) {
                db.save(child);
            }
            Toast.makeText(act, "收到聊天邀请", Toast.LENGTH_SHORT).show();
        }
    }
}
