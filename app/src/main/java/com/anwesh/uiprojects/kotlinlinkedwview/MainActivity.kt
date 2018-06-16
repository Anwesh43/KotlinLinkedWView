package com.anwesh.uiprojects.kotlinlinkedwview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.linkedwview.LinkedWView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view: LinkedWView = LinkedWView.create(this)
        fullScreen()
        view.addLinkedWListener({
            Toast.makeText(this, "completed ${it}", Toast.LENGTH_SHORT).show()
        },{
            Toast.makeText(this, "${it} is reset", Toast.LENGTH_SHORT).show()
        })
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}
