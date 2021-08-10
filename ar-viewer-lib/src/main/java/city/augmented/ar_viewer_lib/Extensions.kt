package city.augmented.ar_viewer_lib

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace

inline fun <reified F : Fragment> FragmentManager.replaceFragment(
    @IdRes container: Int,
    crossinline onCommit: (F) -> Unit = {}
) = commit {
    val className = F::class.java.name
    replace<F>(container, className)
    runOnCommit {
        val fragment = findFragmentByTag(className) as F
        onCommit(fragment)
    }
}
