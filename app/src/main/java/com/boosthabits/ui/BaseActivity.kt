package com.boosthabits.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.boosthabits.utils.IdiomaManager

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(IdiomaManager.wrapContext(newBase))
    }
}
