package com.vodang.greenmind.register

import kotlin.test.Test
import kotlin.test.assertEquals

class RegisterHelpersTest {

    @Test
    fun `classifyVietnamRegionByLatitude returns North for Thanh Hoa and above`() {
        assertEquals("North", classifyVietnamRegionByLatitude(19.8))
        assertEquals("North", classifyVietnamRegionByLatitude(21.0))
    }

    @Test
    fun `classifyVietnamRegionByLatitude returns Central between Thanh Hoa and Phu Yen`() {
        assertEquals("Central", classifyVietnamRegionByLatitude(19.79))
        assertEquals("Central", classifyVietnamRegionByLatitude(13.1))
        assertEquals("Central", classifyVietnamRegionByLatitude(16.0))
    }

    @Test
    fun `classifyVietnamRegionByLatitude returns South below Phu Yen`() {
        assertEquals("South", classifyVietnamRegionByLatitude(13.09))
        assertEquals("South", classifyVietnamRegionByLatitude(10.0))
    }

    @Test
    fun `pickerMillisToIsoDate converts Unix epoch day correctly`() {
        assertEquals("1970-01-01", pickerMillisToIsoDate(0L))
        assertEquals("1970-01-02", pickerMillisToIsoDate(86_400_000L))
    }
}
