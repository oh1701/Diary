package com.diary.diary

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.diary.diary.Content_create.Companion.db
import com.diary.diary.databinding.ActivityShortcutsBinding
import com.diary.recycler.Recycler_font
import com.diary.recycler.Recycler_shortcut
import com.diary.recycler.font_list
import com.diary.recycler.font_shrotcut
import com.github.dhaval2404.colorpicker.ColorPickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShortcutsViewModel:ViewModel(){
    var back = MutableLiveData<String>()
    var fontshortcut = MutableLiveData<String>()
    var recyclerobserve = MutableLiveData<String>()
    var mystringshortcut = MutableLiveData<String>()

    fun backClick(){
        back.value = "CLICK"
    }

    fun fontshortcutClick(){
        fontshortcut.value = "CLICK"
    }

    fun mystringshortcutClick(){
        mystringshortcut.value = "CLICK"
    }
}
class Shortcuts : AppCompatActivity(), text_font, Recycler_shortcut_inter {

    lateinit var db: RoomdiaryDB
    lateinit var binding:ActivityShortcutsBinding
    lateinit var viewModel:ShortcutsViewModel
    private var fontrecycler:ArrayList<font_shrotcut> = arrayListOf()
    private var mystringrecycler:ArrayList<font_shrotcut> = arrayListOf()

    var toast:Toast? = null

    lateinit var metrics: DisplayMetrics

    private var shortcutarray:ArrayList<String> = arrayListOf() //저장한 단축키 문장과 똑같을경우 false를 주기 위함.

    private var font_checkd = "기본" //리사이클러뷰 눌러서 다이얼로그 켰는지 확인용
    private var mystring_checkd = "기본"//리사이클러뷰 눌러서 다이얼로그 켰는지 확인용

    private var mystring_editcontent = ""

    private var text_color = "#000000"
    private var shortcut_fontarray:ArrayList<String> = arrayListOf()
    
    private var edit_font = ""
    private var edit_color = "#000000" // 색깔.
    private var line_spacing = 1.0f // 라인간격 확인용
    private var letter_spacing = 0.0f // 자간 확인용
    private var text_size = 16f // 사이즈 확인용
    private var color_btn_array = arrayOfNulls<Button>(6) // 버튼 저장용
    private var color_array = arrayOfNulls<String>(6) //색상 저장용

    private var gettitle = ""
    private var position = -1

    private lateinit var fontshortcut_room:List<Shortcutroom>
    lateinit var context: Context
    var id:Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortcuts)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_shortcuts)
        viewModel = ViewModelProvider(this).get(ShortcutsViewModel::class.java)
        binding.lifecycleOwner = this
        binding.shortcuts = viewModel

        db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java, "RoomDB"
        )
                .build()

        metrics = resources.displayMetrics
        context = this

        when(Setting.darkmodechagend){
            "ON" -> {
                binding.shortcutMainlayout.setBackgroundColor(Color.parseColor("#272626"))
                binding.myShortcutMenu.setTextColor(Color.parseColor("#FB9909"))
                binding.textfontShortcut.setTextColor(Color.parseColor("#B5B2B2"))
                binding.mystringShortcuts.setTextColor(Color.parseColor("#B5B2B2"))

                binding.textfontShortcutAdd.setTextColor(Color.WHITE)
                binding.mystringShortcutsAdd.setTextColor(Color.WHITE)

                binding.backArrow.setImageResource(R.drawable.darkmode_backarrow)
                binding.image1.setImageResource(R.drawable.darkmode_arrowright)
                binding.image2.setImageResource(R.drawable.darkmode_arrowright)
            }
        }

        binding.fontShortcutList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.fontShortcutList.setHasFixedSize(true)
        binding.fontShortcutList.adapter = Recycler_shortcut(fontrecycler, binding.recyclerObserve)

        binding.mystringShortcutList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.mystringShortcutList.setHasFixedSize(true)
        binding.mystringShortcutList.adapter = Recycler_shortcut(mystringrecycler, binding.recyclerObserve)

        CoroutineScope(Dispatchers.Main).launch {
            fontshortcut_room = db.RoomDao().getshortcutAll()
            if(fontshortcut_room.isNotEmpty()){
                for(i in fontshortcut_room.indices){
                    shortcutarray.add(fontshortcut_room[i].shortcut)

                    if(fontshortcut_room[i].shortcutfont?.isNotEmpty() == true) // 폰트가 비어있지 않을시.
                        fontrecycler.add(font_shrotcut(fontshortcut_room[i].id, fontshortcut_room[i].shortcut, fontshortcut_room[i].shortcutfont, fontshortcut_room[i].shortcutmystring))
                    else if(fontshortcut_room[i].shortcutmystring != null) //폰트가 널이고 마이스트링이 존재할시. 서로 다른 리사이클러뷰에 추가하기.
                        mystringrecycler.add(font_shrotcut(fontshortcut_room[i].id, fontshortcut_room[i].shortcut, fontshortcut_room[i].shortcutfont, fontshortcut_room[i].shortcutmystring))
                    else
                        continue
                }
                binding.fontShortcutList.adapter?.notifyDataSetChanged()
                binding.mystringShortcutList.adapter?.notifyDataSetChanged()
            }
        }

        observe()
    }

    fun observe() {
        viewModel.back.observe(this, {
            onBackPressed()
        })

        viewModel.fontshortcut.observe(this, { //폰트 단축키 추가 옵저브

            line_spacing = 1.0f // 라인간격 확인용
            letter_spacing = 0.0f // 자간 확인용
            text_size = 16f // 사이즈 확인용
            color_btn_array = arrayOfNulls<Button>(6) // 버튼 저장용
            color_array = arrayOfNulls<String>(6) //색상 저장용

            font_dialog()
        })

        viewModel.mystringshortcut.observe(this, { //내 문자 단축기 추가 옵저브
            mystring_dialog()
        })

        viewModel.recyclerobserve.observe(this, { // 리사이클러뷰 어댑터 클릭 확인용.
            if(it == "폰트") {
                font_checkd = "폰트"
                gettitle = getshortcut_inter().first
                var font_List = getshortcut_inter().third

                line_spacing = font_List!!.get(0).toFloat() // 라인간격 확인용
                letter_spacing = font_List.get(1).toFloat() // 자간 확인용
                text_size = font_List.get(2).toFloat() // 사이즈 확인용
                edit_font = font_List.get(3)
                text_color = font_List.get(4)
                id = getshortcut_id().first
                position = getshortcut_id().second

                color_btn_array = arrayOfNulls<Button>(6) // 버튼 저장용
                color_array = arrayOfNulls<String>(6) //색상 저장용 다 바꾸기*/
                //여기서는 레이아웃 클릭시 observe 되는거니까, editfont와 같은것들을 해당 레이아웃의 걸로 바꿔주기.
                //font_checkd 가 폰트면 update, 아니면 insert하기.
                font_dialog()
            }
            else if(it == "문자"){
                mystring_checkd = "문자"
                gettitle = getshortcut_inter().first
                mystring_editcontent = getshortcut_inter().second!!
                id = getshortcut_id().first
                position = getshortcut_id().second
                Log.d("확인임", "확인")

                mystring_dialog()
            }
        })
    }

    fun mystring_dialog(){
        val mystringview = LayoutInflater.from(context).inflate(R.layout.shortcut_mystring, null)

        var trasharray = arrayListOf<String>()

        val mystringShortcut = mystringview.findViewById<EditText>(R.id.mystring_shortcut)
        val mystringContent = mystringview.findViewById<EditText>(R.id.mystring_content)
        val mystringOK = mystringview.findViewById<Button>(R.id.mystring_OK)
        val mystringNO = mystringview.findViewById<Button>(R.id.mystring_NO)

        var mystringDialog = Dialog(context)
        mystringDialog.setContentView(mystringview)
        mystringDialog.window!!.attributes.apply { //다이얼로그 크기 지정
            width = metrics.widthPixels * 9 / 10
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        if(mystring_checkd == "문자") {
            mystringShortcut.setText(gettitle)
            mystringContent.setText(mystring_editcontent)
        }

        mystringOK.setOnClickListener { // 적용버튼 눌렀을 경우
            if(shortcutarray.isNotEmpty()){
                for(i in shortcutarray.indices){
                    if(mystringShortcut.text.toString() == shortcutarray[i] && mystringShortcut.text.toString() != gettitle){
                        Toast.makeText(this, "등록된 단축키가 존재합니다.", Toast.LENGTH_SHORT).show()
                        break
                    }
                    else if(i == shortcutarray.size - 1){
                        if (mystringShortcut.length() > 0 && mystringContent.length() > 0) {
                            if(mystring_checkd == "기본") {
                                CoroutineScope(Dispatchers.IO).launch {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.RoomDao().insertshortcut(Shortcutroom(0, mystringShortcut.text.toString(), trasharray, mystringContent.text.toString()))
                                        shortcutarray.add(mystringShortcut.text.toString())
                                    }.join()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        var dd = db.RoomDao().getshortcut(mystringShortcut.text.toString())
                                        mystringrecycler.add(font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring))
                                        binding.mystringShortcutList.adapter?.notifyDataSetChanged()

                                        mystringDialog.dismiss()
                                    }
                                }
                            }
                            else{
                                //업데이트 시키기.
                                CoroutineScope(Dispatchers.IO).launch {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.RoomDao().updateshortcut(Shortcutroom(id, mystringShortcut.text.toString(), trasharray, mystringContent.text.toString()))
                                    }.join()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        var dd = db.RoomDao().getshortcut(mystringShortcut.text.toString())
                                        mystringrecycler[position] = font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring)
                                        binding.mystringShortcutList.adapter?.notifyDataSetChanged()


                                        if(shortcutarray.isNotEmpty()) {
                                            for (i in shortcutarray.indices) {
                                                if (gettitle == shortcutarray[i]) {
                                                    shortcutarray[i] = dd.shortcut
                                                }
                                            }
                                        }

                                        mystringDialog.dismiss()
                                    }
                                }
                            }
                        } else {
                            if(toast != null){
                                toast!!.cancel()
                                toast = Toast.makeText(this, "입력하지 않는 내용이 존재합니다.", Toast.LENGTH_SHORT)
                            }
                            else{
                                toast = Toast.makeText(this, "입력하지 않는 내용이 존재합니다.", Toast.LENGTH_SHORT)
                            }
                            toast!!.show()
                        }
                    }
                }
            }
            else{
                if (mystringShortcut.length() > 0 && mystringContent.length() > 0) {
                    if(mystring_checkd == "기본") {
                        CoroutineScope(Dispatchers.IO).launch {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.RoomDao().insertshortcut(Shortcutroom(0, mystringShortcut.text.toString(), trasharray, mystringShortcut.text.toString()))
                                shortcutarray.add(mystringShortcut.text.toString())
                            }.join()

                            CoroutineScope(Dispatchers.Main).launch {
                                var dd = db.RoomDao().getshortcut(mystringShortcut.text.toString())
                                mystringrecycler.add(font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring))
                                binding.mystringShortcutList.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    else{
                        //업데이트 시키기.
                        CoroutineScope(Dispatchers.IO).launch {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.RoomDao().updateshortcut(Shortcutroom(id, mystringShortcut.text.toString(), trasharray, mystringShortcut.text.toString()))
                            }.join()

                            CoroutineScope(Dispatchers.Main).launch {
                                var dd = db.RoomDao().getshortcut(mystringShortcut.text.toString())
                                mystringrecycler[position] = font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring)
                                binding.mystringShortcutList.adapter?.notifyDataSetChanged()


                                if(shortcutarray.isNotEmpty()) {
                                    for (i in shortcutarray.indices) {
                                        if (gettitle == shortcutarray[i]) {
                                            shortcutarray[i] = dd.shortcut
                                        }
                                    }
                                }
                            }
                        }
                    }

                    mystringDialog.dismiss()
                } else {
                    if(toast != null){
                        toast!!.cancel()
                        toast = Toast.makeText(this, "입력하지 않는 내용이 존재합니다.", Toast.LENGTH_SHORT)
                    }
                    else{
                        toast = Toast.makeText(this, "입력하지 않는 내용이 존재합니다.", Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                }
            }

            mystring_checkd = "기본"
            gettitle = ""
        }

        mystringNO.setOnClickListener {
            mystringDialog.dismiss()

            mystring_checkd = "기본"
            gettitle = ""
        }
        mystringDialog.show()
    }
    fun font_dialog(){
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
            hint = "설정 후 단축키로 지정할 문자를 넣어주세요."
            if(font_checkd == "폰트"){
                typeface = inter_roomdata_stringToFont(edit_font, context)
                setText(gettitle)
                setTextColor(Color.parseColor(text_color))
            }
            else {
                typeface = Typeface.DEFAULT
                setTextColor(Color.parseColor(edit_color))
            }

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
        if(id >= 0 )
            color_array[1] = text_color

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
                text_color = s
            }
        }

        font_positive.setOnClickListener { // 적용버튼 클릭시
            if(shortcutarray.isNotEmpty()) {
                for (i in shortcutarray.indices) {
                    if (preview.text.toString() == shortcutarray[i] && preview.text.toString() != gettitle) {
                        Toast.makeText(this, "등록된 단축키가 존재합니다.", Toast.LENGTH_SHORT).show()
                        break
                    } else if (i == shortcutarray.size - 1) {
                        if (preview.text.toString().isNotEmpty()) {
                            letter_spacing = local_letter_spacing
                            line_spacing = local_line_spacing
                            text_size = local_text_size //임시 저장한 변수들 전역변수에 넣어주기.
                            edit_font = inter_roomdata_fontToString(preview.typeface, this)

                            CoroutineScope(Dispatchers.IO).launch {
                                shortcut_fontarray.add(local_line_spacing.toString())
                                shortcut_fontarray.add(local_letter_spacing.toString())
                                shortcut_fontarray.add(local_text_size.toString())
                                shortcut_fontarray.add(inter_roomdata_fontToString(preview.typeface, context))
                                shortcut_fontarray.add(text_color)

                                if (font_checkd != "폰트") { // 리사이클러뷰를 누른 것이 아닌, 추가키를 눌렀을 경우.
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.RoomDao().insertshortcut(Shortcutroom(0, preview.text.toString(), shortcut_fontarray.toList(), null))
                                        shortcutarray.add(preview.text.toString())
                                    }.join()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        var dd = db.RoomDao().getshortcut(preview.text.toString())
                                        fontrecycler.add(font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring))
                                        binding.fontShortcutList.adapter?.notifyDataSetChanged()
                                    }
                                } else {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.RoomDao().updateshortcut(Shortcutroom(id, preview.text.toString(), shortcut_fontarray.toList(), null))
                                    }.join()

                                    CoroutineScope(Dispatchers.Main).launch {
                                        var dd = db.RoomDao().getshortcut(preview.text.toString())
                                        fontrecycler[position] = font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring)
                                        binding.fontShortcutList.adapter?.notifyDataSetChanged()

                                        if(shortcutarray.isNotEmpty()) {
                                            for (i in shortcutarray.indices) {
                                                if (gettitle == shortcutarray[i]) {
                                                    shortcutarray[i] = dd.shortcut
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            shortcut_fontarray.removeAll(shortcut_fontarray)
                            Log.d("타입은", shortcut_fontarray.toString())
                            Log.d("타입은", inter_roomdata_fontToString(preview.typeface, this))

                            font_dialog.dismiss()
                        } else {
                            if (toast != null) {
                                toast!!.cancel()
                                toast = Toast.makeText(this, "단축키로 지정할 문자를 입력해주세요.", Toast.LENGTH_SHORT)
                            } else {
                                toast = Toast.makeText(this, "단축키로 지정할 문자를 입력해주세요.", Toast.LENGTH_SHORT)
                            }
                            toast!!.show()
                        }
                    }
                }
            }
            else{
                if (preview.text.toString().isNotEmpty()) {
                    letter_spacing = local_letter_spacing
                    line_spacing = local_line_spacing
                    text_size = local_text_size //임시 저장한 변수들 전역변수에 넣어주기.
                    edit_font = inter_roomdata_fontToString(preview.typeface, this)

                    CoroutineScope(Dispatchers.IO).launch {
                        shortcut_fontarray.add(local_line_spacing.toString())
                        shortcut_fontarray.add(local_letter_spacing.toString())
                        shortcut_fontarray.add(local_text_size.toString())
                        shortcut_fontarray.add(inter_roomdata_fontToString(preview.typeface, context))
                        shortcut_fontarray.add(text_color)

                        if (font_checkd != "폰트") { // 리사이클러뷰를 누른 것이 아닌, 추가키를 눌렀을 경우.
                            CoroutineScope(Dispatchers.IO).launch {
                                db.RoomDao().insertshortcut(Shortcutroom(0, preview.text.toString(), shortcut_fontarray.toList(), null))
                                shortcutarray.add(preview.text.toString())
                            }.join()

                            CoroutineScope(Dispatchers.Main).launch {
                                var dd = db.RoomDao().getshortcut(preview.text.toString())
                                fontrecycler.add(font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring))
                                binding.fontShortcutList.adapter?.notifyDataSetChanged()

                                font_dialog.dismiss()
                            }
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.RoomDao().updateshortcut(Shortcutroom(id, preview.text.toString(), shortcut_fontarray.toList(), null))
                            }.join()

                            CoroutineScope(Dispatchers.Main).launch {
                                var dd = db.RoomDao().getshortcut(preview.text.toString())
                                fontrecycler[position] = font_shrotcut(dd.id, dd.shortcut, dd.shortcutfont, dd.shortcutmystring)
                                binding.fontShortcutList.adapter?.notifyDataSetChanged()

                                if(shortcutarray.isNotEmpty()) {
                                    for (i in shortcutarray.indices) {
                                        if (gettitle == shortcutarray[i]) {
                                            shortcutarray[i] = dd.shortcut
                                        }
                                    }
                                }
                                font_dialog.dismiss()
                            }
                        }
                    }

                    shortcut_fontarray.removeAll(shortcut_fontarray)
                    Log.d("타입은", shortcut_fontarray.toString())
                    Log.d("타입은", inter_roomdata_fontToString(preview.typeface, this))
                    font_dialog.dismiss()
                } else {
                    if (toast != null) {
                        toast!!.cancel()
                        toast = Toast.makeText(this, "단축키로 지정할 문자를 입력해주세요.", Toast.LENGTH_SHORT)
                    } else {
                        toast = Toast.makeText(this, "단축키로 지정할 문자를 입력해주세요.", Toast.LENGTH_SHORT)
                    }
                    toast!!.show()
                }
            }

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


            font_checkd = "기본"
            gettitle = ""
        }

        font_cancel.setOnClickListener {
            font_checkd = "기본"
            gettitle = ""

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
    }
}