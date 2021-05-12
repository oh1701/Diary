package com.diary.diary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.CoroutinesRoom
import androidx.room.Room
import com.diary.diary.databinding.ActivityTagSettingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagViewModel:ViewModel(){
    val mytag = MutableLiveData<String>()

    fun myTagClick(){
        mytag.value = "CLICK"
    }
}
class tag_setting : AppCompatActivity() {
    lateinit var binding:ActivityTagSettingBinding
    var tag_array:ArrayList<taglist> = arrayListOf()
    lateinit var viewModel:TagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tag_setting)
        viewModel = ViewModelProvider(this).get(TagViewModel::class.java)
        binding.lifecycleOwner = this
        binding.mytag = viewModel

        var dd = arrayListOf<String>()
        var bb = 1

        var db = Room.databaseBuilder(
                applicationContext, RoomdiaryDB::class.java, "RoomDB"
        )
                .build()

        CoroutineScope(Dispatchers.IO).launch {
            var room = db.RoomDao().getAll()

            CoroutineScope(Dispatchers.IO).launch {
                for (i in room.indices) {
                    for (j in room[i].taglist.indices) {
                        dd.add(room[i].taglist[j])
                    }

                    if(i == room.size - 1){
                        for(d in dd.distinct().indices) {
                            var a = dd.distinct()
                            tag_array.add(taglist(a[d]))
                        }
                    }
                }
            }.join()

            when(Setting.darkmodechagend){
                "ON" -> {
                    binding.tagSettingMainLayout.setBackgroundColor(Color.parseColor("#272626"))
                    binding.backArrow.setImageResource(R.drawable.darkmode_backarrow)
                    binding.myUsedTag.setTextColor(Color.parseColor("#FB9909"))
                    binding.tagSize.setTextColor(Color.parseColor("#B5B2B2"))
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                binding.tagSize.setText("현재까지 작성한 태그의 갯수 : ${dd?.distinct().size}")
                binding.tagSettingRecycler.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                binding.tagSettingRecycler.setHasFixedSize(true)
                binding.tagSettingRecycler.adapter = tagRecycler(tag_array)
                Log.d("비비비비", bb.toString())
            }
        }

        observe()
    }

    fun observe(){
        viewModel.mytag.observe(this, {
            onBackPressed()
        })
    }
}

class tagRecycler(val tagsettinglist:ArrayList<taglist>): RecyclerView.Adapter<tagRecycler.ViewHolder>(){

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): tagRecycler.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_tag, parent,false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return tagsettinglist.size
    }

    override fun onBindViewHolder(holder: tagRecycler.ViewHolder, position: Int) {
        holder.tag.apply{
            setText("# ${tagsettinglist[position].tag}")
            when (Setting.darkmodechagend) {
                "ON" -> setTextColor(Color.WHITE)
                "OFF" -> setTextColor(Color.BLACK)
            }
        }

        holder.layout.apply{
            setOnClickListener {
                var intent = Intent(context, search_diary::class.java)
                intent.putExtra("태그이동", tagsettinglist[position].tag)
                context.startActivity(intent)
            }
            when(Setting.darkmodechagend){
                "ON" -> setBackgroundResource(R.drawable.layout_click_dark)
                "OFF" -> setBackgroundResource(R.drawable.layout_click)
            }
        }
    }


    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var tag = itemView.findViewById<TextView>(R.id.recycler_textfont_text)
        var layout = itemView.findViewById<LinearLayout>(R.id.recycler_tag_layout)
    }
}

class taglist(val tag:String)