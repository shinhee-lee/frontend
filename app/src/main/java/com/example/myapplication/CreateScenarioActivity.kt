package com.example.myapplication

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.util.Base64
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.temporary.CustomImageView
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.get
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@SuppressLint("CheckResult")
class CreateScenarioActivity : AppCompatActivity() {
    private lateinit var launcher: ActivityResultLauncher<String>

    private lateinit var objectLayout: LinearLayout
    private lateinit var contentbtn: Button
    private lateinit var objectbtn: Button
    private lateinit var startbtn: Button
    private lateinit var backgroundbtn: Button
    private lateinit var EditTextContent: Editable
    private lateinit var backgroundview: ImageView

    private var x = 0f
    private var y = 0f
    private var dx = 0f
    private var dy = 0f
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    private var currentImageView:CustomImageView? = null
    private lateinit var framelayout: FrameLayout

    /*//서버에서 받는 오브젝트, 배경 array
    private lateinit var iArray: Array<String>
    private lateinit var sArray: Array<String>*/
    //오브젝트 배열
    private lateinit var oArray: Array<String>
    private lateinit var osArray: Array<String>
    //배경 배열
    private lateinit var bArray: Array<String>
    private lateinit var bsArray: Array<String>

    var checkNum = 0

    var saved = 0


    //최종 변환된 이미지 문자열
    var finalImg = " "
    //성공적으로 나열됐는지 확인
    var checkInt = 0

    //save버튼이 얼마나 클릭되었는지
    var clicked = 0
    //서버에 보낼 사진, 이야기, 문장 array
    val imgList = ArrayList<String>()
    val contentList = ArrayList<String>()
    var sentenceResult = ""
    /*val sentenceList = ArrayList<ArrayList<String>>()*/
    /*val pageSentence = ArrayList<String>()*/

    /*private lateinit var imgArray: Array<String>
    private lateinit var contentArray: Array<String>*/
    /*var imgArray: Array<String>? = emptyArray()
    var contentArray: Array<String>? = emptyArray()*/
    //서버에 보낼 title
    private lateinit var title:String

    //삭제 저장 마지막 다음 버튼
    private lateinit var deletebtn: Button
    private lateinit var savebtn: Button
    private lateinit var lastbtn: Button
    private lateinit var nextbtn: Button

    private lateinit var backbtn: Button

    //페이지별 배열과 인덱스
    private var pageIndex: Int = 0

    //마지막 페이지 버튼 눌렀는지 아닌지 알려주는 변수
    private var islast: Int = 0

    //intent
    private lateinit var idfromMakeStory: String
    private lateinit var pwfromMakeStory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createscenario)
        supportActionBar?.hide()

        idfromMakeStory = intent.getStringExtra("idInput")!!
        pwfromMakeStory = intent.getStringExtra("pwInput")!!

        /*//처음에 배열 받음
        getArrayFromServer(idfromMakeStory, "o")
        getArrayFromServer(idfromMakeStory, "b")*/
        /*iArray = arrayOf("1")
        sArray = arrayOf("1")*/

        oArray = arrayOf("1")
        osArray = arrayOf("1")
        bArray = arrayOf("1")
        bsArray = arrayOf("1")

        // objectlayout, framelayout 가져오기
        objectLayout = findViewById(R.id.objectLayout)
        framelayout = findViewById(R.id.pictureLayout)
        backgroundview = findViewById(R.id.backgroundview)

        //scale 조정 detector
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        // contentbtn과 objectbtn의 ID 가져오기
        startbtn = findViewById(R.id.startbtn)
        contentbtn = findViewById(R.id.contentbtn)
        objectbtn = findViewById(R.id.objectbtn)
        backgroundbtn = findViewById(R.id.backgroundbtn)

        //삭제 저장 마지막 다음 버튼
        deletebtn = findViewById(R.id.deletebtn)
        savebtn = findViewById(R.id.savebtn)
        lastbtn = findViewById(R.id.lastbtn)
        nextbtn = findViewById(R.id.nextbtn)

        val handler = Handler(Looper.getMainLooper())
        val delayMillis = 2000
        var isTimerRunning = true // 타이머 상태를 나타내는 변수 추가

        Toast.makeText(
            applicationContext,
            "오브젝트 수신 중",
            Toast.LENGTH_SHORT
        ).show()
        val context0 = this //수정
        val timerRunnable = object : java.lang.Runnable {
            override fun run() {
                if (checkNum == 0 && isTimerRunning) { // 타이머 상태를 검사하여 실행 여부 결정
                    getArrayFromServer(idfromMakeStory, "o")
                    // finalImg가 아직 값이 없으면 주기적으로 확인합니다.
                } else if (checkNum == 1 && isTimerRunning) {
                    getArrayFromServer(idfromMakeStory, "b")
                }
                else {
                    startbtn.isEnabled = true
                    startbtn.backgroundTintList = ContextCompat.getColorStateList(context0, R.color.colorBase)
                    contentbtn.isEnabled = true
                    contentbtn.backgroundTintList = ContextCompat.getColorStateList(context0, R.color.baseColor_lighter)
                    objectbtn.isEnabled = true
                    objectbtn.backgroundTintList = ContextCompat.getColorStateList(context0, R.color.baseColor_lighter)
                    backgroundbtn.isEnabled = true
                    backgroundbtn.backgroundTintList = ContextCompat.getColorStateList(context0, R.color.baseColor_lighter)
                    Toast.makeText(applicationContext, "전체 수신 완료", Toast.LENGTH_SHORT).show()
                    isTimerRunning = false
                }

                if (isTimerRunning) {
                    handler.postDelayed(this, delayMillis.toLong())
                }
            }
        }
        handler.post(timerRunnable)

        /*//배열 들어왔으면 버튼 활성화
        if (checkNum == 2){
            startbtn.isEnabled = true
            startbtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorBase)
        }*/

        backbtn = findViewById(R.id.backbtn)
        backbtn.setOnClickListener{
            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)
            intent.putExtra("pwInput", pwfromMakeStory)
            startActivity(intent)
        }

        //startbtn 이벤트
        startbtn.setOnClickListener {
            islast = 0

            EditTextContent = objectLayout.findViewById<EditText>(R.id.existingEditText).editableText

            objectLayout.removeAllViews()
            val editText = EditText(this)
            // editText에 필요한 설정 추가
            editText.id = R.id.newEditText

            editText.text = EditTextContent

            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            editText.layoutParams = layoutParams

            objectLayout.addView(editText)
            EditTextContent = editText.text
        }

        // contentbtn 클릭 이벤트 처리
        contentbtn.setOnClickListener {
            // contentbtn을 클릭했을 때 objectLayout의 레이아웃 설정
            objectLayout.removeAllViews() // 기존의 뷰를 모두 제거

            val editText = EditText(this)
            // editText에 필요한 설정 추가
            editText.id = R.id.newEditText

            editText.text = EditTextContent

            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            editText.layoutParams = layoutParams

            objectLayout.addView(editText)
            EditTextContent = editText.text
        }

        // objectbtn 클릭 이벤트 처리
        objectbtn.setOnClickListener {
            // objectbtn을 클릭했을 때 objectLayout의 레이아웃 설정
            objectLayout.removeAllViews() // 기존의 뷰를 모두 제거
            checkInt = 0

            val gridLayout = GridLayout(this)
            gridLayout.columnCount = 4
            gridLayout.rowCount = 2
            println("그리드 레이아웃 생성")

            val objectType = "o"
            println("idfromMakeStory:$idfromMakeStory")
            val context = this // Context 변수에 저장
            /*getArrayFromServer(idfromMakeStory!!, objectType)*/

            thread (start = true){
                for (i in osArray.indices){
                    println("i: ${osArray[i]}")

                    val button = ImageButton(context)

                    //버튼 크기
                    val layoutParams = GridLayout.LayoutParams()
                    val dpValue = 60
                    val marginValue = 10

                    // 버튼 크기 설정
                    val pxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics
                    ).toInt()
                    layoutParams.width = pxValue
                    layoutParams.height = pxValue

                    // 마진 설정
                    val marginPxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, marginValue.toFloat(), resources.displayMetrics
                    ).toInt()
                    layoutParams.setMargins(
                        marginPxValue,
                        marginPxValue,
                        marginPxValue,
                        marginPxValue
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        // 버튼에 레이아웃 파라미터 적용
                        button.layoutParams = layoutParams

                        //버튼마다 사진 넣기
                        val stb1 = oArray[i]
                        val bitmap1 = stringToBitmap(stb1)
                        val resizedBitmap1 =
                            resizeBitmap(bitmap1, 300, 300, 80)  //bitmap 리사이징
                        button.setImageBitmap(resizedBitmap1)

                        button.setOnClickListener {
                            val index = i
                            val stb2 = oArray[index]
                            val bitmap = stringToBitmap(stb2)    //Array의 원소를 bitmap으로 변경
                            val resizedBitmap = resizeBitmap(bitmap, 500, 500, 80)  //bitmap 리사이징

                            sentenceResult += osArray[index]
                            sentenceResult += ", "


                            //배경 투명화
                            val transparentBitmap = Bitmap.createBitmap(
                                bitmap.width,
                                bitmap.height,
                                Bitmap.Config.ARGB_8888
                            )

                            val pixels1 = IntArray(bitmap.width * bitmap.height)
                            bitmap.getPixels(
                                pixels1,
                                0,
                                bitmap.width,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height
                            )

                            for (k in pixels1.indices) {
                                if (pixels1[k] == Color.BLACK) {
                                    pixels1[k] = Color.argb(
                                        0,
                                        Color.red(pixels1[k]),
                                        Color.green(pixels1[k]),
                                        Color.blue(pixels1[k])
                                    )
                                }
                            }

                            transparentBitmap.setPixels(
                                pixels1,
                                0,
                                transparentBitmap.width,
                                0,
                                0,
                                transparentBitmap.width,
                                transparentBitmap.height
                            )


                            showImage(transparentBitmap)
                        }
                        gridLayout.addView(button)
                    }, 100)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    objectLayout.addView(gridLayout)
                }, 100)

            }
        }

            /*GlobalScope.launch(Dispatchers.IO) {

        }*/

        // backgroundbtn 클릭 이벤트 처리
        backgroundbtn.setOnClickListener {
            // objectbtn을 클릭했을 때 objectLayout의 레이아웃 설정
            objectLayout.removeAllViews() // 기존의 뷰를 모두 제거
            checkInt = 0

            val gridLayout = GridLayout(this)
            gridLayout.columnCount = 4
            gridLayout.rowCount = 2

            val backgroundType = "b"
            val context = this

            thread (start = true){
                for (i in bsArray.indices) {
                    println("for 문 in")
                    checkInt = 1
                    println("checkInt: $checkInt")
                    val button = ImageButton(context)
                    button.contentDescription = "button"

                    //버튼 크기
                    val layoutParams = GridLayout.LayoutParams()
                    val dpValue = 60
                    val marginValue = 10

                    // 버튼 크기 설정
                    val pxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dpValue.toFloat(),
                        resources.displayMetrics
                    ).toInt()
                    layoutParams.width = pxValue
                    layoutParams.height = pxValue

                    // 마진 설정
                    val marginPxValue = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        marginValue.toFloat(),
                        resources.displayMetrics
                    ).toInt()
                    layoutParams.setMargins(
                        marginPxValue,
                        marginPxValue,
                        marginPxValue,
                        marginPxValue
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        // 버튼에 레이아웃 파라미터 적용
                        button.layoutParams = layoutParams

                        // 버튼에 고유한 아이디를 할당
                        button.id = i

                        //버튼마다 사진 넣기
                        val stb1 = bArray[i]
                        val bitmap1 = stringToBitmap(stb1)
                        val resizedBitmap1 =
                            resizeBitmap(bitmap1, 300, 300, 80)  //bitmap 리사이징
                        button.setImageBitmap(resizedBitmap1)

                        button.setOnClickListener {
                            val index = i
                            val stb = bArray[index]
                            val bitmap = stringToBitmap(stb)
                            sentenceResult += bsArray[index]
                            sentenceResult += ", "

                            // backgroundview의 크기 정보 가져오기
                            val backgroundWidth = backgroundview.width
                            val backgroundHeight = backgroundview.height
                            println("backgroundwidth: $backgroundWidth")
                            println("backgroundheight: $backgroundHeight")

                            // 비율 계산
                            val imageWidth = bitmap.width
                            val imageHeight = bitmap.height

                            val widthRatio =
                                backgroundWidth.toFloat() / imageWidth.toFloat()
                            val heightRatio =
                                backgroundHeight.toFloat() / imageHeight.toFloat()

                            // 이미지 크기 조정
                            val scaledWidth: Int
                            val scaledHeight: Int

                            if (widthRatio > heightRatio) {
                                // 이미지의 너비가 높이보다 클 때, 이미지의 너비를 이미지뷰에 맞추고 높이를 비율에 맞게 조정
                                scaledWidth = backgroundWidth
                                scaledHeight = (imageHeight * widthRatio).toInt()
                            } else {
                                // 이미지의 높이가 너비보다 클 때, 이미지의 높이를 이미지뷰에 맞추고 너비를 비율에 맞게 조정
                                scaledWidth = (imageWidth * heightRatio).toInt()
                                scaledHeight = backgroundHeight
                            }

                            // 이미지를 가운데에 배치하고 나머지 너비를 잘라냄
                            val scaledBitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                scaledWidth,
                                scaledHeight,
                                true
                            )
                            val x = (backgroundWidth - scaledWidth) / 2
                            val y = (backgroundHeight - scaledHeight) / 2

                            val finalBitmap = Bitmap.createBitmap(
                                backgroundWidth,
                                backgroundHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(finalBitmap)
                            canvas.drawBitmap(
                                scaledBitmap,
                                x.toFloat(),
                                y.toFloat(),
                                null
                            )

                            // 비트맵을 backgroundview에 설정
                            backgroundview.setImageBitmap(finalBitmap)
                        }
                        gridLayout.addView(button)
                    }, 100)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    objectLayout.addView(gridLayout)
                }, 100)
            }
                /*Handler(Looper.getMainLooper()).postDelayed({
                    println("서버에서 stringArray 받음")

                    for (i in stcArray.indices) {
                        println("for 문 in")
                        checkInt = 1
                        println("checkInt: $checkInt")
                        val button = ImageButton(context)
                        button.contentDescription = "button"

                        //버튼 크기
                        val layoutParams = GridLayout.LayoutParams()
                        val dpValue = 80
                        val marginValue = 10

                        // 버튼 크기 설정
                        val pxValue = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            dpValue.toFloat(),
                            resources.displayMetrics
                        ).toInt()
                        layoutParams.width = pxValue
                        layoutParams.height = pxValue

                        // 마진 설정
                        val marginPxValue = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            marginValue.toFloat(),
                            resources.displayMetrics
                        ).toInt()
                        layoutParams.setMargins(
                            marginPxValue,
                            marginPxValue,
                            marginPxValue,
                            marginPxValue
                        )

                        // 버튼에 레이아웃 파라미터 적용
                        button.layoutParams = layoutParams

                        // 버튼에 고유한 아이디를 할당
                        button.id = i

                        //버튼마다 사진 넣기
                        val stb1 = imgArray[i]
                        val bitmap1 = stringToBitmap(stb1)
                        val resizedBitmap1 =
                            resizeBitmap(bitmap1, 300, 300, 80)  //bitmap 리사이징
                        button.setImageBitmap(resizedBitmap1)

                        button.setOnClickListener {
                            val index = i
                            val stb = imgArray[index]
                            val bitmap = stringToBitmap(stb)
                            sentenceResult += stcArray[index]
                            sentenceResult += ", "

                            // backgroundview의 크기 정보 가져오기
                            val backgroundWidth = backgroundview.width
                            val backgroundHeight = backgroundview.height
                            println("backgroundwidth: $backgroundWidth")
                            println("backgroundheight: $backgroundHeight")

                            // 비율 계산
                            val imageWidth = bitmap.width
                            val imageHeight = bitmap.height

                            val widthRatio =
                                backgroundWidth.toFloat() / imageWidth.toFloat()
                            val heightRatio =
                                backgroundHeight.toFloat() / imageHeight.toFloat()

                            // 이미지 크기 조정
                            val scaledWidth: Int
                            val scaledHeight: Int

                            if (widthRatio > heightRatio) {
                                // 이미지의 너비가 높이보다 클 때, 이미지의 너비를 이미지뷰에 맞추고 높이를 비율에 맞게 조정
                                scaledWidth = backgroundWidth
                                scaledHeight = (imageHeight * widthRatio).toInt()
                            } else {
                                // 이미지의 높이가 너비보다 클 때, 이미지의 높이를 이미지뷰에 맞추고 너비를 비율에 맞게 조정
                                scaledWidth = (imageWidth * heightRatio).toInt()
                                scaledHeight = backgroundHeight
                            }

                            // 이미지를 가운데에 배치하고 나머지 너비를 잘라냄
                            val scaledBitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                scaledWidth,
                                scaledHeight,
                                true
                            )
                            val x = (backgroundWidth - scaledWidth) / 2
                            val y = (backgroundHeight - scaledHeight) / 2

                            val finalBitmap = Bitmap.createBitmap(
                                backgroundWidth,
                                backgroundHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(finalBitmap)
                            canvas.drawBitmap(
                                scaledBitmap,
                                x.toFloat(),
                                y.toFloat(),
                                null
                            )

                            // 비트맵을 backgroundview에 설정
                            backgroundview.setImageBitmap(finalBitmap)
                        }
                        gridLayout.addView(button)
                    }
                    objectLayout.addView(gridLayout)
                }, 1500)*/
                /*runOnUiThread {
                    Handler(Looper.getMainLooper()).postDelayed({
                        for (i in imgArray.indices) {
                            println("for 문 in")
                            checkInt = 1
                            println("checkInt: $checkInt")
                            val button = ImageButton(context)
                            button.contentDescription = "button"

                            //버튼 크기
                            val layoutParams = GridLayout.LayoutParams()
                            val dpValue = 80
                            val marginValue = 10

                            // 버튼 크기 설정
                            val pxValue = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                dpValue.toFloat(),
                                resources.displayMetrics
                            ).toInt()
                            layoutParams.width = pxValue
                            layoutParams.height = pxValue

                            // 마진 설정
                            val marginPxValue = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                marginValue.toFloat(),
                                resources.displayMetrics
                            ).toInt()
                            layoutParams.setMargins(
                                marginPxValue,
                                marginPxValue,
                                marginPxValue,
                                marginPxValue
                            )

                            // 버튼에 레이아웃 파라미터 적용
                            button.layoutParams = layoutParams

                            // 버튼에 고유한 아이디를 할당
                            button.id = i

                            //버튼마다 사진 넣기
                            val stb1 = imgArray[i]
                            val bitmap1 = stringToBitmap(stb1)
                            val resizedBitmap1 =
                                resizeBitmap(bitmap1, 300, 300, 80)  //bitmap 리사이징
                            button.setImageBitmap(resizedBitmap1)

                            runOnUiThread {
                                button.setOnClickListener {
                                    val index = i
                                    val stb = imgArray[index]
                                    val bitmap = stringToBitmap(stb)
                                    sentenceResult += stcArray[index]
                                    sentenceResult += ", "

                                    // backgroundview의 크기 정보 가져오기
                                    val backgroundWidth = backgroundview.width
                                    val backgroundHeight = backgroundview.height
                                    println("backgroundwidth: $backgroundWidth")
                                    println("backgroundheight: $backgroundHeight")

                                    // 비율 계산
                                    val imageWidth = bitmap.width
                                    val imageHeight = bitmap.height

                                    val widthRatio =
                                        backgroundWidth.toFloat() / imageWidth.toFloat()
                                    val heightRatio =
                                        backgroundHeight.toFloat() / imageHeight.toFloat()

                                    // 이미지 크기 조정
                                    val scaledWidth: Int
                                    val scaledHeight: Int

                                    if (widthRatio > heightRatio) {
                                        // 이미지의 너비가 높이보다 클 때, 이미지의 너비를 이미지뷰에 맞추고 높이를 비율에 맞게 조정
                                        scaledWidth = backgroundWidth
                                        scaledHeight = (imageHeight * widthRatio).toInt()
                                    } else {
                                        // 이미지의 높이가 너비보다 클 때, 이미지의 높이를 이미지뷰에 맞추고 너비를 비율에 맞게 조정
                                        scaledWidth = (imageWidth * heightRatio).toInt()
                                        scaledHeight = backgroundHeight
                                    }

                                    // 이미지를 가운데에 배치하고 나머지 너비를 잘라냄
                                    val scaledBitmap = Bitmap.createScaledBitmap(
                                        bitmap,
                                        scaledWidth,
                                        scaledHeight,
                                        true
                                    )
                                    val x = (backgroundWidth - scaledWidth) / 2
                                    val y = (backgroundHeight - scaledHeight) / 2

                                    val finalBitmap = Bitmap.createBitmap(
                                        backgroundWidth,
                                        backgroundHeight,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = Canvas(finalBitmap)
                                    canvas.drawBitmap(
                                        scaledBitmap,
                                        x.toFloat(),
                                        y.toFloat(),
                                        null
                                    )

                                    // 비트맵을 backgroundview에 설정
                                    backgroundview.setImageBitmap(finalBitmap)
                                }
                            }
                            gridLayout.addView(button)
                        }
                        objectLayout.addView(gridLayout)
                    }, 3000) //수정?
                }*/
        }

        //아마도 삭제 가능??
        /*imageView = findViewById(R.id.iv_activity_main)
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())*/

        //삭제 버튼 이벤트
        // >> 선택된 오브젝트(이미지뷰) 삭제
        deletebtn.setOnClickListener {
            if(islast == 0 ){
                // currentImageView가 null이 아니라면 삭제
                currentImageView?.let { imageView ->
                    val parentView = imageView.parent as? ViewGroup
                    parentView?.removeView(imageView)
                    currentImageView = null
                }
            }
            else if(islast == 1){
                Toast.makeText(applicationContext, "마지막 페이지를 만드셨습니다. 더 이상 만드실 수 없습니다.", Toast.LENGTH_SHORT).show()

            }
        }

        //저장 버튼 이벤트 >> 수정 필요??
        // >> 해당 framelayout 캡쳐한 사진과 내용 배열에 넣기
        savebtn.setOnClickListener {
            saved = 1
            println("pageindex = $pageIndex")
            //캡쳐한 사진 bitmap
            val bitmap = requestSaveScreenshotPermission()
            println("requestsavescreenshotpermission 끝")
            //비트맵 string으로 변경
            val encodedString = bitmapToBase64(bitmap!!)

            val resizeEncodedString = resizeEncodedImageString(encodedString, 512, 512, 80)

            //이야기 내용
            val content = EditTextContent
            val content2 = content.toString()

            //비트맵과 내용 각각의 배열에 넣기
            if(clicked == 0){
                imgList.add(resizeEncodedString)
                contentList.add(content2)
                /*sentenceList.add(pageSentence)*/
            }
            else {
                imgList[pageIndex] = "$resizeEncodedString"
                contentList[pageIndex] = "$content2"
                /*sentenceList[pageIndex] = pageSentence*/
            }

            /*//비트맵과 내용 각각의 배열에 넣기
            imgArray[pageIndex] = "$encodedString"
            contentArray[pageIndex] = "$content"*/

            clicked = clicked + 1
            println("clicked = $clicked")

            Toast.makeText(applicationContext, "저장 완료", Toast.LENGTH_SHORT).show()
        }

        //마지막 페이지 버튼 이벤트
        // >> 최종적인 배열 서버에 전달 + 마지막 페이지인거 서버에게 알려주기 + 다음 페이지 막기(다음 버튼 없애기)
        lastbtn.setOnClickListener {
            if(saved == 0){
                Toast.makeText(applicationContext, "페이지 저장 필요", Toast.LENGTH_SHORT).show()
            }
            else if (saved == 1) {
                saved = 0
                islast = 1
                finalImg = " "
                showFinalImg2(finalImg)
                //이미지, 문장, 스타일 서버로 보내기
                finalChange(pageIndex)
                //안내 문구
                Toast.makeText(applicationContext, "최종 변환 중", Toast.LENGTH_SHORT).show()
            }
        }

        //다음 페이지 버튼 이벤트
        // >> framelayout과 objectlayout 초기화(content도 초기화)
        //페이지 인덱스는 pageIndex*2
        nextbtn.setOnClickListener {
            if (saved == 0){
                Toast.makeText(applicationContext, "페이지 저장 후 다음 페이지로", Toast.LENGTH_SHORT).show()
            }
            else if (saved == 1){
                saved = 0
                finalImg = " "
                showFinalImg(finalImg)
                //이미지, 문장, 스타일 서버로 보내기
                finalChange(pageIndex)
                //안내 문구
                Toast.makeText(applicationContext, "최종 변환 중", Toast.LENGTH_SHORT).show()

                runOnUiThread{
                    Handler(Looper.getMainLooper()).postDelayed({
                        //pagesentence 초기화
                        /*pageSentence.clear()*/
                        sentenceResult=""
                        //clicked 0으로 초기화
                        clicked = 0

                        //object layout 초기화
                        objectLayout.removeAllViews()

                        // 레이아웃 설정
                        objectLayout.orientation = LinearLayout.HORIZONTAL
                        objectLayout.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        objectLayout.setBackgroundColor(resources.getColor(R.color.baseColor_lighter))

                        // EditText 설정
                        val existingEditText = EditText(this)
                        existingEditText.id = R.id.existingEditText
                        existingEditText.layoutParams = LinearLayout.LayoutParams(
                            1, LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        existingEditText.setPadding(5, 5, 5, 5) // EditText의 패딩 설정

                        // Button 설정
                        val startButton = Button(this)
                        startButton.id = R.id.startbtn
                        startButton.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        startButton.setPadding(20, 20, 20, 20) // Button의 패딩 설정
                        startButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.baseColor_lighter))
                        startButton.text = "시작하려면 누르세요"
                        startButton.setTextColor(resources.getColor(R.color.colorText))
                        startButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

                        // object layout에 EditText와 Button 추가
                        objectLayout.addView(existingEditText)
                        objectLayout.addView(startButton)

                        //버튼 누르면 동작하도록
                        startButton.setOnClickListener {
                            //pageIndex++
                            pageIndex = pageIndex+1

                            startbtn.performClick()
                        }



                        //frame layout 초기화
                        framelayout.removeAllViews() // 기존의 뷰를 모두 제거
                        println("frame width: ${framelayout.width}")
                        println("frame height: ${framelayout.height}")

                        //수정
                        framelayout.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            resources.getDimensionPixelSize(R.dimen.picture_layout_height)
                        )

                        /*// pictureLayout 설정
                        val pictureLayout = FrameLayout(this)
                        pictureLayout.id = R.id.pictureLayout
                        pictureLayout.layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            resources.getDimensionPixelSize(R.dimen.picture_layout_height)
                        )
                        println("picture width: ${pictureLayout.width}")
                        println("picture height: ${pictureLayout.height}")*/

                        //수정
                        // backgroundView 설정 (ImageView)
                        /*val backgroundView = ImageView(this)
                        backgroundView.id = R.id.backgroundview
                        backgroundView.layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        println("backgroundview width: ${backgroundView.width}")*/


                        /*// framelayout에 pictureLayout 추가
                        framelayout.addView(pictureLayout)
                        // pictureLayout에 backgroundView 추가
                        pictureLayout.addView(backgroundView)*/
                        framelayout.addView(backgroundview)
                        /*framelayout.addView(backgroundView)*/

                        //content 초기화 >> 필요한가??
                    }, 1000)
                }
            }
        }
    }

    //인코딩된 이미지 리사이징
    fun resizeEncodedImageString(encodedImageString: String, maxWidth: Int, maxHeight: Int, quality: Int): String {
        //base64 >> bitmap으로 decode
        val decodedByteArray = Base64.decode(encodedImageString, Base64.DEFAULT)
        val decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)

        //원본 높이, 길이
        val originalWidth = decodedBitmap.width
        val originalHeight = decodedBitmap.height

        //원본 비율
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

        val scaledWidth: Int
        val scaledHeight: Int
        if (originalWidth > originalHeight) {
            // 가로로 긴 사진
            scaledWidth = maxWidth
            scaledHeight = (scaledWidth / aspectRatio).toInt()
        } else if (originalHeight > originalWidth) {
            // 세로로 긴 사진
            scaledHeight = maxHeight
            scaledWidth = (scaledHeight * aspectRatio).toInt()
        } else {
            // 정사각형 사진
            scaledWidth = maxWidth
            scaledHeight = maxHeight
        }

        //리사이징
        val scaledBitmap = Bitmap.createScaledBitmap(decodedBitmap, scaledWidth, scaledHeight, true)

        //인코딩 (bitmap >> bytearray >> base64string)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val encodedResizedImageString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        outputStream.close()

        return encodedResizedImageString
    }

    //줌 이벤트
    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f)
            currentImageView?.scaleX = scaleFactor
            currentImageView?.scaleY = scaleFactor
            return true
        }
    }

    //터치 이벤트 처리 함수
    private fun handleTouchEvent(imageView: CustomImageView, event: MotionEvent) {
        scaleGestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentImageView = imageView
                x = event.x
                y = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (imageView == currentImageView) {
                    if (scaleGestureDetector.isInProgress) {
                        // 이미지를 확대/축소하는 도중에는 이동하지 않음
                        return@handleTouchEvent
                    }

                    dx = event.x - x
                    dy = event.y - y

                    // 애니메이션 적용을 위한 값을 보간
                    val startX = imageView.translationX
                    val startY = imageView.translationY
                    val endX = startX + dx
                    val endY = startY + dy

                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = 0
                    animator.addUpdateListener { valueAnimator ->
                        val fraction = valueAnimator.animatedFraction
                        val interpolatedX = startX + fraction * (endX - startX)
                        val interpolatedY = startY + fraction * (endY - startY)
                        imageView.translationX = interpolatedX
                        imageView.translationY = interpolatedY
                    }
                    animator.start()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (imageView == currentImageView) {
                    currentImageView = null
                }
            }
        }
    }

    //단위 변경 함수
    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    //비트맵 인코딩 (서버로 전송하기 위한 형태 취하기)
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        return base64String
    }

    //최종 변환된 사진을 보여주는 팝업창 띄우기
    private fun showFinalImg(image:String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_finalchange, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("최종 변환된 이미지 보는 창")

        val alertDialog = dialogBuilder.create()

        val imgView = dialogView.findViewById<ImageView>(R.id.img)

        val handler = Handler(Looper.getMainLooper())
        val delayMillis = 3000
        var isTimerRunning = true // 타이머 상태를 나타내는 변수 추가

        val timerRunnable = object : Runnable {
            override fun run() {
                if (finalImg != " " && isTimerRunning) {
                    val decoded = Base64.decode(finalImg, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                    imgView.setImageBitmap(bitmap)

                    // 원하는 처리가 끝났으면 타이머를 중지합니다.
                    isTimerRunning = false
                } else if (isTimerRunning) {
                    // finalImg가 아직 값이 없으면 주기적으로 확인합니다.
                    handler.postDelayed(this, delayMillis.toLong())
                }
            }
        }

        handler.post(timerRunnable)


        val okButton = dialogView.findViewById<Button>(R.id.okbtn)
        okButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    //최종 변환된 사진을 보여주는 팝업창 띄우기 (마지막용)
    private fun showFinalImg2(image:String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_finalchange, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("최종 변환된 이미지 보는 창")

        val alertDialog = dialogBuilder.create()

        val imgView = dialogView.findViewById<ImageView>(R.id.img)

        val handler = Handler(Looper.getMainLooper())
        val delayMillis = 3000
        var isTimerRunning = true // 타이머 상태를 나타내는 변수 추가

        val timerRunnable = object : Runnable {
            override fun run() {
                if (finalImg != " " && isTimerRunning) {
                    val decoded = Base64.decode(finalImg, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                    imgView.setImageBitmap(bitmap)

                    // 원하는 처리가 끝났으면 타이머를 중지합니다.
                    isTimerRunning = false
                } else if (isTimerRunning) {
                    // finalImg가 아직 값이 없으면 주기적으로 확인합니다.
                    handler.postDelayed(this, delayMillis.toLong())
                }
            }
        }

        handler.post(timerRunnable)


        val okButton = dialogView.findViewById<Button>(R.id.okbtn)
        okButton.setOnClickListener {
            showPopup(idfromMakeStory)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    //title 지정 팝업창 띄우기
    private fun showPopup(id:String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("제목 입력 창")

        val alertDialog = dialogBuilder.create()

        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            val editText = dialogView.findViewById<EditText>(R.id.editText)
            title = editText.text.toString()

            val imgArray: Array<String> = imgList.toTypedArray()
            val contentArray: Array<String> = contentList.toTypedArray()

            sendArrayToserver(id!!, imgArray, contentArray, title)
            Toast.makeText(applicationContext, "저장되었습니다", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MakestoryActivity::class.java)
            intent.putExtra("idInput", idfromMakeStory)
            intent.putExtra("pwInput", pwfromMakeStory)
            startActivity(intent)

            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    //jsonArray >> stringArray //아마 수정 필요????
    fun jsonArrayToStringArray(jsonArray: JSONArray): Array<String> {
        val stringArray = Array(jsonArray.length()) { "" }

        for (i in 0 until jsonArray.length()) {
            val element = jsonArray.optString(i)
            stringArray[i] = element
        }

        return stringArray
    }

    //완성된 이야기 전송하는 함수
    //배열(비트맵+내용) 보내기, 마지막 페이지 알려주기
    private fun sendArrayToserver(id:String, imageArray:Array<String>, storyArray:Array<String>, title:String){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        val json = JSONObject()
        json.put("key", "8")
        json.put("id", id)
        json.put("image", JSONArray(imageArray))
        json.put("story", JSONArray(storyArray))
        json.put("title", title)

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    Toast.makeText(applicationContext, "Failed to upload image: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                /*Thread{
                    var str = response.body?.string()
                    println(str)
                }.start()*/
                if (response.isSuccessful){
                    val bodyString = response.body?.string()
                    if(bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")

                            runOnUiThread {
                                if (dataString == "1") {
                                    Toast.makeText(
                                        applicationContext,
                                        "DB에 이야기 송신 성공",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "DB에 이야기 송신 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "서버에서 받은 값 없음: " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else{
                    runOnUiThread{
                        Toast.makeText(applicationContext, "Failed to send story", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }

    //오브젝트 받는 함수
    //서버에서 imageDataString Array받고 return stringArray
    //수정
    private fun getArrayFromServer(id:String, type:String//callback: (Array<String>, Array<String>
    ){ //suspend fun
        var imageArray: Array<String>? = emptyArray()
        var sentenceArray: Array<String>? = emptyArray()

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val json = JSONObject()
        json.put("key", "9")
        json.put("id", id)
        json.put("cate", type)
        println("jsonobject 생성")

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        println("request ok")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to send:" + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                println("onFailure")
//                callback.invoke(imageArray!!, sentenceArray!!)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()

                    if (bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")
                            runOnUiThread {
                                if (dataString == "1") {
                                    val imageDataArray = jsonObject.getJSONArray("image")
                                    val sentenceDataArray = jsonObject.getJSONArray("sentence")
                                    println("이미지, 문장 받음")

                                    imageArray = jsonArrayToStringArray(imageDataArray)
                                    sentenceArray = jsonArrayToStringArray(sentenceDataArray)
                                    println("함수 안: stringArray 받음")
                                    println("stringArray: "+imageArray!!.joinToString())
                                    println("sentenceArray: "+sentenceArray!!.joinToString())

                                    if(type == "o"){
                                        oArray = imageArray!!
                                        osArray = sentenceArray!!
                                        println("oArray: "+oArray.joinToString())
                                        println("osArray: "+osArray.joinToString())

                                        checkNum = 1
                                        Toast.makeText(
                                            applicationContext,
                                            "물체 수신 완료",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else if(type == "b"){
                                        bArray = imageArray!!
                                        bsArray = sentenceArray!!
                                        println("bArray: "+bArray.joinToString())
                                        println("bsArray: "+bsArray.joinToString())

                                        checkNum = 2
                                        Toast.makeText(
                                            applicationContext,
                                            "배경 수신 완료",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    println("osArray: "+osArray.joinToString())
                                    println("bsArray: "+bsArray.joinToString())

                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "오브젝트 수신 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "서버에서 받은 값 없음: " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
//                        callback.invoke(imageArray!!, sentenceArray!!)
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "수신 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
//                        callback.invoke(imageArray!!, sentenceArray!!)
                    }
                }
                response.close()
            }
        })
    }

    //알고리즘 최종 적용 함수(19번)
    private fun finalChange(page: Int){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val url = "http://221.163.223.200:5000/image"
        /*val url = "http://192.168.1.38:83/image"*/
        val client = OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val imageString = imgList[page]
        /*val sentenceString = sentenceList[page]*/
        /*val sentenceString = pageSentence
        println("pageSentence: $pageSentence")*/
        val sentenceString = sentenceResult
        println("pageSentence: $sentenceResult")

        val json = JSONObject()
        json.put("key", "19")
        json.put("image", imageString)
        json.put("sentence", sentenceString)
        json.put("style", "ghibli style")

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .url(url)
//            .addHeader("Connection", "close")
            .post(body)
            .build()

        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    Toast.makeText(applicationContext, "Failed to upload image: " + e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                /*Thread{
                    var str = response.body?.string()
                    println(str)
                }.start()*/
                if (response.isSuccessful){
                    val bodyString = response.body?.string()
                    if(bodyString != null) {
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val dataString = jsonObject.getString("ok_sign")

                            runOnUiThread {
                                if (dataString == "1") {
                                    println("dataString = 1")
                                    val dataString2 = jsonObject.getString("result")
                                    imgList[page] = dataString2
                                    finalImg = dataString2

                                    Toast.makeText(
                                        applicationContext,
                                        "변경된 이미지 저장 완료",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (dataString == "0") {
                                    Toast.makeText(
                                        applicationContext,
                                        "변경된 이미지 저장 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "서버에서 받은 값 없음: " + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else{
                    runOnUiThread{
                        Toast.makeText(applicationContext, "Failed to receive image", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }

    //사진 string Base64로 디코딩 >> bitmap로 변환 return bitmap
    private fun stringToBitmap(string:String): Bitmap{
        val imageDataBytes = Base64.decode(string, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageDataBytes, 0, imageDataBytes.size)
    }

    //이미지를 이미지뷰에 넣고 framelayout에 올리는 함수
    private fun showImage(bitmap: Bitmap){
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = 10

        val imageView = CustomImageView(this)
        imageView.setImageBitmap(bitmap)

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        imageView.layoutParams = layoutParams

        /*imageView.setOnTouchListener{ view, event ->
            handleTouchEvent(imageView, event)
            if(event.action == MotionEvent.ACTION_UP){
                view.performClick()
            }
            true
        }*/
        imageView.setOnTouchListener { view, event ->
            handleTouchEvent(imageView, event)
            if (event.action == MotionEvent.ACTION_UP) {
                currentImageView = imageView
                view.performClick()
            }
            true
        }

        framelayout.addView(imageView)
    }

    //사진 리사이징 함수 >> 아마도 수정 필요?
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int, quality: Int): Bitmap {
        // 원본 높이, 길이
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // 원본 비율
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

        val scaledWidth: Int
        val scaledHeight: Int
        if (originalWidth > originalHeight) {
            // 가로로 긴 사진
            scaledWidth = maxWidth
            scaledHeight = (scaledWidth / aspectRatio).toInt()
        } else if (originalHeight > originalWidth) {
            // 세로로 긴 사진
            scaledHeight = maxHeight
            scaledWidth = (scaledHeight * aspectRatio).toInt()
        } else {
            // 정사각형 사진
            scaledWidth = maxWidth
            scaledHeight = maxHeight
        }

        // 리사이징
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        // 압축
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        // 압축된 비트맵 반환
        return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
    }

    //캡쳐(picturelayout = framelayout) 후 저장
    //>> 캡쳐만 필요함! 갤러리 저장이 아니라 서버로 보낼것

    //캡쳐 함수
    private fun captureScreen(view: View): Bitmap {
        println("captureScreen")
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val background = view.background
        if (background != null) {
            background.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    //갤러리 저장 함수 >> 사용x
    private fun saveOnGallery(bitmap: Bitmap){
        println("saveongallery")
        bitmap.let{
            val filename = "${System.currentTimeMillis()}.jpg"

            val values = ContentValues().apply{
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val outputStream = contentResolver.openOutputStream(uri!!)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream?.close()

            Toast.makeText(this, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /*private fun captureAndSendScreenshot() {
        println("captureAndSendScreenshot")
        val screenshot = captureScreen(framelayout)
        sendBitmapToServer(screenshot)
        *//*saveScreenshot(screenshot, "layout_screenshot")*//*
        println("screenshot success")
    }*/

    //갤러리 접근 권한 >> savebtn
    private fun requestSaveScreenshotPermission(): Bitmap? {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
            println("requestsavescreenshotpermission first")
            return null
        } else {
            println("permission")
            println("requestsavescreenshotpermission second")
            return captureScreen(framelayout)
        }
    }
    /*private fun requestSaveScreenshotPermission() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        } else {
            println("permission")
            captureScreen(framelayout)
        }
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureScreen(framelayout)
                println("onrequestpermissionsresult first")
            } else {
                println("onrequestpermissionsresult second")
                Toast.makeText(this, "Permission denied. Unable to save screenshot.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}