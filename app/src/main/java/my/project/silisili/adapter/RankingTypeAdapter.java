package my.project.silisili.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.HashMap;

import my.project.silisili.R;
import my.project.silisili.main.ranking.RankingFragment;
import my.project.silisili.util.Utils;

/**
 * 排行榜分类的适配器
 */
public class RankingTypeAdapter extends FragmentStatePagerAdapter {
    private static final String[] TABS = Utils.getArray(R.array.ranking_array);
    private final int num;
    private final HashMap<Integer, Fragment> mFragmentHashMap = new HashMap<>();

    public RankingTypeAdapter(FragmentManager fm, int num) {
        super(fm);
        this.num = num;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return createFragment(position);
    }

    @Override
    public int getCount() {
        return num;
    }

    private Fragment createFragment(int pos) {
        Fragment fragment = mFragmentHashMap.get(pos);

        if (fragment == null) {
            fragment = RankingFragment.newInstance(TABS[pos]);
            mFragmentHashMap.put(pos, fragment);
        }
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }
}
