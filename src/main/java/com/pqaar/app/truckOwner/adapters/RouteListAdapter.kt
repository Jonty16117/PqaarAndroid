 package com.pqaar.app.truckOwner.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.repository.MandiAdminRepo
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.TruckHistory
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis


class RouteListAdapter(
    private var liveRoutesListItem: LiveRoutesListItem,
    private var headerPos: Int,
    private var activity: Activity,
    private var fragment: Fragment,
) : RecyclerView.Adapter<RouteListAdapter.ViewHolder>(),
    AdapterView.OnItemSelectedListener {

    private lateinit var alertDialogBox: AlertDialog
    private lateinit var alertDialogMandi: TextView
    private lateinit var alertDialogGodown: TextView
    private lateinit var alertDialogYesBtn: Button
    private lateinit var alertDialogNoBtn: Button
    private lateinit var alertDialogSpinner: Spinner
    private val model: TruckOwnerViewModel
    private var liveTrucksInfo = ArrayList<TruckHistory>()
    private var liveAuctionList = ArrayList<LiveAuctionListItem>()
    private var truckSelected = "Select Truck"


    init {
        inflateAlertBoxDialog()

        model = ViewModelProviders.of(fragment)
            .get(TruckOwnerViewModel::class.java)

        model.getLiveTruckDataList().observe(fragment, {
            liveTrucksInfo = ArrayList()
            liveTrucksInfo.addAll(it.values)
        })

        model.getLiveAuctionList().observe(fragment, {
            liveAuctionList = ArrayList<LiveAuctionListItem>()
            liveAuctionList.addAll(it.values)
            liveAuctionList = ArrayList(liveAuctionList.sortedWith(compareBy {
                it.CurrNo!!.toInt()
            }))
        })
    }

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
                    layoutInflater.inflate(R.layout.live_route_list_header_item, parent, false)
                )
            }
            else -> {
                ViewHolder.ItemViewHolder(
                    layoutInflater.inflate(R.layout.live_route_list_sub_item,
                        parent,
                        false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.HeaderViewHolder -> {
                holder.onBind(liveRoutesListItem, onHeaderClicked(), headerPos)
            }
            is ViewHolder.ItemViewHolder -> {
                holder.onBind(liveRoutesListItem.Routes[position - 1],
                    onChildClicked(liveRoutesListItem, position - 1),
                    position)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isExpanded) {
            liveRoutesListItem.Routes.size + 1
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
            notifyItemRangeInserted(1, liveRoutesListItem.Routes.size)
            notifyItemChanged(0)
        } else {
            notifyItemRangeRemoved(1, liveRoutesListItem.Routes.size)
            notifyItemChanged(0)
        }
    }

    /**
     * Populate the alert dialog box with data and define actions on button click
     */
    private fun onChildClicked(liveRoutesListItem: LiveRoutesListItem, position: Int) =
        View.OnClickListener {
            alertDialogMandi.text = liveRoutesListItem.Mandi
            alertDialogGodown.text = liveRoutesListItem.Routes[position].Des

            val openTrucks = ArrayList<String>()
            liveTrucksInfo.forEach { truck ->
                if (
                    (truck.Status != "DelInProg") &&
                    (truck.CurrentListNo.toInt() <= liveAuctionList.size) &&
                    (truck.TruckNo == liveAuctionList[truck.CurrentListNo.toInt() - 1].TruckNo) &&
                    (liveAuctionList[truck.CurrentListNo.toInt() - 1].Closed == "false") &&
                    (liveAuctionList[truck.CurrentListNo.toInt() - 1].StartTime!! <= CurrDateTimeInMillis())
                )
                         {
                    openTrucks.add(truck.TruckNo)
                }
            }
            val spinnerAdapter = ArrayAdapter<String>(
                activity,
                android.R.layout.simple_spinner_item,
                openTrucks
            )
            spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
            alertDialogSpinner.adapter = spinnerAdapter
            alertDialogSpinner.onItemSelectedListener = this

            alertDialogYesBtn.setOnClickListener {
                //last check before sending the request bid request
                if(liveRoutesListItem.Routes[position].Got >=
                    liveRoutesListItem.Routes[position].Got) {
                    alertDialogBox.dismiss()
                    Snackbar.make(fragment.view!!, "All routes for this mandi are full!",
                        Snackbar.LENGTH_LONG).show()
                }

                GlobalScope.launch(Dispatchers.IO) {
                    val job = async {
                        model.bookRoute(
                            truckNo = truckSelected,
                            src = liveRoutesListItem.Mandi,
                            des = liveRoutesListItem.Routes[position].Des
                        )
                    }
                    job.await()
                }
                Snackbar.make(fragment.view!!, "Request to book truck: ${truckSelected} " +
                        "for route ${liveRoutesListItem.Mandi} to " +
                        "${liveRoutesListItem.Routes[position].Des} send successfully!",
                    Snackbar.LENGTH_LONG).show()
                alertDialogBox.dismiss()
            }
            alertDialogBox.show()

        }

    /**
     * Making alert dialog box
     */
    private fun inflateAlertBoxDialog() {
        val alertDialogBoxBuilder = AlertDialog.Builder(activity)
        val alertDialogBoxView = LayoutInflater.from(activity)
            .inflate(R.layout.close_bid_alert_dialog_box, null)
        alertDialogBox = alertDialogBoxBuilder.create()
        alertDialogBox.setCancelable(false)
        alertDialogBox.setView(alertDialogBoxView)

        alertDialogMandi = alertDialogBoxView.findViewById<TextView>(R.id.textView36)
        alertDialogGodown = alertDialogBoxView.findViewById<TextView>(R.id.textView26)
        alertDialogYesBtn = alertDialogBoxView.findViewById<Button>(R.id.button5)
        alertDialogNoBtn = alertDialogBoxView.findViewById<Button>(R.id.button6)
        alertDialogSpinner = alertDialogBoxView.findViewById<Spinner>(R.id.spinner)

        alertDialogNoBtn.setOnClickListener {
            alertDialogBox.dismiss()
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class HeaderViewHolder(itemView: View) : ViewHolder(itemView) {
            private val seqNo = itemView.findViewById<TextView>(R.id.textView23)
            private val mandi = itemView.findViewById<TextView>(R.id.mandi)
            private val totalRoutes = itemView.findViewById<TextView>(R.id.textView25)
            private val routesFilled = itemView.findViewById<TextView>(R.id.textView18)
            private val green_ind = itemView.findViewById<ImageView>(R.id.imageView6)
            private val red_ind = itemView.findViewById<ImageView>(R.id.imageView7)

            fun onBind(
                header: LiveRoutesListItem,
                onClickListener: View.OnClickListener,
                position: Int,
            ) {
                seqNo.text = (position + 1).toString()
                mandi.text = header.Mandi
                var countTotalRoutes = 0
                var countTotalRoutesFilled = 0

                header.Routes.forEach {
                    countTotalRoutes += it.Req
                    countTotalRoutesFilled += it.Got
                }

                totalRoutes.text = countTotalRoutes.toString()
                routesFilled.text = countTotalRoutesFilled.toString()

                if (countTotalRoutes == countTotalRoutesFilled) {
                    green_ind.isVisible = false
                    red_ind.isVisible = true
                } else {
                    green_ind.isVisible = true
                    red_ind.isVisible = false
                }

                itemView.setOnClickListener {
                    onClickListener.onClick(it)
                }
            }

        }

        class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
            private val seqNo = itemView.findViewById<TextView>(R.id.textView9)
            private val godown = itemView.findViewById<TextView>(R.id.godown)
            private val truckFilledInfo = itemView.findViewById<TextView>(R.id.textView18)
            private val rate = itemView.findViewById<TextView>(R.id.textView29)
            private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress)

            fun onBind(
                route: LiveRoutesListItem.RouteDestination,
                onClickListener: View.OnClickListener,
                pos: Int,
            ) {
                seqNo.text = pos.toString()
                godown.text = route.Des
                val progressText = "${route.Got}/${route.Req}"
                truckFilledInfo.text = progressText
                val rateText = "₹${route.Rate}"
                rate.text = rateText
                progressBar.progress = ((route.Got.toFloat() / route.Req.toFloat()) * 100).toInt()

                if (route.Got < route.Req) {
                    itemView.setOnClickListener {
                        onClickListener.onClick(it)
                    }
                }
            }

        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        truckSelected = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //do nothing
    }
}


/*
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

 */