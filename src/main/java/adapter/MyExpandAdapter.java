package adapter;

import java.util.List;

import util.Util;
import vo.ChatRoom;
import vo.Myself;
import vo.RoomChild;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.activity.ChatGroupAct;
import com.activity.R;

/**
 * 
 * @desc
 * @author WY 创建时间 2014年3月11日 下午2:11:43
 */
public class MyExpandAdapter extends BaseExpandableListAdapter {

    private List<ChatRoom> data;

    Activity context;

    public MyExpandAdapter(Activity context, List<ChatRoom> data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition).getChild(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = makeView(R.layout.expand_list_child_item);
        }
        ImageView image = (ImageView) convertView.findViewById(R.id.expand_image);
        TextView title = (TextView) convertView.findViewById(R.id.expand_title);
        image.setImageResource(R.drawable.ic_launcher);
        title.setText(data.get(groupPosition).getChild(childPosition).getName());
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (Util.isEmpty(data)) {
            return 0;
        }
        return data.get(groupPosition).childsSize();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        if (convertView == null) {
            convertView = makeView(R.layout.expand_list_item);
        }
        ImageView image = (ImageView) convertView.findViewById(R.id.expand_image);
        TextView title = (TextView) convertView.findViewById(R.id.expand_title);
        Button btn = (Button) convertView.findViewById(R.id.expand_add_child);
        convertView.setTag(data.get(groupPosition).getGrouppTag());
        title.setText(data.get(groupPosition).getGroupName());
        image.setVisibility(View.INVISIBLE);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("0", data.get(groupPosition));
                intent.putExtras(bundle);
                intent.setClass(context, ChatGroupAct.class);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public View makeView(int resId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(resId, null);
    }

    public void addRoom(ChatRoom room) {
        data.add(room);
        notifyDataSetChanged();
    }

    public void addChild(int groupPosition, RoomChild u) {
        data.get(groupPosition).addToChilds(u);
        notifyDataSetChanged();
    }

    public void addChild(int groupPosition, List<RoomChild> u) {
        data.get(groupPosition).addToChilds(u);
        ;
        notifyDataSetChanged();
    }

    public void removeChild(int groupPosition, Myself u) {
        data.get(groupPosition).removeChild(u);
        notifyDataSetChanged();
    }

    public void removeChild(int groupPosition, int childPosition) {
        data.get(groupPosition).removeChild(childPosition);
        notifyDataSetChanged();
    }

    public void removeRoom(ChatRoom room) {
        data.remove(room);
        notifyDataSetChanged();
    }

    public void removeRoom(int positon) {
        data.remove(positon);
        notifyDataSetChanged();
    }

    public void reLoad(List<ChatRoom> newDatas) {
        data.clear();
        data.addAll(newDatas);
        notifyDataSetChanged();
    }

    public boolean hasChild(int groupPosition) {
        if (Util.isEmpty(data.get(groupPosition).getChildDatas())) {
            return false;
        }
        return true;
    }
}
