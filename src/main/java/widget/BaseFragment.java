package widget;

import java.io.Serializable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public abstract class BaseFragment extends Fragment {
    
    @Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState);

    public View makeView(int resId) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        return inflater.inflate(resId, null);
    }
    
    public void skip(Class<? extends Activity> cls) {
        startActivity(new Intent(getActivity(), cls));
    }

    public void skip(String action) {
        startActivity(new Intent(action));
    }

    public void skip(String action, Serializable... serializ) {
        Intent intent = new Intent(action);
        Bundle extras = new Bundle();
        for (int i = 0; i < serializ.length; i++) {
            Serializable s = serializ[i];
            extras.putSerializable(i + "", s);
        }
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void skip(Class<? extends Activity> cls, Serializable... serializ) {
        Intent intent = new Intent(getActivity(), cls);
        Bundle extras = new Bundle();
        for (int i = 0; i < serializ.length; i++) {
            Serializable s = serializ[i];
            extras.putSerializable(i + "", s);
        }
        intent.putExtras(extras);
        startActivity(intent);
    }

    public Serializable getVo(String key) {
        Intent myIntent = getActivity().getIntent();
        Bundle bundle = myIntent.getExtras();
        Serializable vo = bundle.getSerializable(key);
        return vo;
    }
    
    public void toast(Object obj) {
        toast(obj, 0);
    }

    private void toast(Object obj, int dur) {
        if (obj != null) {
            Toast.makeText(getActivity(), obj.toString(), dur).show();
        } else {
            Toast.makeText(getActivity(), "提示内容为空", dur).show();
        }
    }
}
