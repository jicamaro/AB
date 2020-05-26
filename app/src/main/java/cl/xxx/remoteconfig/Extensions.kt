package cl.xxx.remoteconfig

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <V : View> Fragment.bindView(id: Int):
        ReadOnlyProperty<Fragment, V> = required(id, viewFinder)
fun <V : View> View.bindView(id: Int):
        ReadOnlyProperty<View, V> = required(id, viewFinder)
fun <V : View> Activity.bindView(id: Int):
        ReadOnlyProperty<Activity, V> = required(id, viewFinder)
fun <V : View> Dialog.bindView(id: Int):
        ReadOnlyProperty<Dialog, V> = required(id, viewFinder)
fun <T : Any> Activity.bindBundle(key: String):
        ReadOnlyProperty<Activity, T> = required(key, argumentsFinder)

fun <V : View> Fragment.bindOptionalView(id: Int):
        ReadOnlyProperty<Fragment, V?> = optional(id, viewFinder)
fun <V : View> View.bindOptionalView(id: Int):
        ReadOnlyProperty<View, V?> = optional(id, viewFinder)
fun <V : View> Activity.bindOptionalView(id: Int):
        ReadOnlyProperty<Activity, V?> = optional(id, viewFinder)
fun <V : View> Dialog.bindOptionalView(id: Int):
        ReadOnlyProperty<Dialog, V?> = optional(id, viewFinder)
fun <T> Activity.bindOptionalBundle(key: String):
        ReadOnlyProperty<Activity, T?> = optional(key, argumentsFinder)

private val View.viewFinder: View.(Int) -> View?
    get() = { findViewById(it) }
private val Activity.viewFinder: Activity.(Int) -> View?
    get() = { findViewById(it) }
private val Dialog.viewFinder: Dialog.(Int) -> View?
    get() = { findViewById(it) }
private val Fragment.viewFinder: Fragment.(Int) -> View?
    get() = { view!!.findViewById(it) }
private val Activity.argumentsFinder: Activity.(String) -> Any?
    get() = { intent.extras?.get(it) }

private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing =
    throw IllegalStateException("View ID $id for '${desc.name}' not found.")

private fun bundleNotFound(key: String, desc: KProperty<*>): Nothing =
    throw IllegalStateException("Bundle KEY $key for '${desc.name}' not found.")

@Suppress("UNCHECKED_CAST")
private fun <T, B : Any?> required(key: String, finder: T.(String) -> Any?) = Lazy { t: T, desc ->
    t.finder(key) as B ?: bundleNotFound(key, desc)
}

@Suppress("UNCHECKED_CAST")
private fun <T, B : Any?> optional(key: String, finder: T.(String) -> Any?) = Lazy { t: T, _ -> t.finder(key) as B? }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(id: Int, finder: T.(Int) -> View?) = Lazy { t: T, desc ->
    t.finder(id) as V? ?: viewNotFound(id, desc)
}

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> optional(id: Int, finder: T.(Int) -> View?) = Lazy { t: T, _ -> t.finder(id) as V? }

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(ids: IntArray, finder: T.(Int) -> View?) = Lazy { t: T, desc ->
    ids.map {
        t.finder(it) as V? ?: viewNotFound(it, desc)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> optional(ids: IntArray, finder: T.(Int) -> View?) = Lazy { t: T, _ ->
    ids.map {
        t.finder(it) as V? }.filterNotNull()
}

private class Lazy<T, V>(private val initializer: (T, KProperty<*>) -> V) :
    ReadOnlyProperty<T, V>, LifecycleObserver {
    private object EMPTY
    private var value: Any? = EMPTY
    private var attachedToLifecycleOwner = false

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        checkAddToLifecycleOwner(thisRef)
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    private fun checkAddToLifecycleOwner(thisRef: T) {
        if (!attachedToLifecycleOwner && thisRef is LifecycleOwner) {
            thisRef.lifecycle.addObserver(this)
            attachedToLifecycleOwner = true
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun destroy() {
        value = EMPTY
    }
}
