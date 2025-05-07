package com.zhouyu.pet_science.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.databinding.ItemPetCardBinding
import com.zhouyu.pet_science.model.Pet
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL

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

        // 设置宠物年龄
        val ageText = buildString {
            if (pet.ageYear > 0) append("${pet.ageYear}岁")
            if (pet.ageMonth > 0) {
                if (isNotEmpty()) append(" ")
                append("${pet.ageMonth}个月")
            }
            if (isEmpty()) append("未满月")
        }
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
    }

    override fun getItemCount(): Int = pets.size
}