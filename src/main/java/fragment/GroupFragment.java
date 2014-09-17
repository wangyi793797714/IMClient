package fragment;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import util.Util;
import vo.ChatRoom;
import vo.RoomChild;
import widget.BadgeView;
import adapter.MyExpandAdapter;
import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.RelativeLayout;
import application.IMApplication;
import broadcast.GroupChatReceiver;
import broadcast.OfflineGroupChatReceiver;
import broadcast.RefreshChatRoomReceiver;
import broadcast.ReqestCreateChatRoomReceiver;

import com.activity.HomeActivity;
import com.activity.R;

import config.Const;
/**
 * 
 * @Des: 
 * @author Rhino 
 * @version V1.0 
 * @created  2014年9月17日 下午2:15:14
 */
public class GroupFragment extends BaseFragment {

    private ExpandableListView exListView;

    private MyExpandAdapter adapter;

    /** 控制列表的展开，用于每次都定位于展开的项 */
    private int sign = -1;

    private CreateRoom create;

    private boolean isFirst = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        create = (CreateRoom) activity;
        FinalDb db = FinalDb.create(getActivity(), FileOperator.getDbPath(getActivity()), true);
        adapter = new MyExpandAdapter(getActivity(), new ArrayList<ChatRoom>());
        create.create(adapter);
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

        IntentFilter Filter2 = new IntentFilter();
        Filter2.addAction(Const.ACTION_ADD_FRIENDTOCHAT);
        RefreshChatRoomReceiver receiver2 = new RefreshChatRoomReceiver(adapter);
        IMApplication.APP.reReceiver(receiver2, Filter2);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = makeView(R.layout.main_frag_three);
        exListView = (ExpandableListView) view.findViewById(R.id.chat_room_list);
        IntentFilter Filter2 = new IntentFilter();
        Filter2.addAction(Const.ACTION_GROUP_MAIN);
        GroupChatReceiver receiver = new GroupChatReceiver(getActivity(), exListView);
        IMApplication.APP.reReceiver(receiver, Filter2);
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
        initBroadcast(isFirst);
        return view;
    }

    public void initBroadcast(boolean flag) {
        if (flag) {
            IntentFilter Filter3 = new IntentFilter();
            Filter3.addAction(Const.ACTION_GROUP_OFFLINE_MSG);
            OfflineGroupChatReceiver receiver3 = new OfflineGroupChatReceiver(getActivity(),
                    exListView);
            IMApplication.APP.reReceiver(receiver3, Filter3);
            isFirst = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        for (Long key : HomeActivity.groupMsgs.keySet()) {
            RelativeLayout rl = (RelativeLayout) exListView.findViewWithTag(key);
            if (rl == null) {
                return;
            }
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

    public interface CreateRoom {
        public void create(MyExpandAdapter adapter);
    }
}
