package com.example.dymessagelite.ui.detail

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dymessagelite.R
import com.example.dymessagelite.databinding.ActivityMessageDetailBinding
import kotlinx.coroutines.selects.whileSelect
import java.lang.Exception

class MessageDetailActivity : AppCompatActivity(),View.OnClickListener {
    private lateinit var binding: ActivityMessageDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        binding = ActivityMessageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayUser()
        initToolBar()

    }

    override fun onClick(v: View?) {

    }
    fun initToolBar(){
        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }
    }
    fun displayUser(){
        val nickname = intent.getStringExtra("nickname");
        binding.tvDetailNickname.text = nickname;
        val headImageStr = intent.getStringExtra("headImage");
        try {
            val imageId = resources.getIdentifier(
                headImageStr,
                "drawable",
                packageName
            )
            if(imageId != 0){
                binding.ivDetailAvatar.setImageResource(imageId)
            }else{
                binding.ivDetailAvatar.setImageResource(R.drawable.myhead)
            }
        }catch (e: Exception){
            e.printStackTrace()
            binding.ivDetailAvatar.setImageResource(R.drawable.myhead)
        }
    }
}