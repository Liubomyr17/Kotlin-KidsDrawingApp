package com.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Gallery
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*


class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint: ImageButton? =
        null // A variable for current color is picked from color pallet.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat()) // Setting the default brush size to drawing view.

        /**
         * This is to select the default Image button which is
         * active and color is already defined in the drawing view class.
         * As the default color is black so in our color pallet it is on 2 position.
         * But the array list start position is 0 so the black color is at position 1.
         */
        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
        ib_gallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }
        ib_undo.setOnClickListener {
            drawing_view.onClickUndo()
        }
    }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == GALLERY) {
                    try {
                        if (data!!.data != null) {
                            iv_background.visibility = View.VISIBLE
                            iv_background.setImageURI(data.data)
                        } else {
                            Toast.makeText(
                                    this@MainActivity,
                                    "Error in parsing the image or its corrupted.",
                            Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }


    /**
     * Method is used to launch the dialog to select different brush sizes.
     */
    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size :")
        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        })
        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        })

        val largeBtn = brushDialog.ib_large_brush
        largeBtn.setOnClickListener(View.OnClickListener {
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        })
        brushDialog.show()
    }

    // TODO(Step 2 - A function for color selection.)
    /**
     * Method is called when color is clicked from pallet_normal.
     *
     * @param view ImageButton on which click took place.
     */
    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            // Update the color
            val imageButton = view as ImageButton
            // Here the tag is used for swaping the current color with previous color.
            // The tag stores the selected view
            val colorTag = imageButton.tag.toString()
            // The color is set as per the selected tag here.
            drawing_view.setColor(colorTag)
            // Swap the backgrounds for last active and currently active image button.
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
            mImageButtonCurrentPaint!!.setImageDrawable(
                    ContextCompat.getDrawable(
                            this,
                            R.drawable.pallet_normal
                    )
            )

            //Current view is updated with selected view in the form of ImageButton.
            mImageButtonCurrentPaint = view
        }
    }
        private fun requestStoragePermission() {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())) {
                Toast.makeText(this, "Need permission to add a Background", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        this@MainActivity,
                        "Permission granted now you can read the storage files",
                        Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                        this@MainActivity,
                        "Oops, you've just denied the permission",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width,
            view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return returnedBitmap
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}