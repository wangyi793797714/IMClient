package adapter;

import java.util.ArrayList;
import java.util.List;

import vo.RoomChild;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.activity.R;

public class GroupChatAdapter extends SimpleAdapter<RoomChild> {

    public List<Boolean> isChecked;

    @SuppressLint("UseSparseArrays")
    public GroupChatAdapter(List<RoomChild> data, Context activity) {
        super(data, activity);
        isChecked = new ArrayList<Boolean>();
        for (int i = 0; i < data.size(); i++) {
            isChecked.add(false);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = makeView(R.layout.group_chat_item);
            holder.box = (CheckBox) convertView.findViewById(R.id.is_check);
            holder.name = (TextView) convertView.findViewById(R.id.friend_name);

            final int p = position;
            holder.box.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    isChecked.set(p, cb.isChecked());
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        RoomChild user = getItem(position);
        holder.name.setText(user.getName());
        holder.box.setChecked(isChecked.get(position));
        return convertView;
    }

    class Holder {
        private TextView name;
        private CheckBox box;
    }
}
