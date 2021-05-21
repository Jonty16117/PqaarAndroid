package com.pqaar.app.truckOwner.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.Bid
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import com.pqaar.app.utils.TimeConversions.MillisToTimestamp

class AuctionListAdapter(
    private var liveAuctionList: ArrayList<LiveAuctionListItem>) :
    RecyclerView.Adapter<AuctionListAdapter.ViewHolder>() {

    private val TAG = "AuctionListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder.HeaderViewHolder(
            layoutInflater.inflate(R.layout.live_auction_item,
                parent,
                false)
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        (holder as ViewHolder.HeaderViewHolder)
            .setData(liveAuctionList[position], position)
    }

    override fun getItemCount(): Int {
        return liveAuctionList.size
    }

    fun updateList(updatedLiveAuctionList: ArrayList<LiveAuctionListItem>) {
        liveAuctionList = ArrayList()
        liveAuctionList.addAll(updatedLiveAuctionList)
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
            private val TAG = "AuctionListAdapterViewHolder"

            private val textView9 = itemView.findViewById<TextView>(R.id.textView9)
            private val truck_no = itemView.findViewById<TextView>(R.id.truck_no)
            private val textView17 = itemView.findViewById<TextView>(R.id.textView17)
            private val textView18 = itemView.findViewById<TextView>(R.id.textView18)
            private val prev_no = itemView.findViewById<TextView>(R.id.prev_no)
            private val status = itemView.findViewById<TextView>(R.id.textView6)
            private val textViewTimer = itemView.findViewById<TextView>(R.id.textView11)
            private val locked = itemView.findViewById<ImageView>(R.id.locked)
            private val unlocked = itemView.findViewById<ImageView>(R.id.unlocked)
            private val nextUnlock = itemView.findViewById<ImageView>(R.id.next_unlock)
            private val accepted = itemView.findViewById<ImageView>(R.id.accepted)

           @SuppressLint("SetTextI18n")
            fun setData(liveAuctionListItem: LiveAuctionListItem, position: Int) {
                textView9.text = (position + 1).toString()
                truck_no.text = liveAuctionListItem.TruckNo
                textView17.text = if (liveAuctionListItem.Src!!.trim().length == 0) "_" else liveAuctionListItem.Src
                textView18.text = if (liveAuctionListItem.Des!!.trim().length == 0) "_" else liveAuctionListItem.Des
                prev_no.text = if (liveAuctionListItem.PrevNo!!.trim().length == 0) "_" else liveAuctionListItem.PrevNo

                if(liveAuctionListItem.Closed!!.trim() == "true") {
                    status.text = "Booked"
                    accepted.isVisible = true
                    unlocked.isVisible = false
                    locked.isVisible = false
                    nextUnlock.isVisible = false
                    textViewTimer.isVisible = false
                } else {

//                    currtime = 1621562101000
//                    startime = 1621601280000
                    //if the current item is unlocked
                    Log.d(TAG, "curr time: ${MillisToTimestamp(CurrDateTimeInMillis())}, " +
                            "truck start time: ${MillisToTimestamp(liveAuctionListItem.StartTime!!)}")
                    if(CurrDateTimeInMillis() >= liveAuctionListItem.StartTime!!) {
                        Log.d(TAG, "Unlocked")
                        status.text = "Unlocked"
                        unlocked.isVisible = true
                        locked.isVisible = false
                        nextUnlock.isVisible = false
                        accepted.isVisible = false
                        textViewTimer.isVisible = false
                    } else {
                        Log.d(TAG, "Locked")

                        status.text = "Locked"
                        locked.isVisible = true
                        unlocked.isVisible = false
                        nextUnlock.isVisible = false
                        accepted.isVisible = false
                        textViewTimer.isVisible = true


                        var min: Long
                        var sec: Long
                        Log.d(TAG, "StartTime of ${liveAuctionListItem.TruckNo}: ${liveAuctionListItem.StartTime}")
                        Log.d(TAG, "Timer of ${liveAuctionListItem.StartTime!! -CurrDateTimeInMillis()}")
                        Log.d(TAG, "curr time in milis ${CurrDateTimeInMillis()}")
                        val timer = object : CountDownTimer(
                            (liveAuctionListItem.StartTime!! - CurrDateTimeInMillis()),
                            1000) {
                            override fun onTick(timeLeft: Long) {
                                min = (timeLeft/(1000*60))%60
                                sec = (timeLeft/1000)%60
                                textViewTimer.text = "${min.toString().padStart(2, '0')}:" +
                                        sec.toString().padStart(2, '0')
                            }
//                            1621601320000
                            override fun onFinish() {
                                Log.d(TAG, "unlocked ${liveAuctionListItem.TruckNo}")
                                status.text = "Unlocked"
                                unlocked.isVisible = true
                                locked.isVisible = false
                                nextUnlock.isVisible = false
                                accepted.isVisible = false
                                textViewTimer.isVisible = false
                            }
                        }
                        timer.start()
                    }

                }
            }

        }


    }
}