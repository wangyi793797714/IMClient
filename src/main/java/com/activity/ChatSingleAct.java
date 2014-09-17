package com.activity;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import net.tsz.afinal.FinalDb;
import net.tsz.afinal.annotation.view.ViewInject;

import org.springframework.util.support.Base64;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import application.IMApplication;
import aysntask.FetchOnlineUserTask;
import aysntask.LoginTask;
import aysntask.ShowLast10MsgsTask;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import config.Const;

public class ChatSingleAct extends BaseActivity {

	@ViewInject(id = R.id.lv_chat_detail)
	private PullToRefreshListView chatList;

	@ViewInject(id = R.id.text)
	private ImageView textIma;

	@ViewInject(id = R.id.image)
	private ImageView imageIma;

	@ViewInject(id = R.id.content)
	private EditText input;

	@ViewInject(id = R.id.album)
	private Button album;

	@ViewInject(id = R.id.take_photo)
	private Button takePhoto;

	@ViewInject(id = R.id.play)
	private Button play;

	@ViewInject(id = R.id.record)
	private Button recordBtn;

	@ViewInject(id = R.id.photo_op)
	private LinearLayout photoOp;

	private ChatAdapter adapter;

	public static int sendId = -1;

	private FinalDb db;
	
	/**通过点击通知栏，传入的信息*/
	private Content notifyMsg;
	private Myself vo;
	private List<Content> msgs;
	private MediaRecorder record = null;
	private MediaPlayer player = null;
	
	/**本地保存的声音路径，存于数据库，在播放的时候，直接根据路径播放，由voiceName+本地sd卡的路径决定*/
	private String voicePath;

	/**发送出去的音频文件名字*/
	private String voiceName;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_detail);
		IMApplication.APP.addActivity(this);
		db = FinalDb.create(getActivity(),
				FileOperator.getDbPath(getActivity()), true);

		// 由通知栏传入
		notifyMsg = (Content) getIntent().getExtras().getSerializable("3");
		// 由主界面传入
		vo = (Myself) getVo("0");
		
		int tempId=-1;
		
		if(notifyMsg!=null){
			tempId=notifyMsg.getSendId();	
		}else{
			tempId=vo.getChannelId();
		}
		sendId = vo.getChannelId();

		// 包含网络未读消息和本地未读消息（本地未读消息数据库中已经存在）
		msgs = (List<Content>) getVo("1");

		adapter = new ChatAdapter(new ArrayList<Content>(), activity);
		// 获取最近10条已读消息
		String key1 = tempId + ""
				+ db.findAll(Myself.class).get(0).getChannelId();
		String key2 = db.findAll(Myself.class).get(0).getChannelId() + ""
				+ tempId;
		final String sqlSplit = " belongTo = '" + key1 + "' or belongTo = '"
				+ key2 + "' ";
		List<Content> lastMsgs = db.findAllByWhere(Content.class,
				" isRead = 'true' and " + sqlSplit, "date DESC LIMIT 10 ");
		List<Content> tempDatas = new ArrayList<Content>();
		if (!Util.isEmpty(lastMsgs)) {
			tempDatas.addAll(lastMsgs);
		}
		if (!Util.isEmpty(msgs)) {
			sendId = msgs.get(0).getSendId();
			for (Content content : msgs) {
				if ("false".equals(content.getIsLocalMsg())) {
					// 网络离线消息设置为本地并且为已读
					content.setIsRead("true");
					content.setIsLocalMsg("true");
					if (content.getMsgType() == 0) {
						db.save(content);
					} else if (content.getMsgType() == 1) {
						try {
							byte[] bitmapArray = Base64
									.decode(content.getMsg());
							Bitmap bit = BitmapFactory.decodeByteArray(
									bitmapArray, 0, bitmapArray.length);
							FileOperator.saveImage2Sd(this, bit,
									content.getMsgLocalUrl());
							content.setMsg("");
							db.save(content);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						FileOperator.saveVoice2Sd(this, content.getMsg(),
								content.getMsgLocalUrl());
						content.setMsg("");
						content.setMsgLocalUrl(FileOperator.getLocalVoiceFolderPath(activity)+ content.getMsgLocalUrl());
						db.save(content);
					}
					tempDatas.add(content);
				} else {
					// 本地已经存在的未读消息，修改为已读
					content.setIsRead("true");
					content.setIsLocalMsg("true");
					db.update(content);
				}
			}
		}

		Collections.sort(tempDatas, new MsgComparator());
		adapter.addItems(tempDatas);
		if (notifyMsg != null) {
			List<Content> orderMsgs=new ArrayList<Content>();
			//如果从通知栏获取的信息，标记为已读并保存到本地，同时删除掉在Home消息容器中的对应数据
			sendId = notifyMsg.getSendId();
			notifyMsg.setIsRead("true");
			notifyMsg.setIsLocalMsg("true");
			//获取未读消息，包含已经存在本地的，以及网络发送来的，以及未读的通知
			List<Content> unReadMsgs= HomeActivity.singleMsgs.get(notifyMsg.getSendId());
			if(!Util.isEmpty(unReadMsgs)){
				for (Content content : unReadMsgs) {
					if ("false".equals(content.getIsLocalMsg())) {
						// 网络离线消息设置为本地并且为已读
						content.setIsRead("true");
						content.setIsLocalMsg("true");
						if (content.getMsgType() == 0) {
							db.save(content);
						} else if (content.getMsgType() == 1) {
							try {
								byte[] bitmapArray = Base64
										.decode(content.getMsg());
								Bitmap bit = BitmapFactory.decodeByteArray(
										bitmapArray, 0, bitmapArray.length);
								FileOperator.saveImage2Sd(this, bit,
										content.getMsgLocalUrl());
								content.setMsg("");
								db.save(content);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							FileOperator.saveVoice2Sd(this, content.getMsg(),
									content.getMsgLocalUrl());
							content.setMsg("");
							content.setMsgLocalUrl(FileOperator.getLocalVoiceFolderPath(activity)+ content.getMsgLocalUrl());
							db.save(content);
						}
						orderMsgs.add(content);
					} else {
						// 本地已经存在的未读消息，修改为已读
						content.setIsRead("true");
						content.setIsLocalMsg("true");
						db.update(content);
					}
				}
			}
			Collections.sort(orderMsgs, new MsgComparator());
			adapter.addItems(orderMsgs,adapter.getCount());
			HomeActivity.singleMsgs.remove(vo.getChannelId());
			sendBroadcast(ChatSingleAct.this, notifyMsg);
		}
		chatList.setAdapter(adapter);
		chatList.setMode(Mode.PULL_FROM_START);
		registerBoradcastReceiver(new msgBroadcastReceiver());
		textIma.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				photoOp.setVisibility(View.GONE);
				final Content content = new Content();
				content.setBelongTo(vo.getChannelId() + ""
						+ +db.findAll(Myself.class).get(0).getChannelId());
				content.setDate(new Date());
				content.setMsg(input.getText().toString());
				input.setText("");
				// 指定发送消息的人为当前登录的人
				content.setSendName(LoginTask.currentName);
				content.setSendMsg(true);
				content.setMsgType(0);
				content.setSendId(db.findAll(Myself.class).get(0)
						.getChannelId());
				if (notifyMsg != null) {
					content.setReceiveName(notifyMsg.getSendName());
					content.setReceiveId(notifyMsg.getSendId());
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
							public void operationComplete(
									Future<? super Void> future)
									throws Exception {
								if (future.isSuccess()) {
									ChatSingleAct.this
											.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													adapter.addItem(content,
															adapter.getCount());
													content.setIsRead("true");
													content.setIsLocalMsg("true");
													db.save(content);
													chatList.setSelection(adapter
															.getCount() - 1);
												}
											});
								}
							}
						});
			}
		});

		imageIma.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (photoOp.isShown()) {
					photoOp.setVisibility(View.GONE);
				} else {
					photoOp.setVisibility(View.VISIBLE);
				}
			}
		});
		album.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(Intent.createChooser(intent, "选择照片"), 0);
			}
		});

		takePhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
						Environment.getExternalStorageDirectory(), "temp.png")));
				startActivityForResult(Intent.createChooser(intent, "拍照中……"), 1);
			}
		});

		recordBtn.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				toast("开始录音");
				if (record == null) {
					record = new MediaRecorder();
				}
				voiceName=UUID.randomUUID().toString() + ".amr";
				voicePath = FileOperator.getLocalVoiceFolderPath(activity)
						+voiceName;
				
				record.setAudioSource(MediaRecorder.AudioSource.MIC);
				record.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				record.setOutputFile(voicePath);
				record.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				try {
					record.prepare();
				} catch (IOException e) {
					e.printStackTrace();
				}
				record.start();
				return true;
			}
		});
		recordBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					break;
				case MotionEvent.ACTION_UP:
					if (record != null) {
						record.stop();
						record.release();
						record = null;
						/*****/

						photoOp.setVisibility(View.GONE);
						final Content content = new Content();
						content.setBelongTo(vo.getChannelId()
								+ ""
								+ +db.findAll(Myself.class).get(0)
										.getChannelId());
						content.setDate(new Date());
						FileInputStream in = null;
						byte[] b = null;
						try {
							in = new FileInputStream(voicePath);
							b = new byte[in.available()];
							in.read(b);
						} catch (IOException e) {
							e.printStackTrace();
						}
						String voiceString = android.util.Base64
								.encodeToString(b, android.util.Base64.DEFAULT);
						content.setMsg(voiceString);
//						content.setMsgLocalUrl(voicePath);
						content.setMsgLocalUrl(voiceName);
						
						// 指定发送消息的人为当前登录的人
						content.setSendName(LoginTask.currentName);
						content.setSendMsg(true);
						content.setMsgType(2);
						content.setSendId(db.findAll(Myself.class).get(0)
								.getChannelId());
						if (notifyMsg != null) {
							content.setReceiveName(notifyMsg.getSendName());
							content.setReceiveId(notifyMsg.getSendId());
						} else {
							if (!Util.isEmpty(msgs)) {
								content.setReceiveId(msgs.get(0).getSendId());
								content.setReceiveName(msgs.get(0)
										.getSendName());
							} else {
								content.setReceiveName(vo.getName());
								content.setReceiveId(vo.getChannelId());
							}
						}
						sendId = content.getReceiveId();
						FetchOnlineUserTask.channel
								.writeAndFlush(content)
								.addListener(
										new GenericFutureListener<Future<? super Void>>() {
											@Override
											public void operationComplete(
													Future<? super Void> future)
													throws Exception {
												if (future.isSuccess()) {
													ChatSingleAct.this
															.runOnUiThread(new Runnable() {

																@Override
																public void run() {
																	adapter.addItem(
																			content,
																			adapter.getCount());
																	content.setIsRead("true");
																	content.setIsLocalMsg("true");
																	content.setMsgLocalUrl(voicePath);
																	db.save(content);
																	chatList.setSelection(adapter
																			.getCount() - 1);
																}
															});
												}
											}
										});

					}
					break;
				default:
					break;
				}
				return false;
			}
		});
		play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (player == null) {
					player = new MediaPlayer();
				}
				player.reset();
				try {
					player.setDataSource(FileOperator
							.getLocalImageFolderPath(activity) + "123.amr");
					player.prepare();
					player.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// select * from users order by id limit 10 offset 0
		// offset代表从第几条记录“之后“开始查询，limit表明查询多少条结果

		chatList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> arg0) {
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_ABBREV_ALL);
				chatList.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				new ShowLast10MsgsTask(activity, chatList, adapter)
						.execute(sqlSplit);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		photoOp.setVisibility(View.GONE);
		if (resultCode == RESULT_OK) {
			if (requestCode == 0) {
				photoZoom(data.getData());
			} else if (requestCode == 1) {
				File temp = new File(Environment.getExternalStorageDirectory()
						+ "/temp.png");
				photoZoom(Uri.fromFile(temp));
			} else if (requestCode == 2) {
				final Bitmap bit = data.getParcelableExtra("data");
				final Content content = new Content();
				content.setBelongTo(vo.getChannelId() + ""
						+ +db.findAll(Myself.class).get(0).getChannelId());
				content.setDate(new Date());
				// 指定发送消息的人为当前登录的人
				content.setSendName(LoginTask.currentName);
				content.setSendMsg(true);
				content.setMsgType(1);
				content.setSendId(db.findAll(Myself.class).get(0)
						.getChannelId());
				if (notifyMsg != null) {
					content.setReceiveName(notifyMsg.getSendName());
					content.setReceiveId(notifyMsg.getSendId());
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

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bit.compress(Bitmap.CompressFormat.PNG, 100, bos);
				byte[] src = bos.toByteArray();
				String imageString = android.util.Base64.encodeToString(src,
						android.util.Base64.DEFAULT);
				content.setMsg(imageString);
				final String imageUrl = UUID.randomUUID().toString();
				content.setMsgLocalUrl(imageUrl);
				FetchOnlineUserTask.channel.writeAndFlush(content).addListener(
						new GenericFutureListener<Future<? super Void>>() {
							@Override
							public void operationComplete(
									Future<? super Void> future)
									throws Exception {
								if (future.isSuccess()) {
									ChatSingleAct.this
											.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													adapter.addItem(content,
															adapter.getCount());
													content.setIsRead("true");
													content.setIsLocalMsg("true");
													content.setMsg("");
													db.save(content);
													chatList.setSelection(adapter
															.getCount() - 1);
													FileOperator.saveImage2Sd(
															ChatSingleAct.this,
															bit, imageUrl);
												}
											});
								}
							}
						});

			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 图片缩放
	private void photoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1.5);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 2);
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
