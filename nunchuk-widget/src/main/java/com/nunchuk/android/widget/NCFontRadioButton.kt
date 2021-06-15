package com.nunchuk.android.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import com.nunchuk.android.widget.util.FontInitializer

class NCFontRadioButton : AppCompatRadioButton {

    private val initializer: FontInitializer by lazy { FontInitializer(context) }

    constructor(context: Context) : super(context) {
        initializer.initTypeface(this)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initializer.initTypeface(this, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializer.initTypeface(this, attrs)
    }
}
