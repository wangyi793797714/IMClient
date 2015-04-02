package com.activity;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import vo.HeartbeatVo;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import aysntask.FetchOnlineUserTask;

/**
 * 
 * @Des:
 * @author Rhino
 * @version V1.0
 * @created 2015年3月24日 下午2:54:09
 */
public class HeartbeatService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("心跳服务打开");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(20000);
						if(FetchOnlineUserTask.channel!=null){
							HeartbeatVo vo=new HeartbeatVo();
							vo.setMsg("OK");
							FetchOnlineUserTask.channel.writeAndFlush(vo).addListener(new GenericFutureListener<Future<? super Void>>() {

								@Override
								public void operationComplete(
										Future<? super Void> future)
										throws Exception {
									if(future.isSuccess()){
										System.out.println("依然在线");
									}else{
										System.out.println("掉线了");
									}
								}
							});
							
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, START_STICKY, startId);
	}

	@Override
	public void onDestroy() {
		Intent sevice = new Intent(this, HeartbeatService.class);
		this.startService(sevice);
		super.onDestroy();
	}
}
