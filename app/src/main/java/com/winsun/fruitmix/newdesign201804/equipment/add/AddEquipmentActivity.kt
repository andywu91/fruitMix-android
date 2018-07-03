package com.winsun.fruitmix.newdesign201804.equipment.add

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.winsun.fruitmix.BaseToolbarActivity
import com.winsun.fruitmix.R
import com.winsun.fruitmix.command.BaseAbstractCommand
import com.winsun.fruitmix.dialog.BottomMenuListDialogFactory
import com.winsun.fruitmix.equipment.search.data.Equipment
import com.winsun.fruitmix.equipment.search.data.EquipmentSearchManager
import com.winsun.fruitmix.equipment.search.data.InjectEquipment
import com.winsun.fruitmix.model.BottomMenuItem
import com.winsun.fruitmix.newdesign201804.equipment.add.data.BaseNewEquipmentInfo
import com.winsun.fruitmix.newdesign201804.equipment.add.data.FakeEquipmentSearchManger
import com.winsun.fruitmix.newdesign201804.equipment.add.data.FakeNewEquipmentInfoDataSource
import com.winsun.fruitmix.newdesign201804.equipment.add.data.InjectNewEquipmentInfoDataSource
import com.winsun.fruitmix.newdesign201804.equipment.list.data.InjectEquipmentItemDataSource
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.EQUIPMENT_IP_KEY
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.EQUIPMENT_NAME_KEY
import com.winsun.fruitmix.newdesign201804.equipment.reinitialization.ReinitializationActivity
import kotlinx.android.synthetic.main.activity_add_equipment.*

const val REINITIALIZATION_ACTIVITY_REQUEST_CODE = 0x1002
const val FINISH_REINITIALIZATION_RESULT_CODE = 0x1003

const val IP_BY_MANUAL_ACTIVITY_REQUEST_CODE = 0x1004
const val IP_BY_MANUAL_ACTIVITY_RESULT_CODE = 0x1005

const val IP_BY_MANUAL = "ip_by_manual"
const val PORT_BY_MANUAL = "port_by_manual"


class AddEquipmentActivity : BaseToolbarActivity(), SearchEquipmentUIState, EquipmentUIState, AddEquipmentView {

    private lateinit var addEquipmentPresenter: AddEquipmentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbarWhiteStyle(toolbarViewModel)
        setStatusBarToolbarBgColor(R.color.new_design_primary_color)

        toolbarViewModel.showMenu.set(true)
        toolbarViewModel.menuResID.set(R.drawable.more_icon_white)

        toolbarViewModel.setToolbarMenuBtnOnClickListener {

            showEquipmentMenu()

        }

        addEquipmentPresenter = AddEquipmentPresenter(InjectEquipment.provideEquipmentSearchManager(this),
                this, this, this, InjectNewEquipmentInfoDataSource.inject(this),
                InjectEquipmentItemDataSource.inject(this))

        new_equipment_viewPager.adapter = addEquipmentPresenter.getViewPagerAdapter()

        viewpager_indicator.setViewPager(new_equipment_viewPager)

        new_equipment_viewPager.adapter?.registerDataSetObserver(viewpager_indicator.dataSetObserver)

        new_equipment_viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                addEquipmentPresenter.onPageSelect(position)

            }

        })

        operate_btn.setOnClickListener {

            addEquipmentPresenter.operateBtnOnClick(this, operate_btn)

        }

        search_progressbar.indeterminateDrawable.setColorFilter(
                ContextCompat.getColor(this, R.color.new_design_progressbar_color),
                android.graphics.PorterDuff.Mode.SRC_IN)

        addEquipmentPresenter.startSearchState()

    }

    override fun onDestroy() {
        super.onDestroy()

        addEquipmentPresenter.onDestroy()
    }

    override fun generateContent(root: ViewGroup?): View {

        return LayoutInflater.from(this).inflate(R.layout.activity_add_equipment, root, false)

    }

    override fun getToolbarTitle(): String {
        return getString(R.string.add_equipment)
    }

    override fun startSearchState() {

        add_equipment_title.setText(R.string.searching_equipment)

        search_progressbar.visibility = View.VISIBLE

        refresh_layout.visibility = View.INVISIBLE
        viewpager_indicator.visibility = View.INVISIBLE

        new_equipment_viewPager.visibility = View.INVISIBLE
        new_equipment_viewPager.currentItem = 0

        operate_btn.visibility = View.INVISIBLE

    }

    override fun searchTimeoutState(showEquipmentViewPager: Boolean) {

        search_progressbar.visibility = View.INVISIBLE

        if (showEquipmentViewPager) {

            new_equipment_viewPager.visibility = View.VISIBLE
            viewpager_indicator.visibility = View.VISIBLE

            refresh_layout.visibility = View.INVISIBLE

            operate_btn.visibility = View.VISIBLE

        } else {

            add_equipment_title.setText(R.string.undiscovered_equipment)

            new_equipment_viewPager.visibility = View.INVISIBLE
            viewpager_indicator.visibility = View.INVISIBLE

            refresh_layout.visibility = View.VISIBLE

            refresh_layout.setOnClickListener {

                addEquipmentPresenter.startSearchState()

            }

            operate_btn.visibility = View.INVISIBLE

        }

    }

    override fun searchSucceedState() {

//        if (new_equipment_viewPager.currentItem == 0)
//            addEquipmentPresenter.onPageSelect(0)

        addEquipmentPresenter.onPageSelect(new_equipment_viewPager.currentItem)

        searchTimeoutState(true)

    }

    override fun useExistDiskData() {

        operate_btn.setBackgroundResource(R.drawable.green_btn_bg)

        operate_btn.setText(R.string.use_exist_disk_data)
        operate_btn.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white))

    }

    override fun selectDiskBeforeUseExistDiskData() {

        operate_btn.setBackgroundResource(R.drawable.white_btn_bg)

        operate_btn.setText(R.string.use_exist_disk_data)
        operate_btn.setTextColor(ContextCompat.getColor(this, R.color.twenty_six_percent_black))

    }

    override fun addAvailableEquipment() {

        operate_btn.setBackgroundResource(R.drawable.green_btn_bg)

        operate_btn.setText(R.string.add_immediately)
        operate_btn.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white))

    }

    override fun reinitialization() {

        operate_btn.setBackgroundResource(R.drawable.green_btn_bg)

        operate_btn.setText(R.string.next_step)
        operate_btn.setTextColor(ContextCompat.getColor(this, R.color.eighty_seven_percent_white))

    }

    override fun refreshStationName(stationName: String) {
        add_equipment_title.text = stationName
    }

    private fun showEquipmentMenu() {

        val bottomMenuItems = mutableListOf<BottomMenuItem>()

        if (addEquipmentPresenter.getItemSize() > 0) {

            bottomMenuItems.add(BottomMenuItem(0, getString(R.string.refresh), object : BaseAbstractCommand() {

                override fun execute() {
                    super.execute()

                    addEquipmentPresenter.startSearchState()
                }

            }))

        }

        bottomMenuItems.add(BottomMenuItem(0, getString(R.string.add_equipment_manually), object : BaseAbstractCommand() {

            override fun execute() {

            }

        }))

        bottomMenuItems.add(BottomMenuItem(0, getString(R.string.add_equipment_by_ip), object : BaseAbstractCommand() {
            override fun execute() {
                enterAddEquipmentByIp()
            }

        }))

        BottomMenuListDialogFactory(bottomMenuItems).createDialog(this).show()

    }

    private fun enterAddEquipmentByIp() {

        val intent = Intent(this, AddEquipmentByIpActivity::class.java)

        startActivityForResult(intent, IP_BY_MANUAL_ACTIVITY_REQUEST_CODE)
    }

    override fun enterReinitialization(baseNewEquipmentInfo: BaseNewEquipmentInfo) {

        val intent = Intent(this, ReinitializationActivity::class.java)
        intent.putExtra(EQUIPMENT_NAME_KEY, baseNewEquipmentInfo.equipmentName)
        intent.putExtra(EQUIPMENT_IP_KEY, baseNewEquipmentInfo.equipmentIP)

        startActivityForResult(intent, REINITIALIZATION_ACTIVITY_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REINITIALIZATION_ACTIVITY_REQUEST_CODE && resultCode == FINISH_REINITIALIZATION_RESULT_CODE)
            finish()
        else if (requestCode == IP_BY_MANUAL_ACTIVITY_REQUEST_CODE && resultCode == IP_BY_MANUAL_ACTIVITY_RESULT_CODE) {

            val ip = data?.getStringExtra(IP_BY_MANUAL)
            val port = data?.getIntExtra(PORT_BY_MANUAL, 3001)

            val equipment = Equipment("", listOf(ip), port!!)

            addEquipmentPresenter.handleIpByManual(equipment)

        }

    }

}
