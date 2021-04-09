package com.pqaar.app.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.internal.ContextUtils.getActivity
import com.pqaar.app.R
import com.pqaar.app.model.Bid

class BidListAdapter(private val activity: Activity, private val bidListItem: ArrayList<Bid>) :
    RecyclerView.Adapter<BidListAdapter.BidListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BidListViewHolder {
        return BidListViewHolder(
            activity.layoutInflater.inflate((R.layout.bid_item), parent,
                false))
    }

    override fun onBindViewHolder(holder: BidListViewHolder, position: Int) {
        holder.setData(bidListItem[position], position)
    }

    override fun getItemCount(): Int {
        return bidListItem.size
    }

    inner class BidListViewHolder(private val bidItemView: View) : RecyclerView.ViewHolder(bidItemView) {

        @SuppressLint("SetTextI18n")
        fun setData(bid: Bid, position: Int) {
            bidItemView.findViewById<TextView>(R.id.textView9).text = (position + 1).toString()
            bidItemView.findViewById<TextView>(R.id.truck_no).text = bid.truckNo
            bidItemView.findViewById<TextView>(R.id.textView17).text = bid.pickUpLocation
            bidItemView.findViewById<TextView>(R.id.textView18).text = bid.dropLocation
            bidItemView.findViewById<TextView>(R.id.prev_no).text = bid.prevNumber.toString()

            val card = bidItemView.findViewById<CardView>(R.id.cardview)

            //a = accepted
            //r = rejected
            //o = open
            if (bid.isUserNumber) {
                when (bid.bidStatus) {
                    'a' -> {
                        card.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.green))
                        bidItemView.findViewById<ImageView>(R.id.accepted).isVisible = true
                        bidItemView.findViewById<ImageView>(R.id.rejected).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.open).isVisible = false
                    }
                    'r' -> {
                        bidItemView.findViewById<CardView>(R.id.cardview)
                            .setCardBackgroundColor(ContextCompat.getColor(activity, R.color.red))
                        bidItemView.findViewById<ImageView>(R.id.accepted).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.rejected).isVisible = true
                        bidItemView.findViewById<ImageView>(R.id.open).isVisible = false
                    }
                    else -> {
                        bidItemView.findViewById<CardView>(R.id.cardview)
                            .setCardBackgroundColor(ContextCompat.getColor(activity, R.color.yellow))
                        bidItemView.findViewById<ImageView>(R.id.accepted).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.rejected).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.open).isVisible = true
                    }
                }
            }
            else {
                when (bid.bidStatus) {
                    'a' -> {
                        bidItemView.findViewById<CardView>(R.id.cardview)
                            .setCardBackgroundColor(ContextCompat.getColor(activity, R.color.lightBlue))
                        bidItemView.findViewById<ImageView>(R.id.accepted).isVisible = true
                        bidItemView.findViewById<ImageView>(R.id.rejected).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.open).isVisible = false
                    }
                    'r' -> {
                        bidItemView.findViewById<CardView>(R.id.cardview)
                            .setCardBackgroundColor(ContextCompat.getColor(activity, R.color.lightRed))
                        bidItemView.findViewById<ImageView>(R.id.accepted).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.rejected).isVisible = true
                        bidItemView.findViewById<ImageView>(R.id.open).isVisible = false
                    }
                    else -> {
                        bidItemView.findViewById<CardView>(R.id.cardview)
                            .setCardBackgroundColor(ContextCompat.getColor(activity, R.color.lightPurple))
                        bidItemView.findViewById<ImageView>(R.id.accepted).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.rejected).isVisible = false
                        bidItemView.findViewById<ImageView>(R.id.open).isVisible = true
                    }
                }
            }
        }
    }
}