package adapter;

import java.util.List;

import vo.Myself;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activity.R;

public class FriendAdapter extends SimpleAdapter<Myself> {

    public FriendAdapter(List<Myself> data, Context activity) {
        super(data, activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = makeView(R.layout.item_user_online);
            holder.name = (TextView) convertView.findViewById(R.id.user_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Myself user = getItem(position);
        holder.name.setText(user.getName());
        return convertView;
    }

    class Holder {
        private TextView name;
    }
}
