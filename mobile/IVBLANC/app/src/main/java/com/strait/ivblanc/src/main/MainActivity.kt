package com.strait.ivblanc.src.main

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.strait.ivblanc.R
import com.strait.ivblanc.config.ApplicationClass
import com.strait.ivblanc.config.BaseActivity
import com.strait.ivblanc.data.model.dto.Clothes
import com.strait.ivblanc.data.model.dto.History
import com.strait.ivblanc.data.model.dto.Style
import com.strait.ivblanc.data.model.viewmodel.ClothesViewModel
import com.strait.ivblanc.data.model.viewmodel.FriendViewModel
import com.strait.ivblanc.data.model.viewmodel.MainViewModel
import com.strait.ivblanc.data.model.viewmodel.StyleViewModel
import com.strait.ivblanc.databinding.ActivityMainBinding
import com.strait.ivblanc.src.friend.FriendNoti
import com.strait.ivblanc.src.history.StyleSelectActivity
import com.strait.ivblanc.src.photoSelect.PhotoSelectActivity
import com.strait.ivblanc.src.styleMaking.StyleMakingActivity
import com.strait.ivblanc.ui.PhotoListFragment
import com.strait.ivblanc.ui.StylePhotoListFragment
import com.strait.ivblanc.util.CategoryCode
import com.strait.ivblanc.util.LoginUtil
import com.strait.ivblanc.util.PermissionUtil
import com.strait.ivblanc.util.StatusCode


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    val SP_NAME = "fcm_message"
    val mainViewModel: MainViewModel by viewModels()
    val friendViewModel: FriendViewModel by viewModels()
    val clothesViewModel: ClothesViewModel by viewModels()
    val styleViewModel: StyleViewModel by viewModels()
    lateinit var dialog: Dialog
    lateinit var permissionUtil: PermissionUtil

    private val preContractStartActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    setFragment(StylePhotoListFragment(), it.getStringExtra("result")!!)
                    val item: MenuItem =
                        binding.bottomNavMain.menu.findItem(R.id.nav_style).setChecked(true)
                }
            }
        }
    val addClothesContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == StatusCode.OK) {
            clothesViewModel.getAllClothesWithCategory(clothesViewModel.currentCategory)
        }
    }
    val addStyleContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == StatusCode.OK) {
            styleViewModel.getAllStyles()
        }
    }

    companion object {
        // Notification Channel ID
        const val channel_id = "IVBLANC"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getFCM()
        clothesViewModel.getAllClothesWithCategory(CategoryCode.TOTAL)
        styleViewModel.getAllStyles()
        mainViewModel.setTrailingIcon(R.drawable.ic_baseline_notifications_24)

        ApplicationClass.livePush.observe(this) {
            if (ApplicationClass.livePush.value!! > 0) {
                binding.toolbarMain.badge.visibility = View.VISIBLE
                binding.toolbarMain.badge.setNumber(readSharedPreference("fcm").size)
            } else {
                binding.toolbarMain.badge.visibility = View.GONE
            }
        }

        permissionUtil = PermissionUtil(this)
        permissionUtil.permissionListener = object : PermissionUtil.PermissionListener {
            override fun run() {
                init()
            }
        }

        init()
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    private fun init() {

        setToolbar()
        // ??? ?????? ??????
        setFragment(PhotoListFragment(), "clothes")

        // nav click ??? fragment ??????
        binding.bottomNavMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_clothes -> {
                    setFragment(PhotoListFragment(), "clothes")
                    true
                }
                // Style??? ??????
                R.id.nav_style -> {
                    setFragment(StylePhotoListFragment(), "style")
                    true
                }
                R.id.nav_history -> {
                    setFragment(HistoryFragment(), "history")
                    true
                }
                R.id.nav_friend -> {
                    setFragment(FriendFragment(), "friend")
                    true
                }
                else -> false
            }
        }

        friendViewModel.friendName.observe(this) {
            if (friendViewModel.friendName.value == "error1") {
                friendDialog("?????? ??????????????????.")
            } else if (friendViewModel.friendName.value == "error2") {
                friendDialog("?????? ???????????? ???????????????.")
            } else {
                friendDialog(friendViewModel.friendName.value + "?????? ??????????????? ???????????????")
            }

        }
    }

    private fun setFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout_main_container, fragment, tag).commit()
        when(tag) {
            "clothes" -> {
                setToolbarTitle(resources.getString(R.string.closetEn))
                mainViewModel.setLeadingIcon(R.color.transparent)
                mainViewModel.setTrailingIcon(R.drawable.ic_baseline_notifications_24)
            }
            "style" -> {
                setToolbarTitle(resources.getString(R.string.pickEn))
                mainViewModel.setLeadingIcon(R.color.transparent)
                mainViewModel.setTrailingIcon(R.drawable.ic_baseline_notifications_24)
            }
            "history" -> {
                setToolbarTitle(resources.getString(R.string.historyEn))
                mainViewModel.setLeadingIcon(R.color.transparent)
                mainViewModel.setTrailingIcon(R.drawable.ic_baseline_notifications_24)
            }
            "friend" -> {
                setToolbarTitle(resources.getString(R.string.shareEn))
                mainViewModel.setLeadingIcon(R.drawable.ic_baseline_person_add_24)
                mainViewModel.setTrailingIcon(R.drawable.ic_baseline_notifications_24)
            }
        }
    }


    // TODO: 2022/02/04 ????????? ?????? ???????????? ?????? ???????????? ??????
    private fun setToolbar() {
        setSupportActionBar(binding.toolbarMain.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        mainViewModel.toolbarTitle.observe(this) {
            setToolbarTitle(it)
        }
        mainViewModel.leadingIconDrawable.observe(this) {
            setLeadingIcon(it, getListener(it))
        }

        mainViewModel.trailingIconDrawable.observe(this) {
            setTrailingIcon(it, getListener(it))
        }

        binding.toolbarMain.imageViewToolbarLogoutIcon.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                LoginUtil.signOut()
                finish()
            }
        }
    }

    // imageView??? background drawable Id??? ?????? ?????? ????????? ??????
    private fun getListener(resId: Int): View.OnClickListener {
        return when (resId) {
            R.drawable.ic_close -> object : View.OnClickListener {
                override fun onClick(v: View?) {
                    this@MainActivity.onBackPressed()
                }
            }
            // ?????? add Drawable ??????, ?????? fragment??? tag??? ????????? ??????
            R.drawable.ic_add -> {
                when (getCurrentFragmentTag()) {
                    "clothes" -> {
                        View.OnClickListener {
                            val intent = Intent(this@MainActivity, PhotoSelectActivity::class.java).apply {
                                putExtra("intend", PhotoSelectActivity.CLOTHES)
                            }
                            addClothesContract.launch(intent)
                        }
                    }
                    "style" -> {
                        View.OnClickListener {
                            val intent = Intent(
                                this@MainActivity,
                                StyleMakingActivity::class.java
                            )
                            addStyleContract.launch(intent)
                        }
                    }
                    "history" -> {
                        View.OnClickListener {
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    StyleSelectActivity::class.java
                                ).apply {
                                    putExtra("history", History("", "", 0.0,0.0, 0, emptyList(), "", "", 0, 0, "", "", 0, "??????"))
                                }
                            )
                        }
                    }
                    else -> View.OnClickListener {}
                }
            }
            R.drawable.ic_baseline_notifications_24 -> {
                View.OnClickListener {
                    val intent = Intent(this@MainActivity, FriendNoti::class.java)
                    preContractStartActivityResult.launch(intent)
                }
            }
            R.drawable.ic_baseline_person_add_24 -> {
                View.OnClickListener {
                    initDialog()
                }
            }
            else -> View.OnClickListener { }
        }
    }

    private fun getCurrentFragmentTag(): String? =
        supportFragmentManager.findFragmentById(R.id.frameLayout_main_container)?.tag

    private fun setToolbarTitle(title: String) {
        binding.toolbarMain.textViewToolbar.text = title
    }

    private fun setLeadingIcon(resId: Int, clickListener: View.OnClickListener) {
        try {
            binding.toolbarMain.imageViewToolbarLeadingIcon.background =
                ResourcesCompat.getDrawable(resources, resId, null)
        } catch (e: Exception) {
        }
        binding.toolbarMain.imageViewToolbarLeadingIcon.setOnClickListener(clickListener)
    }

    private fun setTrailingIcon(resId: Int, clickListener: View.OnClickListener) {
        try {
            binding.toolbarMain.imageViewToolbarTrailingIcon.background =
                ResourcesCompat.getDrawable(resources, resId, null)
        } catch (e: Exception) {
        }
        binding.toolbarMain.imageViewToolbarTrailingIcon.setOnClickListener(clickListener)
    }

    fun getFCM() {
        // FCM ?????? ??????
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
        })
        createNotificationChannel(channel_id, "ssafy")
    }

    // NotificationChannel ??????
    private fun createNotificationChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkPermissions() {
        if(!permissionUtil.checkPermissions(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))) {
            permissionUtil.requestPermissions()
        } else {
            init()
        }
    }

    private fun initDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_friend, null)

        val dialogText = dialogView.findViewById<EditText>(R.id.dialogEt)
        AlertDialog.Builder(this, R.style.MyDialogTheme).setView(dialogView)
            .setPositiveButton(
                "??????"
            ) { dialog, i ->
                Log.d("ssss", "initDialog: " + dialogText.text)

                friendViewModel.requestFriend(LoginUtil.getUserInfo()!!.email, dialogText.text.toString())
            }

            .setNegativeButton("??????") { dialogInterface, i ->
            }
            .show()
    }

    fun friendDialog(title: String) {
        MaterialAlertDialogBuilder(this, R.style.MyDialogTheme)
            .setTitle(title)
            .setPositiveButton("??????") { dialog, which ->
                // Respond to positive button press
                object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        //applicant ?????? ?????? ?????????????????????
                        onBackPressed()
                    }
                }
            }
            .show()
    }

    private fun readSharedPreference(key: String): ArrayList<String> {
        val sp = binding.root.context.getSharedPreferences(
            SP_NAME,
            FirebaseMessagingService.MODE_PRIVATE
        )
        val gson = Gson()
        val json = sp.getString(key, "") ?: ""
        val type = object : TypeToken<ArrayList<String>>() {}.type
        val obj: ArrayList<String> = gson.fromJson(json, type) ?: ArrayList()
        return obj
    }
}