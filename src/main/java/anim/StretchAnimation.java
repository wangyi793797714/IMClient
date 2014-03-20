package anim;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * 
 * @author manymore13
 * @Blog http://blog.csdn.net/manymore13
 */
public class StretchAnimation {

	private final static String TAG = "SizeChange";
	private Interpolator mInterpolator; // 好多书上翻译为插值器
	private View mView; // 你要伸缩的view
	private int mCurrSize; // 当前大小
	private int mRawSize;
	private int mMinSize; // 最小大小 固定值
	private int mMaxSize; // 最大大小 固定值
	private boolean isFinished = true;// 动画结束标识
	private TYPE mType = TYPE.horizontal;
	private final static int FRAMTIME = 20;// 一帧的时间 毫秒

	public static enum TYPE {
		horizontal, // 改变view水平方向的大小
		vertical // 改变view竖直方向的大小
	}

	private int mDuration; // 动画运行的时间
	private long mStartTime;// 动画开始时间
	private float mDurationReciprocal;
	private int mDSize; // 需要改变view大小的增量

	public StretchAnimation(int maxSize, int minSize, TYPE type, int duration) {
		if (minSize >= maxSize) {
			throw new RuntimeException("View的最大改变值不能小于最小改变值");
		}
		mMinSize = minSize;
		mMaxSize = maxSize;
		mType = type;
		mDuration = duration;
	}

	public void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	public TYPE getmType() {
		return mType;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	private void changeViewSize() {

		if (mView != null && mView.getVisibility() != View.GONE) {
			LayoutParams params = mView.getLayoutParams();
			if (mType == TYPE.vertical) {
				params.height = mCurrSize;
			} else if (mType == TYPE.horizontal) {
				params.width = mCurrSize;
			}
			Log.i(TAG, "CurrSize = " + mCurrSize + " Max=" + mMaxSize + " min="
					+ mMinSize);
			mView.setLayoutParams(params);
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.what == 1) {
				if (!computeViewSize()) {

					mHandler.sendEmptyMessageDelayed(1, FRAMTIME);
				} else {
					if (animationlistener != null) {
						animationlistener.animationEnd(mView);
					}
				}
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * @return 返回true 表示动画完成
	 */
	private boolean computeViewSize() {

		if (isFinished) {
			return isFinished;
		}
		int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);

		if (timePassed <= mDuration) {
			float x = timePassed * mDurationReciprocal;
			if (mInterpolator != null) {
				x = mInterpolator.getInterpolation(x);
			}
			mCurrSize = mRawSize + Math.round(x * mDSize);
		} else {

			isFinished = true;
			mCurrSize = mRawSize + mDSize;

		}
		changeViewSize();
		return isFinished;
	}

	public void startAnimation(View view) {

		if (view != null) {
			mView = view;
		} else {
			Log.e(TAG, "view 不能为空");
			return;
		}
		// LayoutParams params = mView.getLayoutParams();

		if (isFinished) {
			mDurationReciprocal = 1.0f / (float) mDuration;
			if (mType == TYPE.vertical) {
				mRawSize = mCurrSize = mView.getHeight();
			} else if (mType == TYPE.horizontal) {
				mRawSize = mCurrSize = mView.getWidth();
			}
			Log.i(TAG, "mRawSize=" + mRawSize);
			if (mCurrSize > mMaxSize || mCurrSize < mMinSize) {
				throw new RuntimeException(
						"View 的大小不达标 currentViewSize > mMaxSize || currentViewSize < mMinSize");
			}
			isFinished = false;
			mStartTime = AnimationUtils.currentAnimationTimeMillis(); // 动画开始时间
			if (mCurrSize < mMaxSize) {
				mDSize = mMaxSize - mCurrSize;
			} else {
				mDSize = mMinSize - mMaxSize;
			}
			Log.i(TAG, "mDSize=" + mDSize);
			mHandler.sendEmptyMessage(1);
		}
	}

	private AnimationListener animationlistener;

	public interface AnimationListener {
		public void animationEnd(View v);
	}

	public void setOnAnimationListener(AnimationListener listener) {
		animationlistener = listener;
	}

}
