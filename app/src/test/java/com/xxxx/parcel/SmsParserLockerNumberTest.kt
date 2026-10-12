package com.xxxx.parcel

import com.xxxx.parcel.util.SmsParser
import org.junit.Assert.assertEquals
import org.junit.Test

class SmsParserLockerNumberTest {

    private fun lockerOf(sms: String): String = SmsParser().parseSms(sms).lockerNumber

    @Test
    fun chineseNumeralLocker() {
        assertEquals("4", lockerOf("您的快递已存放至丰巢四号柜，取件码1234"))
    }

    @Test
    fun chineseCompoundNumeralLocker() {
        assertEquals("23", lockerOf("快递已放入二十三号柜格口12，取件码 88-66"))
        assertEquals("15", lockerOf("十五号柜取件码1111"))
    }

    @Test
    fun chineseHundredLocker() {
        assertEquals("105", lockerOf("一百零五号柜取件码1111"))
    }

    @Test
    fun arabicNumeralLockerUnchanged() {
        assertEquals("7", lockerOf("快递已放入7号柜23-32格口，取件码7777"))
    }

    @Test
    fun lockerAddressNormalizedWhenPreferred() {
        val parser = SmsParser().apply { preferLockerAddress = true }
        val result = parser.parseSms("您的快递已存放至丰巢四号柜，取件码1234")
        assertEquals("4号柜", result.address)
        val result2 = parser.parseSms("快递已放入二十三号柜格口12，取件码8866")
        assertEquals("23号柜", result2.address)
    }
}
