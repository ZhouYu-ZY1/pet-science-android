package com.zhouyu.pet_science.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.UserInfoEditActivity
import com.zhouyu.pet_science.databinding.ItemPetCardBinding
import com.zhouyu.pet_science.model.Pet
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.utils.ConsoleUtils
import java.util.Calendar
import java.util.Date

class PetAdapter(private val context: Context, private val pets: List<Pet>) :
    RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(val binding: ItemPetCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetCardBinding.inflate(LayoutInflater.from(context), parent, false)
        return PetViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        val binding = holder.binding

        binding.tvPetName.text = pet.name
        binding.tvPetBreed.text = pet.breed

        // 计算宠物年龄
        val ageText = calculatePetAgeText(pet.birthday)
        binding.tvPetAge.text = ageText

        // 加载宠物头像
        if (pet.avatarUrl.isNotEmpty()) {
            Glide.with(context)
                .load(BASE_URL + pet.avatarUrl)
                .apply(RequestOptions())
                .transform(CircleCrop())
                .into(binding.ivPetAvatar)
        } else {
            // 根据宠物类型设置默认图标
            val iconRes = when (pet.type) {
                "dog" -> R.mipmap.default_dog
                "other" -> R.drawable.ic_paw
                else -> R.mipmap.default_cat
            }
            binding.ivPetAvatar.setImageResource(iconRes)
        }
        holder.itemView.setOnClickListener{
            context.startActivity(Intent(context, UserInfoEditActivity::class.java).apply {
                putExtra("type", "editPetInfo")
                putExtra("petId", pet.id)
            })
        }
    }

    override fun getItemCount(): Int = pets.size
}

// 计算宠物年龄文本
private fun calculatePetAgeText(birthday: Date): String {
    val now = Calendar.getInstance()
    val birthCal = Calendar.getInstance().apply { time = birthday }
    
    var years = now.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
    
    if (months < 0) {
        years--
        months += 12
    }
    
    return buildString {
        if (years > 0) append("${years}岁")
        if (months > 0) {
            if (isNotEmpty()) append(" ")
            append("${months}个月")
        }
        if (isEmpty()) append("未满月")
    }
}