package adapter;

import java.util.List;

import util.Util;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class SimpleAdapter<E> extends BaseAdapter {

    protected List<E> data;

    protected Context context;

    public SimpleAdapter(List<E> data, Context activity) {
        this.data = data;
        this.context = activity;
    }

    @Override
    public int getCount() {
        if (!Util.isEmpty(data)) {
            return data.size();
        }
        return 0;
    }

    @Override
    public E getItem(int arg0) {
        if (!Util.isEmpty(data)) {
            return data.get(arg0);
        }
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void addItems(List<E> extras) {
        data.addAll(getCount(), extras);
        // 通知数据的变更，必须加上
        notifyDataSetChanged();
    }

    public void addItems(List<E> extras, int local) {
        data.addAll(local, extras);
        // 通知数据的变更，必须加上
        notifyDataSetChanged();
    }

    public void addItem(E extras, int local) {
        data.add(local, extras);
        // 通知数据的变更，必须加上
        notifyDataSetChanged();
    }

    public void reload(List<E> newData) {
        data.clear();
        addItems(newData);
    }

    public void refresh() {
        if (!Util.isEmpty(data)) {
            notifyDataSetChanged();
        }
    }

    public void removeByPos(int position) {
        E e = getItem(position);
        remove(e);
    }

    public void remove(E item) {
        data.remove(item);
        notifyDataSetChanged();
    }

    public List<E> getDataSource() {
        return data;
    }

    public int getPosition(E e) {
        if (!Util.isEmpty(data)) {
            return data.indexOf(e);
        }
        return 0;
    }

    public View makeView(int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layoutId, null);
    }
}
