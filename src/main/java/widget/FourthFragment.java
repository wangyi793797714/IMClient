package widget;

import java.util.ArrayList;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import vo.Myself;
import adapter.FriendAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import application.IMApplication;
import aysntask.AddFriendRequestTask;
import aysntask.FetchFriendTask;
import broadcast.ReponseAddFriendReceiver;
import broadcast.ReqestAddFriendReceiver;

import com.activity.R;

import config.Const;

public class FourthFragment extends BaseFragment {

    private Button btn;

    private ListView friendList;

    private FriendAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        adapter = new FriendAdapter(new ArrayList<Myself>(), activity);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.ACTION_ADDFRIEND_REQUEST);
        ReqestAddFriendReceiver receiver = new ReqestAddFriendReceiver(getActivity());
        IMApplication.APP.reReceiver(receiver, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Const.ACTION_ADDFRIEND_REPONSE);
        ReponseAddFriendReceiver receiver2 = new ReponseAddFriendReceiver(getActivity(),adapter);
        IMApplication.APP.reReceiver(receiver2, filter2);
        
        FinalDb db = FinalDb.create(getActivity(), FileOperator.getDbPath(getActivity()), true);
        //从服务器拉去好友列表
        new FetchFriendTask(getActivity(), adapter).execute(db.findAll(Myself.class).get(0)
                .getChannelId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = makeView(R.layout.main_frag_four);
        btn = (Button) view.findViewById(R.id.add_friend);
        friendList = (ListView) view.findViewById(R.id.friend_list);
        friendList.setAdapter(adapter);

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
        return view;
    }

}
