package my.project.silisili.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import my.project.silisili.R;
import my.project.silisili.bean.RankingBean;
import my.project.silisili.main.ranking.RankingFragment;
import my.project.silisili.util.Utils;

/**
 * 排行榜分类(共6类)的适配器
 * 使用自定义排序规则
 */
public class RankingTypeAdapter extends FragmentStatePagerAdapter {
    private static final String[] TABS = Utils.getArray(R.array.my_ranking_array);
    private final int num;
    private final Map<String, ArrayList<RankingBean>> map;
    private final HashMap<Integer, Fragment> mFragmentHashMap = new HashMap<>();

    public RankingTypeAdapter(FragmentManager fm, int num, Map<String, ArrayList<RankingBean>> map) {
        super(fm);
        this.num = num;
        this.map = map;
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
            fragment = RankingFragment.newInstance(map.get(TABS[pos]));
            mFragmentHashMap.put(pos, fragment);
        }
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }
}
