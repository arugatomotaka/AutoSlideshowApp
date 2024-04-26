package jp.techacademy.tomotaka.aruga.autoslideshowapp


import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import jp.techacademy.tomotaka.aruga.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_REQUEST_CODE = 100

    val imageUris = mutableListOf<Uri>()

    var currentIndex = 0

    private var isPlaying = false

    private var handler = Handler(Looper.getMainLooper())

    private var timer: Timer? = null


    // APIレベルによって許可が必要なパーミッションを切り替える
    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // パーミッションの許可状態を確認する
        if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            getContentsInfo()
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(readImagesPermission),
                PERMISSIONS_REQUEST_CODE
            )
        }
        Log.d("aaa", imageUris.size.toString())

//        setImages()


        binding.susumubutton.setOnClickListener {
// リストが空でないこと、または現在のインデックスがリストの最後の要素を指していないことを確認

            if (imageUris.isNotEmpty()) {

                if (currentIndex < imageUris.size - 1) {

                    currentIndex++
                } else {

                    currentIndex = 0
                }
                binding.imageView.setImageURI(imageUris[currentIndex])
            }

        }

        binding.modorubutton.setOnClickListener {
// リストが空でないこと、または現在のインデックスがリストの最後の要素を指していないことを確認

            if (imageUris.isNotEmpty()) {
                if (currentIndex > 0) {

                    currentIndex--
                } else {
                    currentIndex = imageUris.size - 1
                }
                binding.imageView.setImageURI(imageUris[currentIndex])

            }

        }


        binding.saiseibutton.setOnClickListener {
            if (imageUris.isNotEmpty()) {
                if (!isPlaying) {

                    timer = Timer()

                    timer!!.schedule(object : TimerTask() {
                        override fun run() {
                            handler.post {
                                if (imageUris.isNotEmpty()) {
                                    if (currentIndex < imageUris.size - 1) {

                                        currentIndex++
                                    } else {

                                        currentIndex = 0
                                    }
                                    binding.imageView.setImageURI(imageUris[currentIndex])
                                }
                            }

                        }
                    }, 2000, 2000)

                    binding.susumubutton.isEnabled = false
                    binding.modorubutton.isEnabled = false
                    binding.saiseibutton.text = "停止"


                } else {
                    timer?.cancel()
                    binding.susumubutton.isEnabled = true
                    binding.modorubutton.isEnabled = true
                    binding.saiseibutton.text = "再生"

                }
                isPlaying = !isPlaying
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    Toast.makeText(this, "許可がないとアプリケーションを使用できません", Toast.LENGTH_LONG).show()
                    // アプリケーションを終了
                }
        }
    }


    private fun getContentsInfo() {
        // 画像の情報を取得する

        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // indexからIDを取得し、そのIDから画像のURIを取得する
                    val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(fieldIndex)
                    val imageUri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    imageUris.add(imageUri)

                    // Log.d("ANDROID", "URI : $imageUri")
                } while (cursor.moveToNext())
            }
            setImages()
            cursor.close()
        } else {
            Log.d("aaa", "エラーが出ました")
        }
    }

    //画像を設定
    private fun setImages() {
        if (imageUris.isNotEmpty()) {
            binding.imageView.setImageURI(imageUris[0])
        }
    }
}




