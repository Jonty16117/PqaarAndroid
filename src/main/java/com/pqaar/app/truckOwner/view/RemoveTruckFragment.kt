package com.pqaar.app.truckOwner.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.TruckHistory
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel

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

        val trucksList = ArrayList<TruckHistory>()
        val adapter = Adapter(context!!, trucksList)

        recyclerView.layoutManager = LinearLayoutManager(context!!)
        recyclerView.adapter = adapter

        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        model.getLiveTruckDataList().observe(this, { liveTrucksList ->
            progressBar.isVisible = true
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
        private val trucksList: ArrayList<TruckHistory>,
    ) :
        RecyclerView.Adapter<Adapter.ViewHolder>(),
        AdapterView.OnItemSelectedListener {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(context)
                    .inflate((R.layout.truck_item), parent,
                        false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setData(trucksList[position], position, onItemClicked())
        }


        private fun onItemClicked() = View.OnClickListener {

        }

        override fun getItemCount(): Int {
            return trucksList.size
        }

        inner class ViewHolder(private val truckItemView: View) :
            RecyclerView.ViewHolder(truckItemView) {

            fun setData(
                truckHistoryItem: TruckHistory,
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