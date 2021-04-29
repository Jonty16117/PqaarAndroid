package com.pqaar.app.pahunchAdmin.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.PahunchTicket
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo
import com.pqaar.app.pahunchAdmin.viewModel.PahunchAdminViewModel
import com.pqaar.app.utils.TimeConversions.MillisToTimestamp


@SuppressLint("SetTextI18n")
class PahunchAdminDashboard : AppCompatActivity() {
    //private val TAG = "PahunchAdminDashboard"

    private var pahunchHistory = ArrayList<PahunchTicket>()

    private lateinit var pahunchHistoryAdapter: PahunchHistoryAdapter

    private lateinit var historyRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pahunch_admin_dashboard)

        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        val model = ViewModelProviders.of(this)
            .get(PahunchAdminViewModel::class.java)

        pahunchHistoryAdapter = PahunchHistoryAdapter(this, pahunchHistory)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = pahunchHistoryAdapter

        model.getPahunchHistory().observe(this, {
            pahunchHistory = ArrayList()
            pahunchHistory.addAll(it)
            updatePahunchHistoryAdapter()
        })

    }

    private fun updatePahunchHistoryAdapter() {
        pahunchHistoryAdapter.notifyDataSetChanged()
    }

    inner class PahunchHistoryAdapter(
        private val context: Context,
        private val pahunchHistory: ArrayList<PahunchTicket>,
    ) :
        RecyclerView.Adapter<PahunchHistoryAdapter.ViewHolder>(),
        AdapterView.OnItemSelectedListener {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context)
                    .inflate((R.layout.pahunch_history_item), parent,
                        false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setData(pahunchHistory[position], position, onItemClicked())
        }


        private fun onItemClicked() = View.OnClickListener {

        }

        override fun getItemCount(): Int {
            return pahunchHistory.size
        }

        inner class ViewHolder(private val truckItemView: View) :
            RecyclerView.ViewHolder(truckItemView) {

            fun setData(
                pahunchHistory: PahunchTicket,
                position: Int,
                onClickListener: View.OnClickListener,
            ) {
                val seqNo = truckItemView.findViewById<TextView>(R.id.truck_no)
                val truckNo = truckItemView.findViewById<TextView>(R.id.textView3)
                val date = truckItemView.findViewById<TextView>(R.id.textView43)
                val time = truckItemView.findViewById<TextView>(R.id.textView44)
                val mandi = truckItemView.findViewById<TextView>(R.id.textView45)
                val status = truckItemView.findViewById<TextView>(R.id.textView46)
                val delAcceptedIcon = truckItemView.findViewById<ImageView>(R.id.imageView14)
                val delRejectedIcon = truckItemView.findViewById<ImageView>(R.id.imageView12)

                seqNo.text = (position + 1).toString()
                truckNo.text = pahunchHistory.TruckNo
                mandi.text = pahunchHistory.Source

                val dateTime = MillisToTimestamp(pahunchHistory.Timestamp).split(" ")
                date.text = dateTime[0]
                time.text = dateTime[1]

                when (pahunchHistory.Status) {
                    "DelPass" -> {
                        status.text = "Delivery Accepted"
                        delAcceptedIcon.isVisible = true
                        delRejectedIcon.isVisible = false
                    }
                    "DelFail" -> {
                        status.text = "Delivery Rejected"
                        delAcceptedIcon.isVisible = false
                        delRejectedIcon.isVisible = true
                    }
                }

                itemView.setOnClickListener {
                    onClickListener.onClick(it)
                }
            }
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            //do nothing
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }
    }
}