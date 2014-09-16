package adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import util.FileOperator;
import vo.Content;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.activity.R;

public class ChatAdapter extends SimpleAdapter<Content> {

	private int COME_MSG = 0;

	private int SEND_MSG = 1;

	private Activity act;

	MediaPlayer player;

	public ChatAdapter(List<Content> data, Activity activity) {
		super(data, activity);
		this.act = activity;
		player = new MediaPlayer();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Content item = getItem(position);
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			if (item.isSendMsg()) {
				convertView = makeView(R.layout.item_chat_send);
			} else {
				convertView = makeView(R.layout.item_chat_receive);
			}
			holder.msg = (TextView) convertView.findViewById(R.id.sender_msg);
			holder.name = (TextView) convertView.findViewById(R.id.sender_name);
			holder.image = (ImageView) convertView
					.findViewById(R.id.sender_image);
			holder.palyBtn = (Button) convertView
					.findViewById(R.id.sender_voice);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		if (item != null) {
			if (item.getMsgType() == 0) {
				holder.image.setVisibility(View.GONE);
				holder.palyBtn.setVisibility(View.GONE);
				holder.msg.setVisibility(View.VISIBLE);
				holder.msg.setText(item.getMsg());
			} else if (item.getMsgType() == 1) {
				holder.image.setVisibility(View.VISIBLE);
				holder.msg.setVisibility(View.GONE);
				holder.palyBtn.setVisibility(View.GONE);
				Bitmap bit = BitmapFactory.decodeFile(FileOperator
						.getLocalImageFolderPath(act) + item.getMsgLocalUrl());
				holder.image.setImageBitmap(bit);
			} else {
				holder.image.setVisibility(View.GONE);
				holder.msg.setVisibility(View.GONE);
				holder.palyBtn.setVisibility(View.VISIBLE);
				holder.palyBtn.setOnClickListener(new ButtonListener(position,
						holder.palyBtn));
			}
			holder.name.setText(item.getSendName());
		}
		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		Content item = getItem(position);
		if (item.isSendMsg()) {
			return SEND_MSG;
		} else {
			return COME_MSG;
		}
	}

	@Override
	public int getViewTypeCount() {
		// 这个方法默认返回1，如果希望listview的item都是一样的就返回1，我们这里有两种风格，返回2
		return 2;
	}

	private static class Holder {

		private TextView name;

		private TextView msg;

		private ImageView image;

		private Button palyBtn;
	}

	class ButtonListener implements OnClickListener {

		private int position;

		private Button btn;

		ButtonListener(int pos, Button btn) {
			this.position = pos;
			this.btn = btn;
		}

		@Override
		public void onClick(View v) {
			int vid = v.getId();
			if (vid == btn.getId()) {
				Content msg = getItem(position);
				if (player == null) {
					player = new MediaPlayer();
				}
				player.reset();
				try {
//					player.setDataSource(msg.getMsgLocalUrl());
//					
					File file = new File(msg.getMsgLocalUrl()); 
					FileInputStream fis = new FileInputStream(file); 
//					player.setDataSource(fis., fis.getStartOffset(), fis.getLength());
					player.setDataSource(fis.getFD()); 
					player.prepare();
					player.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
