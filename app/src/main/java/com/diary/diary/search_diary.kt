package com.diary.diary

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.diary.diary.MainActivity.Companion.room
import com.diary.diary.MainActivity.Companion.roomcheck
import com.diary.diary.databinding.ActivitySearchDiaryBinding
import com.diary.recycler.Recycler_main
import com.diary.recycler.Recycler_tag
import com.diary.recycler.list
import com.diary.recycler.tagline
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds


class SearchViewModel:ViewModel(){
    var tag_edit_search = MutableLiveData<String>()
    var back = MutableLiveData<String>()

    fun backClick(){
        back.value = ""
    }

}

class search_diary : AppCompatActivity(), layout_remove {

    lateinit var binding:ActivitySearchDiaryBinding
    lateinit var viewModel:SearchViewModel
    var searchlist:ArrayList<list> = arrayListOf()
    var recenttag:ArrayList<tagline> = arrayListOf()
    var dd = 0
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_diary)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_diary)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        binding.lifecycleOwner = this
        binding.search = viewModel
        sharedPreferences = getSharedPreferences("LOCK_PASSWORD", 0)


        MobileAds.initialize(this) {}
        var mAdView = binding.adview2
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON"){ // 뒤로가기로 설정창에서 다크모드 같은것 설정 후 나왔을 경우 대비.
            binding.searchAllLayout.setBackgroundColor(Color.parseColor("#272626"))
            binding.searchEdit.setTextColor(Color.WHITE)
            binding.searchEdit.setHintTextColor(Color.WHITE)
            binding.searchCount.setTextColor(Color.WHITE)
            binding.backArrow.setImageResource(R.drawable.darkmode_backarrow)
            darkmodesetting("다크모드")
        }

        binding.searchRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.searchTagRecyclerview.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP) //가로정렬, 꽉차면 다음칸으로 넘어가게 만듬.

        if(roomcheck){ // 태그 리사이클러뷰 추가용.
            loop@for(i in room.size - 1 downTo 0 ){
                for(j in room[i].taglist.size - 1 downTo 0){
                    dd++
                    recenttag.add(tagline("# ", room[i].taglist[j]))
                    if(dd == 8){
                        break@loop
                    }
                }
            }
        }

        binding.searchTagRecyclerview.setHasFixedSize(true)
        binding.searchTagRecyclerview.adapter = Recycler_tag(recenttag, binding.searchEdit)

        binding.searchEdit.addTextChangedListener(object:TextWatcher{// Livedata 사용시 너무 반복해서 출력되어 이거로 사용.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                searchlist.clear() //글자 삭제시 초기화
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var ee = s.toString()
                var count = 0
                Log.d("확인", ee)

                searchlist.clear() //글자 삭제시 초기화

                if (roomcheck) {
                    for (i in room.size - 1 downTo 0) {
                                if (room[i].title.indexOf(ee) != -1 && ee.isNotEmpty() || room[i].content.indexOf(ee) != -1 && ee.isNotEmpty() || room[i].allcontent.indexOf(ee) != -1 && ee.isNotEmpty()) {
                                    var filtera = room[i].title.filter { it.startsWith(ee) } //제목 일치여부
                                    var filterb = room[i].content.filter { it.startsWith(ee) }
                                    var filterd = room[i].allcontent.filter { it.startsWith(ee) }
                                    if (room[i].title == filtera || room[i].content == filterb || room[i].allcontent == filterd) {
                                        count++
                                        searchlist.add(list(room[i].id, room[i].title, room[i].content, room[i].uri_string_array, room[i].edit_font, room[i].date_daytofweek, room[i].daytoweek, room[i].dateLong))
                                    }
                                }
                                else if (room[i].taglist.isNotEmpty()) {
                                    loop@for (j in room[i].taglist.size - 1 downTo 0) {
                                        if(ee.startsWith("#") && ee.length > 1)
                                            ee = ee.substring(1)

                                        if (room[i].taglist[j].indexOf(ee.replace("#", "")) != -1 && ee.isNotEmpty() || room[i].taglist[j].indexOf(ee) != -1 && ee.isNotEmpty()) { // 태그 일치 여부
                                            //replace를 이용하여 #만 입력했을 경우 태그 사용한 모든 것들 나오게끔. # 이후 글자가 있을경우 # 삭제하고 해당 글자가 포함된 태그들 찾아내기.

                                            var filterc = room[i].taglist[j].filter {it.startsWith(ee) }
                                            if (room[i].taglist[j] == filterc) {
                                                count++
                                                searchlist.add(list(room[i].id, room[i].title, room[i].content, room[i].uri_string_array, room[i].edit_font, room[i].date_daytofweek, room[i].daytoweek, room[i].dateLong))
                                                break@loop
                                            }
                                        }
                                    }
                                }
                             else {
                                continue
                            }
                        }
                    }

                if(ee.isEmpty()) { //글자값 비어잇을때
                    binding.searchCount.setText("최근 사용한 태그")
                    binding.flexTagRecycler.visibility = View.VISIBLE
                }
                else{
                    binding.flexTagRecycler.visibility = View.GONE
                    binding.searchCount.setText("찾아낸 일기는 총 ${count}개 입니다.")
                }

                binding.searchRecyclerview.adapter?.notifyDataSetChanged()
                binding.searchRecyclerview.setHasFixedSize(true)
                binding.searchRecyclerview.adapter = Recycler_main(searchlist, binding.aaaaaa, "search_diary")

            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("확인", "확인이여열")
            }
        })

        viewModel.back.observe(this, {
            onBackPressed()
        })
    }

    override fun onResume() {
        super.onResume()

        if(intent.hasExtra("태그이동")){ //ontextchanged 이용하기 위해 onresume에 설정.
            binding.searchEdit.setText("#${intent.getStringExtra("태그이동")}")
        }
    }

    private fun Any.startsWith(string:String):Boolean{
        return true
    }
}

