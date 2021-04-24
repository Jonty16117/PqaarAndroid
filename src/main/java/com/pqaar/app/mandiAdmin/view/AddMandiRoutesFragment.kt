package com.pqaar.app.mandiAdmin.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pqaar.app.R
import com.pqaar.app.model.PropRoutesListItem
import com.pqaar.app.mandiAdmin.viewModel.MandiAdminViewModel
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class AddMandiRoutesFragment : Fragment() {
    private val TAG = "AddMandiRoutesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_mandi_routes, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        val uploadRoutesBtn = view.findViewById<Button>(R.id.button2)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar2)

        val model = ViewModelProviders.of(this)
            .get(MandiAdminViewModel::class.java)

        val routes = ArrayList<PropRoutesListItem>()
        val adapter = RoutesAdapter(context!!, routes)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        /**
         * Making alert dialog box
         */
        val alertDialogBoxBuilder = AlertDialog.Builder(requireActivity())
        val alertDialogBoxView = LayoutInflater.from(activity)
            .inflate(R.layout.fragment_add_routes_alert_dialog_box, null)
        val alertDialogBox = alertDialogBoxBuilder.create()
        alertDialogBox.setCancelable(true)
        alertDialogBox.setView(alertDialogBoxView)
        alertDialogBox.setOnShowListener {
            alertDialogBox
                .getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
            alertDialogBox
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
        }
        val des = alertDialogBoxView.findViewById<EditText>(R.id.editTextTextPersonName)
        val req = alertDialogBoxView.findViewById<EditText>(R.id.editTextTextPersonName2)

        /**
         * Setting actions on text buttons of alert dialog box
         */
        alertDialogBox.setButton(AlertDialog.BUTTON_POSITIVE, "Add"
        ) { dialog, which ->
            if (des.text.isNullOrEmpty() || req.text.isNullOrEmpty()) {
                Snackbar.make(view, "Please fill all the details!",
                    Snackbar.LENGTH_SHORT).show()
            } else {
                routes.add(PropRoutesListItem(
                    des = des.text.trim().toString(),
                    req = req.text.trim().toString()
                ))
                adapter.notifyDataSetChanged()
                des.text.clear()
                req.text.clear()
                Snackbar.make(view, "Route added!",
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        alertDialogBox.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"
        ) { dialog, which ->
            alertDialogBox.dismiss()
        }


        fab.setOnClickListener { fabView ->
            alertDialogBox.show()
        }

        uploadRoutesBtn.setOnClickListener {
            val routesAdded = routes.size
            val routesCopy = ArrayList(routes)
            if (routesAdded != 0) {
                progressBar.isVisible = true
                Snackbar.make(view, "Sending routes list to Union Admin!",
                    Snackbar.LENGTH_SHORT).show()

                GlobalScope.launch(Dispatchers.IO) {
                    val executionTime = measureTimeMillis {
                        val job1 = async {
                            model.AddRoutes(routesCopy)
                        }
                        job1.await()
                    }
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "ExecutionTime = $executionTime")
                        routes.clear()
                        adapter.notifyItemRangeRemoved(0, routesAdded)
                        Snackbar.make(view, "Routes List sent to Union Admin Successfully!",
                            Snackbar.LENGTH_SHORT).show()
                        progressBar.isVisible = false
                    }
                }

            } else {
                Snackbar.make(view, "Routes list empty!",
                    Snackbar.LENGTH_SHORT).show()
            }
        }

        /**
         * Swipe to delete item in recycler view
         */
        val onSwipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                //do nothing
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currPosition = viewHolder.adapterPosition
                routes.removeAt(currPosition)
                adapter.notifyItemRemoved(currPosition)
            }
        }
        ItemTouchHelper(onSwipe).attachToRecyclerView(recyclerView)

        return view
    }

    inner class RoutesAdapter(
        private val context: Context,
        private val routes: ArrayList<PropRoutesListItem>,
    ) :
        RecyclerView.Adapter<RoutesAdapter.RoutesViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesViewHolder {
            return RoutesViewHolder(
                LayoutInflater.from(context)
                    .inflate((R.layout.madni_admin_routes_history_sub_item), parent,
                        false))
        }

        override fun onBindViewHolder(holder: RoutesViewHolder, position: Int) {
            holder.setData(routes[position], position)
        }

        override fun getItemCount(): Int {
            return routes.size
        }

        inner class RoutesViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

            @SuppressLint("SetTextI18n")
            fun setData(route: PropRoutesListItem, position: Int) {
                val seqNo = view.findViewById<TextView>(R.id.textView9)
                val godown = view.findViewById<TextView>(R.id.godown)
                val req = view.findViewById<TextView>(R.id.textView18)

                seqNo.text = (position + 1).toString()
                godown.text = route.des
                req.text = route.req
            }
        }
    }
}
