package widget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import util.Util;
import vo.Content;
import vo.Myself;
import adapter.OnlineAdapter;
import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import application.IMApplication;
import aysntask.FetchOnlineUserTask;
import broadcast.OfflineMsgReceiver;
import broadcast.ReponseAddFriendReceiver;
import broadcast.ReqestAddFriendReceiver;

import com.activity.ChatSingleAct;
import com.activity.HomeActivity;
import com.activity.R;

import config.Const;

public class FirstFragment extends BaseFragment {

    private ListView onlineList;

    private OnlineAdapter adapter;

    private Regist regist;

    private boolean isFirst = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        regist = (Regist) activity;
        adapter = new OnlineAdapter(new ArrayList<Myself>(), getActivity());
        activity = getActivity();
        FinalDb db = FinalDb.create(getActivity(), FileOperator.getDbPath(getActivity()), true);
        new FetchOnlineUserTask(activity, adapter).execute(db.findAll(Myself.class).get(0));
    }

    public void initBroadcast(boolean flag) {
        if (flag) {
            regist.registBroadcast(onlineList, adapter);
            IntentFilter filter = new IntentFilter(Const.ACTION_ADDFRIEND_REQUEST);
            ReqestAddFriendReceiver receiver = new ReqestAddFriendReceiver(getActivity());
            IMApplication.APP.reReceiver(receiver, filter);

            IntentFilter filter2 = new IntentFilter(Const.ACTION_ADDFRIEND_REPONSE);
            ReponseAddFriendReceiver receiver2 = new ReponseAddFriendReceiver(getActivity(),
                    adapter);
            IMApplication.APP.reReceiver(receiver2, filter2);

            IntentFilter filter3 = new IntentFilter(Const.ACTION_OFFLINE_MSG);
            IMApplication.APP.registerReceiver(new OfflineMsgReceiver(adapter, onlineList,
                    getActivity()), filter3);

            isFirst = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = makeView(R.layout.main_frag_one);
        onlineList = (ListView) view.findViewById(R.id.online);
        onlineList.setAdapter(adapter);

        onlineList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int postion, long arg3) {
                Myself user = adapter.getItem(postion);
                List<Content> msgs = HomeActivity.singleMsgs.get(user.getChannelId());
                if (!Util.isEmpty(msgs)) {
                    HomeActivity.singleMsgs.remove(user.getChannelId());
                    RelativeLayout rl = (RelativeLayout) view;
                    View tipsView = rl.getChildAt(1);
                    tipsView.setVisibility(View.GONE);
                }
                skip(ChatSingleAct.class, user, (Serializable) msgs);
            }
        });
        initBroadcast(isFirst);
        return view;
    }

    public interface Regist {
        public void registBroadcast(ListView onlineList, OnlineAdapter adapter);
    }
}
