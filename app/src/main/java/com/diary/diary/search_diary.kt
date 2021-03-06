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

        if(sharedPreferences.getString("DARK_MODE", "").toString() == "ON"){ // ??????????????? ??????????????? ???????????? ????????? ?????? ??? ????????? ?????? ??????.
            binding.searchAllLayout.setBackgroundColor(Color.parseColor("#272626"))
            binding.searchEdit.setTextColor(Color.WHITE)
            binding.searchEdit.setHintTextColor(Color.WHITE)
            binding.searchCount.setTextColor(Color.WHITE)
            binding.backArrow.setImageResource(R.drawable.darkmode_backarrow)
            darkmodesetting("????????????")
        }

        binding.searchRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.searchTagRecyclerview.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP) //????????????, ????????? ??????????????? ???????????? ??????.

        if(roomcheck){ // ?????? ?????????????????? ?????????.
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

        binding.searchEdit.addTextChangedListener(object:TextWatcher{// Livedata ????????? ?????? ???????????? ???????????? ????????? ??????.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                searchlist.clear() //?????? ????????? ?????????
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var ee = s.toString()
                var count = 0
                Log.d("??????", ee)

                searchlist.clear() //?????? ????????? ?????????

                if (roomcheck) {
                    for (i in room.size - 1 downTo 0) {
                                if (room[i].title.indexOf(ee) != -1 && ee.isNotEmpty() || room[i].content.indexOf(ee) != -1 && ee.isNotEmpty() || room[i].allcontent.indexOf(ee) != -1 && ee.isNotEmpty()) {
                                    var filtera = room[i].title.filter { it.startsWith(ee) } //?????? ????????????
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

                                        if (room[i].taglist[j].indexOf(ee.replace("#", "")) != -1 && ee.isNotEmpty() || room[i].taglist[j].indexOf(ee) != -1 && ee.isNotEmpty()) { // ?????? ?????? ??????
                                            //replace??? ???????????? #??? ???????????? ?????? ?????? ????????? ?????? ?????? ????????????. # ?????? ????????? ???????????? # ???????????? ?????? ????????? ????????? ????????? ????????????.

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

                if(ee.isEmpty()) { //????????? ???????????????
                    binding.searchCount.setText("?????? ????????? ??????")
                    binding.flexTagRecycler.visibility = View.VISIBLE
                }
                else{
                    binding.flexTagRecycler.visibility = View.GONE
                    binding.searchCount.setText("????????? ????????? ??? ${count}??? ?????????.")
                }

                binding.searchRecyclerview.adapter?.notifyDataSetChanged()
                binding.searchRecyclerview.setHasFixedSize(true)
                binding.searchRecyclerview.adapter = Recycler_main(searchlist, binding.aaaaaa, "search_diary")

            }

            override fun afterTextChanged(s: Editable?) {
                Log.d("??????", "???????????????")
            }
        })

        viewModel.back.observe(this, {
            onBackPressed()
        })
    }

    override fun onResume() {
        super.onResume()

        if(intent.hasExtra("????????????")){ //ontextchanged ???????????? ?????? onresume??? ??????.
            binding.searchEdit.setText("#${intent.getStringExtra("????????????")}")
        }
    }

    private fun Any.startsWith(string:String):Boolean{
        return true
    }
}

