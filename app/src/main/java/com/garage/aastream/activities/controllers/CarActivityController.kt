package com.garage.aastream.activities.controllers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.garage.aastream.App
import com.garage.aastream.BuildConfig
import com.garage.aastream.R
import com.garage.aastream.activities.ResultRequestActivity
import com.garage.aastream.adapters.AppListAdapter
import com.garage.aastream.handlers.*
import com.garage.aastream.interfaces.OnAppClickedCallback
import com.garage.aastream.interfaces.OnAppListLoadedCallback
import com.garage.aastream.interfaces.OnScreenLockCallback
import com.garage.aastream.interfaces.OnTwoFingerTapCallback
import com.garage.aastream.minitouch.MiniTouchHandler
import com.garage.aastream.models.AppItem
import com.garage.aastream.receivers.ScreenLockReceiver
import com.garage.aastream.utils.Const
import com.garage.aastream.utils.DevLog
import com.garage.aastream.views.MarginDecoration
import com.google.android.apps.auto.sdk.CarUiController
import com.google.android.apps.auto.sdk.DayNightStyle
import kotlinx.android.synthetic.main.activity_car.view.*
import kotlinx.android.synthetic.main.view_car_terminal.view.*
import javax.inject.Inject

/**
 * Created by Endy Rubbin on 28.05.2019 10:54.
 * For project: AAStream
 */
class CarActivityController(val context: Application) : OnScreenLockCallback, OnAppClickedCallback,
    OnAppListLoadedCallback, OnTwoFingerTapCallback {

    @Inject lateinit var appHandler: AppHandler
    @Inject lateinit var preferences: PreferenceHandler
    @Inject lateinit var brightnessHandler: BrightnessHandler
    @Inject lateinit var rotationHandler: RotationHandler
    @Inject lateinit var miniTouchHandler: MiniTouchHandler
    @Inject lateinit var audioHandler: AudioHandler
    @Inject lateinit var terminalController: TerminalController

    private lateinit var rootView: View
    private lateinit var windowManager: WindowManager
    private lateinit var adapter: AppListAdapter
    private lateinit var orientationListener: OrientationEventListener
    private var carUiController: CarUiController? = null

    private val apps = ArrayList<AppItem>()
    private var currentView = ViewType.VIEW_NONE
    private var restarted = false
    private var initialMenuX = 0f
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjection: MediaProjection? = null
    private var projectionCode: Int = 0
    private var projectionIntent: Intent? = null

    private val screenLockReceiver = ScreenLockReceiver(this)
    private val filter = IntentFilter()

    private val requestHandler = Handler(Handler.Callback { msg ->
        if (msg?.what == Const.REQUEST_MEDIA_PROJECTION_PERMISSION) {
            projectionCode = msg.arg2
            projectionIntent = msg.obj as Intent
            DevLog.d("Permission granted - starting screen capture")
            startScreenCapture()
        }
        false
    })

    init {
        (context as App).component.inject(this)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
    }

    /**
     * Called when Activity is created
     */
    fun onCreate(rootView: View, windowManager: WindowManager, restarted: Boolean,
                 carUiController: CarUiController? = null) {
        this.restarted = restarted
        this.rootView = rootView
        this.windowManager = windowManager
        this.carUiController = carUiController
        terminalController.init(rootView)
        initViews()
        initCarUiController()
        requestProjectionPermission()
    }

    /**
     * Called when Activity is resumed
     */
    fun onResume() {
        miniTouchHandler.updateValues()
        loadApps()
        DevLog.d("Car activity resumed")
    }

    /**
     * Called when Activity is started
     */
    fun onStart() {
        DevLog.d("Car activity started")
        onScreenOn()
        audioHandler.start()
        orientationListener.enable()
        miniTouchHandler.start(rootView.car_stream_view, this)
        brightnessHandler.setScreenBrightness()
        rotationHandler.setScreenRotation()
        context.registerReceiver(screenLockReceiver, filter)
    }

    /**
     * Called when Activity is stopped
     */
    fun onStop() {
        DevLog.d("Car activity stopped")
        onScreenOff()
        audioHandler.stop()
        orientationListener.disable()
        miniTouchHandler.stop()
        context.unregisterReceiver(screenLockReceiver)
    }

    /**
     * Called when Activity is destroyed
     */
    fun onDestroy() {
        DevLog.d("Car activity destroyed $restarted")
        terminalController.stop()
        if (!restarted) {
            brightnessHandler.restoreScreenBrightness()
            rotationHandler.restoreScreenRotation()
        }
    }

    /**
     * Called when Activity configuration has changed
     */
    fun onConfigurationChanged() {
        miniTouchHandler.updateValues()
    }

    /**
     * Called when car activity focus has changed
     */
    fun onWindowFocusChanged() {
        startScreenCapture()
    }

    /**
     * Sturt dummy activity to handle permission granted result
     */
    private fun startActivityForResult(what: Int, intent: Intent) {
        ResultRequestActivity.startActivityForResult(context, requestHandler, what, intent, what)
    }

    /**
     * Request permission to record screen
     */
    private fun requestProjectionPermission() {
        DevLog.d("Request projection permission")
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            Const.REQUEST_MEDIA_PROJECTION_PERMISSION,
            mediaProjectionManager.createScreenCaptureIntent()
        )
    }

    /**
     * Initialize views and set listeners
     */
    private fun initViews() {
        orientationListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                miniTouchHandler.updateValues()
            }
        }

        adapter = AppListAdapter(context, this)
        rootView.car_app_grid.itemAnimator = null
        rootView.car_app_grid.addItemDecoration(MarginDecoration(context))
        rootView.car_app_grid.adapter = adapter

        rootView.car_menu_app_list.setOnClickListener { showScreen(ViewType.VIEW_APP_LIST.value) }
        rootView.car_menu_favorites.setOnClickListener { showScreen(ViewType.VIEW_FAVORITES.value) }
        rootView.car_menu_terminal.setOnClickListener { showScreen(ViewType.VIEW_TERMINAL.value) }
        rootView.car_menu_close.setOnClickListener { switchMenuVisibility(false) }
        rootView.car_menu_holder.post {
            initialMenuX = rootView.car_menu_holder.width.toFloat()
            rootView.car_menu_holder.x = -initialMenuX
        }
        rootView.car_menu_terminal.visibility = if (preferences.getBoolean(PreferenceHandler.KEY_DEBUG_ENABLED, BuildConfig.DEBUG)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        switchMenuVisibility(preferences.getBoolean(PreferenceHandler.KEY_SIDEBAR_SWITCH, Const.DEFAULT_SHOW_SIDEBAR))
    }

    /**
     * Initialize car UI controller
     */
    private fun initCarUiController() {
        carUiController?.statusBarController?.setTitle("")
        carUiController?.statusBarController?.hideAppHeader()
        carUiController?.statusBarController?.setAppBarAlpha(0.0f)
        carUiController?.statusBarController?.setAppBarBackgroundColor(Color.WHITE)
        carUiController?.statusBarController?.setDayNightStyle(DayNightStyle.AUTO)
        carUiController?.menuController?.hideMenuButton()
    }

    /**
     * Show/hide sidebar menu
     */
    private fun switchMenuVisibility(visible: Boolean) {
        rootView.car_menu_holder.post {
            if (visible) {
                rootView.car_menu_holder.animate().cancel()
                rootView.car_menu_holder.animate()
                    .alpha(1f)
                    .x(0f)
                    .setStartDelay(Const.DEFAULT_ANIMATION_DELAY)
                    .setDuration(Const.DEFAULT_ANIMATION_DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            showScreen(preferences.getInt(PreferenceHandler.KEY_STARTUP_VALUE, Const.DEFAULT_SCREEN))
                        }
                    })
                    .start()
            } else {
                showScreen(ViewType.VIEW_NONE.value)
                rootView.car_menu_holder.animate()
                    .alpha(0f)
                    .x(-initialMenuX)
                    .setStartDelay(Const.DEFAULT_ANIMATION_DELAY)
                    .setDuration(Const.DEFAULT_ANIMATION_DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            showScreen(ViewType.VIEW_NONE.value)
                        }
                    })
                    .start()
            }
        }
    }

    /**
     * Show selected screen
     */
    private fun showScreen(index: Int) {
        DevLog.d("Showing screen if: $index != ${currentView.value}")
        if (index != currentView.value) {
            hideKeyboard()
            when (index) {
                ViewType.VIEW_NONE.value -> {
                    rootView.car_app_grid.visibility = View.GONE
                    rootView.view_car_terminal.visibility = View.GONE
                }
                ViewType.VIEW_APP_LIST.value -> showAllApps()
                ViewType.VIEW_FAVORITES.value -> showFavorites()
                ViewType.VIEW_TERMINAL.value -> showTerminal()
            }
        }
    }

    /**
     * Show terminal view
     */
    private fun showTerminal() {
        DevLog.d("Showing terminal")
        currentView = ViewType.VIEW_TERMINAL
        rootView.view_car_terminal.visibility = View.VISIBLE
        rootView.car_app_grid.visibility = View.GONE
        rootView.car_app_favorite_empty.visibility = View.GONE
        rootView.car_app_grid_loader.visibility = View.GONE
    }

    /**
     * Shows all device apps
     */
    private fun showAllApps() {
        DevLog.d("Showing all apps")
        currentView = ViewType.VIEW_APP_LIST
        rootView.car_app_grid.visibility = View.GONE
        rootView.car_app_favorite_empty.visibility = View.GONE
        rootView.view_car_terminal.visibility = View.GONE
        if (apps.isEmpty()) {
            rootView.car_app_grid_loader.visibility = View.VISIBLE
        } else {
            rootView.car_app_grid_loader.visibility = View.GONE
            rootView.car_app_grid.visibility = View.VISIBLE
            adapter.addAll(apps)
        }
    }

    /**
     * Shows all favorite apps if exists
     */
    private fun showFavorites() {
        DevLog.d("Showing favorite apps")
        currentView = ViewType.VIEW_FAVORITES
        rootView.car_app_grid.visibility = View.VISIBLE
        rootView.view_car_terminal.visibility = View.GONE
        showFavoritePlaceholder()
        appHandler.getFavorites().let {
            adapter.addAll(it)
        }
    }

    /**
     * Show / hide favorite app placeholder
     */
    private fun showFavoritePlaceholder() {
        rootView.car_app_grid_loader.visibility = View.GONE
        appHandler.getFavorites().let {
            if (it.isEmpty()) {
                rootView.car_app_favorite_empty.visibility = View.VISIBLE
            } else {
                rootView.car_app_favorite_empty.visibility = View.GONE
            }
        }
    }

    /**
     * @return selected app from app list or null
     */
    private fun getSelectedApp(app: AppItem): AppItem? {
        return apps.firstOrNull { it.equalTo(app) }
    }

    /**
     * Query for installed device apps
     */
    private fun loadApps() {
        appHandler.loadApps(this)
    }

    /**
     * Show error Toast with provided message String
     */
    private fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Hide keyboard
     */
    private fun hideKeyboard() {
        rootView.terminal_input.let {
            it.postDelayed({
                val inputManager = context.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(it.windowToken, 0)
            }, 100)
        }
    }

    /**
     * Set current [AppItem] as favorite or not
     */
    private fun setAppFavorite(app: AppItem) {
        val wasFavorite = appHandler.isInFavorites(app)
        apps.firstOrNull { it.equalTo(app) }?.favorite = !wasFavorite
        preferences.putFavorites(apps.filter { it.favorite } as ArrayList<AppItem>)

        if (ViewType.VIEW_FAVORITES == currentView) {
            adapter.removeFavorite(app)
            showFavoritePlaceholder()
        } else {
            adapter.setFavorite(app, wasFavorite)
        }

        if (wasFavorite) {
            showToastMessage(context.getString(R.string.txt_removed_from_favorites))
        } else {
            showToastMessage(context.getString(R.string.txt_added_to_favorites))
        }
    }

    /**
     * Start screen capture
     */
    private fun startScreenCapture() {
        Handler(Looper.getMainLooper()).postDelayed({
            rootView.car_stream_view.post {
                DevLog.d("Will starting screen capture if ($projectionCode) != 0 && ($projectionIntent) != null")
                if (projectionIntent != null || projectionCode != 0) {
                    stopScreenCapture()
                    DevLog.d("Starting screen capture $projectionCode $projectionIntent")
                    miniTouchHandler.updateTouchTransformations(true)

                    val metrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(metrics)
                    val screenDensity = metrics.densityDpi
                    val mediaProjectionManager = context.getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE
                    ) as MediaProjectionManager

                    mediaProjection = mediaProjectionManager.getMediaProjection(projectionCode, projectionIntent!!)
                    mediaProjection?.let {
                        val width = rootView.car_stream_view.width
                        val height = rootView.car_stream_view.height

                        DevLog.d("Screen width: $width")
                        DevLog.d("Screen height: $height")
                        DevLog.d("Screen density: $screenDensity")

                        if (width > 0 && height > 0) {
                            virtualDisplay = it.createVirtualDisplay(
                                "ScreenCapture",
                                width, height, screenDensity,
                                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                                rootView.car_stream_view.holder.surface, null, null
                            )
                        }
                    }
                }
            }
        }, Const.DEFAULT_ANIMATION_DELAY)
    }

    /**
     * Stop screen capture
     */
    private fun stopScreenCapture() {
        DevLog.d("Stopping screen capture")
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
    }

    /**
     * Called when device screen is unlocked
     */
    override fun onScreenUnlocked() {
        DevLog.d("Screen Unlocked")
        rootView.car_stream_view.keepScreenOn = false
    }

    /**
     * Called when device screen is turned on
     */
    override fun onScreenOn() {
        DevLog.d("Screen ON")
        rootView.car_stream_view.keepScreenOn = true
        startScreenCapture()
    }

    /**
     * Called when device screen is turned off
     */
    override fun onScreenOff() {
        DevLog.d("Screen OFF")
        rootView.car_stream_view.keepScreenOn = false
        stopScreenCapture()
    }

    /**
     * Called when screen is tapped with two fingers
     */
    override fun onTwoFingerTapped() {
        switchMenuVisibility(true)
    }

    /**
     * Called when query for device apps is finished and results are returned
     */
    override fun onAppListLoaded(apps: ArrayList<AppItem>) {
        Handler(Looper.getMainLooper()).post {
            DevLog.d("App list loaded")
            var updated = false
            if (this.apps.size == 0 || this.apps.size != apps.size) {
                this.apps.addAll(apps)
                updated = true
            } else {
                apps.forEach { newApp ->
                    var added = false
                    this.apps.forEach { currentApp ->
                        if (newApp.equalTo(currentApp)) {
                            added = true
                        }
                    }
                    if (!added) {
                        this.apps.add(newApp)
                        updated = true
                    }
                }
            }
            // Update favorites
            apps.forEach {
                it.favorite = appHandler.isInFavorites(it)
            }
            if (updated) {
                if (ViewType.VIEW_APP_LIST == currentView) {
                    showAllApps()
                } else if (ViewType.VIEW_FAVORITES == currentView) {
                    showFavorites()
                }
            }
        }
    }

    /**
     * Called when query for device apps has failed
     */
    override fun onAppListLoadFailed() {
        DevLog.d("App list load failed")
        if (ViewType.VIEW_APP_LIST == currentView) {
            showToastMessage(context.getString(R.string.err_app_list_load_failed))
        }
    }

    /**
     * Called when an app in app list is clicked
     */
    override fun onAppClicked(app: AppItem) {
        Handler(Looper.getMainLooper()).post {
            getSelectedApp(app)?.let {
                DevLog.d("App clicked: $it")
                context.packageManager.getLaunchIntentForPackage(it.packageName)?.let { intent ->
                    switchMenuVisibility(false)
                    context.startActivity(intent)
                } ?: showToastMessage(context.getString(R.string.err_app_launch_failed))
            }
        }
    }

    /**
     * Called when an app in app list is long clicked
     */
    override fun onAppLongClicked(app: AppItem) {
        Handler(Looper.getMainLooper()).post {
            getSelectedApp(app)?.let {
                DevLog.d("App long clicked: $it")
                setAppFavorite(it)
            }
        }
    }

    companion object {
        enum class ViewType(val value: Int) {
            VIEW_NONE(0),
            VIEW_APP_LIST(1),
            VIEW_FAVORITES(2),
            VIEW_TERMINAL(3)
        }
    }
}