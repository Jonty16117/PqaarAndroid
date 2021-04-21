package com.pqaar.app.ui.TruckOwner

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.pqaar.app.R
import com.pqaar.app.utils.DbPaths.TRUCK_RC
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI

class ManageTrucksTest : AppCompatActivity(), PermissionListener {

    private lateinit var filepath: Uri
    private lateinit var bitmap: Bitmap

    private lateinit var rcimage: ImageView
    private lateinit var browsebtn: Button
    private lateinit var uploadbtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_trucks_test)

        rcimage = findViewById<ImageView>(R.id.imageView5)
        browsebtn = findViewById<Button>(R.id.button3)
        uploadbtn = findViewById<Button>(R.id.button4)


        browsebtn.setOnClickListener {
            Dexter
                .withActivity(this)
                .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(this)
                .check()
        }

        uploadbtn.setOnClickListener {
            uploadTruckRC()
        }
    }

    private fun uploadTruckRC() {
        val firebaseSt = FirebaseStorage.getInstance()
        val ref = firebaseSt.reference.child(TRUCK_RC)
        val baos = ByteArrayOutputStream()
        val rcImage = bitmap
        rcImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val data = baos.toByteArray()
        ref.putBytes(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Truck application sent successfully!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to send truck application, please try again!",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
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
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (intentData != null) {
                filepath = intentData.data!!
                val inputStream = contentResolver.openInputStream(filepath)
                bitmap = BitmapFactory.decodeStream(inputStream)
                rcimage.setImageBitmap(bitmap)
            } else {

            }
        }
        super.onActivityResult(requestCode, resultCode, intentData)
    }
}