package com.garage.aastream.adapters

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.row_app_item.view.*
import com.garage.aastream.R
import com.garage.aastream.injection.GlideApp
import com.garage.aastream.interfaces.OnAppClickedCallback
import com.garage.aastream.models.AppItem
import com.garage.aastream.utils.DevLog
import kotlin.random.Random

/**
 * Created by Endy Rubbin on 23.05.2019 15:02.
 * For project: AAStream
 */
class AppListAdapter(
    val context: Context,
    val callback: OnAppClickedCallback
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private var currentPosition = DEFAULT_INDEX
    private val apps: ArrayList<AppItem> = ArrayList()
    private val glide = GlideApp.with(context)

    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    /**
     * Update the list adapter with new items
     */
    fun addAll(apps: ArrayList<AppItem>) {
        DevLog.d("Notifying app list")
        this.apps.clear()
        this.apps.addAll(apps)
        currentPosition = DEFAULT_INDEX
        notifyDataSetChanged()
    }

    /**
     * Update list item and set it as favorite or not
     */
    fun setFavorite(app: AppItem, favorite: Boolean) {
        apps.indexOfFirst { it.equalTo(app)}.takeIf {it >= 0}?.let {
            DevLog.d("Notifying app item $it $favorite ${apps[it]}")
            apps[it].favorite = !favorite
            currentPosition = DEFAULT_INDEX
            notifyItemChanged(it)
        }
    }

    /**
     * Remove item from list
     */
    fun removeFavorite(app: AppItem) {
        apps.indexOfFirst { it.equalTo(app)}.takeIf {it >= 0}?.let {
            apps.removeAt(it)
            currentPosition = DEFAULT_INDEX
            notifyItemRemoved(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_app_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animate = if (position > currentPosition) {
            currentPosition = position
            true
        } else false
        holder.bind(apps[position], animate)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.clearAnimation()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(app: AppItem, animate: Boolean = true) {
            if (app.drawable != null) {
                glide.load(app.drawable)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .override(48.px, 48.px)
                    .into(itemView.item_app_icon)
            } else {
                glide.load(if (app.icon != null) Uri.parse(app.icon) else android.R.drawable.sym_def_app_icon)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .override(48.px, 48.px)
                    .listener(object: RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            app.drawable = resource
                            return false
                        }
                    })
                    .into(itemView.item_app_icon)
            }

            itemView.item_app_name.text = app.label
            itemView.item_app_favorite.visibility = if (app.favorite) View.VISIBLE else View.GONE
            itemView.setOnClickListener { callback.onAppClicked(app) }
            itemView.setOnLongClickListener {
                callback.onAppLongClicked(app)
                true
            }

            if (animate) {
                val scale = Random.nextFloat() * (MAX_START_SCALE - MIN_START_SCALE) + MIN_START_SCALE
                val delay = (Random.nextInt(MAX_START_DELAY) + MIN_START_DELAY).toLong()
                val duration = (Random.nextInt(MAX_DURATION) + MIN_DURATION).toLong()

                if (itemView.alpha != 0f) {
                    itemView.alpha = MIN_ALPHA
                }
                itemView.scaleX = scale
                itemView.scaleY = scale


                itemView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(delay)
                    .setDuration(duration)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            } else {
                itemView.scaleX = 1f
                itemView.scaleY = 1f
                itemView.alpha = 1f
            }
        }

        fun clearAnimation() {
            itemView.animate().cancel()
            itemView.clearAnimation()
        }
    }

    companion object {
        const val DEFAULT_INDEX = -1
        const val MIN_ALPHA = 0.5f
        const val MIN_START_SCALE = 0.4f
        const val MAX_START_SCALE = 0.8f
        const val MIN_START_DELAY = 50
        const val MAX_START_DELAY = 200
        const val MIN_DURATION = 200
        const val MAX_DURATION = 500
    }
}