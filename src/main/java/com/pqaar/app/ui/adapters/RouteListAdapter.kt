package com.pqaar.app.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.Route

class RouteListAdapter(private val activity: Activity, private val routeListItem: ArrayList<Route>) :
    RecyclerView.Adapter<RouteListAdapter.RouteListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteListViewHolder {
        return RouteListViewHolder(
            activity.layoutInflater.inflate((R.layout.route_item), parent,
                false))
    }

    override fun onBindViewHolder(holder: RouteListViewHolder, position: Int) {
        holder.setData(routeListItem[position], position)
    }

    override fun getItemCount(): Int {
        return routeListItem.size
    }

    inner class RouteListViewHolder(private val routeItemView: View) : RecyclerView.ViewHolder(routeItemView) {

        @SuppressLint("SetTextI18n")
        fun setData(route: Route, position: Int) {
            routeItemView.findViewById<TextView>(R.id.textView9).text = (position + 1).toString()
            routeItemView.findViewById<ProgressBar>(R.id.progress).progress = ((route.trucksProvided.toFloat()/route.trucksNeeded)*100).toInt()
            routeItemView.findViewById<TextView>(R.id.textView22).text = route.trucksProvided.toString() + "/" + route.trucksNeeded.toString()
            routeItemView.findViewById<TextView>(R.id.textView17).text = route.pickUpLocation
            routeItemView.findViewById<TextView>(R.id.textView18).text = route.dropLocation
        }
    }
}