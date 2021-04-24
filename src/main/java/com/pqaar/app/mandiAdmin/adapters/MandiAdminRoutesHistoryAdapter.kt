package com.pqaar.app.mandiAdmin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.MandiRoutesHistoryItem
import com.pqaar.app.utils.TimeConversions.MillisToTimestamp

class MandiAdminRoutesHistoryAdapter(private var groupedItem: MandiRoutesHistoryItem) :
    RecyclerView.Adapter<MandiAdminRoutesHistoryAdapter.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    private var isExpanded: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                ViewHolder.HeaderViewHolder(
                    layoutInflater.inflate(R.layout.madni_admin_routes_history_item, parent, false)
                )
            }
            else -> {
                ViewHolder.ItemViewHolder(
                    layoutInflater.inflate(R.layout.madni_admin_routes_history_sub_item,
                        parent,
                        false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.HeaderViewHolder -> {
                holder.onBind(groupedItem, onHeaderClicked())
            }
            is ViewHolder.ItemViewHolder -> {
                holder.onBind(groupedItem.Routes[position - 1], position)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isExpanded) {
            groupedItem.Routes.size + 1
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    fun setItems(newData: MandiRoutesHistoryItem) {
        groupedItem = newData
    }

    private fun onHeaderClicked() = object : View.OnClickListener {
        override fun onClick(view: View?) {
            isExpanded = !isExpanded

            if (isExpanded) {
                notifyItemRangeInserted(1, groupedItem.Routes.size)
                notifyItemChanged(0)
            } else {
                notifyItemRangeRemoved(1, groupedItem.Routes.size)
                notifyItemChanged(0)
            }
        }

    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
            // private val textView9 = itemView.findViewById<TextView>(R.id.textView9)
            private val prev_no = itemView.findViewById<TextView>(R.id.prev_no)
            private val textView18 = itemView.findViewById<TextView>(R.id.textView18)
            private val timestamp = itemView.findViewById<TextView>(R.id.textView23)
            private val green_ind = itemView.findViewById<ImageView>(R.id.imageView6)
            private val red_ind = itemView.findViewById<ImageView>(R.id.imageView7)

            fun onBind(
                header: MandiRoutesHistoryItem,
                onClickListener: View.OnClickListener,
            ) {
                prev_no.text = header.Status
                timestamp.text = MillisToTimestamp(header.Timestamp)
                textView18.text = header.Routes.size.toString()

                when (header.Status) {
                    "Live" -> {
                        green_ind.isVisible = true
                        red_ind.isVisible = false
                    }
                    else -> {
                        green_ind.isVisible = false
                        red_ind.isVisible = true
                    }
                }

                itemView.setOnClickListener {
                    onClickListener.onClick(it)
                }
            }

        }

        class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
            private val godown = itemView.findViewById<TextView>(R.id.godown)
            private val req = itemView.findViewById<TextView>(R.id.textView18)
            private val seqNo = itemView.findViewById<TextView>(R.id.textView9)

            //Pair<String, Int>, represents each route in the MandiRoutesHistoryItem
            fun onBind(item: Pair<String, Int>, pos: Int) {
                godown.text = item.first
                req.text = item.second.toString()
                seqNo.text = pos.toString()
            }

        }

    }
}


/*
@SuppressLint("InflateParams")
class MandiAdminRoutesHistoryAdapter(private val context: Context, private val routesHistory: ArrayList<MandiRoutesHistoryItem>) :
    BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return routesHistory.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return routesHistory[groupPosition].Routes.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return routesHistory[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return routesHistory[groupPosition].Routes[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate((R.layout.madni_admin_routes_history_item), parent,
                    false)
        }
        val prev_no = view!!.findViewById<TextView>(R.id.prev_no)
        val textView9 = view.findViewById<TextView>(R.id.textView9)
        val textView18 = view.findViewById<TextView>(R.id.textView18)

        textView9.text = groupPosition.toString()
        prev_no.text = routesHistory[groupPosition].Status
        textView18.text = routesHistory[groupPosition].Routes.size.toString()

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?,
    ): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate((R.layout.madni_admin_routes_history_sub_item), parent,
                    false)
        }

        val godown = view!!.findViewById<TextView>(R.id.godown)
        val textView18 = view.findViewById<TextView>(R.id.textView18)

        godown.text = routesHistory[groupPosition].Routes[childPosition].first
        textView18.text = routesHistory[groupPosition].Routes[childPosition].second.toString()

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
*/