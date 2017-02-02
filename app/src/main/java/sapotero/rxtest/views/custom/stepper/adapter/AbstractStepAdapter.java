package sapotero.rxtest.views.custom.stepper.adapter;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import sapotero.rxtest.views.custom.stepper.Step;
import timber.log.Timber;


public abstract class AbstractStepAdapter<T extends Fragment & Step> extends FragmentPagerAdapter {

    public static final int DEFAULT_NEXT_BUTTON_TEXT = -1;

    private final FragmentManager mFragmentManager;

    public AbstractStepAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
    }

    @Override
    public final T getItem(int position) {
        return createStep(position);
    }

    protected abstract T createStep(int position);

    public Step findStep(ViewPager viewPager, int position) {
        String fragmentTag =  "android:switcher:" + viewPager.getId() + ":" + this.getItemId(position);
//        String fragmentTag =  "android:switcher:" + viewPager.getId() + ":" + this.getItemId(position);
        Timber.tag("STEPPER FRAGMENTS").i( fragmentTag );
        return (Step) mFragmentManager.findFragmentByTag(fragmentTag);
    }

    @StringRes
    public int getNextButtonText(int position) {
        return DEFAULT_NEXT_BUTTON_TEXT;
    }

}
