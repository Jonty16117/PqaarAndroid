package com.pqaar.app.truckOwner.view

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.pqaar.app.R
import com.pqaar.app.common.LoginActivity
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class RemoveTruckFragment : Fragment() {
    private val TAG = "RemoveTruckFragment"

    private lateinit var alertDialogBox: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_remove_truck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar4)
        val backButton = view.findViewById<Button>(R.id.button)

        backButton.setOnClickListener {
            fragmentManager!!.popBackStack()
        }

        val trucksList = ArrayList<LiveTruckDataItem>()
        val adapter = Adapter(context!!, trucksList)

        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.adapter = adapter

        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        model.getLiveTruckDataList().observe(this, { liveTrucksList ->
            progressBar.isVisible = true
            Log.d(TAG, "flag")
            trucksList.clear()
            liveTrucksList.forEach{
                trucksList.add(it.value)
            }
            adapter.notifyDataSetChanged()
            progressBar.isVisible = false
        })


    }

    inner class Adapter(
        private val context: Context,
        private val trucksList: ArrayList<LiveTruckDataItem>,
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>(),
        AdapterView.OnItemSelectedListener {

        private val model: TruckOwnerViewModel

        init {
            model = ViewModelProviders.of(requireActivity())
                .get(TruckOwnerViewModel::class.java)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context)
                    .inflate((R.layout.truck_item), parent,
                        false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setData(trucksList[position], position, onItemClicked(position))
        }


        private fun onItemClicked(position: Int) = View.OnClickListener {
            Log.d(TAG, "Item clicked")
            val alertDialog: AlertDialog
            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            builder.setTitle("Send remove truck request")
            builder.setMessage("Are you sure you want to remove truck ${trucksList[position].TruckNo}?")
            builder.setIcon(R.drawable.ic_delete)
            builder.setPositiveButton("Confirm") { dialogInterface, _ ->
                GlobalScope.launch(Dispatchers.IO) {
                    model.removeTruck(trucksList[position].TruckNo)
                }
                Toast.makeText(context, "Sent truck remove request successfully!", Toast.LENGTH_LONG).show()
                dialogInterface.dismiss()
            }
            builder.setNegativeButton("Back") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            alertDialog = builder.create()
            alertDialog.setCancelable(true)
            alertDialog.show()

            //changing color of text in alert dialog box
            val messageText = alertDialog.findViewById<TextView>(android.R.id.message)
            val logoutBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val cancelBtn = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            logoutBtn.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            cancelBtn.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            messageText?.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))

        }

        override fun getItemCount(): Int {
            return trucksList.size
        }

        inner class ViewHolder(private val truckItemView: View) :
            RecyclerView.ViewHolder(truckItemView) {

            fun setData(
                truckHistoryItem: LiveTruckDataItem,
                position: Int,
                onClickListener: View.OnClickListener
            ) {
                val seqNo = truckItemView.findViewById<TextView>(R.id.truck_no)
                val truckNo = truckItemView.findViewById<TextView>(R.id.textView3)
                val deleteIcon = truckItemView.findViewById<ImageView>(R.id.imageView)
                val deleteInProgressIcon = truckItemView.findViewById<ImageView>(R.id.imageView12)

                seqNo.text = (position + 1).toString()
                truckNo.text = truckHistoryItem.TruckNo
                deleteIcon.isVisible = true
                deleteInProgressIcon.isVisible = false

                truckItemView.setOnClickListener {
                    onClickListener.onClick(it)
                }
            }
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }
    }

}