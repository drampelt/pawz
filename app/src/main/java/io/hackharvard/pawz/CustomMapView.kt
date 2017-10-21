package io.hackharvard.pawz

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView

/**
 * Created by daniel on 2017-10-21.
 * Copyright © 2017 Chalk.com Education Inc. All rights reserved.
 */

class CustomMapView : MapView {
    constructor(p0: Context?) : super(p0)
    constructor(p0: Context?, p1: AttributeSet?) : super(p0, p1)
    constructor(p0: Context?, p1: AttributeSet?, p2: Int) : super(p0, p1, p2)
    constructor(p0: Context?, p1: GoogleMapOptions?) : super(p0, p1)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> this.parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP -> this.parent.requestDisallowInterceptTouchEvent(false)
        }
        super.dispatchTouchEvent(ev)
        return true
    }
}