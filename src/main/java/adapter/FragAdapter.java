package adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragAdapter extends FragmentPagerAdapter {

	private List<Fragment> data;

	public FragAdapter(FragmentManager fm, List<Fragment> data) {
		super(fm);
		this.data = data;
	}

	@Override
	public synchronized Fragment getItem(int position) {
		return data.get(position);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	public void addFragment(int position, Fragment frag) {
		data.add(position, frag);
		notifyDataSetChanged();
	}

	public void addFragment(Fragment frag) {
		data.add(frag);
		notifyDataSetChanged();
	}
}
