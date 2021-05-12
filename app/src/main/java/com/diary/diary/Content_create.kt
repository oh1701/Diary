package com.diary.diary

import android.R.attr
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
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
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.diary.diary.databinding.ActivityContentCreateBinding
import com.diary.recycler.*
import com.github.dhaval2404.colorpicker.ColorPickerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.*
import java.io.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


// @내 폰트@, @모든 사진 삭제@, @시간@, @날짜@, @기념일@, @ << 와 같은 단축키 설정. observe 통해서 edit들을 확인하고, 만약 저 글자들이 포함되는 순간, 이벤트 발생. 이후 글자 삭제.
// 위의 것들은 기본 단축키. 사용자가 설정하는 것에 따라 추가 단축키를 만들고 그것을 실행하면 사용자가 설정했던 문자가 나오도록 설정하기.

// 현재 observe 부문에서 단축키 만드는중. 나중에 설정에서 단축키 설정하고 room으로 가져오기. room으로 가져온 단축키는 array로 설정해서 for문 돌리고 contain으로 비교, replace로 없애기
// 모든 종료 이벤트 시 interface의 string을 꺼짐으로 설정해주기. <<< 안해도 되는듯

class Roommodel:ViewModel(){
    private val edittitle = MutableLiveData<String>()
    private val editcontent = MutableLiveData<String>()
    var title = MutableLiveData<String>()
    var textcontent = MutableLiveData<String>()

    fun getEdi() = edittitle
    fun getContent() = editcontent

    fun Checktext() = title // 확인버튼 누르면 변경, 옵저버가 변경을 파악하여 coroutine을 통해 room에게 전송
    fun Checkcontenttext() = textcontent

    fun onclick(){
        title.value = edittitle.value
        textcontent.value = editcontent.value
        Log.d("옵저브온클릭", "보내진 것은 ${title.value}")
    }
}
lateinit var recy:RecyclerView
lateinit var tag_array:ArrayList<tagline>

class Content_create : AppCompatActivity(), text_font, Inter_recycler_remove { // intent 통해서 전해진 데이터값으로 태그를 받는다. 태그값에 따라 room에 넣어지는 값도 달리하기.

    lateinit var binding:ActivityContentCreateBinding
    var toast: Toast? = null
    var tag_room_array:ArrayList<String> = arrayListOf()

    private lateinit var shortcut:List<Shortcutroom>
    private var uri_array:ArrayList<String> = arrayListOf() // Uri주소를 uri.parse 통해 스트링으로 받아와 roop 전달.

    private var image_array:ArrayList<ImageView?> = arrayListOf() //이미지 저장 및 전달용 리스트
    private var Edit_array:ArrayList<EditText?> = arrayListOf() // 내용 저장 및 전달용 리스트
    private var edit_font = "" // 폰트 전달.
    private var edit_color = "#000000" // 색깔 전달.
    private var line_spacing = 1.0f // 라인간격 확인, 전달용
    private var letter_spacing = 0.0f // 자간 확인, 전달용
    private var text_size = 16f // 사이즈 확인, 전달용

    private var Edit_strarray: ArrayList<String?> = arrayListOf()
    private var button_array:ArrayList<ImageButton?> = arrayListOf()
    private var frame_array:ArrayList<FrameLayout?> = arrayListOf()
    private var linear_array:ArrayList<LinearLayout?> = arrayListOf()
    private var shortcuts:ArrayList<String?> = arrayListOf()

    private var color_btn_array = arrayOfNulls<Button>(6) // 버튼 저장용
    private var color_array = arrayOfNulls<String>(6) //색상 저장용

    private var notouch_change = 1 // 터치 무효화 버튼 이벤트 감지
    private var tag_changed = 1 // 태그 버튼 클릭 이벤트 감지
    private var trash_changed = 1 // 제거용(쓰레기통) 버튼 클릭 이벤트 감지


    lateinit var intent_room: Diaryroom // 리사이클러뷰 클릭 후 intent 통해서 넘어올시 가져올 데이터
    private var remove_btn_id = -1


    var titletext = "" //Dao에 넣는용도의 제목(observe 받아와서 넣어짐)
    var contenttext = "" // Dao에 넣는용도의 내용(observe 받아와서 넣어짐)
    lateinit var context:Context

    val cal = Calendar.getInstance()

    var year = cal.get(Calendar.YEAR)
    var month = cal.get(Calendar.MONTH) + 1
    var day = cal.get(Calendar.DATE)
    var dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

    var hour = cal.get(Calendar.HOUR_OF_DAY)
    var minute = cal.get(Calendar.MINUTE)
    var second = cal.get(Calendar.SECOND)


    companion object{
        lateinit var viewModel:Roommodel
        lateinit var db:RoomdiaryDB
        lateinit var sharedPreferences:SharedPreferences
        
        var CAMERA_REQUEST = 1000 // 사진 리퀘스트 코드
        var PICTURE_REQUEST = 2000 // 갤러리 리퀘스트 코드
        lateinit var PHOTO_PATH:String //사진 경로

        lateinit var metrics:DisplayMetrics // 디바이스 화면 크기 알아내는 변수
    }

    fun loadBitmapFromMediaStoreBy(photoUri: Uri?): Bitmap? {
        var image: Bitmap? = null
        try {
            image = if (Build.VERSION.SDK_INT > 27) { // Api 버전별 이미지 처리
                val source: ImageDecoder.Source =
                        ImageDecoder.createSource(contentResolver, photoUri!!)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content_create)

        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(Roommodel::class.java)
        binding.creatediary = viewModel

        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java,
                "RoomDB"
        )
                .build()

        CoroutineScope(Dispatchers.IO).launch {
            if(db.RoomDao().getshortcutAll().isNotEmpty()){
                shortcut = db.RoomDao().getshortcutAll()
            }
        }

        tag_array = arrayListOf()
        binding.FlexRecycler.layoutManager = FlexboxLayoutManager(
                this,
                FlexDirection.ROW,
                FlexWrap.WRAP
        ) //가로정렬, 꽉차면 다음칸으로 넘어가게 만듬.
        binding.FlexRecycler.setHasFixedSize(true)
        binding.FlexRecycler.adapter = Recycler_tag(tag_array, null)

        binding.contentDate.setText("${year}년, ${month}월 ${day}일")

        recy = binding.FlexRecycler
        metrics = resources.displayMetrics

        Log.d("확인", "온크리에이트")

        //레이아웃 클릭으로 들어왔을 시

        if(intent.hasExtra("이동")){
            var getId = intent.getIntExtra("이동", 0)
            Log.d("아이디확인", getId.toString())
            layout_click_intent(getId)

            var getDate = intent.getLongExtra("이동날짜", 0)
            Log.d("날짜확인", getDate.toString())

            year = getDate.toString().substring(0,4).toInt()
            month = getDate.toString().substring(4,6).toInt()
            day = getDate.toString().substring(6,8).toInt()
            hour = getDate.toString().substring(8,10).toInt()
            minute = getDate.toString().substring(10,12).toInt()
            second = getDate.toString().substring(12).toInt()
            binding.contentDate.setText("${year}년, ${month}월 ${day}일")
            Log.d("날짜용", "$year")
            Log.d("날짜용", "$month")
            Log.d("날짜용", "$day")
            Log.d("날짜용", "$hour")
            Log.d("날짜용", "$minute")
            Log.d("날짜용", "$second")//20210511225236

            var getDayofWeek = intent.getStringExtra("이동날짜요일")
            dayOfWeek = dayofweekInt(getDayofWeek!!)
        }

        sharedPreferences = getSharedPreferences("LOCK_PASSWORD", 0)
        if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON"){
            binding.contentDate.setTextColor(Color.parseColor("#FB9909"))
            binding.contentTitle.setTextColor(Color.WHITE)
            binding.contentTitle.setHintTextColor(Color.WHITE)
            binding.contentText.setTextColor(Color.WHITE)
            binding.contentText.setHintTextColor(Color.WHITE)
            binding.layout.setBackgroundColor(Color.parseColor("#272626"))
            binding.contentView.setBackgroundColor(Color.parseColor("#FB9909"))

            binding.backBtn.setImageResource(R.drawable.darkmode_cancel)
            binding.fontChange.setImageResource(R.drawable.darkmode_textfiled)
            binding.camera.setImageResource(R.drawable.darkmode_camera)
            binding.picture.setImageResource(R.drawable.darkmode_gallery)
            binding.notouch.setImageResource(R.drawable.darkmode_notouch)
            binding.tag.setImageResource(R.drawable.darkmode_tag)
            binding.trash.setImageResource(R.drawable.darkmode_trash)

            binding.success.setTextColor(Color.WHITE)
        }
        // 함수 불러올 공간
        observemodel()
        clickListener()
    }

    fun layout_click_intent(id:Int){
        CoroutineScope(Dispatchers.IO).launch {
            CoroutineScope(Dispatchers.IO).launch {
                intent_room = db.RoomDao().getlayoutid(id)

                line_spacing = intent_room.linespacing
                letter_spacing = intent_room.letterspacing
                text_size = intent_room.edit_size
                edit_color = intent_room.edit_color
                //shortcuts = intent_room.Shortcuts
            }.join()

            CoroutineScope(Dispatchers.Main).launch { //UI 관련은 Main에서 설정. 받아온 데이터들 넣는 작업 코루틴.
                binding.contentTitle.apply {
                    if(sharedPreferences.getString("DARK_MODE", "").toString() != "ON") {
                        setTextColor(Color.parseColor(intent_room.edit_color))
                    }
                    setText(intent_room.title)
                    typeface = inter_roomdata_stringToFont(intent_room.edit_font, context)
                    letterSpacing = intent_room.letterspacing
                }
                binding.contentText.apply {
                    if(sharedPreferences.getString("DARK_MODE", "").toString() != "ON")
                        setTextColor(Color.parseColor(intent_room.edit_color))

                    setText(intent_room.content)
                    typeface = inter_roomdata_stringToFont(intent_room.edit_font, context)
                    letterSpacing = intent_room.letterspacing
                    setLineSpacing(0.0f, intent_room.linespacing)
                    textSize = intent_room.edit_size
                }
                if (intent_room.uri_string_array.isNotEmpty()) {
                    for (i in intent_room.uri_string_array.indices) { //받아온 uri 사이즈 -1 만큼 반복해서 뷰 생성하게 만든다.
                        Glide.with(context).load(Uri.parse(intent_room.uri_string_array[i])).into(createView())
                        uri_array.add(intent_room.uri_string_array[i]!!)

                        Log.d("실행22", "실행되었음.$i")
                        Log.d("실행22", "실행되었음1.${intent_room.uri_string_array.size}")
                        Log.d("실행22", "실행되었음2.${Edit_array.size}")

                        Edit_array[i]!!.apply {
                            if(sharedPreferences.getString("DARK_MODE", "").toString() != "ON")
                                setTextColor(Color.parseColor(intent_room.edit_color))
                            if(intent_room.edit_string_array.isNotEmpty())
                                setText(intent_room.edit_string_array[i])

                            typeface = inter_roomdata_stringToFont(intent_room.edit_font, context)
                            letterSpacing = intent_room.letterspacing
                            setLineSpacing(0.0F, intent_room.linespacing)
                            textSize = intent_room.edit_size
                        }
                    }
                }

                if(intent_room.taglist.isNotEmpty()){
                    for(i in intent_room.taglist.indices){
                        tag_array.add(tagline("# ", intent_room.taglist[i]))
                        tag_room_array.add(intent_room.taglist[i])
                    }
                    binding.FlexRecycler.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("확인", "온리즘")
        if(Edit_array.isNotEmpty()) {
            for (i in Edit_array.indices) {
                Edit_array[i]?.typeface = binding.contentText.typeface // 현재 메인 내용의 typeface와 동일하게 설정한다.
                Edit_array[i]?.setTextColor(binding.contentText.textColors)
                Edit_array[i]?.setLineSpacing(0.0f, line_spacing)
                Edit_array[i]?.letterSpacing = letter_spacing
                Edit_array[i]?.textSize = text_size
            }
        }

        if(linear_array.isNotEmpty()) { // 쓰레기통버튼 on일경우 사진 추가했을때 remove버튼 나오게 하기.
            if (trash_changed == -1) {
                for (i in 0 until button_array.size) {
                    button_array[i]?.visibility = View.VISIBLE
                }
            }
        }

        if(notouch_change == -1) { // 터치 버튼 on일경우 사진 추가했을때 터치 안되게 하기.
            for (i in 0 until button_array.size) {
                Edit_array[i]?.isEnabled = false
                image_array[i]?.isEnabled = false
            }
        }
    }

    override fun onBackPressed() {

        if(binding.matchPhoto.visibility == View.VISIBLE) {
            binding.matchPhoto.setImageResource(0)
            binding.matchPhoto.visibility = View.GONE
        }
        else if(titletext.isNotEmpty() || contenttext.isNotEmpty()) {
            warning_dialog("백 버튼 경고")
        }
        else{
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // 사진, 갤러리 설정하기.
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICTURE_REQUEST -> { // 내가 설정한 리퀘스트 코드. PICTURE_REQUEST 맞으면 (갤러리에서 가져오는 기능
                if (resultCode == RESULT_OK) { // 리졸트 코드가 맞으면(호출이 되면)
                    remove_btn_id++

                    val imageView = createView()
                    val dataUri = data?.data //픽쳐 uri는 이거로. String list로 보내면 될듯.

                    try {

                        var picturepath = ""
                        lateinit var pathfile:File

                        fun saveBitmapToJpeg(bitmap: Bitmap) {
                            val time:String = (SimpleDateFormat("yyyyMMdd__HHmmss").format(Date())) + ".jpg" //사진 구별을 위한 이름 설정
                            //var tempFile: File? = File(cacheDir, "imgName")  //사진 구별을 위한 이름 설정
                            //var path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            try {
                                pathfile = File.createTempFile("JPEG_${time}_", ".jpg", filesDir).apply { // 경로가 데이터부분에 들어가야하는데 database 설정해야하나.
                                    picturepath = this.absolutePath  //이미지 경로 넣어주기.
                                }.also{
                                    val photoURI = FileProvider.getUriForFile(
                                            this,
                                            "com.diary.diary.fileprovider",
                                            it
                                    )
                                }
                                val folder = File(picturepath)
                                if(!folder.isDirectory){ // 디렉토리 폴더가 없을시.
                                    folder.mkdirs() // make directory 줄임말로 해당 경로에 폴더 자동으로 새로 만들어줌.
                                }

                                var out: FileOutputStream = FileOutputStream(pathfile)
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                uri_array.add(Uri.fromFile(pathfile).toString())

                                Glide.with(this).load(Uri.fromFile(pathfile)).into(imageView)

                                Log.d("경로", Uri.fromFile(pathfile).toString())
                                out.close()
                            } catch (e: Exception) {
                                Log.d("오류", e.message.toString())
                            }
                        }

                        fun uri() {
                            var instream: InputStream? = contentResolver.openInputStream(dataUri!!)
                            var bitmap = BitmapFactory.decodeStream(instream)
                            instream?.close()
                            loadBitmapFromMediaStoreBy(dataUri)?.let { saveBitmapToJpeg(it) } // 이미지 uri를 가져가서 다른 경로에 복사 저장한다.
                        }

                        uri()
                        Log.d("확인유유", Uri.parse(picturepath).toString())
                        Log.d("data", dataUri.toString())

                    } catch (e: Exception) {
                        Toast.makeText(this, "오류 $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            CAMERA_REQUEST -> { // 내가 설정한 리퀘스트 코드. CAMERA_REQUEST 맞으면 카메라 사진 기능
                if (resultCode == RESULT_OK) {//이미지 성공적으로 가져왔을시
                    remove_btn_id++

                    val bitmap: Bitmap
                    val imageView = createView() //만들어진 이미지뷰를 리턴받음.
                    val file = File(PHOTO_PATH)

                    Glide.with(this).load(Uri.fromFile(file)).into(imageView) //uri는 file 가져온 것으로 함. 사용이유는 갤럭시에서 가끔 사진 회전된 상태로 나타남을 방지하기 위해.

                    if (Build.VERSION.SDK_INT < 28) { //안드로이드 9.0(pie) 미만일경우
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                        imageView.setImageBitmap(bitmap)
                    } else { //9.0보다 높을 경우
                        val decode = ImageDecoder.createSource(
                                this.contentResolver,
                                Uri.fromFile(file)
                        )
                        bitmap = ImageDecoder.decodeBitmap(decode)
                        imageView.setImageBitmap(bitmap)
                    }

                    savePhoto(bitmap)
                }
            }
        }
    }

    private fun createView():ImageView{ // 뷰 만드는 공간

        val create_frame = FrameLayout(this).apply {
            val frame_params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            this.layoutParams = frame_params
        }

        val imageview = ImageView(this).apply {
            var params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) //x 값은 디바이스 크기의 %, y는 x와 어울리는 크기만큼.
            params.gravity = Gravity.LEFT
            this.layoutParams = params// this.layoutParams = ViewGroup.LayoutParams(x, y) 이거로 된다.
            this.setOnClickListener {
                binding.matchPhoto.setImageDrawable(this.drawable)
                binding.matchPhoto.visibility = View.VISIBLE
            }
            background = resources.getDrawable(R.drawable.imageview_cornerround, null)
            clipToOutline = true
            maxHeight = metrics.heightPixels * 4 / 10
            adjustViewBounds = true //이게 있어야 이미지뷰 maxheight가 작동한다.
            Log.d("크기", this.height.toString())
        }

        val editText = EditText(this).apply {
            this.setBackgroundResource(android.R.color.transparent)
            this.typeface = binding.contentText.typeface //기본 타입페이스는 serif(메인 기본 폰트)
            this.textSize = 16F //기본 사이즈는 16 (메인 기본 사이즈)
            this.gravity = left
            this.hint = "내용을 추가하세요."
            this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON"){
                this.setTextColor(Color.WHITE)
                this.setHintTextColor(Color.WHITE)
            }

            this.addTextChangedListener(object : TextWatcher { // mvvm패턴으로 적용하고 싶은데, 동적 추가한 뷰는 mvvm 추가할 줄 몰라서 이거로 만족하자.
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    var watcherString = s.toString()

                    if (shortcut.isNotEmpty()) {
                        for (i in shortcut.indices) {
                            if (watcherString.contains("@${shortcut[i].shortcut}@", true)) {
                                if (shortcut[i].shortcutfont?.isNotEmpty() == true) { // 폰트 단축키가 존재할시.
                                    if (toast != null) {
                                        toast!!.cancel()
                                        toast = Toast.makeText(context, "저장한 폰트가 적용되었습니다.", Toast.LENGTH_SHORT)
                                    } else {
                                        toast = Toast.makeText(context, "저장한 폰트가 적용되었습니다.", Toast.LENGTH_SHORT)
                                    }
                                    toast!!.show()

                                    binding.contentText.setLineSpacing(0.0f, shortcut[i].shortcutfont!!.get(0).toFloat())
                                    binding.contentText.letterSpacing = shortcut[i].shortcutfont!!.get(1).toFloat()
                                    binding.contentText.textSize = shortcut[i].shortcutfont!!.get(2).toFloat()
                                    binding.contentText.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)
                                    binding.contentText.setTextColor(Color.parseColor(shortcut[i].shortcutfont!!.get(4)))

                                    binding.contentTitle.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)
                                    binding.contentTitle.setTextColor(Color.parseColor(shortcut[i].shortcutfont!!.get(4)))

                                    binding.contentDate.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)

                                    line_spacing = shortcut[i].shortcutfont!!.get(0).toFloat()
                                    letter_spacing = shortcut[i].shortcutfont!!.get(1).toFloat()
                                    text_size = shortcut[i].shortcutfont!!.get(2).toFloat()
                                    edit_font = shortcut[i].shortcutfont!!.get(3)
                                    edit_color = shortcut[i].shortcutfont!!.get(4)

                                    for (j in Edit_array.indices) {
                                        Edit_array[j]?.setLineSpacing(0.0f, shortcut[i].shortcutfont!!.get(0).toFloat())
                                        Edit_array[j]?.letterSpacing = shortcut[i].shortcutfont!!.get(1).toFloat()
                                        Edit_array[j]?.textSize = shortcut[i].shortcutfont!!.get(2).toFloat()
                                        Edit_array[j]?.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)
                                        Edit_array[j]?.setTextColor(Color.parseColor(shortcut[i].shortcutfont!!.get(4)))
                                        Edit_array[j]?.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)
                                    }

                                    var a = watcherString.replace("@${shortcut[i].shortcut}@", "") //단축키 글자 삭제.
                                    this@apply.setText(a) //binding.contenttext 는 getcontent와 연결되어있음.
                                    break

                                } else if (shortcut[i].shortcutmystring != null) { //폰트 단축키 말고 내 문장 단축키가 존재할시.
                                    if (toast != null) {
                                        toast!!.cancel()
                                        toast = Toast.makeText(context, "저장한 문장을 불러왔습니다.", Toast.LENGTH_SHORT)
                                    } else {
                                        toast = Toast.makeText(context, "저장한 문장을 불러왔습니다.", Toast.LENGTH_SHORT)
                                    }
                                    toast!!.show()

                                    var a = watcherString.replace("@${shortcut[i].shortcut}@", "${shortcut[i].shortcutmystring}")
                                    this@apply.setText(a) //해당 내용을 입력한 부분에 저장한 텍스트 붙여넣기.
                                    break
                                }
                                else if (shortcut[i].shortcut == "현재 날짜"){
                                    val cal = Calendar.getInstance()

                                    var year = cal.get(Calendar.YEAR)
                                    var month = cal.get(Calendar.MONTH) + 1
                                    var day = cal.get(Calendar.DATE)
                                    
                                    var a = watcherString.replace("@${shortcut[i].shortcut}@", "${year}년 ${month}월 ${day}일") //단축키 글자 삭제.
                                    this@apply.setText(a)
                                    Toast.makeText(context, "현재 날짜 가져옴", Toast.LENGTH_SHORT).show()

                                    break
                                }
                                else if (shortcut[i].shortcut == "현재 시간"){
                                    var now = System.currentTimeMillis()
                                    var mdate = Date(now)
                                    var getTime= SimpleDateFormat("HH:mm").format(mdate)

                                    var a = watcherString.replace("@${shortcut[i].shortcut}@", "$getTime") //단축키 글자 삭제.
                                    this@apply.setText(a)
                                    Toast.makeText(context, "현재 시간 가져옴", Toast.LENGTH_SHORT).show()
                                    break
                                }
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        val remove_btn = ImageButton(this).apply {
            var params = FrameLayout.LayoutParams(metrics.widthPixels * 1 / 10, FrameLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.RIGHT or Gravity.TOP
            this.layoutParams = params//Xml의 <layout_gravity>는  java에서 LayoutParams.setGravity(), Xml의 <gravity>는 java에서 View.setGravity()
            this.setImageResource(R.drawable.cancelsvg)
            this.setBackgroundResource(android.R.color.transparent)
            this.id = remove_btn_id //이 id값을 통해서 removebtn 클릭시 저장되어 있는 id값으로 arraylist 삭제함.
            this.visibility = View.GONE

            this.setOnClickListener { // 버튼을 클릭하면 그에 맞는 layout들을 삭제시킴.
                for (i in 0 until button_array.size) { // 버튼 어레이 비교
                    if (this.id == button_array[i]?.id) { // 아이디와 버튼 어레이 아이디와 비교해서 일치시,
                        if (i > 0) { //해당 순서 모든 어레이 업데이트 및 삭제
                            Edit_array[i - 1]!!.setText(Edit_array[i - 1]?.text.toString() + Edit_array[i]?.text.toString()) //입력한 문자열은 그 전의 위치로 이동.
                        } else {
                            binding.contentText.setText(binding.contentText.text.toString() + Edit_array[i]?.text.toString()) // 만약 마지막 남은 하위 Edit일 경우 본문에 적용.
                        }
                        binding.imageEditLayout.removeView(frame_array[i])
                        binding.imageEditLayout.removeView(linear_array[i])

                        linear_array.removeAt(i)
                        frame_array.removeAt(i)
                        button_array.removeAt(i)
                        uri_array.removeAt(i)

                        image_array.removeAt(i)
                        Edit_array.removeAt(i)
                        break // break로 반복 멈춰서 버튼 누를경우 for문 재실행하게끔 만든다.
                    }
                }
            }
        }

        val create_linear = LinearLayout(this)
        val linear_params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        create_linear.layoutParams = linear_params

        create_frame.addView(imageview)
        create_frame.addView(remove_btn)
        create_linear.addView(editText)

        image_array.add(imageview)
        button_array.add(remove_btn)
        Edit_array.add(editText)

        frame_array.add(create_frame)
        linear_array.add(create_linear)

        binding.imageEditLayout.addView(create_frame) // imageEditLayout 은 constraint 의 자식인 Linearlayout
        binding.imageEditLayout.addView(create_linear)

        return imageview
    }

//content://com.android.providers.media.documents/document/image%3A6851
    // /storage/emulated/0/Pictures/20210419_143335.jpeg

    private fun savePhoto(bitmap: Bitmap) { //개별적으로 사용자를 위해 사진 저장하는 함수.
        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/" //저장공간 경로 설정.
        val time:String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "${time}.jpeg"
        val folder = File(folderPath)
        if(!folder.isDirectory){ // 디렉토리 폴더가 없을시.
            folder.mkdirs() // make directory 줄임말로 해당 경로에 폴더 자동으로 새로 만들어줌.
        }

        val out = FileOutputStream(folderPath + fileName) //출력 형태는 경로 + 이름
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) //JPEG 형태로 퀄리티 원본으로 저장.
        Log.d("PHOTO_PATH", "아웃은 ${bitmap.toString()}")
        if(toast == null){
            toast = Toast.makeText(this, "사진이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT)}
        else {
            toast!!.cancel()
            toast =Toast.makeText(this, "사진이 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT)
        }
        toast!!.show()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when(requestCode){ // requestpermissions을 통해 arrayof는 grantresults로, requestcode는 그대로 가져와진다.
            0 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) { // grantResult가 비어있을시 혹은 0번째 확인(읽고 쓰기)가 거절인지, 1번째 확인(카메라) 가 거절인지 확인.
                    warning_dialog("퍼미션 체크")
                }
            }
        }
        return
    }

    private fun warning_dialog(call_check: String){
        val permission_view: View = LayoutInflater.from(this).inflate(R.layout.activity_permission_intent, null)// 커스텀 다이얼로그 생성하기. 권한은 저장공간, 카메라

        val dialog = Dialog(this)
        dialog.setContentView(permission_view)

        val permission_positive_btn =
            permission_view.findViewById<Button>(R.id.warning_positive)
        val permission_negative_btn =
            permission_view.findViewById<Button>(R.id.warning_negative)
        val text = permission_view.findViewById<TextView>(R.id.warnig_text)

        when(call_check) {
            "퍼미션 체크" -> {
                permission_positive_btn.setOnClickListener { //설정버튼 누를시 이동
                    val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(
                            "package:" + packageName // uristring
                    )
                    ) //어플 정보를 가진 설정창으로 이동.
                    startActivity(intent)

                    dialog.dismiss()
                }


                var lp = WindowManager.LayoutParams()
                lp.copyFrom(dialog.window!!.attributes)
                lp.width = metrics.widthPixels * 7 / 10 //레이아웃 params 에 width, height 넣어주기.
                lp.height = metrics.heightPixels * 5 / 10
                dialog.show()
                dialog.window!!.attributes = lp // 다이얼로그 표출 넓이 넣어주기.

                permission_negative_btn.setOnClickListener {
                    dialog.dismiss()
                }
            }

            "백 버튼 경고" -> {
                if(intent.hasExtra("이동")) {
                    text.text = "현재 내용으로 변경 하시겠습니까?"
                    permission_positive_btn.text = "변경"
                    permission_negative_btn.text = "안함"
                }
                else {
                    text.text = "현재 기록되어 있는 내용이 존재합니다. 저장하시겠습니까?"
                    permission_positive_btn.text = "저장"
                    permission_negative_btn.text = "삭제"
                }

                permission_positive_btn.setOnClickListener {

                    if (Edit_array.isNotEmpty()) {
                        for (i in Edit_array.indices) {
                            Edit_strarray.add(Edit_array[i]!!.text.toString())
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        CoroutineScope(Dispatchers.IO).launch {
                            insert_update_dao()
                        }.join()

                        CoroutineScope(Dispatchers.Main).launch {
                            val intent = Intent(context, MainActivity::class.java)
                            intent.putExtra("이동", "이동")
                            startActivity(intent)
                            trash_checkd("꺼짐")
                        }
                    }
                }

                permission_negative_btn.setOnClickListener {
                    dialog.dismiss()

                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("이동", "이동")
                    startActivity(intent)
                    trash_checkd("꺼짐")
                }

                var lp = WindowManager.LayoutParams()
                lp.copyFrom(dialog.window!!.attributes)
                lp.width = metrics.widthPixels * 7 / 10 //레이아웃 params 에 width, height 넣어주기.
                lp.height = metrics.heightPixels * 4 / 10
                dialog.show()
                dialog.window!!.attributes = lp // 다이얼로그 표출 넓이 넣어주기.
            }

        }
    }

    private fun observemodel(){
        viewModel.Checktext().observe(this, {
            Log.d("확인", "값은 ${viewModel.Checktext().value}")

            if (viewModel.Checktext().value != null || viewModel.Checkcontenttext().value != null) {
                if (Edit_array.isNotEmpty()) {
                    for (i in Edit_array.indices) {
                        Edit_strarray.add(Edit_array[i]!!.text.toString())
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    CoroutineScope(Dispatchers.IO).launch {
                        insert_update_dao()
                    }.join()

                    CoroutineScope(Dispatchers.Main).launch {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("이동", "이동")
                        startActivity(intent)
                        trash_checkd("꺼짐")
                    }
                }
            } else {
                Toast.makeText(this, "제목이나 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.getEdi().observe(this, {
            titletext = it
        })

        viewModel.getContent().observe(this, {
            if(shortcut.isNotEmpty()) {
                for (i in shortcut.indices){
                    if(it.contains("@${shortcut[i].shortcut}@", true)){
                        if(shortcut[i].shortcutfont?.isNotEmpty() == true) { // 폰트 단축키가 존재할시.
                            if(toast != null) {
                                toast!!.cancel()
                                toast = Toast.makeText(this, "저장한 폰트가 적용되었습니다.", Toast.LENGTH_SHORT)
                            }
                            else{
                                toast = Toast.makeText(this, "저장한 폰트가 적용되었습니다.", Toast.LENGTH_SHORT)
                            }
                            toast!!.show()

                            binding.contentText.setLineSpacing(0.0f, shortcut[i].shortcutfont!!.get(0).toFloat())
                            binding.contentText.letterSpacing = shortcut[i].shortcutfont!!.get(1).toFloat()
                            binding.contentText.textSize = shortcut[i].shortcutfont!!.get(2).toFloat()
                            binding.contentText.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)
                            binding.contentText.setTextColor(Color.parseColor(shortcut[i].shortcutfont!!.get(4)))

                            binding.contentTitle.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)
                            binding.contentTitle.setTextColor(Color.parseColor(shortcut[i].shortcutfont!!.get(4)))

                            binding.contentDate.typeface = inter_roomdata_stringToFont(shortcut[i].shortcutfont!!.get(3), context)

                            line_spacing = shortcut[i].shortcutfont!!.get(0).toFloat()
                            letter_spacing = shortcut[i].shortcutfont!!.get(1).toFloat()
                            text_size = shortcut[i].shortcutfont!!.get(2).toFloat()
                            edit_font = shortcut[i].shortcutfont!!.get(3)
                            edit_color = shortcut[i].shortcutfont!!.get(4)

                            var a = it.replace("@${shortcut[i].shortcut}@", "") //단축키 글자 삭제.
                            binding.contentText.setText(a) //binding.contenttext 는 getcontent와 연결되어있음.

                            break
                        }

                        else if(shortcut[i].shortcutmystring != null){ //폰트 단축키 말고 내 문장 단축키가 존재할시.
                            if(toast != null) {
                                toast!!.cancel()
                                toast = Toast.makeText(this, "저장한 문장을 불러왔습니다.", Toast.LENGTH_SHORT)
                            }
                            else{
                                toast = Toast.makeText(this, "저장한 문장을 불러왔습니다.", Toast.LENGTH_SHORT)
                            }
                            toast!!.show()

                            var a = it.replace("@${shortcut[i].shortcut}@", "${shortcut[i].shortcutmystring}")
                            binding.contentText.setText(a) //해당 내용을 입력한 부분에 저장한 텍스트 붙여넣기.

                            break
                        }
                        else if (shortcut[i].shortcut == "현재 날짜"){
                            val cal = Calendar.getInstance()

                            var year = cal.get(Calendar.YEAR)
                            var month = cal.get(Calendar.MONTH) + 1
                            var day = cal.get(Calendar.DATE)

                            var a = it.replace("@${shortcut[i].shortcut}@", "${year}년 ${month}월 ${day}일") //단축키 글자 삭제.
                            binding.contentText.setText(a)
                            Toast.makeText(context, "현재 날짜 가져옴", Toast.LENGTH_SHORT).show()

                            break
                        }
                        else if (shortcut[i].shortcut == "현재 시간"){
                            var now = System.currentTimeMillis()
                            var mdate = Date(now)
                            var getTime= SimpleDateFormat("HH:mm").format(mdate)

                            var a = it.replace("@${shortcut[i].shortcut}@", "$getTime") //단축키 글자 삭제.
                            binding.contentText.setText(a)
                            Toast.makeText(context, "현재 시간 가져옴", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                }
            }

            contenttext = it //contenttext는 Dao에 넣는용도.
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
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photofile: File? = try {
                    createImage()
                } catch (ex: IOException) {
                    null
                }
                photofile?.also {
                    val photoURI = FileProvider.getUriForFile(
                            this,
                            "com.diary.diary.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST)
                }
            }
        }
    }

    private fun createImage():File{
        val time:String = SimpleDateFormat("yyyyMMdd__HHmmss").format(Date()) //파일 구분 위해 날짜설정.
        //val storageDir:File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File.createTempFile("JPEG_${time}_", ".jpg", filesDir).apply{
            PHOTO_PATH = absolutePath //PHOTO_PATH에 사진 경로를 붙여넣는다
        }
        uri_array.add(Uri.fromFile(tempFile).toString())

        return tempFile
    }

    private fun clickListener() {

        binding.camera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                cameraIntent()
            } else {
                makeRequest()
            }
        } //사진 버튼 적용 끝


        binding.picture.setOnClickListener { //갤러리에서 사진 가져오기 기능

            if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = ACTION_OPEN_DOCUMENT

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
                if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON")
                    binding.tag.setImageResource(R.drawable.darkmode_tag)

                binding.tagline.visibility = View.GONE
                binding.tag.setBackgroundResource(R.drawable.btn_select)

                if (binding.etn.text.isNotEmpty()) { //값이 있으면 add, 없으면 add안함.
                    tag_array.add(tagline("# ", binding.etn.text.toString())) // 태그, 작성한 입력값을 받은 텍스트값을 매개변수로 한다.
                    tag_room_array.add(binding.etn.text.toString()) // 룸 저장용 tagarray에 추가
                    binding.FlexRecycler.adapter?.notifyDataSetChanged() // 추가된 데이터 새로고침하여 변경
                }

                binding.bottomLinear.visibility = View.VISIBLE
                binding.tagline.visibility = View.GONE
            } else { // 태그 버튼이 켜짐
                if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON")
                    binding.tag.setImageResource(R.drawable.ic_baseline_tag_24)

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
                            tag_room_array.add(binding.etn.text.toString()) // 룸 저장용 tagarray에 추가
                            binding.FlexRecycler.adapter?.notifyDataSetChanged() // 추가된 데이터 새로고침하여 변경

                            binding.tagline.visibility = View.GONE
                            binding.bottomLinear.visibility = View.VISIBLE // 태그, 쓰레기통 버튼 꺼짐. 및 나머지 리니어 VISIBLE

                            if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON")
                                binding.tag.setImageResource(R.drawable.darkmode_tag)

                            binding.tag.setBackgroundResource(R.drawable.btn_select)
                            binding.etn.text = null // null 값으로 설정
                        } else {
                            if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON")
                                binding.tag.setImageResource(R.drawable.darkmode_tag)

                            binding.tag.setBackgroundResource(R.drawable.btn_select)
                            binding.bottomLinear.visibility = View.VISIBLE
                            binding.tagline.visibility = View.GONE// 태그 및 나머지 리니어 visible
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
            var color: String? = null
            val fontview = LayoutInflater.from(this).inflate(R.layout.font_dialog, null)

            val colorpicker = fontview.findViewById<ColorPickerView>(R.id.colorview)
            val preview = fontview.findViewById<EditText>(R.id.preview_edit)

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
                if (preview.letterSpacing <= 0.0f) {
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
                if (local_text_size > 16) {
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

            preview.addTextChangedListener(object : TextWatcher { //EditText 2줄로 제한하기 위한 textwatch.
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

                font_dialog.dismiss()
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
                var dd = AlertDialog.Builder(this)
                if (intent.hasExtra("이동")) {
                    dd.setTitle("현재 내용으로 변경 하시겠습니까?")
                    dd.setPositiveButton("변경") { dialog, which ->
                        if (Edit_array.isNotEmpty()) {
                            for (i in Edit_array.indices) {
                                Edit_strarray.add(Edit_array[i]!!.text.toString())
                            }
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            CoroutineScope(Dispatchers.IO).launch {
                                insert_update_dao()
                            }.join()

                            CoroutineScope(Dispatchers.Main).launch {
                                val intent = Intent(context, MainActivity::class.java)
                                intent.putExtra("이동", "이동")
                                startActivity(intent)
                                trash_checkd("꺼짐")
                            }
                        }
                    }
                    dd.setNegativeButton("안함") { dialog, which ->
                        finish()
                    }
                    dd.show()
                } else {
                    dd.setTitle("내용이 존재합니다. 저장하시겠습니까?")
                    dd.setPositiveButton("저장") { dialog, which ->
                        if (Edit_array.isNotEmpty()) {
                            for (i in Edit_array.indices) {
                                Edit_strarray.add(Edit_array[i]!!.text.toString())
                            }
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            CoroutineScope(Dispatchers.IO).launch {
                                insert_update_dao()
                            }.join()

                            CoroutineScope(Dispatchers.Main).launch {
                                val intent = Intent(context, MainActivity::class.java)
                                intent.putExtra("이동", "이동")
                                startActivity(intent)
                                trash_checkd("꺼짐")
                            }
                        }
                    }
                    dd.setNegativeButton("삭제") { dialog, which ->
                        finish()
                    }
                    dd.show()
                }
                //저장할건지 물어보고 저장한다고 하면 내용 작성 안하면 삭제.
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        } //x 버튼 적용 끝
        
        binding.contentDate.setOnClickListener {
            val year: Int = year
            val month: Int = month - 1
            val date: Int = day

            val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    binding.contentDate.setText("${year}년, ${month + 1}월 ${dayOfMonth}일")

                    this@Content_create.year = year
                    this@Content_create.month = month + 1
                    this@Content_create.day = dayOfMonth

                    var dayOfweek = "${year}0${month + 1}$dayOfMonth"
                    var dateFormat = SimpleDateFormat("yyyyMMdd")
                    var date = dateFormat.parse(dayOfweek)
                    var calendar = Calendar.getInstance()
                    calendar.time = date

                    dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                }
            }, year, month, date)
            dlg.show()

        }
    }


    suspend fun insert_update_dao(){ // 반복되는 insertdao 부르기 위함.
        var taglist = tag_room_array.toList()

        var month_string = month.toString()
        var day_string = day.toString()
        var hour_string = hour.toString()
        var minute_string = minute.toString()
        var second_string = second.toString()

        if(month < 10)
            month_string = "0$month"
        if(day < 10)
            day_string = "0$day"
        if(hour < 10)
            hour_string = "0$hour"
        if(minute < 10)
             minute_string = "0$minute"
        if (second < 10)
            second_string = "0$second"

        Log.d("시간은은은", "월$month_string, 일$day_string, 시$hour_string, 분$minute_string, 초$second_string")


        val urilist = uri_array.toList()
        val editlist = Edit_strarray.toList()
        val short = shortcuts.toList()
        edit_font = inter_roomdata_fontToString(binding.contentText.typeface, context)

        val dateLong: Long = (year.toString() + month_string + day_string + hour_string + minute_string + second_string).toLong() //이거로 리사이클러뷰 비교해서 최신 날짜가 위로 오게끔 만들기.
        Log.d("시간은", dateLong.toString())
        val date_daytofweek = "$month_string.$day_string " //이거로 리사이클러뷰 날짜 속성에 넣기. 비교시 datearray의 year를 통해서 년도 구별, moth를 통해서 월 구별하기.
        val daytoweek = dayofweekfunction(dayOfWeek)
        Log.d("요일은", date_daytofweek.toString())

        var allcontent = contenttext
        for(i in editlist.indices){
            allcontent += editlist[i]
        }

        if (color_array.isNotEmpty()) {
            if (color_array[1] != null)
                edit_color = color_array[1]!!
        }

        if(intent.hasExtra("이동")) {
            db.RoomDao().updateDao(Diaryroom(intent_room.id, titletext, contenttext, urilist,editlist, text_size, edit_font, edit_color, line_spacing, letter_spacing, short, dateLong, date_daytofweek, daytoweek, taglist, allcontent))
            Log.d("이동실행","이동실행")
        }
        else {
            db.RoomDao().insertDao(Diaryroom(0, titletext, contenttext, urilist, editlist, text_size, edit_font, edit_color, line_spacing, letter_spacing, short, dateLong, date_daytofweek, daytoweek, taglist, allcontent))
            Log.d("이동미실행","이동미실행")
        }
    }
    
    fun dayofweekfunction(week:Int):String{
        var d= ""
        when(week){
            1 ->  d ="일"
            2 ->  d ="월"
            3 ->  d ="화"
            4 ->  d ="수"
            5 ->  d ="목"
            6 ->  d ="금"
            7 ->  d ="토"
        }
        return d
    }

    fun dayofweekInt(week:String):Int{
        var d= 0
        when(week){
            "일" -> d = 1
            "월" -> d = 2
            "화"-> d = 3
            "수"-> d = 4
            "목"-> d = 5
            "금"-> d = 6
            "토"-> d = 7
        }
        return d
    }
}
/*
*             if(tag_array.isNotEmpty()){
                for(i in tag_array.indices){
                    Log.d("태그는", "$i, ${tag_array[i].tag_content}")
                }
            } 이거 변형해서 태그 insert하기.
*
* */