package broadcast;

import java.util.List;

import vo.Myself;
import widget.BadgeView;
import adapter.OnlineAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.activity.HomeActivity;

import config.Const;

public class OfflineSingleChatMsgReceiver extends BroadcastReceiver {

    private OnlineAdapter adapter;

    private ListView onlineList;

    private Activity act;

    public OfflineSingleChatMsgReceiver(OnlineAdapter adapter, ListView onlineList, Activity act) {
        this.adapter = adapter;
        this.onlineList = onlineList;
        this.act = act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_OFFLINE_MSG.equals(intent.getAction())) {
            List<Myself> friendsList = adapter.getDataSource();
            //此时，消息容器中的消息记录全部是服务器返回的离线消息
            for (Integer key : HomeActivity.singleMsgs.keySet()) {
                for (int i = 0; i < friendsList.size(); i++) {
                    if (key == friendsList.get(i).getChannelId()) {
                        if (HomeActivity.singleMsgs.get(key).size() > 0) {
                            int position = adapter.getPosition(friendsList.get(i));
                            RelativeLayout rl = (RelativeLayout) onlineList.getChildAt(position);
                            BadgeView tips = new BadgeView(act, rl.getChildAt(1));
                            tips.setText(HomeActivity.singleMsgs.get(key).size() + "");
                            tips.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                            tips.toggle(null, null);
                        }
                    }
                }
            }

        }
    }
}