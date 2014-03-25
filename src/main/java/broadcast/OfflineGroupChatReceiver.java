package broadcast;

import widget.BadgeView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.activity.HomeActivity;

import config.Const;

public class OfflineGroupChatReceiver extends BroadcastReceiver {

    private ExpandableListView exListView;

    private Activity act;

    public OfflineGroupChatReceiver(Activity act, ExpandableListView exListView) {
        this.exListView = exListView;
        this.act = act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_GROUP_OFFLINE_MSG.equals(intent.getAction())) {
            for (Long key : HomeActivity.groupMsgs.keySet()) {
                if((HomeActivity.groupMsgs.get(key).size()>0)){
                    RelativeLayout rl = (RelativeLayout) exListView.findViewWithTag(key);
                    rl.getChildAt(3).setVisibility(View.VISIBLE);
                    BadgeView tips = new BadgeView(act, rl.getChildAt(3));
                    tips.setText(HomeActivity.groupMsgs.get(key).size() + "");
                    tips.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                    tips.toggle(null, null);
                }
            }
        }
    }
}
