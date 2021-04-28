package com.pqaar.app.truckOwner.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.pqaar.app.R
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel
import com.pqaar.app.utils.DbPaths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AddTruckFragment : Fragment(), PermissionListener {
    private val TAG = "AddTruckFragment"

    private lateinit var filepath: Uri
    private var rcFrontBitmap: Bitmap? = null
    private var rcBackBitmap: Bitmap? = null

    private lateinit var rcfront: ImageView
    private lateinit var rcback: ImageView
    private lateinit var truckNo: EditText
    private lateinit var truckRcNo: EditText
    private lateinit var submitApplication: Button

    private var clickedRcFrontImageView: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_truck, container, false)

        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        rcfront = view.findViewById<ImageView>(R.id.imageView9)
        rcback = view.findViewById<ImageView>(R.id.imageView8)
        truckNo = view.findViewById<EditText>(R.id.textInputTruckNo)
        truckRcNo = view.findViewById<EditText>(R.id.textInputRcNo)
        submitApplication = view.findViewById<Button>(R.id.submitTruckApplication)


        rcfront.setOnClickListener {
            clickedRcFrontImageView = true
            Dexter
                .withActivity(requireActivity())
                .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(this)
                .check()
        }

        rcback.setOnClickListener {
            clickedRcFrontImageView = false
            Dexter
                .withActivity(requireActivity())
                .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(this)
                .check()
        }

        submitApplication.setOnClickListener {
            if (truckNo.text.trim().isEmpty()) {
                Snackbar.make(it, "Please enter your Truck Number!",
                    Snackbar.LENGTH_LONG).show()
            } else if (truckRcNo.text.trim().isEmpty()) {
                Snackbar.make(it, "Please enter your Truck RC Number!",
                    Snackbar.LENGTH_LONG).show()
            } else if(rcBackBitmap == null || rcFrontBitmap == null) {
                Snackbar.make(it, "Please select both the front and back images of " +
                        "your Truck's RC!",
                    Snackbar.LENGTH_LONG).show()
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    val job = async {
                        model.UploadTruckRc(rcFront = rcFrontBitmap!!, rcBack = rcBackBitmap!!)
                    }
                    job.await()
                }
                rcfront.setImageBitmap(null)
                rcback.setImageBitmap(null)
                truckNo.text.clear()
                truckRcNo.text.clear()
                Snackbar.make(it, "Truck RC submitted successfully!",
                    Snackbar.LENGTH_LONG).show()
            }
        }
        return view

    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(intent, "Please select image"), 1)
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?,
    ) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            if (intentData != null) {
                filepath = intentData.data!!
                val inputStream = context!!.contentResolver.openInputStream(filepath)
                if(clickedRcFrontImageView) {
                    rcFrontBitmap = BitmapFactory.decodeStream(inputStream)
                    rcfront.setImageBitmap(rcFrontBitmap)
                } else {
                    rcBackBitmap = BitmapFactory.decodeStream(inputStream)
                    rcback.setImageBitmap(rcBackBitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intentData)
    }
}