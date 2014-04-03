package netty4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import util.NotificationUtil;
import vo.AddFriendRequest;
import vo.AddFriendResponse;
import vo.ChatRoom;
import vo.Content;
import vo.FriendBody;
import vo.Friends;
import vo.Myself;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.activity.ChatGroupAct;
import com.activity.ChatSingleAct;
import com.activity.HomeActivity;

import config.Const;

public class ChatClientHandler extends SimpleChannelInboundHandler<Object> {

    private Activity act;

    public ChatClientHandler(Activity act) {
        this.act = act;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, Object msg) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return super.acceptInboundMessage(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Content) {
            final Content content = (Content) msg;
            content.setSendMsg(false);
            Intent intent = new Intent();
            ActivityManager am = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            // 私聊消息
            if (content.getGrouppTag() == 0) {
                if (cn.getClassName().equals("com.activity.ChatSingleAct")) {
                    if (content.getSendId() == ChatSingleAct.sendId) {
                        intent.setAction(Const.ACTION_SINGLE_BROADCAST);
                        FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
                        db.save(content);
                    } else {
                        // 构造一个user
                        Myself u = new Myself();
                        u.setChannelId(content.getReceiveId());
                        u.setName(content.getReceiveName());
                        intent.setAction(Const.ACTION_SINGLE_MSG);
                        // 指定接收消息者
                        intent.putExtra("user", u);
                    }
                } else {
                    // 构造一个user
                    Myself u = new Myself();
                    u.setChannelId(content.getReceiveId());
                    u.setName(content.getReceiveName());
                    intent.setAction(Const.ACTION_SINGLE_MSG);
                    // 指定接收消息者
                    intent.putExtra("user", u);
                    if (!cn.getClassName().equals("com.activity.HomeActivity")) {
                        NotificationUtil.sendNotify(act, ChatSingleAct.class, u, content);
                    }
                }
                intent.putExtra("msg", content);
            } else {
                // 群聊消息

                // 1.在主界面,但不在群里界面
                // 2.在主界面，同时在群里界面
                // 3.不在主界面
                if (cn.getClassName().equals("com.activity.HomeActivity")) {
                    intent.setAction(Const.ACTION_GROUP_MAIN);
                    if (HomeActivity.groupMsgs.get(content.getGrouppTag()) != null) {
                        HomeActivity.groupMsgs.get(content.getGrouppTag()).add(content);
                    } else {
                        List<Content> data = new ArrayList<Content>();
                        data.add(content);
                        HomeActivity.groupMsgs.put(content.getGrouppTag(), data);
                    }
                } else if (cn.getClassName().equals("com.activity.ChatGroupAct")
                        && ChatGroupAct.CurrentGroup == content.getGrouppTag()) {
                    intent.setAction(Const.ACTION_GROUP_CHAT);
                    intent.putExtra("msg", content);
                } else if (cn.getClassName().equals("com.activity.ChatGroupAct")
                        && ChatGroupAct.CurrentGroup != content.getGrouppTag()) {
                    if (HomeActivity.groupMsgs.get(content.getGrouppTag()) != null) {
                        HomeActivity.groupMsgs.get(content.getGrouppTag()).add(content);
                    } else {
                        List<Content> data = new ArrayList<Content>();
                        data.add(content);
                        HomeActivity.groupMsgs.put(content.getGrouppTag(), data);
                    }
                }
            }
            act.sendBroadcast(intent);
        } else if (msg instanceof Friends) {
            final Friends user = (Friends) msg;
            Intent intent = new Intent();
            intent.setAction(Const.ACTION_ON_OR_OFF_LINE);
            Myself self = new Myself();
            self.setChannelId(user.getChannelId());
            self.setName(user.getName());
            intent.putExtra("user", self);
            act.sendBroadcast(intent);
        }

        else if (msg instanceof ChatRoom) {
            ChatRoom room = (ChatRoom) msg;
            Intent intent = new Intent();
            intent.setAction(Const.ACTION_CREATE_CHAT_ROOM);
            intent.putExtra("room", room);
            act.sendBroadcast(intent);
        }

        else if (msg instanceof AddFriendRequest) {
            AddFriendRequest req = (AddFriendRequest) msg;
            Intent intent = new Intent();
            intent.setAction(Const.ACTION_ADDFRIEND_REQUEST);
            intent.putExtra("req", req);
            act.sendBroadcast(intent);
        } else if (msg instanceof AddFriendResponse) {
            AddFriendResponse resp = (AddFriendResponse) msg;
            Intent intent = new Intent();
            intent.setAction(Const.ACTION_ADDFRIEND_REPONSE);
            intent.putExtra("resp", resp);
            act.sendBroadcast(intent);
        } else if (msg instanceof FriendBody) {
            FriendBody body = (FriendBody) msg;
            Intent intent = new Intent();
            intent.setAction(Const.ACTION_ADD_FRIENDTOCHAT);
            intent.putExtra("newFriend", body);
            act.sendBroadcast(intent);
        }
    }
}
