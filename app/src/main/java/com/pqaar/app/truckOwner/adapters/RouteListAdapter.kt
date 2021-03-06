package com.pqaar.app.truckOwner.adapters

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pqaar.app.R
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import kotlinx.coroutines.*


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
    private var liveTrucksInfo = ArrayList<LiveTruckDataItem>()
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
                holder.onBind(
                    liveRoutesListItem.Routes[position - 1],
                    onChildClicked(liveRoutesListItem, position - 1),
                    position,
                    model.getLiveAuctionTimestamps().value!!.first,
                    model.getLiveAuctionTimestamps().value!!.second,
                    model.getAuctionsBonusTimeInfo().value!!.StartTime,
                    model.getAuctionsBonusTimeInfo().value!!.EndTime
                )
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
                ) {
                    openTrucks.add(truck.TruckNo)
                }
            }
            val spinnerAdapter = ArrayAdapter<String>(
                activity,
                android.R.layout.simple_spinner_item,
                openTrucks
            )
            spinnerAdapter.setDropDownViewResource(
                R.layout.custom_spinner_item
            )
            alertDialogSpinner.adapter = spinnerAdapter
            alertDialogSpinner.onItemSelectedListener = this

            alertDialogYesBtn.setOnClickListener {
                //last check before sending the request bid request
                val isAfterAuctionStartTime =
                    CurrDateTimeInMillis() >= model.getLiveAuctionTimestamps().value!!.first
                val isBeforeAuctionEndTime =
                    CurrDateTimeInMillis() < model.getLiveAuctionTimestamps().value!!.second
                val isInMainAuctionTime = isAfterAuctionStartTime && isBeforeAuctionEndTime
                val isRouteAvailable =
                    liveRoutesListItem.Routes[position].Got < liveRoutesListItem.Routes[position].Got
                val isEligibleUnderBonusTime =
                    (CurrDateTimeInMillis() >= model.getAuctionsBonusTimeInfo().value!!.StartTime) &&
                            (CurrDateTimeInMillis() < model.getAuctionsBonusTimeInfo().value!!.EndTime)

                if ((isInMainAuctionTime || isEligibleUnderBonusTime) && isRouteAvailable) {
                    val acceptText = "Truck book failed, please check routes and auction time!"
                    showSnackbar(acceptText,
                        ContextCompat.getColor(activity, android.R.color.holo_red_light))
                    alertDialogBox.dismiss()
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
                val rejectText = "Request to book truck: ${truckSelected} " +
                        "for route ${liveRoutesListItem.Mandi} to " +
                        "${liveRoutesListItem.Routes[position].Des} send successfully!"
                showSnackbar(rejectText,
                    ContextCompat.getColor(activity, android.R.color.holo_green_light))
                alertDialogBox.dismiss()
            }
            alertDialogBox.show()

        }

    private fun showSnackbar(message: String, color: Int) {
        val snackbar = Snackbar.make(fragment.view!!, message,
            Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(color)
        snackbar.show()
    }

    /**
     * Making alert dialog box
     */
    private fun inflateAlertBoxDialog() {
        val alertDialogBoxBuilder = AlertDialog.Builder(activity)
        val alertDialogBoxView = LayoutInflater.from(activity)
            .inflate(R.layout.close_bid_alert_dialog_box, null)
        alertDialogBox = alertDialogBoxBuilder.create()
        alertDialogBox.setCancelable(true)
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
                auctionStartTime: Long,
                auctionEndTime: Long,
                auctionBonusStartTime: Long,
                auctionBonusEndTime: Long,
            ) {
                seqNo.text = pos.toString()
                godown.text = route.Des
                val progressText = "${route.Got}/${route.Req}"
                truckFilledInfo.text = progressText
                val rateText = "???${route.Rate}"
                rate.text = rateText
                progressBar.progress = ((route.Got.toFloat() / route.Req.toFloat()) * 100).toInt()


                val isInMainAuctionTime =
                    (CurrDateTimeInMillis() >= auctionStartTime) && (CurrDateTimeInMillis() < auctionEndTime)
                val isRouteAvailable = route.Got < route.Req
                val isEligibleUnderBonusTime = (CurrDateTimeInMillis() >= auctionBonusStartTime) &&
                        (CurrDateTimeInMillis() < auctionBonusEndTime)

                if ((isInMainAuctionTime || isEligibleUnderBonusTime) && isRouteAvailable) {
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
