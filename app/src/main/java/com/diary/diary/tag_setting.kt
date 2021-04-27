package com.diary.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diary.diary.databinding.ActivityShortCutsBinding
import com.diary.diary.databinding.ActivityTagSettingBinding

class tag_setting : AppCompatActivity() {
    lateinit var binding:ActivityTagSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var dd = intent.getStringArrayListExtra("태그 설정")
        if(!dd.isNullOrEmpty()) {
            for (i in dd.indices){
                Log.d("태그는", dd[i].toString())
            }
        }

        binding.tagSize.setText("현재까지 작성한 태그의 갯수 : ${dd?.size}")

        binding.tagSettingRecycler
    }
}

class tagRecycler(val tagsettinglist:ArrayList<taglist>): RecyclerView.Adapter<tagRecycler.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): tagRecycler.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_setting_recycler_layout, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: tagRecycler.ViewHolder, position: Int) {
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return tagsettinglist.size
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }
}

class taglist(val dd:String)