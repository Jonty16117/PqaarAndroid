package com.pqaar.app.unionAdmin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.LivePropRoutesListItem
import com.pqaar.app.utils.TimeConversions

class LivePropListAdapter(private var groupedItem: LivePropRoutesListItem) :
    RecyclerView.Adapter<LivePropListAdapter.ViewHolder>() {

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
                    layoutInflater.inflate(R.layout.mandi_admin_routes_history_item, parent, false)
                )
            }
            else -> {
                ViewHolder.ItemViewHolder(
                    layoutInflater.inflate(R.layout.mandi_admin_routes_history_sub_item,
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

    private fun onHeaderClicked() = View.OnClickListener {
        isExpanded = !isExpanded

        if (isExpanded) {
            notifyItemRangeInserted(1, groupedItem.Routes.size)
            notifyItemChanged(0)
        } else {
            notifyItemRangeRemoved(1, groupedItem.Routes.size)
            notifyItemChanged(0)
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
                header: LivePropRoutesListItem,
                onClickListener: View.OnClickListener,
            ) {
                prev_no.text = header.Status
                timestamp.text = TimeConversions.MillisToTimestamp(header.Timestamp)
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

            //Pair<String, Int>, represents each route in the LivePropRoutesListItem
            fun onBind(item: Pair<String, Int>, pos: Int) {
                godown.text = item.first
                req.text = item.second.toString()
                seqNo.text = pos.toString()
            }

        }

    }
}


