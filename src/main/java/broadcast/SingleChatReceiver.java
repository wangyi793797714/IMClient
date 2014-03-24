package broadcast;

import java.util.ArrayList;
import java.util.List;

import util.Util;
import vo.Content;
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

public class SingleChatReceiver extends BroadcastReceiver {

    private OnlineAdapter adapter;

    private ListView onlineList;

    private Activity act;

    public SingleChatReceiver(OnlineAdapter adapter, ListView onlineList, Activity act) {
        this.adapter = adapter;
        this.onlineList = onlineList;
        this.act = act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_SINGLE_MSG.equals(intent.getAction())) {
            Content content = (Content) intent.getSerializableExtra("msg");
            // getDb().save(content);
            List<Content> msgs = HomeActivity.singleMsgs.get(content.getSendId());
            if (Util.isEmpty(msgs)) {
                msgs = new ArrayList<Content>();
            }
            msgs.add(content);
            HomeActivity.singleMsgs.put(content.getSendId(), msgs);
            List<Myself> friendsList = adapter.getDataSource();
            for (int i = 0; i < friendsList.size(); i++) {
                if (content.getSendId() == friendsList.get(i).getChannelId()) {
                    if (HomeActivity.singleMsgs.get(content.getSendId()).size() > 0) {
                        int position = adapter.getPosition(friendsList.get(i));
                        RelativeLayout rl = (RelativeLayout) onlineList.getChildAt(position);
                        BadgeView tips = new BadgeView(act, rl.getChildAt(1));
                        tips.setText(HomeActivity.singleMsgs.get(content.getSendId()).size() + "");
                        tips.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                        tips.toggle(null, null);
                    }
                }
            }
        }
    }
}