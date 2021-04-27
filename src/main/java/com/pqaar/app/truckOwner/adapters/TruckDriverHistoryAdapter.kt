package com.pqaar.app.truckOwner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.TruckHistory
import com.pqaar.app.utils.TimeConversions.MillisToTimestamp

class TruckDriverHistoryAdapter(
    private val context: Context,
    private val truckHistoryList: ArrayList<TruckHistory>,
) :
    RecyclerView.Adapter<TruckDriverHistoryAdapter.TruckHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TruckHistoryViewHolder {
        return TruckHistoryViewHolder(
            LayoutInflater.from(context)
                .inflate((R.layout.history_truck_item), parent,
                    false))
    }

    override fun onBindViewHolder(holder: TruckHistoryViewHolder, position: Int) {
        holder.setData(truckHistoryList[position], position)
    }

    override fun getItemCount(): Int {
        return truckHistoryList.size
    }

    inner class TruckHistoryViewHolder(private val truckItemView: View) :
        RecyclerView.ViewHolder(truckItemView) {

        fun setData(truckHistoryItem: TruckHistory, position: Int) {
            truckItemView.findViewById<TextView>(R.id.textView9).text = (position + 1).toString()
            truckItemView.findViewById<TextView>(R.id.textView7).text = truckHistoryItem.TruckNo
            truckItemView.findViewById<TextView>(R.id.textView12).text =
                truckHistoryItem.CurrentListNo
            truckItemView.findViewById<TextView>(R.id.textView17).text =
                truckHistoryItem.Route.first /*Source*/
            truckItemView.findViewById<TextView>(R.id.textView18).text =
                truckHistoryItem.Route.second /*Destination*/

            val date = MillisToTimestamp(truckHistoryItem.Timestamp.toLong()).split(" ")[0]
            val time = MillisToTimestamp(truckHistoryItem.Timestamp.toLong()).split(" ")[1]
            truckItemView.findViewById<TextView>(R.id.textView6).text = date
            truckItemView.findViewById<TextView>(R.id.textView30).text = time
//            val card = truckItemView.findViewById<CardView>(R.id.cardview)

            if (truckHistoryItem.Status == "DelPass") {
                /*card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green))*/
                truckItemView.findViewById<ImageView>(R.id.delPass).isVisible = true
                truckItemView.findViewById<ImageView>(R.id.delFail).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.delInProg).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.unassigned).isVisible = false
                truckItemView.findViewById<TextView>(R.id.textView34).text = "Del. Suc."
            } else if (truckHistoryItem.Status == "DelFail") {
                truckItemView.findViewById<ImageView>(R.id.delPass).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.delFail).isVisible = true
                truckItemView.findViewById<ImageView>(R.id.delInProg).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.unassigned).isVisible = false
                truckItemView.findViewById<TextView>(R.id.textView34).text = "Del. Fail"

            } else if (truckHistoryItem.Status == "DelInProg"){
                truckItemView.findViewById<ImageView>(R.id.delPass).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.delFail).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.delInProg).isVisible = true
                truckItemView.findViewById<ImageView>(R.id.unassigned).isVisible = false
                truckItemView.findViewById<TextView>(R.id.textView34).text = "Del. In Prog."
            } else {
                truckItemView.findViewById<ImageView>(R.id.delPass).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.delFail).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.delInProg).isVisible = false
                truckItemView.findViewById<ImageView>(R.id.unassigned).isVisible = true
                truckItemView.findViewById<TextView>(R.id.textView34).text = "_"
            }
        }
    }
}