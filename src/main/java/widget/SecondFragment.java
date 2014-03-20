//package widget;
//
//import io.netty.channel.ChannelFuture;
//import io.netty.util.concurrent.Future;
//import io.netty.util.concurrent.GenericFutureListener;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import net.tsz.afinal.FinalDb;
//import util.FileOperator;
//import util.Util;
//import vo.Content;
//import vo.RoomChild;
//import vo.OnlineFriends;
//import adapter.ChatAdapter;
//import adapter.GroupChatAdapter;
//import adapter.OnlineAdapter;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ListView;
//import aysntask.FetchOnlineUserTask;
//import aysntask.LoginTask;
//
//import com.activity.R;
//
//public class SecondFragment extends BaseFragment {
//
//    private Button addFriend;
//
//    private ListView friendList;
//
//    private OnlineAdapter adapter;
//
//    private ListView chatList;
//
//    private Button sendBtn;
//
//    private EditText input;
//
//    ChannelFuture lastWriteFuture = null;
//
//    ChatAdapter chatAdapter;
//
//    GroupRegist regist;
//    
//    private boolean isFirst=true;
//    
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        regist = (GroupRegist) activity;
//    }
//    
//    public void initBroadcast(boolean flag) {
//        if(flag){
//            regist.regist(chatList, chatAdapter);
//            isFirst=false;
//        }
//    }
//    
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        adapter = new OnlineAdapter(new ArrayList<OnlineFriends>(), getActivity());
//        chatAdapter = new ChatAdapter(new ArrayList<Content>(), getActivity());
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.main_frag_two, container, false);
//        addFriend = (Button) view.findViewById(R.id.add_friend);
//        friendList = (ListView) view.findViewById(R.id.friend_list);
//        chatList=(ListView) view.findViewById(R.id.lv_chat_detail);
//        sendBtn = (Button) view.findViewById(R.id.send);
//        input = (EditText) view.findViewById(R.id.content);
//        
//        chatList.setAdapter(chatAdapter);
//        friendList.setAdapter(adapter);
//        addFriend.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("请选择好友");
//                builder.setIcon(android.R.drawable.ic_dialog_info);
//                View view = makeView(R.layout.group_chat_list);
//                builder.setView(view);
//                ListView list = (ListView) view.findViewById(R.id.group_chat_list);
//                FinalDb  db=FinalDb.create(getActivity(),FileOperator.getDbPath(getActivity()),true);
//                List<OnlineFriends> tempFriends = db.findAll(OnlineFriends.class);
//                List<RoomChild> src = new ArrayList<RoomChild>();
//                for (OnlineFriends u : tempFriends) {
//                    RoomChild child = new RoomChild();
//                    child.setChannelId(u.getChannelId());
//                    child.setName(u.getName());
//                }
//                final GroupChatAdapter gcAdapter = new GroupChatAdapter(src, getActivity());
//                list.setAdapter(gcAdapter);
//                builder.setPositiveButton("确定", new Dialog.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        List<RoomChild> tempList = new ArrayList<RoomChild>();
//                        List<OnlineFriends> tempSource = adapter.getDataSource();
//                        for (int i = 0; i < gcAdapter.isChecked.size(); i++) {
//                            if (gcAdapter.isChecked.get(i)) {
//                                RoomChild checkedUser = gcAdapter.getItem(i);
//                                boolean flag = true;
//                                if (!Util.isEmpty(tempSource)) {
//                                    for (OnlineFriends u : tempSource) {
//                                        if (u.getChannelId() == checkedUser.getChannelId()
//                                                && u.getName().equals(checkedUser.getName())) {
//                                            flag = false;
//                                        }
//                                    }
//                                }
//                                if (flag) {
//                                    tempList.add(checkedUser);
//                                }
//                            }
//                        }
//                        if (!Util.isEmpty(tempList)) {
////                            adapter.addItems(tempList);
//                        }
//                    }
//                });
//                builder.create();
//                builder.show();
//            }
//        });
//        
//        sendBtn.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                final Content content = new Content();
//                content.setDate(new Date());
//                content.setMsg(input.getText().toString());
//                input.setText("");
//                //指定发送者为当前登录的人
//                content.setSendName(LoginTask.currentName);
//                content.setSendMsg(true);
//                content.setReceiveId(0);
//                List<Integer> ids = new ArrayList<Integer>();
//                for (OnlineFriends user:adapter.getDataSource()) {
//                    if(user.getChannelId()!=FetchOnlineUserTask.getChannel().hashCode()){
//                        ids.add(user.getChannelId());
//                    }
//                }
//                content.setTargetIds(ids);
//                lastWriteFuture = FetchOnlineUserTask.getChannel().writeAndFlush(content);
//                lastWriteFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
//                    @Override
//                    public void operationComplete(Future<? super Void> future) throws Exception {
//                        if (future.isSuccess()) {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    chatAdapter.addItem(content, chatAdapter.getCount());
//                                    chatList.setSelection(chatAdapter.getCount() - 1);
//                                }
//                            });
//                        }
//                    }
//                });
//            }
//        });
//        initBroadcast(isFirst);
//        return view;
//    }
//    
//    public interface GroupRegist{
//        public void regist(ListView chatList, ChatAdapter adapter);
//    }
//}
