package com.nunchuk.android.messages.components.detail.viewer

import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.nunchuk.android.messages.components.detail.model.RoomMediaSource
import com.nunchuk.android.messages.databinding.ItemVideoViewerBinding
import com.nunchuk.android.messages.util.downloadFile
import kotlinx.coroutines.*
import org.matrix.android.sdk.api.session.room.model.message.getFileUrl
import timber.log.Timber

class VideoViewHolder(
    private val binding: ItemVideoViewerBinding,
    lifecycleOwner: LifecycleOwner,
) : BaseMediaViewHolder(binding), DefaultLifecycleObserver {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val fileService = sessionHolder.getSafeActiveSession()?.fileService()
    private var playWhenReady: Boolean = true
    private var downloadJob: Job? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun bind(item: RoomMediaSource) {
        val data = item as RoomMediaSource.Video

        buildRequestManager(
            data.thumbnail,
            data.allowNonMxcUrls,
            binding.viewThumbnail
        ).override(maxImageWidth, maxImageHeight)
            .fitCenter()
            .into(binding.viewThumbnail)

        val url = data.content.getFileUrl()
        Timber.d("bind $url")
        if (url?.startsWith("content://") == true && data.allowNonMxcUrls) {
            onVideoURLReady(url)
        } else {
            downloadJob = coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        fileService?.downloadFile(data)
                    }
                }
                ensureActive()
                if (result.isSuccess) {
                    onVideoURLReady(result.getOrThrow()?.absolutePath)
                }
            }
        }
    }

    override fun onAttached() {
        super.onAttached()
        tryStart()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        tryStart()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        release()
    }

    override fun onDetached() {
        release()
        super.onDetached()
    }

    override fun onRecycled() {
        Timber.d("onRecycled")
        downloadJob?.cancel()
        binding.videoView.tag = null
        release()
        super.onRecycled()
    }

    private fun tryStart() {
        playWhenReady = true
        binding.videoView.tag?.let { url ->
            onVideoURLReady(url as? String)
        }
    }

    private fun release() {
        playWhenReady = false
        runCatching {
            binding.videoView.suspend()
            binding.videoView.setOnPreparedListener(null)
        }
    }

    private fun onVideoURLReady(url: String?) {
        Timber.d("onVideoURLReady", url)
        binding.videoView.tag = url
        binding.videoView.isVisible = true
        binding.viewThumbnail.isVisible = false
        binding.videoView.setVideoPath(url)
        binding.videoView.setOnPreparedListener {
            if (playWhenReady) {
                binding.videoView.start()
            }
        }
    }
}