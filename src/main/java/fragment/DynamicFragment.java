package fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activity.R;
/**
 * 
 * @Des: 动态界面
 * @author Rhino 
 * @version V1.0 
 * @created  2014年9月17日 下午2:14:58
 */
public class DynamicFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = makeView(R.layout.main_frag_four);
        return view;
    }
}
