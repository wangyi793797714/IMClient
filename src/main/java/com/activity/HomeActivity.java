package com.activity;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import util.Util;
import vo.ChatRoom;
import vo.Content;
import vo.Myself;
import vo.Friends;
import vo.RoomChild;
import widget.FirstFragment;
import widget.FirstFragment.Regist;
import widget.FourthFragment;
import widget.TestFragment;
import widget.ThirdFragment;
import widget.ThirdFragment.createRoom;
import adapter.FragAdapter;
import adapter.GroupChatAdapter;
import adapter.MyExpandAdapter;
import adapter.OnlineAdapter;
import android.R.color;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import anim.StretchAnimation;
import application.IMApplication;
import aysntask.AddFriendRequestTask;
import aysntask.FetchOnlineUserTask;
import broadcast.NotifiReceiver;
import broadcast.SingleChatReceiver;
import broadcast.UserOnlineReceiver;
import config.Const;

public class HomeActivity extends FragmentActivity implements OnClickListener,
        StretchAnimation.AnimationListener, Regist, createRoom {

    private FragAdapter adapter;

    private ViewPager pager;

    private Button page1, page2, page3, page4;

    // 屏幕宽度
    private int screentWidth = 0;

    @SuppressWarnings("unused")
    private int screentHeight = 0;

    // View可伸展最长的宽度
    private int maxSize;

    // View可伸展最小宽度
    private int minSize;

    // 当前点击的View
    private View currentView;

    // 显示最长的那个View
    private View preView;

    // 主布局ViewGroup
    private LinearLayout mainContain;

    private StretchAnimation stretchanimation;

    private static View tempView = null;

    private UserOnlineReceiver uReceiver = null;
    private NotifiReceiver nReceiver = null;
    private SingleChatReceiver sReceiver = null;

    public static Map<Integer, List<Content>> singleMsgs = new ConcurrentHashMap<Integer, List<Content>>();

    // key:属于groupPosition
    public static Map<Long, List<Content>> groupMsgs = new ConcurrentHashMap<Long, List<Content>>();

    private FinalDb db = null;

    public static int currentIndex = 0;

    MyExpandAdapter myAdapter;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.layout_home);
        getActionBar().setCustomView(R.layout.main_action_button);
        getActionBar().setDisplayShowCustomEnabled(true);
        db = FinalDb.create(this, FileOperator.getDbPath(this), true);
        pager = (ViewPager) findViewById(R.id.main_pager);
        page1 = (Button) findViewById(R.id.button1);
        page2 = (Button) findViewById(R.id.button2);
        page3 = (Button) findViewById(R.id.button3);
        page4 = (Button) findViewById(R.id.button4);

        mainContain = (LinearLayout) this.findViewById(R.id.main_contain);
        initCommonData();
        initViewData(0);
        initViewPage();
        initListener();
    }

    private void initCommonData() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screentWidth = metric.widthPixels; // 屏幕宽度（像素）
        screentHeight = metric.heightPixels;
        //
        measureSize(screentWidth);
        stretchanimation = new StretchAnimation(maxSize, minSize, StretchAnimation.TYPE.horizontal,
                400);
        // 你可以换不能给的插值器
        stretchanimation.setInterpolator(new BounceInterpolator());
        // 动画时间
        stretchanimation.setDuration(400);
        // 回调
        stretchanimation.setOnAnimationListener(this);
    }

    private void initViewData(int index) {

        View child;
        int sizeValue = 0;
        LayoutParams params = null;
        int childCount = mainContain.getChildCount();
        if (index < 0 || index >= childCount) {
            throw new RuntimeException("index 超出范围");
        }

        for (int i = 0; i < childCount; i++) {

            child = mainContain.getChildAt(i);
            child.setOnClickListener(this);
            params = child.getLayoutParams();

            if (i == index) {
                preView = child;
                sizeValue = maxSize;
            } else {
                sizeValue = minSize;
            }
            if (stretchanimation.getmType() == StretchAnimation.TYPE.horizontal) {
                params.width = sizeValue;
            } else if (stretchanimation.getmType() == StretchAnimation.TYPE.vertical) {
                params.height = sizeValue;
            }

            child.setLayoutParams(params);
            currentView = child;
        }
    }

    private void measureSize(int layoutSize) {
        int halfWidth = layoutSize / 2;
        maxSize = halfWidth - 50;
        minSize = (layoutSize - maxSize) / (mainContain.getChildCount() - 1);

    }

    private void initViewPage() {
        List<Fragment> fragments = new ArrayList<Fragment>();
        FirstFragment one = new FirstFragment();
        FourthFragment two = new FourthFragment();
        ThirdFragment three = new ThirdFragment();
        TestFragment four = new TestFragment();

        fragments.add(one);
        fragments.add(three);
        fragments.add(two);
        fragments.add(four);
        adapter = new FragAdapter(getSupportFragmentManager(), fragments);
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0);
        setListener(0);
        pager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                tempView = mainContain.getChildAt(position);
                setListener(position);
                changeButton(tempView);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    private void initListener() {
        page1.setOnClickListener(this);
        page2.setOnClickListener(this);
        page3.setOnClickListener(this);
        page4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        case R.id.button1:
            pager.setCurrentItem(0);
            tempView = mainContain.getChildAt(0);
            break;
        case R.id.button2:
            pager.setCurrentItem(1);
            tempView = mainContain.getChildAt(1);
            break;
        case R.id.button3:
            pager.setCurrentItem(2);
            tempView = mainContain.getChildAt(2);
            break;
        case R.id.button4:
            pager.setCurrentItem(3);
            tempView = mainContain.getChildAt(3);
            break;
        }

        changeButton(tempView);

    }

    private void changeButton(View tempView) {
        if (tempView == preView) {
            return;
        } else {
            currentView = tempView;
        }
        clickEvent(currentView);
        onOffClickable(false);
        stretchanimation.startAnimation(currentView);
        setListener(currentIndex);
    }

    @Override
    public void animationEnd(View v) {
        onOffClickable(true);
    }

    private void onOffClickable(boolean isClickable) {
        View child;
        int childCount = mainContain.getChildCount();
        for (int i = 0; i < childCount; i++) {
            child = mainContain.getChildAt(i);
            child.setClickable(isClickable);
        }
    }

    private void clickEvent(View view) {
        View child;
        int childCount = mainContain.getChildCount();
        LinearLayout.LayoutParams params;
        for (int i = 0; i < childCount; i++) {
            child = mainContain.getChildAt(i);
            if (preView == child) {
                params = (android.widget.LinearLayout.LayoutParams) child.getLayoutParams();

                if (preView != view) {
                    params.weight = 1.0f;
                }
                child.setLayoutParams(params);

            } else {
                params = (android.widget.LinearLayout.LayoutParams) child.getLayoutParams();
                params.weight = 0.0f;
                if (stretchanimation.getmType() == StretchAnimation.TYPE.horizontal) {
                    params.width = minSize;
                } else if (stretchanimation.getmType() == StretchAnimation.TYPE.vertical) {
                    params.height = minSize;
                }

                child.setLayoutParams(params);
            }
        }
        preView = view;

    }

    public View makeView(int resId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        return inflater.inflate(resId, null);
    }

    @Override
    public void registBroadcast(ListView onlineList, OnlineAdapter adapter) {
        uReceiver = new UserOnlineReceiver(adapter, this);
        nReceiver = new NotifiReceiver(adapter, onlineList);
        sReceiver = new SingleChatReceiver(adapter, onlineList, this);
        registerBoradcastReceiver(uReceiver);
        registerBoradcastMsg(sReceiver);
        registerNotifiReceiver(nReceiver);
    }

    public void registerBoradcastReceiver(BroadcastReceiver receiver) {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.ACTION_ON_OR_OFF_LINE);
        IMApplication.APP.reReceiver(receiver, myIntentFilter);
    }

    public void registerBoradcastMsg(BroadcastReceiver receiver) {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.ACTION_SINGLE_MSG);
        IMApplication.APP.reReceiver(receiver, myIntentFilter);
    }

    public void registerNotifiReceiver(BroadcastReceiver receiver) {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.ACTION_DELETE_TIPS);
        IMApplication.APP.reReceiver(receiver, myIntentFilter);
    }

    public void setListener(final int position) {
        ImageView view =(ImageView) getActionBar().getCustomView();
        Resources res = this.getResources();
        if (position == 0) {
            Drawable myImage = res.getDrawable(R.drawable.add_friend_drawable);
            view.setBackgroundDrawable(myImage);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    final EditText view = new EditText(HomeActivity.this);
                    builder.setTitle("添加好友").setView(view)
                            .setPositiveButton("输入好友号码", new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FinalDb db = FinalDb.create(HomeActivity.this,
                                            FileOperator.getDbPath(HomeActivity.this), true);
                                    new AddFriendRequestTask(null, Integer.parseInt(view.getText()
                                            .toString())).execute(db.findAll(Myself.class).get(0));
                                }
                            }).setIcon(android.R.drawable.ic_dialog_info).show();
                }
            });
        } else if (position == 1) {
            Drawable myImage = res.getDrawable(R.drawable.add_chatroom_drawable);
            view.setBackgroundDrawable(myImage);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle("创建聊天室");
                    View view = makeView(R.layout.dialog_create_room);
                    final EditText input = (EditText) view.findViewById(R.id.chat_theme);
                    ListView list = (ListView) view.findViewById(R.id.online_user);
                    FinalDb db = FinalDb.create(HomeActivity.this,
                            FileOperator.getDbPath(HomeActivity.this), true);
                    // TODO获取在线的好友列表
                    List<Friends> onlines = db.findAllByWhere(Friends.class, "isOnline = 1");
                    List<Myself> tempFriends = new ArrayList<Myself>();
                    if (!Util.isEmpty(onlines)) {
                        for (Friends on : onlines) {
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
                    final GroupChatAdapter gcAdapter = new GroupChatAdapter(src, HomeActivity.this);
                    list.setAdapter(gcAdapter);
                    builder.setView(view);
                    builder.setPositiveButton("确定", new Dialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final FinalDb db = FinalDb.create(HomeActivity.this,
                                    FileOperator.getDbPath(HomeActivity.this), true);
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
                                                HomeActivity.this.runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        db.save(room);
                                                        for (RoomChild child : childDatas) {
                                                            db.save(child);
                                                        }
                                                        myAdapter.addRoom(room);
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
        } else if (position == 2) {
            view.setBackgroundColor(color.transparent);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, position + "", 0).show();
                }
            });
        } else {
            view.setBackgroundColor(color.transparent);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, 3 + "", 0).show();
                }
            });
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this).setMessage("确定要退出吗?")
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setPositiveButton("是", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 删除所有的在线者
                            IMApplication.APP.closeReceiver();
                            // TODO
                            db.deleteAll(Friends.class);
                            if (FetchOnlineUserTask.channel != null) {
                                Friends myInfo = new Friends();
                                myInfo.setChannelId(db.findAll(Myself.class).get(0).getChannelId());
                                myInfo.setName(null);
                                FetchOnlineUserTask.channel.writeAndFlush(myInfo).addListener(
                                        new GenericFutureListener<Future<? super Void>>() {

                                            @Override
                                            public void operationComplete(Future<? super Void> arg0)
                                                    throws Exception {
                                                FetchOnlineUserTask.channel.close().sync();
                                            }
                                        });
                            }
                            finish();
                        }
                    }).show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void create(final MyExpandAdapter adapter) {
        myAdapter = adapter;
    }
}
