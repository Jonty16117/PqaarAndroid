package com.pqaar.app.pahunchAdmin.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.PahunchTicket
import com.pqaar.app.utils.TimeConversions

@SuppressLint("SetTextI18n")
class PahunchHistoryAdapter(
    private val context: Context,
    private val pahunchHistory: ArrayList<PahunchTicket>,
) :
    RecyclerView.Adapter<PahunchHistoryAdapter.PahunchHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PahunchHistoryViewHolder {
        return PahunchHistoryViewHolder(
            LayoutInflater.from(context)
                .inflate((R.layout.pahunch_history_item), parent,
                    false))
    }

    override fun onBindViewHolder(holder: PahunchHistoryViewHolder, position: Int) {
        holder.setData(pahunchHistory[position], position)
    }

    override fun getItemCount(): Int {
        return pahunchHistory.size
    }

    inner class PahunchHistoryViewHolder(private val truckItemView: View) :
        RecyclerView.ViewHolder(truckItemView) {

        fun setData(
            pahunchHistory: PahunchTicket,
            position: Int,
        ) {
            val seqNo = truckItemView.findViewById<TextView>(R.id.truck_no)
            val truckNo = truckItemView.findViewById<TextView>(R.id.textView3)
            val date = truckItemView.findViewById<TextView>(R.id.textView43)
            val time = truckItemView.findViewById<TextView>(R.id.textView44)
            val mandi = truckItemView.findViewById<TextView>(R.id.textView45)
            val status = truckItemView.findViewById<TextView>(R.id.textView46)
            val delInfo = truckItemView.findViewById<TextView>(R.id.textView51)
            val delAcceptedIcon = truckItemView.findViewById<ImageView>(R.id.imageView14)
            val delRejectedIcon = truckItemView.findViewById<ImageView>(R.id.imageView12)

            seqNo.text = (position + 1).toString()
            truckNo.text = pahunchHistory.TruckNo
            val dateTime = TimeConversions.MillisToTimestamp(pahunchHistory.Timestamp).split(" ")
            date.text = dateTime[0]
            time.text = dateTime[1]
            mandi.text = pahunchHistory.Source
            delInfo.text = pahunchHistory.DeliveryInfo
//            Log.d(TAG, "mandi: ${pahunchHistory.Source}")

            when (pahunchHistory.Status) {
                "Accepted" -> {
                    status.text = "Delivery Accepted"
                    delAcceptedIcon.isVisible = true
                    delRejectedIcon.isVisible = false
                }
                "Rejected" -> {
                    status.text = "Delivery Rejected"
                    delAcceptedIcon.isVisible = false
                    delRejectedIcon.isVisible = true
                }
            }

            /*itemView.setOnClickListener {
                onClickListener.onClick(it)
            }*/
        }
    }

    /*override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //do nothing
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //do nothing
    }*/
}