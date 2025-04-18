package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityUserInfoEditBinding
import com.zhouyu.pet_science.databinding.DialogAddPetBinding
import com.zhouyu.pet_science.databinding.ItemPetInfoBinding
import com.zhouyu.pet_science.network.UserHttpUtils
import com.zhouyu.pet_science.pojo.CityJsonBean
import com.zhouyu.pet_science.tools.GetJsonDataUtil.getJson
import com.zhouyu.pet_science.tools.GetJsonDataUtil.parseData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserInfoEditActivity : BaseActivity() {
    // 简单的 Pet 数据类
    data class PetInfo(
        val id: Long = System.currentTimeMillis(), // 临时 ID
        var name: String,
        var type: String, // "cat", "dog", "other"
        var breed: String,
        var ageYear: Int,
        var ageMonth: Int
    )

    private lateinit var binding: ActivityUserInfoEditBinding
    private var selectedAvatarUri: Uri? = null
    private val pets = mutableListOf<PetInfo>()
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    // ActivityResultLauncher 用于选择图片
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            Glide.with(this)
                .load(it)
                .apply(RequestOptions.circleCropTransform()) // 圆形裁剪
                .placeholder(R.drawable.developer_icon) // 默认头像
                .into(binding.ivAvatarPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTopBarView(binding.main, true)

        setupViews()
        setupClickListeners()

        updatePetListView() // 初始时更新宠物列表（显示空提示）
    }

    private fun setupViews() {
        // 个人简介字数统计
        binding.etBio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                binding.tvBioCount.text = "${s?.length ?: 0}/100"
            }
        })
    }

    private fun setupClickListeners() {
        // 跳过按钮
        binding.btnSkip.setOnClickListener {
            navigateToMain()
        }

        // 完成按钮
        binding.btnComplete.setOnClickListener {
            saveUserInfo()
        }

        // 上传头像
        binding.ivAvatarPreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.tvUploadAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 选择生日
        binding.tvBirthday.setOnClickListener {
            showDatePickerDialog()
        }

        initJsonData() // 初始化省市区数据
        // 添加地区选择
        binding.tvLocation.setOnClickListener {
            // 确保数据已加载
            if (options1Items.isNotEmpty() && options2Items.isNotEmpty()) {
                showDistrictPickerView()
            } else {
                showToast("地区数据加载中...")
            }
        }

        // 添加宠物按钮
        binding.btnAddPet.setOnClickListener {
            showAddPetDialog()
        }
    }


    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.tvBirthday.text = dateFormat.format(calendar.time)
        }

        DatePickerDialog(this, dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showAddPetDialog(petToEdit: PetInfo? = null) {
        val dialogBinding = DialogAddPetBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // 设置自定义标题文本 (如果需要动态设置)
        dialogBinding.tvTitle.text = if (petToEdit == null) "添加宠物" else "编辑宠物"

        // 如果是编辑，填充现有数据
        if (petToEdit != null) {
            dialogBinding.etPetName.setText(petToEdit.name)
            when (petToEdit.type) {
                "cat" -> dialogBinding.rbPetTypeCat.isChecked = true
                "dog" -> dialogBinding.rbPetTypeDog.isChecked = true
                "other" -> dialogBinding.rbPetTypeOther.isChecked = true
            }
            dialogBinding.etPetBreed.setText(petToEdit.breed)
            dialogBinding.etPetAgeYear.setText(petToEdit.ageYear.toString())
            dialogBinding.etPetAgeMonth.setText(petToEdit.ageMonth.toString())
        } else {
             dialogBinding.rbPetTypeCat.isChecked = true // 默认选中猫咪
        }

        // 为自定义的确认按钮设置点击监听器
        dialogBinding.btnConfirm.setOnClickListener {
            val name = dialogBinding.etPetName.text.toString().trim()
            if (name.isEmpty()) {
                showToast("请输入宠物名称")
                return@setOnClickListener // 保持在对话框内
            }

            val typeId = dialogBinding.rgPetType.checkedRadioButtonId
            val type = when (typeId) {
                R.id.rbPetTypeDog -> "dog"
                R.id.rbPetTypeOther -> "other"
                else -> "cat" // 默认是 cat
            }
            val breed = dialogBinding.etPetBreed.text.toString().trim()
            val ageYear = dialogBinding.etPetAgeYear.text.toString().toIntOrNull() ?: 0
            val ageMonth = dialogBinding.etPetAgeMonth.text.toString().toIntOrNull() ?: 0

            if (petToEdit == null) {
                // 添加新宠物
                val newPet = PetInfo(
                    name = name,
                    type = type,
                    breed = breed.ifEmpty { "未知品种" },
                    ageYear = ageYear,
                    ageMonth = ageMonth
                )
                pets.add(newPet)
            } else {
                // 更新现有宠物
                petToEdit.name = name
                petToEdit.type = type
                petToEdit.breed = breed.ifEmpty { "未知品种" }
                petToEdit.ageYear = ageYear
                petToEdit.ageMonth = ageMonth
            }

            updatePetListView()
            dialog.dismiss() // 关闭对话框
        }

        // 为自定义的取消按钮设置点击监听器
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss() // 关闭对话框
        }

        // 在 dialog.show() 之前设置窗口背景
        dialog.window?.setBackgroundDrawableResource(R.drawable.view_radius) // 应用圆角背景

        dialog.show()
        // 设置对话框宽度为屏幕宽度的百分比
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window.attributes) // 复制现有属性

            // 获取屏幕宽度
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // 设置宽度为屏幕宽度的 80%
            layoutParams.width = (screenWidth * 0.9).toInt()
            // 高度可以保持自适应
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = layoutParams // 应用新的布局参数
        }
    }

    // 更新宠物列表显示
    private fun updatePetListView() {
        binding.layoutPetList.removeAllViews() // 清空旧视图

        if (pets.isEmpty()) {
            binding.tvEmptyPetTip.visibility = View.VISIBLE
        } else {
            binding.tvEmptyPetTip.visibility = View.GONE
            pets.forEach { pet ->
                addPetItemView(pet)
            }
        }
    }

    // 添加单个宠物信息的视图
    @SuppressLint("SetTextI18n")
    private fun addPetItemView(pet: PetInfo) {
        val itemBinding = ItemPetInfoBinding.inflate(LayoutInflater.from(this), binding.layoutPetList, false)

        itemBinding.tvPetName.text = pet.name

        val ageText = buildString {
            if (pet.ageYear > 0) append("${pet.ageYear}岁")
            if (pet.ageMonth > 0) {
                if (isNotEmpty()) append(" ")
                append("${pet.ageMonth}个月")
            }
            if (isEmpty()) append("未满月")
        }
        itemBinding.tvPetBreedAge.text = "${pet.breed} · $ageText"

        val iconRes = when (pet.type) {
            "dog" -> R.drawable.ic_dog // 需要准备 dog 图标
            "other" -> R.drawable.ic_paw // 需要准备 paw 图标
            else -> R.drawable.ic_cat // 需要准备 cat 图标
        }
        itemBinding.ivPetIcon.setImageResource(iconRes)

        // 删除按钮
        itemBinding.btnPetDelete.setOnClickListener {
            pets.remove(pet)
            updatePetListView()
        }

        // 点击编辑
        itemBinding.root.setOnClickListener {
            showAddPetDialog(pet)
        }


        binding.layoutPetList.addView(itemBinding.root)
    }

    private var options1Items = ArrayList<CityJsonBean>() //省
    private val options2Items = ArrayList<ArrayList<String>>() //市
    private var selectedLocationText = ""

    private fun initJsonData() {
        //解析数据 （省市区三级联动）
        val jsonData: String = getJson(this, "province_new.json") //获取assets目录下的json文件数据
        val jsonBean: ArrayList<CityJsonBean> = parseData(jsonData) //用Gson 转成实体

        //  添加省份数据
        options1Items = jsonBean
        for (i in jsonBean.indices) { //遍历省份
            val cityList = ArrayList<String>() //该省的城市列表（第二级）
            val provinceAreaList =
                ArrayList<ArrayList<String>>() //该省的所有地区列表（第三级）
            for (c in 0 until jsonBean[i].cityList.size) { //遍历该省份的所有城市
                val cityName = jsonBean[i].cityList[c].name
                cityList.add(cityName) //添加城市
                val cityAreaList = ArrayList<String>() //该城市的所有地区列表

                if (jsonBean[i].cityList[c].area == null
                    || jsonBean[i].cityList[c].area.isEmpty()
                ) {
                    cityAreaList.add("")
                } else {
                    cityAreaList.addAll(jsonBean[i].cityList[c].area)
                }
                provinceAreaList.add(cityAreaList) //添加该省所有地区数据
            }
            //添加城市数据
            options2Items.add(cityList)
        }
    }

    private var selectedProvince = 0
    private var selectedCity = 0
    private var districtSplit = " - " // 省市分隔符
    /**
     * 显示地区dialog
     */
    private fun showDistrictPickerView() {
        // 获取当前选中的省市
        val currentLocation = binding.tvLocation.text.toString()
        if (currentLocation.isNotEmpty() && currentLocation != "请选择所在地") {
            val parts = currentLocation.split(districtSplit)
            val provinceName = parts.getOrNull(0) ?: ""
            val cityName = parts.getOrNull(1) ?: ""
            // Find province index
            selectedProvince = options1Items.indexOfFirst { it.pickerViewText == provinceName }
            if (selectedProvince != -1) {
                // Find city index within that province
                selectedCity = options2Items.getOrNull(selectedProvince)?.indexOfFirst { it == cityName } ?: 0
                if (selectedCity == -1) selectedCity = 0
            }
        }

        // 弹出选择器（省市联动）
        val pvOptions: OptionsPickerView<*> = OptionsPickerBuilder(
            this
        ) { options1: Int, options2: Int, _: Int, _: View? ->
            //返回的分别是三个级别的选中位置
            val provinceName = options1Items.getOrNull(options1)?.pickerViewText ?: ""
            val cityName = options2Items.getOrNull(options1)?.getOrNull(options2) ?: ""
            // 拼接最终显示的文本
            selectedLocationText = buildString {
                append(provinceName)
                if (cityName.isNotEmpty()) {
                    append(districtSplit) // 使用-分隔
                    append(cityName)
                }
            }.trim()

            // 更新 TextView 显示
            binding.tvLocation.text = selectedLocationText

        }.setTitleText("请选择您所在的地区")
            .setTextColorCenter(getColor(R.color.textGeneral)) //设置选中项文字颜色
            .setContentTextSize(20)
            .setCancelText("取消") //取消按钮文字
            .setSubmitText("确定") //确认按钮文字
            .setContentTextSize(14) //滚轮文字大小
            .setTitleSize(16) //标题文字大小
            .setOutSideCancelable(true) //点击屏幕，点在控件外部范围时，是否取消显示
            .setTitleColor(getColor(R.color.textGeneral)) //标题文字颜色
            .setSubmitColor(getColor(R.color.Theme)) //确定按钮文字颜色
            .setCancelColor(getColor(R.color.Theme)) //取消按钮文字颜色
            // .setTitleBgColor(getColor(R.color.themeColor)) //标题背景颜色 Night mode (根据你的主题调整)
            // .setBgColor(getColor(R.color.viewColor)) //滚轮背景颜色 Night mode (根据你的主题调整)
            .isDialog(false) //是否显示为对话框样式
            .build<Any>()

        // 设置选择器数据
        pvOptions.setPicker(options1Items as List<Nothing>, options2Items as List<List<Nothing>>) // 二级联动
        pvOptions.setSelectOptions(selectedProvince, selectedCity) // 设置默认选中项
        pvOptions.show()
    }


    private fun saveUserInfo() {
        val nickname = binding.etNickname.text.toString().trim()
        if (nickname.length < 2) {
            showToast("昵称至少需要2个字符")
            return
        }

        val selectedGenderId = binding.rgGender.checkedRadioButtonId
        val gender = when (selectedGenderId) {
            R.id.rbGenderFemale -> "1"
            R.id.rbGenderOther -> "2"
            else -> "0"
        }
        val birthday = binding.tvBirthday.text.toString()
        // --- 从 TextView 获取地区信息 ---
        val location = binding.tvLocation.text.toString().takeIf { it != "请选择所在地" } ?: ""
        val bio = binding.etBio.text.toString().trim()


         executeThread {
            val result = UserHttpUtils.updateUserInfo(
                avatarUri = selectedAvatarUri, // 可能需要处理 Uri 到文件的转换
                nickname = nickname,
                gender = gender,
                birthday = birthday,
                location = location,
                bio = bio,
                pets = pets
            )
            runOnUiThread {
                if (result.first) {
                    showToast("信息保存成功")
                    navigateToMain()
                } else {
                    showToast("保存失败: ${result.second}")
                }
            }
         }

//        // 临时模拟保存成功
//        ConsoleUtils.logErr("保存用户信息: nickname=$nickname, gender=$gender, birthday=$birthday, location=$location, bio=$bio")
//        ConsoleUtils.logErr("宠物信息: ${pets.joinToString()}")
//        showToast("信息已保存（模拟）")
//        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // 关闭当前 Activity
    }
}