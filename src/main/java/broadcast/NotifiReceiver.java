package broadcast;

import java.util.List;

import util.Util;
import vo.Content;
import vo.Myself;
import adapter.OnlineAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.activity.HomeActivity;

import config.Const;

public class NotifiReceiver extends BroadcastReceiver {

    private OnlineAdapter adapter;

    private ListView onlineList;

    public NotifiReceiver(OnlineAdapter adapter, ListView onlineList) {
        this.adapter = adapter;
        this.onlineList = onlineList;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_DELETE_TIPS.equals(intent.getAction())) {
            Content content = (Content) intent.getSerializableExtra("content");
            List<Content> msgs = HomeActivity.singleMsgs.get(content.getSendId());
            List<Myself> onlineUsers = adapter.getDataSource();
            if (!Util.isEmpty(msgs)) {
                HomeActivity.singleMsgs.remove(content.getSendId());
                for (int i = 0; i < onlineUsers.size(); i++) {
                    if (onlineUsers.get(i).getChannelId() == content.getSendId()) {
                        int position = adapter.getPosition(onlineUsers.get(i));
                        RelativeLayout rl = (RelativeLayout) onlineList.getChildAt(position);
                        View tipsView = rl.getChildAt(1);
                        tipsView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

}