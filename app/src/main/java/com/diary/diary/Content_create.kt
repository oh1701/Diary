package com.diary.diary

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.diary.diary.databinding.ActivityContentCreateBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// 현재 문제. 버튼 gravity 오른쪽으로 안가짐.
// 폰트에서 색깔은 colorpicker 사용


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

class Content_create: AppCompatActivity(), Inter_recycler_remove { // intent 통해서 전해진 데이터값으로 태그를 받는다. 태그값에 따라 room에 넣어지는 값도 달리하기.

    lateinit var binding:ActivityContentCreateBinding

    companion object{
        lateinit var viewModel:Roommodel
        lateinit var db:RoomdiaryDB

        val dateformat = DateTimeFormatter.ofPattern("yyyyMMdd")
        val now = LocalDateTime.now().format(dateformat).toLong()

        var titletext = ""
        var contenttext = ""


        var tag_changed = 1
        var trash_changed = 1
        var image_array:ArrayList<ImageView> = arrayListOf()

        var frame_layout_id = 0
        var linear_layout_id = 0
        var image_id = 0
        var image_remove_id = 0
        var edit_id = 0
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

        // 함수 불러올 공간
        observemodel()
        clickListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // 사진, 갤러리 설정하기.
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 2294){ // 내가 설정한 리퀘스트 코드. 2294가 맞으면
            if(resultCode == RESULT_OK){ // 리졸트 코드가 맞으면(호출이 되면)
                frame_layout_id++
                linear_layout_id++
                image_id++
                image_remove_id++
                edit_id++

                val create_frame = FrameLayout(this)
                val frame_params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                create_frame.layoutParams = frame_params
                create_frame.id = frame_layout_id

                val dataUri = data?.data
                var imageview = ImageView(this).apply{
                    this.id = image_id
                    this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                Glide.with(this).load(dataUri).into(imageview)

                var remove_btn = Button(this).apply{
                    this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    this.gravity = Gravity.RIGHT
                }


                val create_linear = LinearLayout(this)
                val linear_params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                create_linear.layoutParams = linear_params
                create_linear.id = linear_layout_id

                val editText = EditText(this).apply {
                    this.setBackgroundResource(android.R.color.transparent)
                    this.typeface = Typeface.SERIF //타입페이스는 serif
                    this.textSize = 16F //사이즈는 16
                    this.gravity = left
                    this.id = edit_id
                    this.hint = "내용을 추가하세요."
                    this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }

                try{
                    var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        dataUri
                    )
                    imageview.setImageBitmap(bitmap)
                    
                    image_array.add(imageview)

                    create_frame.addView(imageview)
                    create_frame.addView(remove_btn)
                    create_linear.addView(editText)
                    binding.imageEditLayout.addView(create_frame) // imageEditLayout 은 constraint 의 자식인 Linearlayout
                    binding.imageEditLayout.addView(create_linear)

                    Log.d("바뀌나", "${image_array[image_array.size - 1]}")
                }
                catch (e: Exception){
                    Toast.makeText(this, "오류 $e", Toast.LENGTH_SHORT).show()
                }
            }
            else{

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){ // requestpermissions을 통해 arrayof는 grantresults로, requestcode는 그대로 가져와진다.
            0 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) { // grantResult가 비어있을시 혹은 0번째 확인(읽고 쓰기)가 거절인지, 1번째 확인(카메라) 가 거절인지 확인.
                    val permission_view: View = LayoutInflater.from(this).inflate(
                        R.layout.activity_permission_intent,
                        null
                    )// 커스텀 다이얼로그 생성하기. 권한은 저장공간, 카메라

                    var dialog = Dialog(this)

                    var permission_positive_btn =
                        permission_view.findViewById<Button>(R.id.warning_positive)
                    var permission_negative_btn =
                        permission_view.findViewById<Button>(R.id.warning_negative)

                    permission_positive_btn.setOnClickListener { //설정버튼 누를시 이동
                        var intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(
                                "package:" + packageName
                            )
                        ) //어플 정보를 가진 설정창으로 이동.
                        startActivity(intent)
                        dialog.dismiss()
                    }

                    permission_negative_btn.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.setContentView(permission_view)

                    val size = Point()

                    val x = (size.x * 0.7f).toInt() //디바이스 사이즈 width 구하기. 70% 비율
                    val y = (size.y * 0.4f).toInt() // 디바이스 사이즈 height 구하기. 40%비율


                    var lp = WindowManager.LayoutParams()
                    lp.copyFrom(dialog.window!!.attributes)
                    lp.width = x //레이아웃 params 에 width, height 넣어주기.
                    lp.height = y
                    dialog.show()
                    dialog.window!!.attributes = lp // 다이얼로그 표출 넓이 넣어주기.

                } else {

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
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ), 0
        )
    }

    private fun clickListener(){
        var notouch_change = 1
        var toast: Toast? = null

        binding.camera.setOnClickListener {
            if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)&&
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED){

            }
            else{
                makeRequest()
            }
        }


        binding.picture.setOnClickListener { //갤러리에서 사진 가져오기 기능

            if((ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)&&
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

                startActivityForResult(Intent.createChooser(intent, "사진을 가져오는 중.."), 2294)
            }
            else{ //퍼미션 하나라도 허용이 안되어있을 시.
                makeRequest()
            }
        }


        binding.notouch.setOnClickListener { //터치 활성화 이벤트
            notouch_change *= -1
            if(notouch_change == -1) {
                binding.contentTitle.isEnabled = false
                binding.contentText.isEnabled = false

                if(toast == null) {
                    toast = Toast.makeText(this, "터치 비활성화", Toast.LENGTH_SHORT)
                }
                else{ // 토스트 삭제 후 재생성.
                    toast!!.cancel()
                    toast = Toast.makeText(this, "터치 비활성화", Toast.LENGTH_SHORT)
                }
            }
            else{
                binding.contentTitle.isEnabled = true
                binding.contentText.isEnabled = true

                if(toast == null) {
                    toast = Toast.makeText(this, "터치 활성화", Toast.LENGTH_SHORT)
                }
                else{ // 토스트 삭제 후 재생성.
                    toast!!.cancel()
                    toast = Toast.makeText(this, "터치 활성화", Toast.LENGTH_SHORT)
                }
            }
            toast!!.show()
        }

        binding.tag.setOnClickListener { //태그 생성 버튼
            tag_changed *= -1 // 태그 체인지드가 1일경우 tag넣는 공간 사라지게 만들기.

            if(tag_changed == 1){ // 태그 버튼이 꺼짐
                binding.tagline.visibility = View.GONE
                binding.tag.setBackgroundResource(R.drawable.btn_select)

                if (binding.etn.text.isNotEmpty()) { //값이 있으면 add, 없으면 add안함.
                    tag_array.add(tagline("# ", binding.etn.text.toString())) // 태그, 작성한 입력값을 받은 텍스트값을 매개변수로 한다.
                    binding.FlexRecycler.adapter?.notifyDataSetChanged() // 추가된 데이터 새로고침하여 변경
                }

                binding.bottomLinear.visibility = View.VISIBLE
                binding.tagline.visibility = View.GONE
            }
            else { // 태그 버튼이 켜짐
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

                            tag_changed = 1
                            binding.tagline.visibility = View.GONE
                            binding.bottomLinear.visibility = View.VISIBLE // 태그, 쓰레기통 버튼 꺼짐. 및 나머지 리니어 VISIBLE

                            binding.tag.setBackgroundResource(R.drawable.btn_select)

                            binding.etn.text = null // null 값으로 설정
                        }
                        else{
                            binding.bottomLinear.visibility = View.VISIBLE
                            binding.tagline.visibility = View.GONE// 태그 및 나머지 리니어 visible
                        }
                        handled = true
                    }
                    handled
                }
            }
            binding.etn.text = null // null 값으로 설정
        }

        binding.trash.setOnClickListener {
            trash_changed *= -1
            recy.adapter?.notifyDataSetChanged() // 쓰레기통 버튼 클릭시 remove 버튼 VISIBLE 상태로 만들기 위함. (업데이트)

            if(trash_changed == 1){
                trash_checkd("꺼짐")
                binding.trash.setBackgroundResource(R.drawable.btn_select)
            }
            else{
                trash_checkd("켜짐")
                binding.trash.setBackgroundResource(R.drawable.btn_on)
            }

        }

        binding.fontChange.setOnClickListener {
            //폰트, 장문, 장단, 글자크기, 글씨색(colorpicker 로 사용자가 원하는 색상 결정하게 만들어주기.)
            //전체가 아닌, 사용자의 커서가 위치한 곳부터 바뀌는 것이면 좋음.
            //폰트 조금 더 늘리자.

        }

        binding.backBtn.setOnClickListener { //X버튼 누를시
            if(viewModel.getEdi().value != null || viewModel.getContent().value != null){
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
                        .setNegativeButton("취소"){ dialog, which ->
                            finish()
                        }
                        .show()
                //저장할건지 물어보고 저장한다고 하면 내용 작성 안하면 삭제.
            }
            else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
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
