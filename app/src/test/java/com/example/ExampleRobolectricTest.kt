package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LauncherRepository
import com.example.model.HomeGridLayout
import com.example.model.HomeScreenMode
import com.example.model.LauncherPreferences
import com.example.model.RecentsOrientation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NXV Launcher", appName)
    }

    @Test
    fun `test default home screen mode is vertical list with scrolling animations`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Clear prefs to check true clean default
        context.getSharedPreferences("launcher_preferences", Context.MODE_PRIVATE).edit().clear().commit()
        val repo = LauncherRepository(context)
        val prefs = repo.getPreferences()
        assertEquals(HomeScreenMode.VERTICAL_DOCK, prefs.homeScreenMode)
        assertTrue(prefs.enableFisheyeScroll)
    }

    @Test
    fun `test home grid layout persistence and all grid options`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = LauncherRepository(context)

        // Test every requested grid size
        val gridOptions = listOf(
            HomeGridLayout.GRID_4x5,
            HomeGridLayout.GRID_4x6,
            HomeGridLayout.GRID_5x5,
            HomeGridLayout.GRID_5x6,
            HomeGridLayout.GRID_5x7,
            HomeGridLayout.GRID_6x6
        )

        for (grid in gridOptions) {
            val prefs = LauncherPreferences(
                homeGridLayout = grid,
                homeScreenMode = HomeScreenMode.GRID
            )
            repo.savePreferences(prefs)

            val loaded = repo.getPreferences()
            assertEquals(grid, loaded.homeGridLayout)
            assertEquals(HomeScreenMode.GRID, loaded.homeScreenMode)
        }
    }

    @Test
    fun `test fill cells of uninstalled apps and lock layout toggles persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = LauncherRepository(context)

        val prefs = LauncherPreferences(
            fillCellsOfUninstalledApps = true,
            lockHomeScreenLayout = true,
            recentsOrientation = RecentsOrientation.VERTICAL,
            showMemoryStatus = false,
            blurAppPreviews = true
        )
        repo.savePreferences(prefs)

        val loaded = repo.getPreferences()
        assertTrue(loaded.fillCellsOfUninstalledApps)
        assertTrue(loaded.lockHomeScreenLayout)
        assertEquals(RecentsOrientation.VERTICAL, loaded.recentsOrientation)
        assertFalse(loaded.showMemoryStatus)
        assertTrue(loaded.blurAppPreviews)
    }

    @Test
    fun `test system memory info calculation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = LauncherRepository(context)
        val memInfo = repo.getSystemMemoryInfo()

        assertTrue(memInfo.totalBytes >= 0)
        assertTrue(memInfo.percentFree in 0..100)
        assertTrue(memInfo.availableGbString.isNotBlank())
        assertTrue(memInfo.totalGbString.isNotBlank())
    }

    @Test
    fun `test separate sound, vibration, and system wallpaper preferences persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = LauncherRepository(context)

        val prefs = LauncherPreferences(
            enableSound = true,
            enableHaptic = false,
            autoApplySystemWallpaper = true
        )
        repo.savePreferences(prefs)

        val loaded = repo.getPreferences()
        assertTrue(loaded.enableSound)
        assertFalse(loaded.enableHaptic)
        assertTrue(loaded.autoApplySystemWallpaper)
    }
}
