package com.winsun.fruitmix.newdesign201804.equipment.add

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.util.Util
import kotlinx.android.synthetic.main.activity_add_equipment_by_ip.*

class AddEquipmentByIpActivity : BaseToolbarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStatusBarToolbarBgColor(R.color.new_design_primary_color)
        setToolbarWhiteStyle(toolbarViewModel)

        toolbarViewModel.navigationIconResId.set(R.drawable.white_clear)

        toolbarViewModel.showSelect.set(true)

        //TODO:add logic:ip format is wrong then selectTextEnable is false,otherwise is true

        toolbarViewModel.selectTextEnable.set(true)

        toolbarViewModel.selectTextResID.set(R.string.confirm)
        toolbarViewModel.selectTextColorResID.set(ContextCompat.getColor(this, R.color.eighty_seven_percent_white))

        toolbarViewModel.setToolbarSelectBtnOnClickListener {

            handleEnterIpAddressFinished()

        }

    }

    private fun handleEnterIpAddressFinished() {

        val ipEditOneNum = getIpEditNum(ip_edit_one)
        val ipEditTwoNum = getIpEditNum(ip_edit_two)
        val ipEditThreeNum = getIpEditNum(ip_edit_three)
        val ipEditFourNum = getIpEditNum(ip_edit_four)

        if (ipEditOneNum == null || ipEditTwoNum == null || ipEditThreeNum == null || ipEditFourNum == null)
            return

        val ip = "$ipEditOneNum.$ipEditTwoNum.$ipEditThreeNum.$ipEditFourNum"

        val portEdit = port_edit.text.toString()

        val portNum: Int = (if (!portEdit.isEmpty()) {
            getIpEditNum(port_edit)
        } else
            3001) ?: return

        val intent = Intent()
        intent.putExtra(IP_BY_MANUAL, ip)
        intent.putExtra(PORT_BY_MANUAL, portNum)

        setResult(IP_BY_MANUAL_ACTIVITY_RESULT_CODE, intent)
        finish()

    }

    private fun getIpEditNum(editText: EditText): Int? {

        val ipEditOne = editText.text.toString()

        val ipEditOneNum = ipEditOne.toIntOrNull()

        if (ipEditOneNum == null || ipEditOneNum < 0 || ipEditOneNum > 255) {
            erro_ip_hint.visibility = View.VISIBLE
        } else
            erro_ip_hint.visibility = View.INVISIBLE

        return ipEditOneNum
    }


    override fun generateContent(root: ViewGroup?): View {

        return LayoutInflater.from(this).inflate(R.layout.activity_add_equipment_by_ip, root, false)

    }


    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment_by_ip)
    }

}
