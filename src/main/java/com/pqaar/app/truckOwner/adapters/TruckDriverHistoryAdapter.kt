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

class TruckDriverHistoryAdapter(private val context: Context, private val truckHistoryList: ArrayList<TruckHistory>) :
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

    inner class TruckHistoryViewHolder(private val truckItemView: View) : RecyclerView.ViewHolder(truckItemView) {

        fun setData(truckHistoryItem: TruckHistory, position: Int) {
            truckItemView.findViewById<TextView>(R.id.textView9).text = (position + 1).toString()
            truckItemView.findViewById<TextView>(R.id.textView7).text = truckHistoryItem.truckNumber
            truckItemView.findViewById<TextView>(R.id.textView17).text = truckHistoryItem.pickUpLocation
            truckItemView.findViewById<TextView>(R.id.textView6).text = truckHistoryItem.pickUpDate
            truckItemView.findViewById<TextView>(R.id.textView22).text = truckHistoryItem.pickUpTime
            truckItemView.findViewById<TextView>(R.id.textView18).text = truckHistoryItem.dropLocation
            truckItemView.findViewById<TextView>(R.id.textView12).text = truckHistoryItem.listNumber.toString()

            val card = truckItemView.findViewById<CardView>(R.id.cardview)

            /*when (truckHistoryItem.deliveryStatus) {
                'a' -> card
                        .setCardBackgroundColor(ContextCompat.getColor(context,R.color.green))
                'r' -> card
                        .setCardBackgroundColor(ContextCompat.getColor(context,R.color.red))
                'p' -> card
                        .setCardBackgroundColor(ContextCompat.getColor(context,R.color.white))
            }*/

            when (truckHistoryItem.deliveryStatus) {
                'a' -> {
                    card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green))
                    truckItemView.findViewById<ImageView>(R.id.accepted).isVisible = true
                    truckItemView.findViewById<ImageView>(R.id.rejected).isVisible = false
                }
                else -> {
                    truckItemView.findViewById<CardView>(R.id.cardview)
                        .setCardBackgroundColor(ContextCompat.getColor(context, R.color.red))
                    truckItemView.findViewById<ImageView>(R.id.accepted).isVisible = false
                    truckItemView.findViewById<ImageView>(R.id.rejected).isVisible = true
                }
            }
        }
    }
}