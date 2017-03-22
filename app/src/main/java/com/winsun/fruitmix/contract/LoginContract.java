package com.winsun.fruitmix.contract;

import android.content.DialogInterface;

import com.winsun.fruitmix.common.BasePresenter;
import com.winsun.fruitmix.common.BaseView;
import com.winsun.fruitmix.model.operationResult.OperationResult;

/**
 * Created by Administrator on 2017/2/4.
 */

public interface LoginContract {

    interface LoginView extends BaseView {

        void setEquipmentGroupNameText(String equipmentGroupNameText);

        void setEquipmentChildNameText(String equipmentChildNameText);

        void setUserDefaultPortraitText(String userDefaultPortraitText);

        void setUserDefaultPortraitBgColor(int userDefaultPortraitBgColor);

        void handleLoginSucceed();

        void handleLoginFail(OperationResult result);

        void finishActivity();

        void hidePwdEditHint();

        void showPwdEditHint();

        void showAlertDialog(int messageResID, int positiveBtnResID, int negativeBtnResID, DialogInterface.OnClickListener positiveBtnOnClickListener,DialogInterface.OnClickListener negativeBtnOnClickListener);
    }

    interface LoginPresenter extends BasePresenter<LoginView> {

        void showEquipmentAndUser();

        void login(String userPassword);

        void onFocusChange(boolean hasFocus);
    }

}
