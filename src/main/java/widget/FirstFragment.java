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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import application.IMApplication;
import aysntask.AddFriendRequestTask;
import aysntask.FetchOnlineUserTask;
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

    private boolean isFirst=true;
    
    private Button btn;
    
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
        if(flag){
            regist.registBroadcast(onlineList, adapter);
            
            IntentFilter filter = new IntentFilter();
            filter.addAction(Const.ACTION_ADDFRIEND_REQUEST);
            ReqestAddFriendReceiver receiver = new ReqestAddFriendReceiver(getActivity());
            IMApplication.APP.reReceiver(receiver, filter);

            IntentFilter filter2 = new IntentFilter();
            filter2.addAction(Const.ACTION_ADDFRIEND_REPONSE);
            ReponseAddFriendReceiver receiver2 = new ReponseAddFriendReceiver(getActivity(),adapter);
            IMApplication.APP.reReceiver(receiver2, filter2);
            
            isFirst=false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = makeView(R.layout.main_frag_one);
        onlineList = (ListView) view.findViewById(R.id.online);
        btn=(Button) view.findViewById(R.id.add_friend);
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
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Builder builder = new AlertDialog.Builder(getActivity());
                final EditText view = new EditText(getActivity());
                builder.setTitle("添加好友").setView(view)
                        .setPositiveButton("输入好友号码", new Dialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FinalDb db = FinalDb.create(getActivity(),
                                        FileOperator.getDbPath(getActivity()), true);
                                new AddFriendRequestTask(null, Integer.parseInt(view.getText()
                                        .toString())).execute(db.findAll(Myself.class).get(0));
                            }
                        }).setIcon(android.R.drawable.ic_dialog_info).show();
            }
        });
        initBroadcast(isFirst);
        return view;
    }

    public interface Regist {
        public void registBroadcast(ListView onlineList, OnlineAdapter adapter);
    }
}
