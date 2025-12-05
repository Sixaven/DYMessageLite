package com.example.dymessagelite.common.tracker

object AppStateTracker {
    enum class CurrentActivity{
        NONE,
        MESSAGE_LIST,
        MESSAGE_DETAIL
    }

    private var curActivity = CurrentActivity.NONE;
    private var curDetailSenderId:String? = null;

    fun onActivityResumed(activityName: CurrentActivity,senderId: String? = null){
        curActivity = activityName;
        if(activityName == CurrentActivity.MESSAGE_DETAIL){
            curDetailSenderId = senderId
        }else{
            curDetailSenderId = null;
        }
    }

    fun onActivityPaused(){
        curDetailSenderId = null;
        curActivity = CurrentActivity.NONE;
    }

    fun getCurActivity(): CurrentActivity{
        return curActivity;
    }

    fun getCurDetailSenderId(): String?{
        return curDetailSenderId;
    }
}