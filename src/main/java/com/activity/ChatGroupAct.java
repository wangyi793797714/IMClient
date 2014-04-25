package com.activity;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import net.tsz.afinal.FinalDb;
import net.tsz.afinal.annotation.view.ViewInject;
import util.FileOperator;
import util.MsgComparator;
import util.Util;
import vo.ChatRoom;
import vo.Content;
import vo.FriendBody;
import vo.Friends;
import vo.Myself;
import vo.RoomChild;
import adapter.ChatAdapter;
import adapter.GroupChatAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import application.IMApplication;
import aysntask.AddFriendToRoomTask;
import aysntask.FetchOnlineUserTask;
import aysntask.LoginTask;
import aysntask.ShowLast10MsgsTask;
import config.Const;

public class ChatGroupAct extends BaseActivity {

    @ViewInject(id = R.id.lv_chat_detail)
    private PullToRefreshListView chatList;

    @ViewInject(id = R.id.send)
    private Button sendBtn;

    @ViewInject(id = R.id.content)
    private EditText input;

    private ChatAdapter chatAdapter;

    private List<RoomChild> friends;

    public static Long CurrentGroup = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatAdapter = new ChatAdapter(new ArrayList<Content>(), activity);
        final ChatRoom room = ((ChatRoom) getVo("0"));
        friends = room.getChildDatas();
        CurrentGroup = friends.get(0).getGroupTag();
        final String sql = " grouppTag= " + CurrentGroup + " and isRead = 'true'";
        List<Content> lastList = db.findAllByWhere(Content.class, sql, "date DESC LIMIT 10");
        if (!Util.isEmpty(lastList)) {
            Collections.sort(lastList, new MsgComparator());
            chatAdapter.addItems(lastList);
        }
        if (!Util.isEmpty(HomeActivity.groupMsgs.get(((ChatRoom) getVo("0")).getGrouppTag()))) {
            List<Content> msgs = HomeActivity.groupMsgs.get(((ChatRoom) getVo("0")).getGrouppTag());
            for (Content msg : msgs) {
                if ("false".equals(msg.getIsLocalMsg())) {
                    // 网络离线消息设置为本地并且为已读
                    msg.setIsLocalMsg("true");
                    msg.setIsRead("true");
                    db.save(msg);
                } else {
                    // 本地已经存在的未读消息，修改为已读
                    msg.setIsLocalMsg("true");
                    msg.setIsRead("true");
                    db.update(msg);
                }
            }
            chatAdapter.addItems(msgs);
            HomeActivity.groupMsgs.get((((ChatRoom) getVo("0")).getGrouppTag())).clear();
        }
        setContentView(R.layout.group_chat);
        IMApplication.APP.addActivity(this);
        initView();
        registerBoradcastReceiver(new msgBroadcastReceiver());
        getActionBar().setCustomView(R.layout.main_action_button);
        getActionBar().setDisplayShowCustomEnabled(true);
        ImageView addFriend = (ImageView) getActionBar().getCustomView();
        addFriend.setBackgroundDrawable(getDrawableRes(R.drawable.add_friend_drawable));
        addFriend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("请选择好友");
                builder.setIcon(android.R.drawable.ic_dialog_info);
                View view = makeView(R.layout.group_chat_list);
                builder.setView(view);
                ListView list = (ListView) view.findViewById(R.id.group_chat_list);
                FinalDb db = FinalDb.create(activity, FileOperator.getDbPath(activity), true);
                // 获取在线的好友列表,同已经在此群组里的好友取差集，便是不在此聊天群组中的在线好友

                List<Friends> onlines = db.findAllByWhere(Friends.class, "isOnline = 1");
                List<Myself> onlineUser = new ArrayList<Myself>();
                if (!Util.isEmpty(onlines)) {
                    for (Friends on : onlines) {
                        Myself me = new Myself();
                        me.setChannelId(on.getChannelId());
                        me.setName(on.getName());
                        onlineUser.add(me);
                    }
                }

                List<RoomChild> existChilds = friends;
                if (!Util.isEmpty(onlineUser)) {
                    for (int i = 0; i < onlineUser.size(); i++) {
                        for (RoomChild user : existChilds) {
                            if (onlineUser.get(i).getChannelId() == user.getChannelId()) {
                                onlineUser.remove(i);
                                i--;
                                break;
                            }
                        }
                    }
                }
                List<RoomChild> src = new ArrayList<RoomChild>();
                if (!Util.isEmpty(onlineUser)) {
                    for (Myself u : onlineUser) {
                        RoomChild child = new RoomChild();
                        child.setChannelId(u.getChannelId());
                        child.setName(u.getName());
                        src.add(child);
                    }
                }
                final GroupChatAdapter gcAdapter = new GroupChatAdapter(src, activity);
                list.setAdapter(gcAdapter);
                builder.setPositiveButton("确定", new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<RoomChild> tempList = new ArrayList<RoomChild>();
                        List<Integer> targetIds = new ArrayList<Integer>();
                        for (int i = 0; i < gcAdapter.isChecked.size(); i++) {
                            if (gcAdapter.isChecked.get(i)) {
                                RoomChild checkedUser = gcAdapter.getItem(i);
                                RoomChild child = new RoomChild();
                                child.setChannelId(checkedUser.getChannelId());
                                child.setName(checkedUser.getName());
                                child.setGroupTag(CurrentGroup);
                                targetIds.add(checkedUser.getChannelId());
                                tempList.add(child);
                            }
                        }
                        if (!Util.isEmpty(tempList)) {
                            friends.addAll(tempList);
                        }
                        FriendBody vo = new FriendBody();
                        vo.setRoom(room);
                        new AddFriendToRoomTask(activity).execute(vo);
                    }
                });
                builder.create();
                builder.show();
            }
        });

        chatList.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getApplicationContext(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_ABBREV_ALL);
                chatList.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                new ShowLast10MsgsTask(activity, chatList, chatAdapter).execute(sql);
            }
        });
    }

    public void initView() {
        chatList.setAdapter(chatAdapter);
        sendBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final Content content = new Content();
                content.setDate(new Date());
                content.setMsg(input.getText().toString());
                input.setText("");
                // 指定发送者为当前登录的人
                content.setSendName(LoginTask.currentName);
                content.setSendMsg(true);
                content.setReceiveId(0);
                content.setGrouppTag(CurrentGroup);
                List<Integer> ids = new ArrayList<Integer>();
                final FinalDb db = FinalDb.create(activity, FileOperator.getDbPath(activity), true);
                for (RoomChild user : friends) {
                    if (user.getChannelId() != db.findAll(Myself.class).get(0).getChannelId()) {
                        ids.add(user.getChannelId());
                    }
                }
                content.setTargetIds(ids);
                FetchOnlineUserTask.channel.writeAndFlush(content).addListener(
                        new GenericFutureListener<Future<? super Void>>() {
                            @Override
                            public void operationComplete(Future<? super Void> future)
                                    throws Exception {
                                if (future.isSuccess()) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatAdapter.addItem(content, chatAdapter.getCount());
                                            chatList.setSelection(chatAdapter.getCount() - 1);
                                            content.setIsLocalMsg("true");
                                            content.setIsRead("true");
                                            db.save(content);
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    class msgBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Const.ACTION_GROUP_CHAT.equals(intent.getAction())) {
                Content content = (Content) intent.getSerializableExtra("msg");
                content.setIsLocalMsg("true");
                content.setIsRead("true");
                chatAdapter.addItem(content, chatAdapter.getCount());
                chatList.setSelection(chatAdapter.getCount() - 1);
                db.save(content);
            }
        }

    }

    public void registerBoradcastReceiver(BroadcastReceiver receiver) {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.ACTION_GROUP_CHAT);
        // 注册广播
        IMApplication.APP.reReceiver(receiver, myIntentFilter);
    }
}
