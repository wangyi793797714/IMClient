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
import aysntask.AddFriendToRoomTask;
import aysntask.FetchOnlineUserTask;
import aysntask.LoginTask;
import aysntask.ShowLast10MsgsTask;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import config.Const;

public class ChatGroupAct extends BaseActivity {

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

	@ViewInject(id = R.id.photo_op)
	private LinearLayout photoOp;

	private ChatAdapter chatAdapter;

	private List<RoomChild> friends;

	public static Long CurrentGroup = -1L;

	@ViewInject(id = R.id.record)
	private Button recordBtn;

	MediaRecorder record = null;
	MediaPlayer player = null;

	String voicePath;
	String remoteVoicePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		chatAdapter = new ChatAdapter(new ArrayList<Content>(), activity);
		final ChatRoom room = ((ChatRoom) getVo("0"));
		friends = room.getChildDatas();
		CurrentGroup = friends.get(0).getGroupTag();
		final String sql = " grouppTag= " + CurrentGroup
				+ " and isRead = 'true'";
		List<Content> lastList = db.findAllByWhere(Content.class, sql,
				"date DESC LIMIT 10");
		if (!Util.isEmpty(lastList)) {
			Collections.sort(lastList, new MsgComparator());
			chatAdapter.addItems(lastList);
		}
		if (!Util.isEmpty(HomeActivity.groupMsgs.get(((ChatRoom) getVo("0"))
				.getGrouppTag()))) {
			List<Content> msgs = HomeActivity.groupMsgs
					.get(((ChatRoom) getVo("0")).getGrouppTag());
			Collections.sort(msgs, new MsgComparator());
			for (Content msg : msgs) {
				if ("false".equals(msg.getIsLocalMsg())) {
					// 网络离线消息设置为本地并且为已读
					// 网络离线消息设置为本地并且为已读
					msg.setIsRead("true");
					msg.setIsLocalMsg("true");
					if (msg.getMsgType() == 0) {
						db.save(msg);
					} else if (msg.getMsgType() == 1) {
						try {
							byte[] bitmapArray = Base64.decode(msg.getMsg());
							Bitmap bit = BitmapFactory.decodeByteArray(
									bitmapArray, 0, bitmapArray.length);
							FileOperator.saveImage2Sd(this, bit,
									msg.getMsgLocalUrl());
							msg.setMsg("");
							db.save(msg);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {

					}
				} else {
					// 本地已经存在的未读消息，修改为已读
					msg.setIsLocalMsg("true");
					msg.setIsRead("true");
					db.update(msg, " uuid = '" + msg.getUuid() + "'");
				}
			}
			chatAdapter.addItems(msgs);
			HomeActivity.groupMsgs
					.get((((ChatRoom) getVo("0")).getGrouppTag())).clear();
		}
		setContentView(R.layout.chat_detail);
		IMApplication.APP.addActivity(this);
		initView();
		registerBoradcastReceiver(new msgBroadcastReceiver());
		getActionBar().setCustomView(R.layout.main_action_button);
		getActionBar().setDisplayShowCustomEnabled(true);
		ImageView addFriend = (ImageView) getActionBar().getCustomView();
		addFriend
				.setBackgroundDrawable(getDrawableRes(R.drawable.add_friend_drawable));
		addFriend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle("请选择好友");
				builder.setIcon(android.R.drawable.ic_dialog_info);
				View view = makeView(R.layout.group_chat_list);
				builder.setView(view);
				ListView list = (ListView) view
						.findViewById(R.id.group_chat_list);
				FinalDb db = FinalDb.create(activity,
						FileOperator.getDbPath(activity), true);
				// 获取在线的好友列表,同已经在此群组里的好友取差集，便是不在此聊天群组中的在线好友

				List<Friends> onlines = db.findAllByWhere(Friends.class,
						"isOnline = 1");
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
							if (onlineUser.get(i).getChannelId() == user
									.getChannelId()) {
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
				final GroupChatAdapter gcAdapter = new GroupChatAdapter(src,
						activity);
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
				String label = DateUtils.formatDateTime(
						getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_ABBREV_ALL);
				chatList.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				new ShowLast10MsgsTask(activity, chatList, chatAdapter)
						.execute(sql);
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
				remoteVoicePath=UUID.randomUUID().toString() + ".amr";
				voicePath = FileOperator.getLocalVoiceFolderPath(activity)
						+ remoteVoicePath;
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
						photoOp.setVisibility(View.GONE);
						final Content content = new Content();
						content.setDate(new Date());
						// 指定发送者为当前登录的人
						content.setSendName(LoginTask.currentName);
						content.setSendMsg(true);
						content.setReceiveId(0);
						content.setGrouppTag(CurrentGroup);
						content.setUuid(UUID.randomUUID().toString());
						List<Integer> ids = new ArrayList<Integer>();
						final FinalDb db = FinalDb.create(activity,
								FileOperator.getDbPath(activity), true);
						for (RoomChild user : friends) {
							if (user.getChannelId() != db.findAll(Myself.class)
									.get(0).getChannelId()) {
								ids.add(user.getChannelId());
							}
						}
						content.setMsgType(2);
						content.setTargetIds(ids);

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
						content.setMsgLocalUrl(remoteVoicePath);

						FetchOnlineUserTask.channel
								.writeAndFlush(content)
								.addListener(
										new GenericFutureListener<Future<? super Void>>() {
											@Override
											public void operationComplete(
													Future<? super Void> future)
													throws Exception {
												if (future.isSuccess()) {
													ChatGroupAct.this
															.runOnUiThread(new Runnable() {

																@Override
																public void run() {
																	chatAdapter.addItem(
																			content,
																			chatAdapter.getCount());
																	content.setIsRead("true");
																	content.setIsLocalMsg("true");
																	content.setMsgLocalUrl(voicePath);
																	db.save(content);
																	chatList.setSelection(chatAdapter
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
				content.setDate(new Date());
				content.setMsg(input.getText().toString());
				input.setText("");
				// 指定发送者为当前登录的人
				content.setSendName(LoginTask.currentName);
				content.setSendMsg(true);
				content.setReceiveId(0);
				content.setGrouppTag(CurrentGroup);
				content.setUuid(UUID.randomUUID().toString());
				List<Integer> ids = new ArrayList<Integer>();
				final FinalDb db = FinalDb.create(activity,
						FileOperator.getDbPath(activity), true);
				for (RoomChild user : friends) {
					if (user.getChannelId() != db.findAll(Myself.class).get(0)
							.getChannelId()) {
						ids.add(user.getChannelId());
					}
				}
				content.setTargetIds(ids);
				content.setMsgType(1);
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
									ChatGroupAct.this
											.runOnUiThread(new Runnable() {

												@Override
												public void run() {
													chatAdapter
															.addItem(
																	content,
																	chatAdapter
																			.getCount());
													content.setIsRead("true");
													content.setIsLocalMsg("true");
													content.setMsg("");
													db.save(content);
													chatList.setSelection(chatAdapter
															.getCount() - 1);
													FileOperator.saveImage2Sd(
															ChatGroupAct.this,
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

	public void initView() {
		chatList.setAdapter(chatAdapter);
		textIma.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				photoOp.setVisibility(View.GONE);
				final Content content = new Content();
				content.setDate(new Date());
				content.setMsg(input.getText().toString());
				input.setText("");
				// 指定发送者为当前登录的人
				content.setSendName(LoginTask.currentName);
				content.setSendMsg(true);
				content.setReceiveId(0);
				content.setGrouppTag(CurrentGroup);
				content.setUuid(UUID.randomUUID().toString());
				List<Integer> ids = new ArrayList<Integer>();
				final FinalDb db = FinalDb.create(activity,
						FileOperator.getDbPath(activity), true);
				for (RoomChild user : friends) {
					if (user.getChannelId() != db.findAll(Myself.class).get(0)
							.getChannelId()) {
						ids.add(user.getChannelId());
					}
				}
				content.setMsgType(0);
				content.setTargetIds(ids);
				FetchOnlineUserTask.channel.writeAndFlush(content).addListener(
						new GenericFutureListener<Future<? super Void>>() {
							@Override
							public void operationComplete(
									Future<? super Void> future)
									throws Exception {
								if (future.isSuccess()) {
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											chatAdapter.addItem(content,
													chatAdapter.getCount());
											chatList.setSelection(chatAdapter
													.getCount() - 1);
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
				if (content.getMsgType() == 0) {
					db.save(content);
				} else if (content.getMsgType() == 1) {
					try {
						byte[] bitmapArray = Base64.decode(content.getMsg());
						Bitmap bit = BitmapFactory.decodeByteArray(bitmapArray,
								0, bitmapArray.length);
						FileOperator.saveImage2Sd(ChatGroupAct.this, bit,
								content.getMsgLocalUrl());
						content.setMsg("");
						db.save(content);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if(content.getMsgType() == 2){
					FileOperator.saveVoice2Sd(activity, content.getMsg(),
							content.getMsgLocalUrl());
					content.setMsg("");
					content.setMsgLocalUrl(FileOperator.getLocalVoiceFolderPath(activity)+ content.getMsgLocalUrl());
					db.save(content);
				}
				chatAdapter.addItem(content, chatAdapter.getCount());
				chatList.setSelection(chatAdapter.getCount() - 1);
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
