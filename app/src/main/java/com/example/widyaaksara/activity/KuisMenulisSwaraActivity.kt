package com.example.widyaaksara.activity

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.widyaaksara.R
import com.example.widyaaksara.api.ApiClient
import com.example.widyaaksara.model.Aksara
import com.example.widyaaksara.model.AksaraModel
import com.example.widyaaksara.model.AksaraResponse
import com.example.widyaaksara.model.Titik
import com.example.widyaaksara.utils.JsonHelper
import com.example.widyaaksara.view.CanvasView
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KuisMenulisSwaraActivity : AppCompatActivity() {
    private lateinit var btnNext: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var imagePola: ImageView
    private lateinit var canvasView: CanvasView
    private lateinit var tvNamaAksara: TextView
    private lateinit var imageAksara: ImageView
    private lateinit var btnValidasi: Button

    private var aksaraList: List<Aksara> = listOf()
    private var aksaraSwaraList: List<AksaraModel> = listOf()
    private var currentIndex = 0
    private var currentAksara: AksaraModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kuis_menulis_swara)

        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)
        imagePola = findViewById(R.id.imagePola)
        canvasView = findViewById(R.id.canvasView)
        tvNamaAksara = findViewById(R.id.tvNamaAksara)
        imageAksara = findViewById(R.id.imageAksara)
        btnValidasi = findViewById(R.id.btnValidasi)

        btnValidasi.setOnClickListener {
            if (currentAksara == null) {
                Toast.makeText(this, "Tunggu hingga data selesai dimuat!", Toast.LENGTH_SHORT).show()
            } else {
                validateDrawing()
            }
        }

        canvasView.setOnTouchListener { _, event ->
            canvasView.onTouchEvent(event)
            true
        }

        aksaraSwaraList = JsonHelper.loadAksaraSwara(this)
        fetchAksaraData()

        btnNext.setOnClickListener {
            if (aksaraList.isNotEmpty()) {
                canvasView.clearCanvas()
                currentIndex = (currentIndex + 1) % aksaraList.size
                updateUI()
            }
        }

        btnBack.setOnClickListener {
            if (aksaraList.isNotEmpty()) {
                canvasView.clearCanvas()
                currentIndex = if (currentIndex - 1 < 0) aksaraList.size - 1 else currentIndex - 1
                updateUI()
            }
        }

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV gagal dimuat!")
        } else {
            Log.d("OpenCV", "OpenCV berhasil dimuat!")
        }

    }

    private fun fetchAksaraData() {
        ApiClient.instance.getAksaraSwara().enqueue(object : Callback<AksaraResponse> {
            override fun onResponse(call: Call<AksaraResponse>, response: Response<AksaraResponse>) {
                if (response.isSuccessful) {
                    aksaraList = response.body()?.data ?: listOf()
                    if (aksaraList.isNotEmpty()) {
                        currentIndex = 0
                        updateUI()
                    }
                } else {
                    Toast.makeText(this@KuisMenulisSwaraActivity, "Gagal mengambil data!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AksaraResponse>, t: Throwable) {
                Toast.makeText(this@KuisMenulisSwaraActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI() {
        if (aksaraList.isNotEmpty() && currentIndex < aksaraList.size) {
            val aksara = aksaraList[currentIndex]
            tvNamaAksara.text = aksara.nama

            Glide.with(this).load(aksara.gambar_aksara).into(imageAksara)

            Glide.with(this).asBitmap().load(aksara.gambar_pola).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(bitmap: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    val polaPoints = extractPolaCoordinates(bitmap)
                    currentAksara?.titik = polaPoints.map { Titik(it.x.toDouble(), it.y.toDouble()) }
                    Log.d("updateUI", "Pola aksara dikonversi ke titik koordinat.")
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
        }
    }

    private fun validateDrawing() {
        if (currentAksara == null) {
            Toast.makeText(this, "Pola belum dimuat, coba lagi!", Toast.LENGTH_SHORT).show()
            return
        }

        val userPoints = canvasView.getUserDrawnPoints()
        val userPointsF = convertToPointFList(userPoints)
        val referencePointsF = convertToPointFList(currentAksara?.titik ?: emptyList())

        if (referencePointsF.isEmpty()) {
            Toast.makeText(this, "Pola belum tersedia untuk validasi!", Toast.LENGTH_SHORT).show()
            return
        }

        var correctPoints = 0
        for (point in userPointsF) {
            if (isPointInPolygon(point, referencePointsF)) {
                correctPoints++
            }
        }

        val similarity = correctPoints.toFloat() / userPointsF.size
        val isCorrect = similarity > 0.8

        if (isCorrect) {
            Toast.makeText(this, "Gambar benar!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Gambar belum sesuai, coba lagi!", Toast.LENGTH_SHORT).show()
        }
    }

    fun convertToPointFList(titikList: List<Titik>): List<PointF> {
        return titikList.map { PointF(it.x.toFloat(), it.y.toFloat()) }
    }

    fun isPointInPolygon(point: PointF, polygon: List<PointF>): Boolean {
        var intersectCount = 0
        for (i in polygon.indices) {
            val j = (i + 1) % polygon.size
            val p1 = polygon[i]
            val p2 = polygon[j]

            if ((p1.y > point.y) != (p2.y > point.y)) {
                val intersectX = p1.x + (point.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y).toInt()
                if (point.x < intersectX) {
                    intersectCount++
                }
            }
        }
        return (intersectCount % 2 == 1)
    }

    fun extractPolaCoordinates(image: Bitmap): List<PointF> {
        val srcMat = Mat()
        Utils.bitmapToMat(image, srcMat)

        // Konversi ke grayscale
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_BGR2GRAY)

        // Deteksi tepi dengan Canny Edge Detection
        Imgproc.Canny(srcMat, srcMat, 100.0, 200.0)

        // Temukan kontur (bentuk aksara)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(srcMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val points = mutableListOf<PointF>()

        for (contour in contours) {
            for (point in contour.toArray()) {
                points.add(PointF(point.x.toFloat(), point.y.toFloat()))
            }
        }

        return points
    }

}
