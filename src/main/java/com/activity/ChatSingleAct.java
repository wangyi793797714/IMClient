package com.activity;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.tsz.afinal.FinalDb;
import net.tsz.afinal.annotation.view.ViewInject;
import util.FileOperator;
import util.MsgComparator;
import util.Util;
import vo.Content;
import vo.Myself;
import adapter.ChatAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import application.IMApplication;
import aysntask.FetchOnlineUserTask;
import aysntask.LoginTask;
import config.Const;

public class ChatSingleAct extends BaseActivity {

    @ViewInject(id = R.id.lv_chat_detail)
    private ListView chatList;

    @ViewInject(id = R.id.send)
    private Button sendBtn;

    @ViewInject(id = R.id.content)
    private EditText input;

    private ChatAdapter adapter;

    public static int sendId = -1;

    private FinalDb db;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_detail);
        IMApplication.APP.addActivity(this);
        db = FinalDb.create(getActivity(), FileOperator.getDbPath(getActivity()), true);

        // 由通知栏传入
        final Content msg = (Content) getIntent().getExtras().getSerializable("3");
        // 由主界面传入
        final Myself vo = (Myself) getVo("0");
        sendId = vo.getChannelId();

        //包含网络未读消息和本地未读消息（本地未读消息数据库中已经存在）
        final List<Content> msgs = (List<Content>) getVo("1");

        adapter = new ChatAdapter(new ArrayList<Content>(), activity);
     // 获取最近10条已读消息
        String key1 = sendId +""+ db.findAll(Myself.class).get(0).getChannelId();
        String key2 = db.findAll(Myself.class).get(0).getChannelId()+""+ sendId;
        List<Content> lastMsgs = db.findAllByWhere(Content.class, " isRead = 'true' and belongTo = '" + key1
                + "' or belongTo = '" + key2+"' ", "date DESC LIMIT 10 ");
        List<Content> tempDatas= new ArrayList<Content>(); 
        if (!Util.isEmpty(lastMsgs)) {
            tempDatas.addAll(lastMsgs);
        }
        if (!Util.isEmpty(msgs)) {
            sendId = msgs.get(0).getSendId();
            for (Content content : msgs) {
            	if("false".equals(content.getIsLocalMsg())){
            		//网络离线消息设置为本地并且为已读
            		content.setIsRead("true");
            		content.setIsLocalMsg("true");
            		db.save(content);
            		tempDatas.add(content);
            	}else{
            		//本地已经存在的未读消息，修改为已读
            		content.setIsRead("true");
            		content.setIsLocalMsg("true");
            		db.update(content);
            	}
            }
        }
        
        Collections.sort(tempDatas, new MsgComparator());
        adapter.addItems(tempDatas);
        if (msg != null) {
            sendId = msg.getSendId();
            adapter.addItem(msg, 0);
            msg.setIsRead("true");
            msg.setIsLocalMsg("true");
            db.save(msg);
            HomeActivity.singleMsgs.remove(vo.getChannelId());
            sendBroadcast(ChatSingleAct.this, msg);
        }
        chatList.setAdapter(adapter);
        registerBoradcastReceiver(new msgBroadcastReceiver());
        sendBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final Content content = new Content();
                content.setBelongTo(vo.getChannelId()+""+
                        + db.findAll(Myself.class).get(0).getChannelId());
                content.setDate(new Date());
                content.setMsg(input.getText().toString());
                input.setText("");
                // 指定发送消息的人为当前登录的人
                content.setSendName(LoginTask.currentName);
                content.setSendMsg(true);
                content.setSendId(db.findAll(Myself.class).get(0).getChannelId());
                if (msg != null) {
                    content.setReceiveName(msg.getSendName());
                    content.setReceiveId(msg.getSendId());
                } else {
                    if (!Util.isEmpty(msgs)) {
                        content.setReceiveId(msgs.get(0).getSendId());
                        content.setReceiveName(msgs.get(0).getSendName());
                    } else {
                        content.setReceiveName(vo.getName());
                        content.setReceiveId(vo.getChannelId());
                    }
                }
                sendId = content.getReceiveId();
                FetchOnlineUserTask.channel.writeAndFlush(content).addListener(
                        new GenericFutureListener<Future<? super Void>>() {
                            @Override
                            public void operationComplete(Future<? super Void> future)
                                    throws Exception {
                                if (future.isSuccess()) {
                                    ChatSingleAct.this.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            adapter.addItem(content, adapter.getCount());
                                            chatList.setSelection(adapter.getCount() - 1);
                                            content.setIsRead("true");
                                            content.setIsLocalMsg("true");
                                            db.save(content);
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    /**
     * 
     * @desc
     * @author WY 创建时间 2014年4月3日 下午2:07:59
     */
    class msgBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Const.ACTION_SINGLE_BROADCAST.equals(intent.getAction())) {
                Content content = (Content) intent.getSerializableExtra("msg");
                adapter.addItem(content, adapter.getCount());
                chatList.setSelection(adapter.getCount() - 1);
            }
        }
    }

    public void registerBoradcastReceiver(BroadcastReceiver receiver) {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Const.ACTION_SINGLE_BROADCAST);
        IMApplication.APP.registerReceiver(receiver, myIntentFilter);
    }

    /**
     * 
     * @desc:发送广播到聊天列表界面，删除掉列表上显示的条数
     * @author WY 创建时间 2014年3月14日 下午2:07:37
     * @param act
     * @param content
     */
    public void sendBroadcast(Context act, Content content) {
        Intent intent = new Intent();
        intent.setAction(Const.ACTION_DELETE_TIPS);
        intent.putExtra("content", content);
        act.sendBroadcast(intent);
    }
}
