package io.github.sceneview.sample.arpositiontracking

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Anchor
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.CursorNode

import io.github.sceneview.utils.doOnApplyWindowInsets

class MainFragment : Fragment(R.layout.fragment_main) {

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton
    lateinit var positionText: TextView
    lateinit var positionTrackingView: PositionTrackingView

    lateinit var cursorNode: CursorNode
    val modelNode: ArModelNode by lazy {
        isLoading = true
        ArModelNode().apply {
            loadModel(context = requireContext(),
                coroutineScope = lifecycleScope,
                glbFileLocation = "models/spiderbot.glb",
                onLoaded = {
                    actionButton.text = getString(R.string.move_object)
                    actionButton.setIconResource(R.drawable.ic_target)
                    isLoading = false
                })
        }
    }

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
            actionButton.isGone = value
        }

    private var lastCameraPosition = Float3()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = view.findViewById(R.id.sceneView)
        sceneView.planeRenderer.isEnabled = false
//        sceneView.planeRenderer.isVisible = false
        // Handle a fallback in case of non AR usage
        // The exception contains the failure reason
        // e.g. SecurityException in case of camera permission denied
        sceneView.onArSessionFailed = { _: Exception ->
            // If AR is not available, we add the model directly to the scene for a 3D only usage
            sceneView.addChild(modelNode)
        }
        sceneView.onTouchAr = { hitResult, _ ->
            val anchor = hitResult.createAnchor()
            anchorOrMove(anchor)
        }
        positionTrackingView = view.findViewById(R.id.positionTrackingView)
        positionText = view.findViewById(R.id.positionText)
        loadingView = view.findViewById(R.id.loadingView)
        actionButton = view.findViewById<ExtendedFloatingActionButton>(R.id.actionButton).apply {
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener {
                cursorNode.createAnchor()?.let {
                    anchorOrMove(it)
                }
            }
        }


        val minLogDelta = 0.1f
        sceneView.onArFrame.add { arFrame ->
            with(sceneView.camera) {

                val delta = lastCameraPosition.minus(position)
                if (delta.x >= minLogDelta || delta.y > minLogDelta || delta.z > minLogDelta) {
                    Log.d("sceneView.camera", "position = ${position.rounded()}")
                    positionText.text = position.rounded()
                    lastCameraPosition = position
                }
                positionTrackingView.userPosition = lastCameraPosition
            }
        }

        cursorNode = CursorNode(context = requireContext(), coroutineScope = lifecycleScope)
        cursorNode.onTrackingChanged = { _, isTracking, _ ->
            if (!isLoading) {
                actionButton.isGone = !isTracking
            }
        }
        sceneView.addChild(cursorNode)
    }

    fun anchorOrMove(anchor: Anchor) {
        if (!sceneView.children.contains(modelNode)) {
            sceneView.addChild(modelNode)
        }
        modelNode.anchor = anchor
    }
}

fun Float3.rounded() = "[ " +
        "x = " + "%.1f".format(x) +
        ", y = " + "%.1f".format(y) +
        ", z = " + "%.1f".format(z) +
        " ]"