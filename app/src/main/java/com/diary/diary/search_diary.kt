package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.diary.recycler.list


class SearchViewModel:ViewModel(){
    var tag_edit_search = MutableLiveData<String>()
}

class search_diary : AppCompatActivity() {

    lateinit var binding:ActivitySearchDiaryBinding
    lateinit var viewModel:SearchViewModel
    var searchlist:ArrayList<list> = arrayListOf()
    var dd = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_diary)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_diary)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        binding.lifecycleOwner = this
        binding.search = viewModel

        binding.searchRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.searchEdit.addTextChangedListener(object:TextWatcher{// Livedata 사용시 너무 반복해서 출력되어 이거로 사용.
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var ee = s.toString()
                var count = 0
                Log.d("확인", ee)

                searchlist.clear() //글자 삭제시 초기화

                if (roomcheck) {
                    for (i in room.size - 1 downTo 0) {
                                if (room[i].title.indexOf(ee) != -1 && ee.isNotEmpty()) {
                                    var filtera = room[i].title.filter { it.startsWith(ee) } //제목 일치여부
                                    if (room[i].title == filtera) {
                                        count++
                                        searchlist.add(list(room[i].id, room[i].title, room[i].content, room[i].uri_string_array, room[i].edit_font, room[i].date_daytofweek, room[i].daytoweek, room[i].dateLong))
                                    }
                                } else if (room[i].content.indexOf(ee) != -1 && ee.isNotEmpty()) { // 메인 내용 일치 여부
                                    var filterb = room[i].content.filter { it.startsWith(ee) }
                                    if (room[i].content == filterb) {
                                        count++
                                        searchlist.add(list(room[i].id, room[i].title, room[i].content, room[i].uri_string_array, room[i].edit_font, room[i].date_daytofweek, room[i].daytoweek, room[i].dateLong))
                                    }
                                }  else if (room[i].allcontent.indexOf(ee) != -1 && ee.isNotEmpty()) { // 모든 내용 일치 여부
                                    var filterd = room[i].allcontent.filter { it.startsWith(ee) }
                                    if (room[i].allcontent == filterd) {
                                        count++
                                        searchlist.add(list(room[i].id, room[i].title, room[i].content, room[i].uri_string_array, room[i].edit_font, room[i].date_daytofweek, room[i].daytoweek, room[i].dateLong))
                                    }
                                }
                                else if (room[i].taglist.isNotEmpty()) {
                                    for (j in room[i].taglist.size - 1 downTo 0) {
                                        if (room[i].taglist[j].indexOf(ee) != -1 && ee.isNotEmpty()) { // 태그 일치 여부
                                            var filterc = room[i].taglist[j].filter { it.startsWith(ee) }
                                            if (room[i].taglist[j] == filterc) {
                                                count++
                                                searchlist.add(list(room[i].id, room[i].title, room[i].content, room[i].uri_string_array, room[i].edit_font, room[i].date_daytofweek, room[i].daytoweek, room[i].dateLong))
                                            }
                                        }
                                    }
                                }
                             else {
                                continue
                            }
                        }
                    }

                binding.searchCount.setText("찾아낸 일기는 총 ${count}개 입니다.")
                binding.searchRecyclerview.adapter?.notifyDataSetChanged()
                binding.searchRecyclerview.setHasFixedSize(true)
                binding.searchRecyclerview.adapter = Recycler_main(searchlist, binding.aaaaaa, "search_diary")
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun Any.startsWith(string:String):Boolean{
        return true
    }
}

