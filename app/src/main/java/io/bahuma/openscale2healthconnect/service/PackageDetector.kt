package io.bahuma.openscale2healthconnect.service

import android.content.pm.PackageManager

class PackageDetector(private val packageManager: PackageManager) {
    fun detectPackage(): String {
        if (doesExist("com.health.openscale")) {
            return "com.health.openscale"
        }

        if (doesExist("com.health.openscale.light")) {
            return "com.health.openscale.light"
        }

        if (doesExist("com.health.openscale.pro")) {
            return "com.health.openscale.pro"
        }

        throw PackageNotFoundException()
    }

    private fun doesExist(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    class PackageNotFoundException() : Exception() {

    }
}