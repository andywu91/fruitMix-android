package com.winsun.fruitmix.refactor.contract;

import android.content.Intent;
import android.view.View;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface MediaMainFragmentContract {

    interface MediaMainFragmentView {

        void resetBottomNavigationItemCheckState();

        void setBottomNavigationItemChecked(int position);

        void setViewPageCurrentItem(int position);

        void setTitleText(int resID);

        void setTitleText(String titleText);

        void setToolbarNavigationIcon(int resID);

        void setSelectModeBtnVisibility(int visibility);

        void showBottomNavAnim();

        void dismissBottomNavAnim();

        void setToolbarNavigationOnClickListener(View.OnClickListener listener);

        int getCurrentViewPageItem();

        boolean isResumed();

        boolean isHidden();

    }

    interface MediaMainFragmentPresenter extends BasePresenter<MediaMainFragmentView>{

        void onCreate();

        void onCreateView();

        void onResume();

        void setViewPageCurrentItem(int position);

        void selectModeBtnClick();

        void switchDrawerOpenState();

        void lockDrawer();

        void unlockDrawer();

        void onNavigationItemSelected(int itemID);

        void onPageSelected(int position);

        void setTitleText(int resID);

        void setTitleText(String titleText);

        void setToolbarNavigationIcon(int resID);

        void setSelectModeBtnVisibility(int visibility);

        void showBottomNavAnim();

        void dismissBottomNavAnim();

        void setToolbarNavigationOnClickListener(View.OnClickListener listener);

        boolean handleBackPressedOrNot();

        void onActivityReenter(int resultCode, Intent data);

        boolean isResumed();

        void onMapSharedElements(List<String> names, Map<String, View> sharedElements);
    }

}