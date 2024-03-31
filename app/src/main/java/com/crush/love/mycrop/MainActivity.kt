package com.crush.love.mycrop

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.crush.love.mycrop.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity.DEFAULT_COMPRESS_FORMAT
import com.yalantis.ucrop.UCropActivity.DEFAULT_COMPRESS_QUALITY
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.model.AspectRatio
import com.yalantis.ucrop.view.CropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView.ScrollingListener
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var mShowLoader = true

    private var mCompressFormat: Bitmap.CompressFormat = DEFAULT_COMPRESS_FORMAT
    private var mCompressQuality: Int = DEFAULT_COMPRESS_QUALITY
    private var ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42
    private val SCALE_WIDGET_SENSITIVITY_COEFFICIENT = 15000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.uCropView.cropImageView.setTransformImageListener(mImageListener)
        //binding.uCropView.cropImageView.isRotateEnabled = false
        setImageData(intent)
        listener()
    }

    private fun listener() {
        binding.btnSubmit.setOnClickListener {
            cropAndSaveImage()
        }

        binding.btnDefault.setOnClickListener {
            aspectRatio(
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO,
                1F
            )
        }

        binding.btn34.setOnClickListener {
            aspectRatio(3F, 4F)
        }

        binding.btn916.setOnClickListener {
            aspectRatio(9F, 16F)
        }

        binding.btn11.setOnClickListener {
            aspectRatio(1F, 1F)
        }

        setupRotateWidget()
        setupScaleWidget()
    }

    private fun aspectRatio(with: Float, height: Float) {
        if (with == CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            binding.uCropView.cropImageView.zoomInImage(initialScale)
        } else {
            binding.uCropView.cropImageView.zoomOutImage(initialScale)
        }

        binding.uCropView.cropImageView.targetAspectRatio = with / height
        binding.uCropView.cropImageView.setImageToWrapCropBounds()
    }

    private var initialScale = -1f
    private val mImageListener: TransformImageView.TransformImageListener =
        object : TransformImageView.TransformImageListener {
            override fun onRotate(currentAngle: Float) {
                setAngleText(currentAngle)
            }

            override fun onScale(currentScale: Float) {
                if (initialScale == -1f) {
                    initialScale = currentScale
                }
                setScaleText(currentScale)
            }

            override fun onLoadComplete() {
                binding.uCropView.animate().alpha(1f).setDuration(300).interpolator =
                    AccelerateInterpolator()
                binding.blockView.isClickable = false
                mShowLoader = false
            }

            override fun onLoadFailure(e: Exception) {
                //setResultError(e)
                finish()
            }
        }


    private fun setImageData(intent: Intent) {
        val inputUri: Uri? = intent.getParcelableExtra(UCrop.EXTRA_INPUT_URI)
        val outputUri: Uri? = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI)
        processOptions(intent)

        if (inputUri != null && outputUri != null) {
            try {
                binding.uCropView.cropImageView.setImageUri(inputUri, outputUri)
            } catch (e: Exception) {
                finish()
            }
        } else {
            finish()
        }
    }

    private fun processOptions(intent: Intent) {
        // Bitmap compression options
        val compressionFormatName: String? =
            intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME)
        val compressFormat: Bitmap.CompressFormat? =
            compressionFormatName?.let { Bitmap.CompressFormat.valueOf(it) }
        mCompressFormat = compressFormat ?: DEFAULT_COMPRESS_FORMAT
        mCompressQuality =
            intent.getIntExtra(UCrop.Options.EXTRA_COMPRESSION_QUALITY, DEFAULT_COMPRESS_QUALITY)

        // Crop image view options
        binding.uCropView.cropImageView.maxBitmapSize = intent.getIntExtra(
            UCrop.Options.EXTRA_MAX_BITMAP_SIZE,
            CropImageView.DEFAULT_MAX_BITMAP_SIZE
        )
        binding.uCropView.cropImageView.setMaxScaleMultiplier(
            intent.getFloatExtra(
                UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER,
                CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER
            )
        )
        binding.uCropView.cropImageView.setImageToWrapCropBoundsAnimDuration(
            intent.getIntExtra(
                UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION,
                CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION
            ).toLong()
        )

        // Overlay view options
        binding.uCropView.overlayView.isFreestyleCropEnabled = intent.getBooleanExtra(
            UCrop.Options.EXTRA_FREE_STYLE_CROP,
            OverlayView.DEFAULT_FREESTYLE_CROP_MODE != OverlayView.FREESTYLE_CROP_MODE_DISABLE
        )

        // màu frame
        binding.uCropView.overlayView.setDimmedColor(
            intent.getIntExtra(
                UCrop.Options.EXTRA_DIMMED_LAYER_COLOR,
                resources.getColor(R.color.white)
            )
        )
        binding.uCropView.overlayView.setCircleDimmedLayer(
            intent.getBooleanExtra(
                UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER,
                OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER
            )
        )

        binding.uCropView.overlayView.setShowCropFrame(
            intent.getBooleanExtra(
                UCrop.Options.EXTRA_SHOW_CROP_FRAME,
                OverlayView.DEFAULT_SHOW_CROP_FRAME
            )
        )
        binding.uCropView.overlayView.setCropFrameColor(
            intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_FRAME_COLOR,
                resources.getColor(R.color.ucrop_color_default_crop_frame)
            )
        )
        binding.uCropView.overlayView.setCropFrameStrokeWidth(
            intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH,
                resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)
            )
        )

        binding.uCropView.overlayView.setShowCropGrid(
            intent.getBooleanExtra(
                UCrop.Options.EXTRA_SHOW_CROP_GRID,
                OverlayView.DEFAULT_SHOW_CROP_GRID
            )
        )
        binding.uCropView.overlayView.setCropGridRowCount(
            intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_GRID_ROW_COUNT,
                OverlayView.DEFAULT_CROP_GRID_ROW_COUNT
            )
        )
        binding.uCropView.overlayView.setCropGridColumnCount(
            intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_GRID_COLUMN_COUNT,
                OverlayView.DEFAULT_CROP_GRID_COLUMN_COUNT
            )
        )
        binding.uCropView.overlayView.setCropGridColor(
            intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_GRID_COLOR,
                resources.getColor(R.color.ucrop_color_default_crop_grid)
            )
        )
        binding.uCropView.overlayView.setCropGridCornerColor(
            intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_GRID_CORNER_COLOR,
                resources.getColor(R.color.ucrop_color_default_crop_grid)
            )
        )
        binding.uCropView.overlayView.setCropGridStrokeWidth(
            intent.getIntExtra(
                UCrop.Options.EXTRA_CROP_GRID_STROKE_WIDTH,
                resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width)
            )
        )

        // Aspect ratio options
        val aspectRatioX: Float = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_X, -1f)
        val aspectRatioY: Float = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_Y, -1f)

        val aspectRationSelectedByDefault: Int =
            intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0)
        val aspectRatioList: ArrayList<AspectRatio>? =
            intent.getParcelableArrayListExtra(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS)

        if (aspectRatioX >= 0 && aspectRatioY >= 0) {
            val targetAspectRatio = aspectRatioX / aspectRatioY
            binding.uCropView.cropImageView.targetAspectRatio =
                if (targetAspectRatio.isNaN()) CropImageView.SOURCE_IMAGE_ASPECT_RATIO else targetAspectRatio
        } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size) {
            val targetAspectRatio =
                aspectRatioList[aspectRationSelectedByDefault].aspectRatioX / aspectRatioList[aspectRationSelectedByDefault].aspectRatioY
            binding.uCropView.cropImageView.targetAspectRatio =
                if (targetAspectRatio.isNaN()) CropImageView.SOURCE_IMAGE_ASPECT_RATIO else targetAspectRatio
        } else {
            binding.uCropView.cropImageView.targetAspectRatio =
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO
        }

        // Result bitmap max size options
        val maxSizeX: Int = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0)
        val maxSizeY: Int = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0)

        if (maxSizeX > 0 && maxSizeY > 0) {
            binding.uCropView.cropImageView.setMaxResultImageSizeX(maxSizeX)
            binding.uCropView.cropImageView.setMaxResultImageSizeY(maxSizeY)
        }
    }

    private fun cropAndSaveImage() {
        binding.blockView.isClickable = true
        mShowLoader = true
        binding.uCropView.cropImageView.cropAndSaveImage(
            mCompressFormat,
            mCompressQuality,
            object : BitmapCropCallback {
                override fun onBitmapCropped(
                    resultUri: Uri,
                    offsetX: Int,
                    offsetY: Int,
                    imageWidth: Int,
                    imageHeight: Int
                ) {
                    setResultUri(
                        resultUri,
                        binding.uCropView.cropImageView.targetAspectRatio,
                        offsetX,
                        offsetY,
                        imageWidth,
                        imageHeight
                    )
                    finish()
                }

                override fun onCropFailure(t: Throwable) {
                    finish()
                }
            })
    }

    private fun setResultUri(
        uri: Uri?,
        resultAspectRatio: Float,
        offsetX: Int,
        offsetY: Int,
        imageWidth: Int,
        imageHeight: Int
    ) {
        setResult(
            RESULT_OK, Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
        )
    }

    private fun setAngleText(angle: Float) {
        binding.tvRotate.text = String.format(Locale.getDefault(), "%.1f°", angle)
    }

    private fun resetRotation() {
        binding.uCropView.cropImageView.postRotate(-binding.uCropView.cropImageView.currentAngle)
        binding.uCropView.cropImageView.setImageToWrapCropBounds()
    }

    private fun rotateByAngle(angle: Int) {
        binding.uCropView.cropImageView.postRotate(angle.toFloat())
        binding.uCropView.cropImageView.setImageToWrapCropBounds()
    }

    private fun setupRotateWidget() {
        binding.rotateScrollWheel.setScrollingListener(object : ScrollingListener {
            override fun onScroll(delta: Float, totalDistance: Float) {
                binding.uCropView.cropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT)
            }

            override fun onScrollEnd() {
                binding.uCropView.cropImageView.setImageToWrapCropBounds()
            }

            override fun onScrollStart() {
                binding.uCropView.cropImageView.cancelAllAnimations()
            }
        })
        binding.rotateScrollWheel.setMiddleLineColor(
            ContextCompat.getColor(
                this,
                com.yalantis.ucrop.R.color.ucrop_color_active_controls_color
            )
        )

        binding.imgResetRotate.setOnClickListener {
            resetRotation()
        }

        binding.imgRotate90.setOnClickListener {
            rotateByAngle(90)
        }
    }

    private fun setupScaleWidget() {
        binding.scaleScrollWheel.setScrollingListener(object : ScrollingListener {
            override fun onScroll(delta: Float, totalDistance: Float) {
                if (delta > 0) {
                    val zomIn: Float = (binding.uCropView.cropImageView.currentScale
                            + delta * ((binding.uCropView.cropImageView.maxScale - binding.uCropView.cropImageView.minScale) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT))
                    binding.uCropView.cropImageView.zoomInImage(zomIn)
                } else {
                    val zomOut: Float = (binding.uCropView.cropImageView.currentScale
                            + delta * ((binding.uCropView.cropImageView.maxScale - binding.uCropView.cropImageView.minScale) / SCALE_WIDGET_SENSITIVITY_COEFFICIENT))
                    binding.uCropView.cropImageView.zoomOutImage(zomOut)
                }
            }

            override fun onScrollEnd() {
                binding.uCropView.cropImageView.setImageToWrapCropBounds()
            }

            override fun onScrollStart() {
                binding.uCropView.cropImageView.cancelAllAnimations()
            }
        })
        binding.scaleScrollWheel.setMiddleLineColor(
            ContextCompat.getColor(
                this,
                com.yalantis.ucrop.R.color.ucrop_color_active_controls_color
            )
        )
    }

    private fun setScaleText(scale: Float) {
        binding.tvScale.text = String.format(
            Locale.getDefault(),
            "%d%%",
            (scale * 100).toInt()
        )
    }

    override fun onStop() {
        super.onStop()
        binding.uCropView.cropImageView.cancelAllAnimations()
    }

}