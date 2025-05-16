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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityUserInfoEditBinding
import com.zhouyu.pet_science.databinding.DialogAddPetBinding
import com.zhouyu.pet_science.databinding.ItemPetInfoBinding
import com.zhouyu.pet_science.fragments.PersonalCenterFragment
import com.zhouyu.pet_science.model.Pet
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.HttpUtils
import com.zhouyu.pet_science.network.PetHttpUtils
import com.zhouyu.pet_science.network.UserHttpUtils
import com.zhouyu.pet_science.pojo.CityJsonBean
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.FileUtils
import com.zhouyu.pet_science.utils.GetJsonDataUtil.getJson
import com.zhouyu.pet_science.utils.GetJsonDataUtil.parseData
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.views.dialog.MyDialog
import com.zhouyu.pet_science.views.dialog.MyProgressDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UserInfoEditActivity : BaseActivity() {
    private lateinit var binding: ActivityUserInfoEditBinding
    private var selectedAvatarUri: Uri? = null
    private var uploadUrl: String? = null
    private var pets = mutableListOf<Pet>()
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var editType: String = ""
    private var editPetId: Long = -1L

    // 临时存储选择的头像URI和上传后的URL
    private var selectedPetAvatarUri: Uri? = null
    private var uploadedPetAvatarUrl: String? = null

    // ActivityResultLauncher 用于选择图片
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            Glide.with(this)
                .load(it)
                .apply(RequestOptions())
                .transform(CircleCrop())
                .placeholder(R.drawable.default_avatar) // 默认头像
                .into(binding.ivAvatarPreview)

            // 上传头像
            uploadAvatar(it)
        }
    }
    
    // ActivityResultLauncher 用于选择宠物图片
    private val pickPetImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedPetAvatarUri = it
            val dialogBinding = getPetDialogBinding()
            if(dialogBinding != null) {
                Glide.with(this)
                    .load(it)
                    .apply(RequestOptions())
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_paw) 
                    .into(dialogBinding.ivPetAvatarPreview)

                // 如果有正在编辑的宠物ID，则立即上传头像
                if (editPetId > 0) {
                    uploadPetAvatar(it, editPetId,false)
                }
            } else {
                showToast("对话框未初始化")
            }
        }
    }
    
    // 保存当前宠物对话框的绑定引用
    private var currentPetDialogBinding: DialogAddPetBinding? = null
    
    // 获取当前的宠物对话框绑定
    private fun getPetDialogBinding(): DialogAddPetBinding? {
        return currentPetDialogBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editType = intent.getStringExtra("type")?.trim() ?: ""

        setTopBarView(binding.main, true)

        setupViews()
        setupClickListeners()

        getUserData()
    }

    private fun setupViews() {
        when (editType) {
            "editUserInfo" -> {
                // 编辑用户信息
                binding.tvTitle.text = "编辑个人信息"
                binding.btnSkip.visibility = View.GONE
                binding.layoutPetInfo.visibility = View.GONE
            }
            "editPetInfo" -> {
                // 编辑宠物信息
                binding.tvTitle.text = "编辑宠物信息"
                binding.btnSkip.visibility = View.GONE
                binding.layoutUserInfo.visibility = View.GONE

                pets = PersonalCenterFragment.userInfo?.pets?.toMutableList() ?: mutableListOf()
                updatePetListView() // 初始时更新宠物列表

                // 获取要编辑的宠物 ID
                editPetId = intent.getLongExtra("petId",-1L)
                if(editPetId == -1L){
                    showAddPetDialog()
                }
            }
        }

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

    // 上传头像到服务器
    private fun uploadAvatar(uri: Uri) {
        executeThread {
            var tempFile: File? = null
            try {
                // 获取文件路径
                tempFile = FileUtils.getRealPathFromURI(uri,this)
                if(tempFile == null || !tempFile.exists()){
                    showToast("获取文件路径失败")
                    return@executeThread
                }
                val result = UserHttpUtils.uploadAvatar(tempFile)
                runOnUiThread {
                    if (result.first) {
                        showToast("头像上传成功")
                        // 更新头像 URL
                        uploadUrl = result.second
                        PersonalCenterFragment.refreshInfo = true
                    } else {
                        showToast("头像上传失败: ${result.second}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showToast("头像上传失败: ${e.message}")
                }
            } finally {
                // 删除临时缓存文件
                try {
                    tempFile?.let {
                        if (it.exists() && it.absolutePath.startsWith(cacheDir.absolutePath)) {
                            it.delete()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 上传宠物头像的方法
    private fun uploadPetAvatar(uri: Uri, petId: Long, isAdd: Boolean) {
        executeThread {
            var tempFile: File? = null
            try {
                // 获取文件路径
                tempFile = FileUtils.getRealPathFromURI(uri, this)
                if(tempFile == null || !tempFile.exists()){
                    showToast("宠物头像上传失败")
                    return@executeThread
                }
                val result = PetHttpUtils.uploadPetAvatar(tempFile, petId)
                runOnUiThread {
                    if (result.first) {
                        if(!isAdd){
                            showToast("宠物头像上传成功")
                        }
                        // 更新头像 URL
                        uploadedPetAvatarUrl = result.second
                        PersonalCenterFragment.refreshInfo = true
                        
                        // 更新本地宠物对象
                        val updatedPet = pets.find { it.id == petId }
                        if (updatedPet != null && result.second != null) {
                            updatedPet.avatarUrl = result.second!!
                            // 刷新宠物列表
                            updatePetListView()
                        }
                    } else {
                        showToast("宠物头像上传失败: ${result.second}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showToast("宠物头像上传失败: ${e.message}")
                }
            } finally {
                // 删除临时缓存文件
                try {
                    tempFile?.let {
                        if (it.exists() && it.absolutePath.startsWith(cacheDir.absolutePath)) {
                            it.delete()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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


    private fun loadUserData(userInfo: User?){
        if (userInfo != null) {
            // 更新用户信息
            binding.etNickname.setText(userInfo.nickname)
            binding.etBio.setText(userInfo.bio.ifEmpty { "" })
            binding.tvLocation.text = userInfo.location
            if (userInfo.birthday.time != 0L) {
                binding.tvBirthday.text = dateFormat.format(userInfo.birthday)
                calendar.time = userInfo.birthday
            }
            binding.rgGender.check(when(userInfo.gender) {
                0 -> R.id.rbGenderMale
                1 -> R.id.rbGenderFemale
                else -> R.id.rbGenderOther
            })

            // 加载头像
            Glide.with(this)
                .load(HttpUtils.BASE_URL + userInfo.avatarUrl)
                .apply(RequestOptions())
                .transform(CircleCrop())
                .into(binding.ivAvatarPreview)

        }
    }

    @SuppressLint("SetTextI18n")
    private fun getUserData() {
        // 先加载传入的User数据，再从后台重新获取，确保数据与后台一致
        loadUserData(putUserInfo)
        executeThread {
            try {
                val userInfo = UserHttpUtils.getUserInfo()
                // 获取用户的宠物列表
                val petList = PetHttpUtils.getUserPets()
                runOnUiThread {
                    loadUserData(userInfo)
                    // 将后台获取的宠物数据转换为PetInfo对象
                    pets = petList.toMutableList()
                    updatePetListView()
                    
                    // 如果是编辑特定宠物，加载该宠物信息
                    if (editType == "editPetInfo" && editPetId > 0) {
                        val petToEdit = pets.find { it.id == editPetId }
                        if (petToEdit != null) {
                            showAddPetDialog(petToEdit)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "加载用户数据失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAddPetDialog(petToEdit: Pet? = null) {
        val dialogBinding = DialogAddPetBinding.inflate(LayoutInflater.from(this))
        // 保存对话框绑定的引用
        currentPetDialogBinding = dialogBinding
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        // 设置自定义标题文本 (如果需要动态设置)
        dialogBinding.tvTitle.text = if (petToEdit == null) "添加宠物" else "编辑宠物"

        // 设置日期选择器
        val petCalendar = Calendar.getInstance()
        
        // 重置头像上传状态
        selectedPetAvatarUri = null
        uploadedPetAvatarUrl = null
        
        // 如果是编辑现有宠物，更新编辑的宠物ID
        if (petToEdit != null) {
            editPetId = petToEdit.id
        }
        
        // 如果是编辑，填充现有数据
        if (petToEdit != null) {
            dialogBinding.etPetName.setText(petToEdit.name)
            when (petToEdit.type) {
                "cat" -> dialogBinding.rbPetTypeCat.isChecked = true
                "dog" -> dialogBinding.rbPetTypeDog.isChecked = true
                "other" -> dialogBinding.rbPetTypeOther.isChecked = true
            }
            dialogBinding.etPetBreed.setText(petToEdit.breed)
            petCalendar.time = petToEdit.birthday
            dialogBinding.tvPetBirthday.text = dateFormat.format(petToEdit.birthday)
            
            // 加载现有宠物头像
            if (petToEdit.avatarUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(HttpUtils.BASE_URL + petToEdit.avatarUrl)
                    .apply(RequestOptions())
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_paw)
                    .into(dialogBinding.ivPetAvatarPreview)
            } else {
                // 根据宠物类型设置默认图标
                val iconRes = when (petToEdit.type) {
                    "dog" -> R.mipmap.default_dog
                    "other" -> R.drawable.ic_paw
                    else -> R.mipmap.default_cat
                }
                dialogBinding.ivPetAvatarPreview.setImageResource(iconRes)
            }
        } else {
            dialogBinding.rbPetTypeCat.isChecked = true // 默认选中猫咪
            dialogBinding.tvPetBirthday.text = "请选择宠物生日"
            // 添加新宠物时重置编辑ID
            editPetId = -1L
        }
        
        // 设置头像点击事件
        dialogBinding.ivPetAvatarPreview.setOnClickListener {
            pickPetImageLauncher.launch("image/*")
        }
        dialogBinding.tvUploadPetAvatar.setOnClickListener {
            pickPetImageLauncher.launch("image/*")
        }
        
        // 设置生日选择点击事件
        dialogBinding.tvPetBirthday.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                petCalendar.set(Calendar.YEAR, year)
                petCalendar.set(Calendar.MONTH, monthOfYear)
                petCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                dialogBinding.tvPetBirthday.text = dateFormat.format(petCalendar.time)
            }

            DatePickerDialog(this, dateSetListener,
                petCalendar.get(Calendar.YEAR),
                petCalendar.get(Calendar.MONTH),
                petCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 为自定义的确认按钮设置点击监听器
        dialogBinding.btnConfirm.setOnClickListener {
            val name = dialogBinding.etPetName.text.toString().trim()
            if (name.isEmpty()) {
                showToast("请输入宠物名称")
                return@setOnClickListener // 保持在对话框内
            }

            if (dialogBinding.tvPetBirthday.text == "请选择宠物生日") {
                showToast("请选择宠物生日")
                return@setOnClickListener
            }

            val typeId = dialogBinding.rgPetType.checkedRadioButtonId
            val type = when (typeId) {
                R.id.rbPetTypeDog -> "dog"
                R.id.rbPetTypeOther -> "other"
                else -> "cat" // 默认是 cat
            }
            val breed = dialogBinding.etPetBreed.text.toString().trim()

            // 显示加载对话框
            val progressDialog = MyProgressDialog(this).apply {
                setTitleStr("加载中")
                setHintStr("正在${if(petToEdit == null) "添加" else "编辑" }宠物信息...")
                show()
            }
            
            executeThread {
                try {
                    if (petToEdit == null) {
                        // 添加新宠物
                        val newPet = Pet(
                            id = 0, // 临时ID，后端会分配真实ID
                            name = name,
                            type = type,
                            breed = breed.ifEmpty { "未知品种" },
                            birthday = petCalendar.time,
                            avatarUrl = "" // 先创建宠物，成功后再上传头像
                        )
                        
                        val result = PetHttpUtils.addPet(newPet)
                        runOnUiThread {
                            if (result.first && result.second != null) {
                                // 使用后端返回的宠物对象（包含真实ID）
                                val createdPet = result.second!!
                                
                                // 如果选择了头像，在创建宠物成功后上传头像
                                if (selectedPetAvatarUri != null) {
                                    uploadPetAvatar(selectedPetAvatarUri!!, createdPet.id, true)
                                }
                                
                                MyToast.show("宠物添加成功",true)
                                pets.add(createdPet)
                                updatePetListView()
                                PersonalCenterFragment.refreshInfo = true
                                progressDialog.dismiss()
                                dialog.dismiss()
                                // 清除对话框引用
                                currentPetDialogBinding = null
                            } else {
                                progressDialog.dismiss()
                                showToast("添加宠物失败")
                            }
                        }
                    } else {
                        // 更新现有宠物
                        val updatedPet = Pet(
                            id = petToEdit.id,
                            name = name,
                            type = type,
                            breed = breed.ifEmpty { "未知品种" },
                            birthday = petCalendar.time,
                            avatarUrl = uploadedPetAvatarUrl ?: petToEdit.avatarUrl // 使用新上传的头像或保留原头像
                        )
                        
                        val result = PetHttpUtils.updatePet(updatedPet)
                        runOnUiThread {
                            progressDialog.dismiss()
                            if (result.first) {
                                MyToast.show("宠物信息更新成功",true)
                                // 更新本地宠物对象
                                petToEdit.name = name
                                petToEdit.type = type
                                petToEdit.breed = breed.ifEmpty { "未知品种" }
                                petToEdit.birthday = petCalendar.time
                                if (uploadedPetAvatarUrl != null) {
                                    petToEdit.avatarUrl = uploadedPetAvatarUrl!!
                                }
                                updatePetListView()
                                PersonalCenterFragment.refreshInfo = true
                                dialog.dismiss()
                                // 清除对话框引用
                                currentPetDialogBinding = null
                            } else {
                                showToast("更新宠物失败: ${result.second}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        progressDialog.dismiss()
                        showToast("操作失败: ${e.message}")
                    }
                }
            }
        }

        // 为自定义的取消按钮设置点击监听器
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss() // 关闭对话框
            // 清除对话框引用
            currentPetDialogBinding = null
        }

        // 在 dialog.show() 之前设置窗口背景
        dialog.window?.setBackgroundDrawableResource(R.drawable.view_radius) // 应用圆角背景
        
        // 设置对话框关闭监听器，确保对话框关闭时清除引用
        dialog.setOnDismissListener {
            currentPetDialogBinding = null
        }

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
    private fun addPetItemView(pet: Pet) {
        val itemBinding = ItemPetInfoBinding.inflate(LayoutInflater.from(this), binding.layoutPetList, false)

        itemBinding.tvPetName.text = pet.name

        // 计算宠物年龄
        val ageText = calculatePetAgeText(pet.birthday)
        itemBinding.tvPetBreedAge.text = "${pet.breed} · $ageText"

        if (pet.avatarUrl.isNotEmpty()) {
            Glide.with(this)
                .load(HttpUtils.BASE_URL + pet.avatarUrl)
                .apply(RequestOptions())
                .transform(CircleCrop())
                .into(itemBinding.ivPetIcon)
        } else {
            // 根据宠物类型设置默认图标
            val iconRes = when (pet.type) {
                "dog" -> R.mipmap.default_dog
                "other" -> R.drawable.ic_paw
                else -> R.mipmap.default_cat
            }
            itemBinding.ivPetIcon.setImageResource(iconRes)
        }

        // 删除按钮
        itemBinding.btnPetDelete.setOnClickListener {
            // 确认删除对话框
            MyDialog(this).apply {
                setTitle("删除宠物")
                setMessage("确定要删除宠物 ${pet.name} 吗？")
                setThemeColor(getColor(R.color.Theme2))
                setYesOnclickListener("确定") {
                    dismiss()
                    val progressDialog = MyProgressDialog(this@UserInfoEditActivity).apply {
                        setTitleStr("加载中")
                        setHintStr("正在删除宠物信息...")
                        show()
                    }
                    executeThread {
                        try {
                            val result = PetHttpUtils.deletePet(pet.id)
                            runOnUiThread {
                                progressDialog.dismiss()
                                if (result.first) {
                                    MyToast.show("宠物删除成功",true)
                                    pets.remove(pet)
                                    updatePetListView()
                                    PersonalCenterFragment.refreshInfo = true
                                } else {
                                    showToast("删除失败: ${result.second}")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                progressDialog.dismiss()
                                showToast("删除失败: ${e.message}")
                            }
                        }
                    }
                }

                setNoOnclickListener("取消") { dismiss() }
                show()
            }
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
             val result:Pair<Boolean, String?>
             when (editType) {
                 "editUserInfo" -> {
                     result = UserHttpUtils.updateUserInfo(
                         avatarUrl = uploadUrl, // 可能需要处理 Uri 到文件的转换
                         nickname = nickname,
                         gender = gender,
                         birthday = birthday,
                         location = location,
                         bio = bio,
                     )
                 }
                 "editPetInfo" -> {
                     result = Pair(true, null)
                 }
                 else -> {
                      result = UserHttpUtils.updateUserInfo(
                         avatarUrl = uploadUrl, // 可能需要处理 Uri 到文件的转换
                         nickname = nickname,
                         gender = gender,
                         birthday = birthday,
                         location = location,
                         bio = bio,
                     )
                 }
             }

            runOnUiThread {
                if (result.first) {
                    showToast("信息保存成功")
                    navigateToMain()
                } else {
                    showToast("保存失败: ${result.second}")
                }
            }
         }
    }

    private fun navigateToMain() {
        if(editType == ""){
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            PersonalCenterFragment.refreshInfo = true
        }
        finish() // 关闭当前 Activity
    }
    override fun finish() {
        putUserInfo = null
        super.finish()
    }

    companion object {
        var putUserInfo: User? = null
    }
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