package my.project.silisili.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.List;

import my.project.silisili.R;
import my.project.silisili.util.Utils;

/**
 * Created by GT on 2018/1/22.
 * 自定义选项
 */

public class PopupWindow {

    private final Context mContext;
    private android.widget.PopupWindow mPopupWindow;
    //the view where PopupWindow lie in
    private View mAnchorView;
    //ListView item data
    private String[] mItemData;
    //the animation for PopupWindow
    private int mPopAnimStyle;
    //the PopupWindow width
    private int mPopupWindowWidth;
    //the PopupWindow height
    private int mPopupWindowHeight;
    private OnItemSelected mOnSelectedListener;
    private boolean mModal;
    private ArrayAdapter<String> spinnerAdapter;

    public PopupWindow(Context mContext) {
        if (mContext == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        this.mContext = mContext;
        setHeightWidth();
    }

    public void setAnchorView(@Nullable View anchor) {
        mAnchorView = anchor;
    }

    public void setItemData(String[] mItemData) {
        this.mItemData = mItemData;
    }

    public void setPopAnimStyle(int mPopAnimStyle) {
        this.mPopAnimStyle = mPopAnimStyle;
    }

    public void setPopupWindowWidth(int mPopupWindowWidth) {
        this.mPopupWindowWidth = mPopupWindowWidth;
    }

    public void setPopupWindowHeight(int mPopupWindowHeight) {
        this.mPopupWindowHeight = mPopupWindowHeight;
    }

    /**
     * Set whether this window should be modal when shown.
     *
     * <p>If a popup window is modal, it will receive all touch and key input.
     * If the user touches outside the popup window's content area the popup window
     * will be dismissed.
     *
     * @param modal {@code true} if the popup window should be modal, {@code false} otherwise.
     */
    public void setModal(boolean modal) {
        mModal = modal;
    }

    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void hide() {
        if (isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public void setOnSelectedListener(OnItemSelected mOnSelectedListener) {
        this.mOnSelectedListener = mOnSelectedListener;
    }

//    private ListView mPopView;

    public void show() {
        if (mAnchorView == null) {
            throw new IllegalArgumentException("PopupWindow show location view can  not be null");
        }
        if (mItemData == null) {
            throw new IllegalArgumentException("please fill ListView Data");
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.view_popup_info, null);// 得到加载view
        AppCompatSpinner sourceSpinner = view.findViewById(R.id.selected_speed);// 数据源选择
        if (spinnerAdapter == null) {
            spinnerAdapter = new ArrayAdapter<>(mContext, R.layout.item_spinner_source, mItemData);
            //spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_source);
            sourceSpinner.setAdapter(spinnerAdapter);

            sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mOnSelectedListener.onSelected(position, mItemData[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        if (Utils.isPortrait()) {
            if (mPopupWindowWidth == 0) {
                mPopupWindowWidth = (int) (mDeviceWidth * 0.6);
            }
            if (mPopupWindowHeight == 0) {
                mPopupWindowHeight = view.getMeasuredHeight();
                if (mPopupWindowHeight > mDeviceHeight / 3) {
                    mPopupWindowHeight = mDeviceHeight / 3;
                }
            }
        } else {
            if (mPopupWindowWidth == 0) {
                mPopupWindowWidth = (int) (mDeviceWidth * 0.3);
            }
            if (mPopupWindowHeight == 0) {
                mPopupWindowHeight = view.getMeasuredHeight();
                if (mPopupWindowHeight > mDeviceHeight / 2) {
                    mPopupWindowHeight = mDeviceHeight / 2;
                }
            }
        }
        //设置下拉框的宽度 为屏幕宽度
        sourceSpinner.setDropDownWidth(mPopupWindowWidth);
        mPopupWindow = new android.widget.PopupWindow(view, mPopupWindowWidth, mPopupWindowHeight);
        if (mPopAnimStyle != 0) {
            mPopupWindow.setAnimationStyle(mPopAnimStyle);
        }
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(mModal);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), (Bitmap) null));


        Rect location = locateView(mAnchorView);
        if (location != null) {
            int x;
            //view中心点X坐标
            int xMiddle = location.left + mAnchorView.getWidth() / 2;
            if (xMiddle > mDeviceWidth / 2) {
                //在右边
                x = xMiddle - mPopupWindowWidth;
            } else {
                x = xMiddle;
            }
            int y;
            //view中心点Y坐标
            int yMiddle = location.top + mAnchorView.getHeight() / 2;
            if (yMiddle > mDeviceHeight / 2) {
                //在下方
                y = yMiddle - mPopupWindowHeight;
            } else {
                //在上方
                y = yMiddle;
            }
            mPopupWindow.showAtLocation(mAnchorView, Gravity.NO_GRAVITY, x, y);
        }
    }

    public Rect locateView(View v) {
        if (v == null) return null;
        int[] loc_int = new int[2];
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }


    private int mDeviceWidth, mDeviceHeight;

    public void setHeightWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //API 13才允许使用新方法
        Point outSize = new Point();
        wm.getDefaultDisplay().getSize(outSize);
        if (outSize.x != 0) {
            mDeviceWidth = outSize.x;
        }
        if (outSize.y != 0) {
            mDeviceHeight = outSize.y;
        }
    }

    public interface OnItemSelected {
        void onSelected(int position, String text);
    }
}
