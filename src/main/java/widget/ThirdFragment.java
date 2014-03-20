package widget;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import util.Util;
import vo.ChatRoom;
import vo.Myself;
import vo.OnlineFriends;
import vo.RoomChild;
import adapter.GroupChatAdapter;
import adapter.MyExpandAdapter;
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
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import application.IMApplication;
import aysntask.FetchOnlineUserTask;
import broadcast.ReqestCreateChatRoomReceiver;
import broadcast.TestGroupChatReceiver;

import com.activity.HomeActivity;
import com.activity.R;

import config.Const;

public class ThirdFragment extends BaseFragment {

    private Button btn;

    private ExpandableListView exListView;

    private MyExpandAdapter adapter;

    /** 控制列表的展开，用于每次都定位于展开的项 */
    private int sign = -1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FinalDb db = FinalDb.create(getActivity(), FileOperator.getDbPath(getActivity()), true);
        adapter = new MyExpandAdapter(getActivity(), new ArrayList<ChatRoom>());
        List<ChatRoom> rooms = db.findAll(ChatRoom.class);
        if (!Util.isEmpty(rooms)) {
            for (ChatRoom room : rooms) {
                List<RoomChild> childs = db.findAllByWhere(RoomChild.class,
                        "GroupTag = " + room.getGrouppTag());
                room.setChildDatas(childs);
                adapter.addRoom(room);
            }
        }
        IntentFilter Filter1 = new IntentFilter();
        Filter1.addAction(Const.ACTION_CREATE_CHAT_ROOM);
        ReqestCreateChatRoomReceiver receiver = new ReqestCreateChatRoomReceiver(adapter,
                getActivity());
        IMApplication.APP.reReceiver(receiver, Filter1);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = makeView(R.layout.main_frag_three);
        btn = (Button) view.findViewById(R.id.create_chat_room);
        exListView = (ExpandableListView) view.findViewById(R.id.chat_room_list);
        IntentFilter Filter2 = new IntentFilter();
        Filter2.addAction(Const.ACTION_GROUP_MAIN);
        TestGroupChatReceiver receiver = new TestGroupChatReceiver(getActivity(), exListView);
        IMApplication.APP.reReceiver(receiver, Filter2);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("创建聊天室");
                View view = makeView(R.layout.dialog_create_room);
                final EditText input = (EditText) view.findViewById(R.id.chat_theme);
                ListView list = (ListView) view.findViewById(R.id.online_user);
                FinalDb db = FinalDb.create(getActivity(), FileOperator.getDbPath(getActivity()),
                        true);
                // TODO获取在线的好友列表
                List<OnlineFriends> onlines = db.findAll(OnlineFriends.class);
                List<Myself> tempFriends = new ArrayList<Myself>();
                if (!Util.isEmpty(onlines)) {
                    for (OnlineFriends on : onlines) {
                        Myself me = new Myself();
                        me.setChannelId(on.getChannelId());
                        me.setName(on.getName());
                        tempFriends.add(me);
                    }
                }
                List<RoomChild> src = new ArrayList<RoomChild>();
                for (Myself u : tempFriends) {
                    RoomChild child = new RoomChild();
                    child.setChannelId(u.getChannelId());
                    child.setName(u.getName());
                    src.add(child);
                }
                final GroupChatAdapter gcAdapter = new GroupChatAdapter(src, getActivity());
                list.setAdapter(gcAdapter);
                builder.setView(view);
                builder.setPositiveButton("确定", new Dialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FinalDb db = FinalDb.create(getActivity(),
                                FileOperator.getDbPath(getActivity()), true);
                        final List<RoomChild> childDatas = new ArrayList<RoomChild>();
                        Long groupTag = System.currentTimeMillis();
                        for (int i = 0; i < gcAdapter.isChecked.size(); i++) {
                            if (gcAdapter.isChecked.get(i)) {
                                RoomChild checkedUser = gcAdapter.getItem(i);
                                checkedUser.setGroupTag(groupTag);
                                childDatas.add(checkedUser);
                            }
                        }

                        String roomName = input.getText().toString();
                        final List<Myself> info = db.findAll(Myself.class);
                        RoomChild rc = new RoomChild();
                        rc.setChannelId(info.get(0).getChannelId());
                        rc.setName(info.get(0).getName());
                        rc.setGroupTag(groupTag);
                        childDatas.add(0, rc);
                        final ChatRoom room = new ChatRoom(groupTag, roomName, childDatas, info
                                .get(0).getChannelId());
                        FetchOnlineUserTask.channel.writeAndFlush(room).addListener(
                                new GenericFutureListener<Future<? super Void>>() {
                                    @Override
                                    public void operationComplete(Future<? super Void> future)
                                            throws Exception {
                                        if (future.isSuccess()) {
                                            getActivity().runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    db.save(room);
                                                    for (RoomChild child : childDatas) {
                                                        db.save(child);
                                                    }
                                                    adapter.addRoom(room);
                                                }
                                            });

                                        }
                                    }
                                });
                    }
                });
                builder.create();
                builder.show();
            }
        });
        exListView.setAdapter(adapter);
        exListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                    int childPosition, long id) {
                RoomChild item = (RoomChild) adapter.getChild(groupPosition, childPosition);
                toast(item);
                return true;
            }
        });
        // 控制list只展开一项
        exListView.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    if (groupPosition != i) {
                        exListView.collapseGroup(i);
                    }
                }
            }
        });
        // 控制list展开的一项为最前面
        exListView.expandGroup(0);
        exListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
                    long id) {
                if (adapter.hasChild(groupPosition)) {
                    if (sign == -1) {
                        exListView.expandGroup(groupPosition);
                        exListView.setSelectedGroup(groupPosition);
                        sign = groupPosition;
                    } else if (sign == groupPosition) {
                        exListView.collapseGroup(groupPosition);
                        sign = -1;
                    } else {
                        exListView.collapseGroup(sign);
                        exListView.expandGroup(groupPosition);
                        exListView.setSelectedGroup(groupPosition);
                        sign = groupPosition;
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Long key : HomeActivity.groupMsgs.keySet()) {
            RelativeLayout rl = (RelativeLayout) exListView.findViewWithTag(key);
            BadgeView tips = new BadgeView(getActivity(), rl.getChildAt(3));
            if (Util.isEmpty(HomeActivity.groupMsgs.get(key))) {
                rl.getChildAt(3).setVisibility(View.INVISIBLE);
            } else {
                tips.setText(HomeActivity.groupMsgs.get(key).size() + "");
                tips.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            }
            tips.toggle(null, null);
        }
    }

}
