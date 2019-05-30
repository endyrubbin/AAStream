package com.garage.aastream.views

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.garage.aastream.R

/**
 * Created by Endy Rubbin on 23.05.2019 14:49.
 * For project: AAStream
 */
class MarginDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private var margin = context.resources.getDimensionPixelSize(R.dimen.item_margin)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, margin, margin, margin)
    }
}