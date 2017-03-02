package com.winsun.fruitmix.refactor.contract;

import com.winsun.fruitmix.refactor.common.BasePresenter;
import com.winsun.fruitmix.refactor.common.BaseView;

/**
 * Created by Administrator on 2017/3/2.
 */

public interface NewPhotoSelectContract {

    interface NewPhotoSelectView extends BaseView{

        void finishActivity();

        void setTitle(String title);

        void setResult(int result);

    }

    interface NewPhotoSelectPresenter extends BasePresenter<NewPhotoSelectView>{

        void handleSelectFinished();

        void initView();

        void setTitle(String title);

    }

}
