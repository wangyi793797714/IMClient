package aysntask;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.tsz.afinal.FinalDb;
import netty4.ChatClientInitializer;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import util.FileOperator;
import util.Util;
import vo.Friends;
import vo.Myself;
import adapter.OnlineAdapter;
import android.app.Activity;
import android.content.Intent;
import config.Const;

public class FetchOnlineUserTask extends BaseTask<Myself, Void, List<Myself>> {

    public static Channel channel;

    private EventLoopGroup group;
    private static Bootstrap bootStrap;
    private Activity act;
    private OnlineAdapter adapter;

    private String name = "";

    public FetchOnlineUserTask(Activity activity, OnlineAdapter adapter) {
        super(activity);
        this.act = activity;
        this.adapter = adapter;
    }

    @Override
    public List<Myself> doExecute(Myself user) throws Exception {
        name = user.getName();
        return connectServer(user);
    }

    @Override
    public void doResult(List<Myself> result) throws Exception {
        if (!Util.isEmpty(result)) {
            List<Myself> arrayList = new ArrayList<Myself>(result);
            Iterator<Myself> it = arrayList.iterator();
            while (it.hasNext()) {
                if (it.next().getName().equals(name)) {
                    it.remove();
                }
            }
            adapter.addItems(arrayList);
            FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
            for (Myself u : arrayList) {
                Friends on = new Friends();
                on.setChannelId(u.getChannelId());
                on.setName(u.getName());
                on.setOnline(u.isOnline());
                db.save(on);
            }
            
            Intent intent = new Intent();
            intent.setAction(Const.ACTION_OFFLINE_MSG);
            act.sendBroadcast(intent);
            
            Intent intent2 = new Intent();
            intent2.setAction(Const.ACTION_GROUP_OFFLINE_MSG);
            act.sendBroadcast(intent2);
        }
    }

    public List<Myself> connectServer(Myself info) {
        group = new NioEventLoopGroup();
        bootStrap = new Bootstrap();
        bootStrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChatClientInitializer(act));
        bootStrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootStrap.option(ChannelOption.TCP_NODELAY, true);
        bootStrap.option(ChannelOption.SO_REUSEADDR, true);
        try {
            channel = bootStrap.connect(Const.NEETY_IP, Const.NETTY_PORT).sync().channel();
        } catch (Exception e) {
            if (e instanceof ConnectException) {
                toast(act, "连接服务器失败");
            }
            System.err.println(e.fillInStackTrace());
            return null;
        }
        if (channel != null && channel.isRegistered()) {
            try {
                // 上线，通知他人
                Friends user = new Friends();
                user.setName(info.getName());
                user.setChannelId(info.getChannelId());
                channel.writeAndFlush(user).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final String url = Const.BASE_URL + "friend?n=" + info.getChannelId();
            HttpHeaders reqtHeaders = new HttpHeaders();
            List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
            acceptMediaTypes.add(MediaType.APPLICATION_JSON);
            reqtHeaders.setAccept(acceptMediaTypes);

            HttpEntity<?> requestEntity = new HttpEntity<Object>(reqtHeaders);

            RestTemplate rest = new RestTemplate();
            rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<Myself[]> resp = rest.exchange(url, HttpMethod.GET, requestEntity,
                    Myself[].class);
            return Arrays.asList(resp.getBody());
        }
        return null;
    }

}
