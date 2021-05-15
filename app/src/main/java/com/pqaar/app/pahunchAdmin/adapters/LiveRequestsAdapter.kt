package com.pqaar.app.pahunchAdmin.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pqaar.app.R
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo
import com.pqaar.app.pahunchAdmin.viewModel.PahunchAdminViewModel
import com.pqaar.app.utils.TimeConversions.MillisToTimestamp
import kotlinx.coroutines.*

class LiveRequestsAdapter(
    private var liveTruckDataItem: ArrayList<LiveTruckDataItem>,
    private var activity: Activity,
    private var fragment: Fragment,
) :
    RecyclerView.Adapter<LiveRequestsAdapter.LiveRequestsViewHolder>(),
    AdapterView.OnItemSelectedListener {

    //    private val TAG = "LiveRequestsViewHolder"
    private var spinnerPosSeleted = 0

    private var model: PahunchAdminViewModel
    private lateinit var alertDialogBox: AlertDialog
    private lateinit var alertDialogTruckNo: TextView
    private lateinit var alertDialogMandi: TextView
    private lateinit var alertDialogGodown: TextView
    private lateinit var alertDialogYesBtn: Button
    private lateinit var alertDialogNoBtn: Button
    private lateinit var alertDialogDelInfo: EditText
    private lateinit var alertDialogSpinner: Spinner

    init {
        inflateAlertBoxDialog()
        model = ViewModelProviders.of(fragment)
            .get(PahunchAdminViewModel::class.java)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveRequestsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LiveRequestsViewHolder.HeaderViewHolder(
            layoutInflater.inflate(R.layout.incoming_truck_request_item,
                parent,
                false)
        )
    }


    override fun onBindViewHolder(holder: LiveRequestsViewHolder, position: Int) {
        (holder as LiveRequestsViewHolder.HeaderViewHolder)
            .setData(liveTruckDataItem[position], position,
                onChildClicked(liveTruckDataItem[position]))
    }

    override fun getItemCount(): Int {
        return liveTruckDataItem.size
    }

    private fun onChildClicked(liveTruckDataItem: LiveTruckDataItem) =
        View.OnClickListener {
            alertDialogTruckNo.text = liveTruckDataItem.TruckNo
            alertDialogMandi.text = liveTruckDataItem.Route.first
            alertDialogGodown.text = liveTruckDataItem.Route.second

            val options = listOf("Accept", "Reject")
            val spinnerAdapter =
                ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, options)
            spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
            alertDialogSpinner.adapter = spinnerAdapter
            alertDialogSpinner.onItemSelectedListener = this
            alertDialogYesBtn.setOnClickListener {
                var delInfo: String = alertDialogDelInfo.text.toString().trim()
                if (delInfo.isEmpty()) {
                    delInfo = " "
                }
                GlobalScope.launch(Dispatchers.IO) {
                    val job1 = async {
                        model.refreshIncomingTrucks()
                    }
                    job1.await()
                    val job2 = async {
                        withContext(Dispatchers.IO) {
                            if (spinnerPosSeleted == 0) {
                                model.AcceptDel(liveTruckDataItem.TruckNo, delInfo)
                            } else {
                                model.RejectDel(liveTruckDataItem.TruckNo, delInfo)
                            }
                        }
                    }
                    job2.await()
                    val job3 = async {
                        withContext(Dispatchers.Main) {
                            val acceptText = "Pahunch Ticket issued for " +
                                    "${liveTruckDataItem.TruckNo} successfully!"
                            showSnackbar(acceptText,
                                ContextCompat.getColor(activity, android.R.color.holo_green_light))
                        }
                    }
                    job3.await()
                }
                alertDialogBox.dismiss()
                alertDialogDelInfo.text.clear()
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
            .inflate(R.layout.truck_delivery_alert_dialog_box, null)
        alertDialogBox = alertDialogBoxBuilder.create()
        alertDialogBox.setCancelable(true)
        alertDialogBox.setView(alertDialogBoxView)

        alertDialogMandi = alertDialogBoxView.findViewById<TextView>(R.id.textView36)
        alertDialogGodown = alertDialogBoxView.findViewById<TextView>(R.id.textView26)
        alertDialogTruckNo = alertDialogBoxView.findViewById<TextView>(R.id.textView49)
        alertDialogYesBtn = alertDialogBoxView.findViewById<Button>(R.id.button5)
        alertDialogNoBtn = alertDialogBoxView.findViewById<Button>(R.id.button6)
        alertDialogDelInfo = alertDialogBoxView.findViewById<EditText>(R.id.delInfo)
        alertDialogSpinner = alertDialogBoxView.findViewById<Spinner>(R.id.spinner)

        alertDialogNoBtn.setOnClickListener {
            alertDialogBox.dismiss()
        }
    }

    sealed class LiveRequestsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class HeaderViewHolder(itemView: View) : LiveRequestsViewHolder(itemView) {
            private val TAG = "LiveRequestsViewHolder"

            private val seqNo = itemView.findViewById<TextView>(R.id.seqNo)
            private val truckNo = itemView.findViewById<TextView>(R.id.textView3)
            private val aucDate = itemView.findViewById<TextView>(R.id.textView43)
            private val aucTime = itemView.findViewById<TextView>(R.id.textView44)
            private val aucListNo = itemView.findViewById<TextView>(R.id.textView47)
            private val mandi = itemView.findViewById<TextView>(R.id.textView45)
            private val truckOwner = itemView.findViewById<TextView>(R.id.textView46)

            @SuppressLint("SetTextI18n")
            fun setData(
                liveTruckDataItem: LiveTruckDataItem, position: Int,
                onClickListener: View.OnClickListener,
            ) {
                seqNo.text = (position + 1).toString()
                truckNo.text = liveTruckDataItem.TruckNo
                val aucDateTime = MillisToTimestamp(liveTruckDataItem.AuctionId).split(" ")
                aucDate.text = aucDateTime[0]
                aucTime.text = aucDateTime[1]
                aucListNo.text = liveTruckDataItem.CurrentListNo
                mandi.text = liveTruckDataItem.Route.first
                val truckOwnerFullName =
                    liveTruckDataItem.Owner.first + " " + liveTruckDataItem.Owner.second
                truckOwner.text = truckOwnerFullName

                if (liveTruckDataItem.Status == "DelInProg") {
                    itemView.setOnClickListener {
                        onClickListener.onClick(it)
                    }
                }
            }

        }


    }

    /**
     * Alter dialog box spinner listener
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        spinnerPosSeleted = position
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}