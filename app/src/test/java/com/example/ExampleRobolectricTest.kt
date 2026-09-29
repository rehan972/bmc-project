package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiTriageService
import com.example.data.AppLanguage
import com.example.data.LanguageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI BMC Complaint Triage", appName)
    }

    @Test
    fun testPotholeTriage() {
        val service = AiTriageService()
        val result = service.evaluateLocalAiEngine(
            description = "There is a huge pothole on the road near the railway station.",
            bitmap = null,
            location = "Andheri"
        )
        assertEquals("Pothole / Road Damage", result.category)
        assertEquals("Roads Department", result.department)
        assertTrue(result.priority == "HIGH" || result.priority == "CRITICAL")
    }

    @Test
    fun testMarathiGarbageTriage() {
        val service = AiTriageService()
        val result = service.evaluateLocalAiEngine(
            description = "आमच्या भागात तीन दिवसांपासून कचरा उचललेला नाही.",
            bitmap = null,
            location = "दादर"
        )
        assertEquals("Waste / Garbage", result.category)
        assertEquals("Solid Waste Management", result.department)
        assertTrue(result.priority == "HIGH" || result.priority == "MEDIUM")
    }

    @Test
    fun testHindiPotholeTriage() {
        val service = AiTriageService()
        val result = service.evaluateLocalAiEngine(
            description = "सड़क पर गहरा गड्ढा है जिससे दुर्घटना हो सकती है।",
            bitmap = null,
            location = "अंधेरी"
        )
        assertEquals("Pothole / Road Damage", result.category)
        assertEquals("Roads Department", result.department)
        assertTrue(result.priority == "HIGH" || result.priority == "CRITICAL")
    }

    @Test
    fun testLanguageManager() {
        val marathiSubmit = LanguageManager.getString("raise_complaint", AppLanguage.MARATHI)
        assertEquals("तक्रार नोंदवा", marathiSubmit)

        val hindiSubmit = LanguageManager.getString("raise_complaint", AppLanguage.HINDI)
        assertEquals("शिकायत दर्ज करें", hindiSubmit)
    }
}
