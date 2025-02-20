package my.project.silisili.main.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.project.silisili.R;
import my.project.silisili.adapter.FragmentAdapter;
import my.project.silisili.application.Silisili;
import my.project.silisili.bean.HomeWekBean;
import my.project.silisili.main.base.LazyFragment;
import my.project.silisili.main.desc.DescActivity;
import my.project.silisili.util.Utils;
import my.project.silisili.util.VideoUtils;

@SuppressLint("ValidFragment")
public class WeekFragment extends LazyFragment {
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    @BindView(R.id.loading)
    ProgressBar loading;
    protected FragmentAdapter adapter;
    private List<HomeWekBean> list = new ArrayList<>();
    private Silisili application;
    private View view;
    private View errorView;
    private TextView errorTitle;
    private String week;
    private Unbinder mUnBinder;

    public WeekFragment(String week) {
        this.week = week;
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_week, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        errorView = getLayoutInflater().inflate(R.layout.base_error_view, (ViewGroup) recyclerView.getParent(), false);
        errorTitle = errorView.findViewById(R.id.title);
        if (Utils.checkHasNavigationBar(getActivity())) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
        if (application == null) application = (Silisili) getActivity().getApplication();
        initAdapter();
        return view;
    }

    @Override
    protected void initData() {
        initWeekData();
    }

    public void initAdapter() {
        if (adapter == null) {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Utils.isPad() ? 5 : 3));
            adapter = new FragmentAdapter(getActivity(), list);
            adapter.openLoadAnimation();
            adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
            adapter.setOnItemClickListener((adapter, view, position) -> {
                if (!Utils.isFastClick()) return;
                HomeWekBean bean = (HomeWekBean) adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("name", bean.getTitle());
                bundle.putString("url", VideoUtils.getSiliUrl(bean.getUrl()));
                startActivity(new Intent(getActivity(), DescActivity.class).putExtras(bundle));
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void initWeekData() {
        loading.setVisibility(View.GONE);
        adapter.removeAllFooterView();
        if (adapter.getData().isEmpty()) {
            list = getList(week);
            if (list.size() == 0) {
                if (!application.error.isEmpty()) {
                    errorTitle.setText(application.error);
                    adapter.setEmptyView(errorView);
                }
            } else
                adapter.setNewData(list);
        }
    }

    private List getList(String week) {
        list = new ArrayList<>();
        if (application.week.length() > 0) {
            try {
                JSONArray arr = new JSONArray(application.week.getString(week));
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject object = new JSONObject(arr.getString(i));
                    list.add(new HomeWekBean(object.getString("title"),
                            object.getString("img"),
                            object.getString("url"),
                            object.getString("drama"),
                            object.getBoolean("new"),
                            object.getString("date")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }
}
