package com.anwesh.uiprojects.linkedwview

/**
 * Created by anweshmishra on 16/06/18.
 */

import android.app.Activity
import android.view.View
import android.content.Context
import android.view.MotionEvent
import android.graphics.*

val LW_NODES : Int = 5

class LinkedWView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State (var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f, 0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LWNode(var i : Int) {

        private val state : State = State()

        private var next : LWNode? = null

        private var prev : LWNode? = null

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = w / LW_NODES
            prev?.draw(canvas, paint)
            canvas.save()
            canvas.translate(gap * i + gap / 2, h / 2)
            for (i in 0..1) {
                canvas.save()
                canvas.translate(gap/4 * (2 * i - 1), 0f)
                for (j in 0..1) {
                    val index : Int = j + 2 * i
                    canvas.save()
                    canvas.rotate(30f * (2 * j - 1))
                    canvas.drawLine(0f, -gap/2 * (1 - j), 0f, -gap/2 * (1 - j) + (1 - 2 * j) * (gap/2) * state.scales[index], paint)
                    canvas.restore()
                }
                canvas.restore()
            }
            canvas.restore()
        }

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < LW_NODES - 1) {
                next = LWNode(i + 1)
                next?.prev = this
            }
        }

        fun getNext(dir : Int, cb : () -> Unit) : LWNode {
            var curr : LWNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedW(var i : Int) {

        private var curr : LWNode = LWNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            paint.color = Color.parseColor("#9b59b6")
            paint.strokeWidth = Math.min(canvas.width, canvas.height).toFloat() / 60
            paint.strokeCap = Paint.Cap.ROUND
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedWView) {

        private val animator : Animator = Animator(view)

        private val lw : LinkedW = LinkedW(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lw.draw(canvas, paint)
            animator.animate {
                lw.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lw.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity)  : LinkedWView {
            val view : LinkedWView = LinkedWView(activity)
            activity.setContentView(view)
            return view
        }
    }
}