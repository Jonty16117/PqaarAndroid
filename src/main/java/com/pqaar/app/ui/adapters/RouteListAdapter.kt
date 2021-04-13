package com.pqaar.app.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.LiveRoutesListItem

class RouteListAdapter(private val activity: Activity, private val routeListItem: ArrayList<LiveRoutesListItem>) :
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
        fun setData(route: LiveRoutesListItem, position: Int) {
            /*routeItemView.findViewById<TextView>(R.id.textView9).text = (position + 1).toString()
            routeItemView.findViewById<ProgressBar>(R.id.progress).progress = ((route.got.toFloat()/route.req.toFloat())*100).toInt()
            routeItemView.findViewById<TextView>(R.id.textView22).text = route.got.toString() + "/" + route.req.toString()
            routeItemView.findViewById<TextView>(R.id.textView17).text = route.src
            routeItemView.findViewById<TextView>(R.id.textView18).text = route.des*/

            routeItemView.findViewById<TextView>(R.id.textView9).text = (position + 1).toString()
            routeItemView.findViewById<ProgressBar>(R.id.progress).progress = ((67/100).toInt())
            routeItemView.findViewById<TextView>(R.id.textView22).text = 67.toString() + "/" + 100.toString()
            routeItemView.findViewById<TextView>(R.id.textView17).text = "Abohar"
            routeItemView.findViewById<TextView>(R.id.textView18).text = "Markfed"
        }
    }
}