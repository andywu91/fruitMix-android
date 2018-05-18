package com.winsun.fruitmix.newdesign201804.equipment.model

import android.content.Context
import com.winsun.fruitmix.R
import com.winsun.fruitmix.newdesign201804.equipment.abnormal.data.DiskItemInfo
import com.winsun.fruitmix.newdesign201804.equipment.add.data.DiskMode
import com.winsun.fruitmix.user.User

abstract class BaseEquipmentItem(val ip: String, val name: String, val uuid: String) {

    abstract fun getEquipmentTypeStr(context: Context): String
    abstract fun getEquipmentTypeIconId(): Int

}

class CloudConnectEquipItem(name: String, uuid: String, ip: String) : BaseEquipmentItem(ip, name, uuid) {

    override fun getEquipmentTypeStr(context: Context): String {
        return ""
    }

    override fun getEquipmentTypeIconId(): Int {
        return R.drawable.cloud_connected
    }

}

class CloudUnConnectedEquipmentItem(val loginUser: User, val type: String, name: String, uuid: String, ip: String) : BaseEquipmentItem(ip, name, uuid) {

    override fun getEquipmentTypeIconId(): Int {
        return R.drawable.cloud_unconnected
    }

    override fun getEquipmentTypeStr(context: Context): String {
        return context.getString(R.string.login_lan)
    }

}

class PowerOffEquipmentItem(name: String, uuid: String, ip: String = "") : BaseEquipmentItem(ip, name, uuid) {

    override fun getEquipmentTypeIconId(): Int {
        return R.drawable.cloud_unconnected
    }

    override fun getEquipmentTypeStr(context: Context): String {
        return context.getString(R.string.shutdown)
    }

}

class OfflineEquipmentItem(name: String, uuid: String, ip: String = "") : BaseEquipmentItem(ip, name, uuid) {

    override fun getEquipmentTypeIconId(): Int {
        return R.drawable.cloud_unconnected
    }

    override fun getEquipmentTypeStr(context: Context): String {
        return context.getString(R.string.station_offline)
    }

}

class UnderReviewEquipmentItem(name: String, uuid: String, ip: String = "") : BaseEquipmentItem(ip, name, uuid) {

    override fun getEquipmentTypeIconId(): Int {
        return R.drawable.under_review
    }

    override fun getEquipmentTypeStr(context: Context): String {
        return context.getString(R.string.under_review)
    }

}

class DiskAbnormalEquipmentItem(name: String, uuid: String, val diskMode: DiskMode,
                                val diskItemInfos: MutableList<DiskItemInfo>, ip: String = "") : BaseEquipmentItem(ip, name, uuid) {

    override fun getEquipmentTypeIconId(): Int {
        return R.drawable.disk_warning_corner
    }

    override fun getEquipmentTypeStr(context: Context): String {
        return context.getString(R.string.disk_abnormal)
    }


}







