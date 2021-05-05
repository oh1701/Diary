package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.databinding.ActivityTagSettingBinding

class tag_setting : AppCompatActivity() {
    lateinit var binding:ActivityTagSettingBinding
    var tag_array:ArrayList<taglist> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var dd = intent.getStringArrayListExtra("태그 설정")
        if(!dd.isNullOrEmpty()) {
            for (i in dd.indices){
                Log.d("태그는", dd[i].toString())
                tag_array.add(taglist(dd[i].toString()))
            }
        }


        binding.tagSize.setText("현재까지 작성한 태그의 갯수 : ${dd?.size}")
        binding.tagSettingRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.tagSettingRecycler.setHasFixedSize(true)
        binding.tagSettingRecycler.adapter = tagRecycler(tag_array)
    }
}

class tagRecycler(val tagsettinglist:ArrayList<taglist>): RecyclerView.Adapter<tagRecycler.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): tagRecycler.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_setting_recycler_layout, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: tagRecycler.ViewHolder, position: Int) {
        holder.tag.setText("# ${tagsettinglist[position].dd}")
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return tagsettinglist.size
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var tag = itemView.findViewById<TextView>(R.id.tag_text_recycler)
    }
}

class taglist(val dd:String)