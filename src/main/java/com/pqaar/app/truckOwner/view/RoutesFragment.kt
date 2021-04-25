package com.pqaar.app.truckOwner.view

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.adapters.MandiAdminRoutesHistoryAdapter
import com.pqaar.app.mandiAdmin.viewModel.MandiAdminViewModel
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.LiveRoutesListItemDTO
import com.pqaar.app.truckOwner.adapters.RouteListAdapter
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel
import com.squareup.okhttp.Route

class RoutesFragment : Fragment() {

    private var liveRoutesList = ArrayList<LiveRoutesListItem>()
    private val adapterList = ArrayList<RouteListAdapter>()
    private lateinit var alertDialogBox: AlertDialog

    private lateinit var alertDialogMandi: TextView
    private lateinit var alertDialogGodown: TextView
    private lateinit var alertDialogTruckNo: TextView
    private lateinit var alertDialogYesBtn: Button
    private lateinit var alertDialogNoBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        inflateAlertBoxDialog()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        val concatAdapter = ConcatAdapter(adapterList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = concatAdapter

        model.getLiveRoutesList().observe(this, {
            refreshLiveRoutesList(it)
            adapterList.forEach { adapter -> concatAdapter.removeAdapter(adapter) }
            adapterList.clear()
            liveRoutesList.forEachIndexed { index, liveRoutesListItem ->
                adapterList.add(RouteListAdapter(liveRoutesListItem, index, requireActivity(), this))
            }
            adapterList.forEach { adapter -> concatAdapter.addAdapter(adapter) }
            concatAdapter.notifyDataSetChanged()
        })


    }

    private fun refreshLiveRoutesList(liveRoutesListDTO: HashMap<String, LiveRoutesListItemDTO>) {
        liveRoutesList.clear()
        liveRoutesListDTO.forEach { liveRoutesListDTOItem ->
            val routes = ArrayList<LiveRoutesListItem.RouteDestination>()
            liveRoutesListDTOItem.value.desData.forEach { route ->
                routes.add(LiveRoutesListItem.RouteDestination(
                    Des = route.key,
                    Req = route.value["Req"]!!.toInt(),
                    Got = route.value["Got"]!!.toInt(),
                    Rate = route.value["Rate"]!!.toInt()
                ))
            }
            liveRoutesList.add(LiveRoutesListItem(
                Mandi = liveRoutesListDTOItem.key,
                Routes = routes
            ))
        }
    }

    /*private fun onChildClicked() = object : View.OnClickListener {
        override fun onClick(view: View?) {
            alertDialogBox.show()
        }
    }*/

    /*private fun inflateAlertBoxDialog() {
        val alertDialogBoxBuilder = AlertDialog.Builder(requireActivity())
        val alertDialogBoxView = LayoutInflater.from(requireActivity())
            .inflate(R.layout.close_bid_alert_dialog_box, null)
        alertDialogBox = alertDialogBoxBuilder.create()
        alertDialogBox.setCancelable(false)
        alertDialogBox.setView(alertDialogBoxView)

        alertDialogMandi = alertDialogBoxView.findViewById<TextView>(R.id.textView36)
        alertDialogGodown = alertDialogBoxView.findViewById<TextView>(R.id.textView26)
        alertDialogTruckNo = alertDialogBoxView.findViewById<TextView>(R.id.textView30)
        alertDialogYesBtn = alertDialogBoxView.findViewById<Button>(R.id.button5)
        alertDialogNoBtn = alertDialogBoxView.findViewById<Button>(R.id.button6)

        alertDialogNoBtn.setOnClickListener {
            alertDialogBox.dismiss()
        }

    }*/
}