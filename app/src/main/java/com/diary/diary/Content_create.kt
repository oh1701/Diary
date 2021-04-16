package com.diary.diary

import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.diary.diary.databinding.ActivityContentCreateBinding
import com.diary.recycler.Recycler_font
import com.diary.recycler.Recycler_tag
import com.diary.recycler.font_list
import com.diary.recycler.tagline
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.ColorPickerView
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

// 현재 문제? 잘모르겠음 이건. 레이아웃 마진 설정하는것. 추가해도 되고 안해도 된다.
// 뷰모델에 onclick 이벤트들 연결시키고 클릭시 각자의 함수 실행시키는 거로 가독성 증가 및 유지보수 쉽게시키기.
// @자동 저장, @내 폰트, @모든 사진 삭제 << 와 같은 단축키 설정. observe 통해서 edit들을 확인하고, 만약 저 글자들이 포함되는 순간, 이벤트 발생. 이후 글자 삭제.
// 레이아웃 삭제시 editarray에서 본 edit으로 붙여넣기.
// 모든 종료 이벤트 시 interface의 string을 꺼짐으로 설정해주기.

class Roommodel:ViewModel(){
    private val edittitle = MutableLiveData<String>()
    private val editcontent = MutableLiveData<String>()

    var title = MutableLiveData<String>()
    var textcontent = MutableLiveData<String>()

    fun getEdi() = edittitle
    fun getContent() = editcontent

    fun Checktext() = title // 확인버튼 누르면 변경, 옵저버가 변경을 파악하여 coroutine을 통해 room에게 전송

    fun onclick(){
        title.value = edittitle.value
        textcontent.value = editcontent.value
        Log.d("TAG", "보내진 것은 ${title.value}")
    }
}

lateinit var recy:RecyclerView
lateinit var tag_array:ArrayList<tagline>

class Content_create : AppCompatActivity(), rere, Inter_recycler_remove { // intent 통해서 전해진 데이터값으로 태그를 받는다. 태그값에 따라 room에 넣어지는 값도 달리하기.

    lateinit var binding:ActivityContentCreateBinding
    
    var image_array:ArrayList<ImageView?> = arrayListOf() //이미지 저장용 리스트
    var button_array:ArrayList<ImageButton?> = arrayListOf()
    var Edit_array:ArrayList<EditText?> = arrayListOf()
    var frame_array:ArrayList<FrameLayout?> = arrayListOf()
    var linear_array:ArrayList<LinearLayout?> = arrayListOf()

    var color_btn_array = arrayOfNulls<Button>(6) // 버튼 저장용
    var color_array = arrayOfNulls<String>(6) //색상 저장용

    var notouch_change = 1 // 터치 무효화 버튼 이벤트 감지
    var tag_changed = 1 // 태그 버튼 클릭 이벤트 감지
    var trash_changed = 1 // 제거용(쓰레기통) 버튼 클릭 이벤트 감지
    var line_spacing = 1.0f // 라인간격 확인용
    var letter_spacing = 0.0f // 자간 확인용
    var text_size = 16f

    var remove_btn_id = -1

    val dateformat = DateTimeFormatter.ofPattern("yyyyMMdd")
    val now = LocalDateTime.now().format(dateformat).toLong() //현재 시간.

    var titletext = "" //제목
    var contenttext = "" // 내용

    companion object{
        lateinit var viewModel:Roommodel
        lateinit var db:RoomdiaryDB
        
        var CAMERA_REQUEST = 1000 // 사진 리퀘스트 코드
        var PICTURE_REQUEST = 2000 // 갤러리 리퀘스트 코드
        lateinit var PHOTO_PATH:String //사진 경로

        lateinit var metrics:DisplayMetrics // 디바이스 화면 크기 알아내는 변수
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content_create)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(Roommodel::class.java)
        binding.creatediary = viewModel

        db = Room.databaseBuilder(
            applicationContext, RoomdiaryDB::class.java,
            "RoomDB"
        )
                .build()
        tag_array = arrayListOf()
        binding.FlexRecycler.layoutManager = FlexboxLayoutManager(
            this,
            FlexDirection.ROW,
            FlexWrap.WRAP
        ) //가로정렬, 꽉차면 다음칸으로 넘어가게 만듬.
        binding.FlexRecycler.setHasFixedSize(true)
        binding.FlexRecycler.adapter = Recycler_tag(tag_array)

        recy = binding.FlexRecycler
        metrics = resources.displayMetrics

        // 함수 불러올 공간
    }

    override fun onResume() {
        super.onResume()

        if(linear_array.isNotEmpty()) { // 쓰레기통버튼 on일경우 사진 추가했을때 remove버튼 나오게 하기.
            if (trash_changed == -1)
                for (i in 0 until button_array.size) {
                    button_array[i]?.visibility = View.VISIBLE
                }
        }

        if(notouch_change == -1) // 터치 버튼 on일경우 사진 추가했을때 터치 안되게 하기.
            for (i in 0 until button_array.size) {
                Edit_array[i]?.isEnabled = false
                image_array[i]?.isEnabled = false
            }

        observemodel()
        clickListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // 사진, 갤러리 설정하기.
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
        PICTURE_REQUEST -> { // 내가 설정한 리퀘스트 코드. PICTURE_REQUEST 맞으면 (갤러리에서 가져오는 기능
                if (resultCode == RESULT_OK) { // 리졸트 코드가 맞으면(호출이 되면)
                    remove_btn_id++

                    val create_frame = FrameLayout(this).apply {
                        val frame_params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                        frame_params.topMargin = 10
                        frame_params.bottomMargin = 10
                        this.layoutParams = frame_params
                    }

                    val dataUri = data?.data
                    val imageview = ImageView(this).apply {
                        var params = FrameLayout.LayoutParams(metrics.widthPixels * 5/10,  FrameLayout.LayoutParams.WRAP_CONTENT) //x 값은 디바이스 크기의 %, y는 x와 어울리는 크기만큼.
                        params.gravity = Gravity.LEFT
                        this.layoutParams = params// this.layoutParams = ViewGroup.LayoutParams(x, y) 이거로 된다.
                    }
                    Glide.with(this).load(dataUri).into(imageview)

                    val remove_btn = ImageButton(this).apply {
                        var params = FrameLayout.LayoutParams(metrics.widthPixels * 1/10, FrameLayout.LayoutParams.WRAP_CONTENT)
                        params.gravity = Gravity.RIGHT
                        this.layoutParams = params//Xml의 <layout_gravity>는  java에서 LayoutParams.setGravity(), Xml의 <gravity>는 java에서 View.setGravity()
                        this.setImageResource(R.drawable.cancelsvg)
                        this.setBackgroundResource(android.R.color.transparent)
                        this.id = remove_btn_id //이 id값을 통해서 removebtn 클릭시 저장되어 있는 id값으로 arraylist 삭제함.
                        this.visibility = View.GONE

                        this.setOnClickListener {
                            binding.imageEditLayout.removeView(frame_array[this.id])
                            binding.imageEditLayout.removeView(linear_array[this.id])
                        }
                    }


                    val create_linear = LinearLayout(this)
                    val linear_params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    create_linear.layoutParams = linear_params

                    val editText = EditText(this).apply {
                        this.setBackgroundResource(android.R.color.transparent)
                        this.typeface = Typeface.SERIF //타입페이스는 serif
                        this.textSize = 16f //사이즈는 16
                        this.gravity = left

                        this.hint = "내용을 추가하세요."
                        this.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    }

                    try {
                        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap( //사진 비트맵 형식으로 가져옴.
                                this.contentResolver,
                                dataUri
                        )
                        imageview.setImageBitmap(bitmap)

                            image_array.add(imageview)
                            button_array.add(remove_btn)
                            Edit_array.add(editText)
                            frame_array.add(create_frame)
                            linear_array.add(create_linear)

                        create_frame.addView(imageview)
                        create_frame.addView(remove_btn)
                        create_linear.addView(editText)
                        binding.imageEditLayout.addView(create_frame) // imageEditLayout 은 constraint 의 자식인 Linearlayout
                        binding.imageEditLayout.addView(create_linear)

                        Log.d("바뀌나", "${image_array[image_array.size - 1]}")
                    } catch (e: Exception) {
                        Toast.makeText(this, "오류 $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            CAMERA_REQUEST -> { // 내가 설정한 리퀘스트 코드. CAMERA_REQUEST 맞으면 카메라 사진 기능
                if(resultCode == RESULT_OK) {//이미지 성공적으로 가져왔을시
                    remove_btn_id++

                    val bitmap:Bitmap
                    val file = File(PHOTO_PATH)

                    val create_frame = FrameLayout(this).apply{
                        val frame_params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        this.layoutParams = frame_params

                    }

                    val imageview = ImageView(this).apply {

                        var params = FrameLayout.LayoutParams(metrics.widthPixels * 5/10,  FrameLayout.LayoutParams.WRAP_CONTENT) //x 값은 디바이스 크기의 %, y는 x와 어울리는 크기만큼.
                        params.gravity = Gravity.LEFT
                        this.layoutParams = params// this.layoutParams = ViewGroup.LayoutParams(x, y) 이거로 된다.
                    }
                    Glide.with(this).load(Uri.fromFile(file)).into(imageview) //uri는 file 가져온 것으로 함. 사용이유는 갤럭시에서 가끔 사진 회전된 상태로 나타남을 방지하기 위해.

                    val remove_btn = ImageButton(this).apply {
                        var params = FrameLayout.LayoutParams(metrics.widthPixels * 1/10, FrameLayout.LayoutParams.WRAP_CONTENT)
                        params.gravity = Gravity.RIGHT
                        this.layoutParams = params//Xml의 <layout_gravity>는  java에서 LayoutParams.setGravity(), Xml의 <gravity>는 java에서 View.setGravity()
                        this.setImageResource(R.drawable.cancelsvg)
                        this.setBackgroundResource(android.R.color.transparent)
                        this.id = remove_btn_id //이 id값을 통해서 removebtn 클릭시 저장되어 있는 id값으로 arraylist 삭제함.
                        this.visibility = View.GONE

                        this.setOnClickListener { // 버튼을 클릭하면 포지션에 맞는 layout들을 삭제시킴.
                            binding.imageEditLayout.removeView(frame_array[this.id])
                            binding.imageEditLayout.removeView(linear_array[this.id])
                        }
                    }

                    val create_linear = LinearLayout(this)
                    val linear_params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    create_linear.layoutParams = linear_params

                    val editText = EditText(this).apply {
                        this.setBackgroundResource(android.R.color.transparent)
                        this.typeface = Typeface.SERIF //타입페이스는 serif
                        this.textSize = 16f //사이즈는 16
                        this.gravity = left
                        this.hint = "내용을 추가하세요."
                        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }

                    if(Build.VERSION.SDK_INT < 28){ //안드로이드 9.0(pie) 미만일경우
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                        imageview.setImageBitmap(bitmap)
                    }
                    else{ //9.0보다 높을 경우
                        val decode = ImageDecoder.createSource(
                                this.contentResolver,
                                Uri.fromFile(file)
                        )
                        bitmap = ImageDecoder.decodeBitmap(decode)
                        imageview.setImageBitmap(bitmap)
                    }

                    image_array.add(imageview)
                    button_array.add(remove_btn)
                    Edit_array.add(editText)
                    frame_array.add(create_frame)
                    linear_array.add(create_linear)

                    create_frame.addView(imageview)
                    create_frame.addView(remove_btn)
                    create_linear.addView(editText)
                    binding.imageEditLayout.addView(create_frame) // imageEditLayout 은 constraint 의 자식인 Linearlayout
                    binding.imageEditLayout.addView(create_linear)

                    savePhoto(bitmap)
                }


            }
        }
    }

    private fun savePhoto(bitmap: Bitmap) {
        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" //저장공간 경로 설정.
        val time:String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "${time}.jpeg"
        val folder = File(folderPath)
        if(!folder.isDirectory){ // 디렉토리 폴더가 없을시.
            folder.mkdirs() // make directory 줄임말로 해당 경로에 폴더 자동으로 새로 만들어줌.
        }

        val out = FileOutputStream(folderPath + fileName) //출력 형태는 경로 + 이름
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) //JPEG 형태로 퀄리티 원본으로 저장.
        Toast.makeText(this, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){ // requestpermissions을 통해 arrayof는 grantresults로, requestcode는 그대로 가져와진다.
            0 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) { // grantResult가 비어있을시 혹은 0번째 확인(읽고 쓰기)가 거절인지, 1번째 확인(카메라) 가 거절인지 확인.
                    val permission_view: View = LayoutInflater.from(this).inflate(R.layout.activity_permission_intent, null)// 커스텀 다이얼로그 생성하기. 권한은 저장공간, 카메라
                    Log.d("실행 여기임", "여기임")

                    val dialog = Dialog(this)
                    dialog.setContentView(permission_view)

                    val permission_positive_btn =
                        permission_view.findViewById<Button>(R.id.warning_positive)
                    val permission_negative_btn =
                        permission_view.findViewById<Button>(R.id.warning_negative)

                    permission_positive_btn.setOnClickListener { //설정버튼 누를시 이동
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(
                                "package:" + packageName // uristring
                            )
                        ) //어플 정보를 가진 설정창으로 이동.
                        startActivity(intent)

                        dialog.dismiss()
                    }

                    permission_negative_btn.setOnClickListener {
                        dialog.dismiss()
                    }

                    var lp = WindowManager.LayoutParams()
                    lp.copyFrom(dialog.window!!.attributes)
                    lp.width = metrics.widthPixels * 7 / 10 //레이아웃 params 에 width, height 넣어주기.
                    lp.height = metrics.heightPixels * 5 / 10
                    dialog.show()
                    dialog.window!!.attributes = lp // 다이얼로그 표출 넓이 넣어주기.

                }

            }
        }
        return
    }

    private fun observemodel(){
        viewModel.getEdi().observe(this, {
            titletext = it
        })

        viewModel.getContent().observe(this, {
            contenttext = it
        })

        viewModel.Checktext().observe(this, {
            Log.d("TAG", "값은 ${viewModel.Checktext().value}")

            if (viewModel.Checktext().value != null) {
                Log.d("TAG", "글자 바뀜")

                CoroutineScope(Dispatchers.IO).launch {
                    db.RoomDao().insertDao(Diaryroom(0, now, titletext, contenttext))
                    Log.d(
                        "TAG날짜",
                        "${db.RoomDao().getAll()}"
                    )  //비동기처리로 Room에 데이터 처리. 만약 now가 똑같을 시 id가 작은것이 아래 리사이클러뷰 출력하게 만들기.
                }

                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
        
    }
    
    private fun makeRequest(){
        ActivityCompat.requestPermissions( //리퀘스트 퍼미션에 가져가기.
            this, arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ), 0
        )
    }

    private fun cameraIntent(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also{
            takePictureIntent -> takePictureIntent.resolveActivity(packageManager)?.also{
                val photofile: File? = try{
                    createImage()
                }catch (ex: IOException){
                    null
                }
                photofile?.also{
                    val photoURI:Uri = FileProvider.getUriForFile(
                            this,
                            "com.diary.diary.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST) //tagpictureIntent를 한 상태로, REQUEST코드 가져감.
                }
        }
        }
    }

    private fun createImage():File{
        val time:String = SimpleDateFormat("yyyyMMdd__HHmmss").format(Date()) //파일 구분 위해 날짜설정.
        val storageDir:File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${time}_", ".jpg", storageDir).apply{
            PHOTO_PATH = absolutePath //PHOTO_PATH에 사진 경로를 붙여넣는다
        }
    }

    private fun clickListener() {
        var toast: Toast? = null

        binding.camera.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                cameraIntent()
            } else {
                makeRequest()
            }
        } //사진 버튼 적용 끝


        binding.picture.setOnClickListener { //갤러리에서 사진 가져오기 기능

            if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                    ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED) {
                //모든 퍼미션 허용시
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT

                startActivityForResult(Intent.createChooser(intent, "사진을 가져오는 중.."), PICTURE_REQUEST)
            } else { //퍼미션 하나라도 허용이 안되어있을 시.
                makeRequest()
            }
        } //갤러리 버튼 적용 끝


        binding.notouch.setOnClickListener { //터치 활성화 이벤트
            notouch_change *= -1
            if (notouch_change == -1) {
                binding.contentTitle.isEnabled = false
                binding.contentText.isEnabled = false
                if (linear_array.isNotEmpty()) {
                    for (i in 0 until linear_array.size) {
                        Edit_array[i]?.isEnabled = false
                        image_array[i]?.isEnabled = false
                    }
                }

                if (toast == null) {
                    toast = Toast.makeText(this, "터치 비활성화", Toast.LENGTH_SHORT)
                } else { // 토스트 삭제 후 재생성.
                    toast!!.cancel()
                    toast = Toast.makeText(this, "터치 비활성화", Toast.LENGTH_SHORT)
                }
                binding.notouch.setBackgroundResource(R.drawable.btn_on)
            } else {
                binding.contentTitle.isEnabled = true
                binding.contentText.isEnabled = true

                if (linear_array.isNotEmpty()) {
                    for (i in 0 until linear_array.size) {
                        Edit_array[i]?.isEnabled = true
                        image_array[i]?.isEnabled = true
                    }
                }

                if (toast == null) {
                    toast = Toast.makeText(this, "터치 활성화", Toast.LENGTH_SHORT)
                } else { // 토스트 삭제 후 재생성.
                    toast!!.cancel()
                    toast = Toast.makeText(this, "터치 활성화", Toast.LENGTH_SHORT)
                }
                binding.notouch.setBackgroundResource(R.drawable.btn_select)
            }
            toast!!.show()
        } // 터치 활성화 버튼 끝

        binding.tag.setOnClickListener { //태그 생성 버튼
            tag_changed *= -1 // 태그 체인지드가 1일경우 tag넣는 공간 사라지게 만들기.

            if (tag_changed == 1) { // 태그 버튼이 꺼짐
                binding.tagline.visibility = View.GONE
                binding.tag.setBackgroundResource(R.drawable.btn_select)

                if (binding.etn.text.isNotEmpty()) { //값이 있으면 add, 없으면 add안함.
                    tag_array.add(tagline("# ", binding.etn.text.toString())) // 태그, 작성한 입력값을 받은 텍스트값을 매개변수로 한다.
                    binding.FlexRecycler.adapter?.notifyDataSetChanged() // 추가된 데이터 새로고침하여 변경
                }

                binding.bottomLinear.visibility = View.VISIBLE
                binding.tagline.visibility = View.GONE
            } else { // 태그 버튼이 켜짐
                binding.tagline.visibility = View.VISIBLE
                binding.bottomLinear.visibility = View.GONE // 태그, 쓰레기통 버튼 켜짐. 및 나머지 리니어 GONE

                binding.tag.setBackgroundResource(R.drawable.btn_on)

                binding.etn.setOnEditorActionListener { textView, action, event ->
                    var handled = false
                    if (action == EditorInfo.IME_ACTION_DONE) { //inputtype text의 완료버튼 선택 시 이벤트.
                        val inputmethodservice = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputmethodservice.hideSoftInputFromWindow(binding.etn.windowToken, 0)
                        inputmethodservice

                        if (binding.etn.text.isNotEmpty()) { //값이 있으면 add, 없으면 add안함.
                            tag_array.add(tagline("# ", binding.etn.text.toString())) // 태그, 작성한 입력값을 받은 텍스트값을 매개변수로 한다.
                            binding.FlexRecycler.adapter?.notifyDataSetChanged() // 추가된 데이터 새로고침하여 변경

                            binding.tagline.visibility = View.GONE
                            binding.bottomLinear.visibility = View.VISIBLE // 태그, 쓰레기통 버튼 꺼짐. 및 나머지 리니어 VISIBLE

                            binding.tag.setBackgroundResource(R.drawable.btn_select)
                            binding.etn.text = null // null 값으로 설정
                        } else {
                            binding.bottomLinear.visibility = View.VISIBLE
                            binding.tagline.visibility = View.GONE// 태그 및 나머지 리니어 visible
                            binding.tag.setBackgroundResource(R.drawable.btn_select)
                        }

                        tag_changed = 1
                        handled = true
                    }
                    handled
                }
            }
            binding.etn.text = null // null 값으로 설정
        } // 태그 버튼 끝

        binding.trash.setOnClickListener {
            trash_changed *= -1
            recy.adapter?.notifyDataSetChanged() // 쓰레기통 버튼 클릭시 remove 버튼 VISIBLE 상태로 만들기 위함. (업데이트)

            if (trash_changed == 1) {
                trash_checkd("꺼짐")
                binding.trash.setBackgroundResource(R.drawable.btn_select)
                if (button_array.size >= 1) {
                    for (i in 0 until button_array.size) {
                        button_array[i]?.visibility = View.GONE
                    }
                }
            } else {
                trash_checkd("켜짐")
                binding.trash.setBackgroundResource(R.drawable.btn_on)
                if (button_array.size >= 1) {
                    for (i in 0 until button_array.size) {
                        button_array[i]?.visibility = View.VISIBLE
                    }
                }
            }
        } //쓰레기통 버튼 적용 끝

        binding.fontChange.setOnClickListener {
            //폰트, 장문, 장단, 글자크기
            //전체가 아닌, 사용자의 커서가 위치한 곳부터 바뀌는 것이면 좋음.
            var color: String? = null
            val fontview = LayoutInflater.from(this).inflate(R.layout.font_dialog, null)

            val colorpicker = fontview.findViewById<ColorPickerView>(R.id.colorview)
            val preview = fontview.findViewById<EditText>(R.id.preview_edit)

            val btn = fontview.findViewById<Button>(R.id.line_btn)

            val btn1 = fontview.findViewById<Button>(R.id.color_btn1)
            val btn2 = fontview.findViewById<Button>(R.id.color_btn2)
            val btn3 = fontview.findViewById<Button>(R.id.color_btn3)
            val btn4 = fontview.findViewById<Button>(R.id.color_btn4)
            val btn5 = fontview.findViewById<Button>(R.id.color_btn5)
            val btn6 = fontview.findViewById<Button>(R.id.color_btn6)

            val plus = fontview.findViewById<Button>(R.id.plus)
            val minus = fontview.findViewById<Button>(R.id.minus)
            val letter_plus = fontview.findViewById<Button>(R.id.letter_plus)
            val letter_minus = fontview.findViewById<Button>(R.id.letter_minus)
            val size_plus = fontview.findViewById<Button>(R.id.size_plus)
            val size_minus = fontview.findViewById<Button>(R.id.size_minus)

            val font_positive = fontview.findViewById<Button>(R.id.font_positive)
            val font_cancel = fontview.findViewById<Button>(R.id.font_cancel)

            var local_line_spacing = line_spacing
            var local_letter_spacing = letter_spacing
            var local_text_size = text_size

            val font_recyclerview = fontview.findViewById<RecyclerView>(R.id.font_recyclerview)

            val font_array: ArrayList<font_list> = arrayListOf(font_list(Typeface.DEFAULT, "기본"), font_list(Typeface.DEFAULT_BOLD, "기본 두꺼움"), font_list(resources.getFont(R.font.bazzi), "넥슨 배찌"), font_list(resources.getFont(R.font.bmeuljiro10yearslater), "을지로 10년 후"), font_list(resources.getFont(R.font.cafe24ohsquareair), "카페24 아네모네 에어"), font_list(resources.getFont(R.font.cafe24oneprettynight), "카페24 고운밤"), font_list(resources.getFont(R.font.cafe24shiningstar), "카페24 빛나는 별"), font_list(resources.getFont(R.font.cafe24ssurroundair), "카페24 써라운드 에어"), font_list(resources.getFont(R.font.chosuncentennial_ttf), "조선 100년"), font_list(resources.getFont(R.font.heiroflightbold), "빛의 사용자 bold"), font_list(resources.getFont(R.font.heiroflightregular), "빛의 사용자 regular"), font_list(resources.getFont(R.font.koreanfrenchtypewriter), "한불 정부표준 타자기"), font_list(resources.getFont(R.font.mapoflower), "마포 꽃"), font_list(resources.getFont(R.font.sdsamliphopangchettfbasic), "삼립 호빵"), font_list(resources.getFont(R.font.yyour), "너만을 비춤")
            )

            font_recyclerview.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
            font_recyclerview.setHasFixedSize(true)
            font_recyclerview.adapter = Recycler_font(font_array, preview)

            preview.apply{
                setTextSize(TypedValue.COMPLEX_UNIT_SP, text_size)
                setTextColor(binding.contentText.textColors)
                typeface = binding.contentText.typeface
                setLineSpacing(0.0f, line_spacing)
                letterSpacing = letter_spacing
            }

            plus.setOnClickListener {
                if (local_line_spacing < 1.8f) {
                    local_line_spacing += 0.2f
                    preview.setLineSpacing(0.0f, local_line_spacing)
                }
                else{
                    toast?.cancel()
                    toast = Toast.makeText(this, "줄 간격이 최대치입니다.", Toast.LENGTH_SHORT)
                    toast?.show()
                }

            }
            minus.setOnClickListener {
                if (local_line_spacing > 1.0f) {
                    local_line_spacing -= 0.2f
                    preview.setLineSpacing(0.0f, local_line_spacing)
                }
                else {
                    toast?.cancel()
                    toast = Toast.makeText(this, "줄 간격이 최소치입니다.", Toast.LENGTH_SHORT)
                    toast?.show()
                }
            }
            letter_plus.setOnClickListener {
                if (preview.letterSpacing >= 0.5f) {
                    toast?.cancel()
                    toast = Toast.makeText(this, "자간이 최대치입니다.", Toast.LENGTH_SHORT)
                    toast?.show()
                } else
                    preview.letterSpacing += 0.1f

                local_letter_spacing = preview.letterSpacing
            }

            letter_minus.setOnClickListener {
                if (preview.letterSpacing <= -0.1f) {
                    toast?.cancel()
                    toast = Toast.makeText(this, "자간이 최소치입니다.", Toast.LENGTH_SHORT)
                    toast?.show()
                } else
                    preview.letterSpacing -= 0.1f

                local_letter_spacing = preview.letterSpacing
            }

            size_plus.setOnClickListener {
                if (local_text_size < 20f) {
                    local_text_size += 1f
                    preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, local_text_size)
                } else {
                    toast?.cancel()
                    toast = Toast.makeText(this, "사이즈가 최대치입니다.", Toast.LENGTH_SHORT)
                    toast?.show()
                }
            }
            size_minus.setOnClickListener {
                if (local_text_size > 12) {
                    local_text_size -= 1f
                    preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, local_text_size)
                } else {
                    toast?.cancel()
                    toast = Toast.makeText(this, "사이즈가 최소치입니다.", Toast.LENGTH_SHORT)
                    toast?.show()
                }
            }

            color_btn_array[0] = btn1
            color_btn_array[1] = btn2
            color_btn_array[2] = btn3
            color_btn_array[3] = btn4
            color_btn_array[4] = btn5
            color_btn_array[5] = btn6

            color_array[0] = "#000000" // 1번째 버튼에 기본 색깔인 검은 색 지정.

            for (i in 0..5) {
                if (color_array[i] != null)
                    color_btn_array[i]?.setBackgroundColor(Color.parseColor(color_array[i])) //다시 불러왔을때 버튼에 재저장.
            }

            preview.addTextChangedListener(object : TextWatcher { //EditText 2줄로 제한하기.
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    Log.d("카운트", preview.lineCount.toString())
                    if (preview.lineCount == 3) {// Edittext 입력 2줄제한
                        var str = preview.text.toString()
                        preview.setText(str.substring(0, str.length - 1))
                        preview.clearFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            val font_dialog = Dialog(this).apply {
                setContentView(fontview)
                window!!.setBackgroundDrawable(ColorDrawable(Color.argb(255, 245, 245, 245)))
            }

            font_dialog.window!!.attributes.apply { //다이얼로그 크기 지정
                width = metrics.widthPixels * 9 / 10
                height = metrics.heightPixels * 9 / 10
            }

            colorpicker.setColorListener { i, s -> //칼라피커에서 사용자가 지정한 위치에 맞는 색깔을 출력.
                if (s != "") {
                    preview.setTextColor(Color.parseColor(s))
                    preview.setHintTextColor(Color.parseColor(s))
                    color = s
                }
            }

            font_positive.setOnClickListener { // 적용버튼 클릭시
                letter_spacing = local_letter_spacing
                line_spacing = local_line_spacing
                text_size = local_text_size //임시 저장한 변수들 전역변수에 넣어주기.

                if (color != null) {
                    for (i in 0..5) {
                        if (color_array[i] == null) {
                            color_array[i] = color
                            color_btn_array[i]?.setBackgroundColor(Color.parseColor(color_array[i]))
                            Log.d("TAG", "실행 11")
                            break
                        } else if (color_array[5] == null) { //마지막인 6 번째가 안채워져 있으면 넘어감.
                            continue
                        } else {
                            if (i == 5) {
                                for (e in i downTo 2) {
                                    color_array[e] = color_array[e - 1]
                                    color_btn_array[e]?.setBackgroundColor(Color.parseColor(color_array[e - 1]))
                                    Log.d("TAG", "실행 $e")
                                }
                                color_array[1] = color
                                color_btn_array[1]?.setBackgroundColor(Color.parseColor(color_array[1]))
                            }
                        }
                    }
                }

                binding.contentText.apply{
                    typeface = preview.typeface
                    setTextColor(preview.textColors)
                    setLineSpacing(0.0f, line_spacing)
                    letterSpacing = letter_spacing
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, text_size)
                }
                binding.contentTitle.apply{
                    typeface = preview.typeface
                    setTextColor(preview.textColors)
                    setLineSpacing(0.0f, line_spacing)
                    letterSpacing = letter_spacing
                }
                binding.contentDate.apply {
                    typeface = preview.typeface
                    setTextColor(preview.textColors)
                }

                if(Edit_array.isNotEmpty()) {
                    for (i in 0 until Edit_array.size) {
                        Edit_array[i]?.apply{
                            typeface = preview.typeface
                            setTextColor(preview.textColors)
                            setLineSpacing(0.0f, line_spacing)
                            letterSpacing = letter_spacing
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, text_size)
                        }
                    }
                }
            }

                font_cancel.setOnClickListener {
                    font_dialog.dismiss()
                }

            for (i in 0..5) {
                color_btn_array[i]?.setOnClickListener {
                    if (color_array[i] != null) { //사용자가 이전에 지정한 색깔이 존재하면 실행. 없을시 실행 안함.
                        preview.setTextColor(Color.parseColor(color_array[i]))
                        preview.setHintTextColor(Color.parseColor(color_array[i]))
                    }
                }
            }

            font_dialog.show()
        } //폰트 적용 끝

        binding.backBtn.setOnClickListener { //X버튼 누를시
            if (viewModel.getEdi().value != null || viewModel.getContent().value != null) {
                Toast.makeText(this, "내용이 있음", Toast.LENGTH_SHORT).show()
                AlertDialog.Builder(this)
                        .setTitle("내용이 존재합니다. 저장하시겠습니까?")
                        .setPositiveButton("저장") { dialog, which ->
                            CoroutineScope(Dispatchers.IO).launch {
                                db.RoomDao().insertDao(Diaryroom(0, now, titletext, contenttext))
                            }

                            startActivity(Intent(this, MainActivity::class.java))
                            Log.d("TAG", "확인")
                        }
                        .setNegativeButton("취소") { dialog, which ->
                            finish()
                        }
                        .show()
                //저장할건지 물어보고 저장한다고 하면 내용 작성 안하면 삭제.
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        } //x 버튼 적용 끝
    }
}
/*
* 이미지뷰 삭제. edittext 삭제도 똑같이 하면 될듯.
            binding.imageEditLayout.removeView(image_array[image_array.size - 1]) // 삭제된다. 나중에 사용자가 삭제하고 싶은 이미지뷰 삭제방법 강구해보자.
           
            image_array.removeAt(image_array.size - 1) // 이거 안하면 array에 남아있어서 1번밖에 되질 않는다.
            * 사진 삭제 시 내용도 삭제. 내용 edittext에 사진 아래의 edittext내용 추가해준다. a.text = a.text + b.text
            *
            * setid로 버튼과 이미지, 에딧텍스트에 id를 int형으로 추가한것을 준다음 버튼 누를시 for문을 통해 int와 setid 비교 후 삭제. 근데 버튼을 어케 추가하지.
* */
