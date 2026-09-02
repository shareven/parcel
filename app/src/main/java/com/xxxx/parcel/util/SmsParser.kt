package com.xxxx.parcel.util

import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

class SmsParser {
    // 是否优先匹配快递柜地址
    var preferLockerAddress: Boolean = true

    // 使用正则表达式来匹配地址和取件码（1个或多个取件码）优先匹配快递柜
    val lockerPattern: Pattern =
        Pattern.compile("""(?i)([0-9]+)号(?:柜|快递柜|丰巢柜|蜂巢柜|熊猫柜|兔喜快递柜)""")
    // 格口号：优先匹配"格口"字样前的字符，如 "7号柜23-32格口"、"格口23-32"
    private val compartmentPattern: Pattern =
        Pattern.compile("""([A-Za-z0-9\-*—_]+)\s*格口""")
    // 变体："格口"字样在前，如 "格口：23-32"、"格口 23-32"
    private val compartmentPrefixPattern: Pattern =
        Pattern.compile("""格口\s*[:：]?\s*([A-Za-z0-9\-*—_]+)""")
    // 兜底：柜号后直接紧跟的字符，如 "7号柜23-32"；兼容 "5号柜领取A11" 这类带连接词写法
    private val compartmentAfterLockerPattern: Pattern =
        Pattern.compile("""[0-9]+号(?:柜|快递柜|丰巢柜|蜂巢柜|熊猫柜|兔喜快递柜)\s*(?:领取|取件)?\s*([A-Za-z0-9\-*—_]+)""")
    // 紧跟数字后的计量单位词 → 是时长/费用等，不是格口，如 "7号柜36小时免费"
    private val unitAfterNumberPattern: Pattern =
        Pattern.compile("""^(小时|天|日|分钟|秒|元|件|个|月|年|折)""")
    private val addressPattern: Pattern =
        Pattern.compile("""(?i)(地址|收货地址|送货地址|位于|放至|已到达|到达|已到|送达|到|已放入|已存放至|已存放|放入)[\s\S]*?([\w\s-]+?(?:门牌|驿站|快递点|门面|柜|,|，|。|$|[“”"'」』]))""")
    private val codePattern: Pattern = Pattern.compile(
        """(?i)(请用|取件码为|提货号为|取货码为|提货码为|取件码"|提货号"|取货码"|提货码"|凭"|取件码“|提货号“|取货码“|提货码“|凭“|取件码（|提货号（|取货码（|提货码（|凭（|取件码『|提货号『|取货码『|提货码『|凭『|取件码【|提货号【|取货码【|提货码【|凭【|取件码\(|提货号\(|取货码\(|提货码\(|凭\(|取件码\[|提货号\[|取货码\[|提货码\[|凭\[|取件码|提货号|取货码|提货码|凭|签收码|操作码|提货编码|快件编号|快件编码|快件码|提货编号|取件编码|取件编号|收货编码|签收编码|取件編號|提貨號碼|運單碼|快遞碼|快件碼|包裹碼|貨品碼)\s*[：:]?\s*[A-Za-z0-9\s-*—]{2,}(?:[，,、][A-Za-z0-9\s-*—]{2,})*"""
    )

    // 动态规则存储
    private val customAddressPatterns = mutableListOf<String>()
    private val customCodePatterns = mutableListOf<Pattern>()
    private val customCodeKeywords = mutableListOf<String>()
    private val ignoreKeywords = mutableListOf<String>()


    data class ParseResult(val address: String, val code: String, val lockerNumber: String, val success: Boolean, val compartmentNumber: String = "")

    fun parseSms(sms: String): ParseResult {
        var foundAddress = ""
        var foundCode = ""

        // 检查是否包含忽略关键词
        for (ignoreKeyword in ignoreKeywords) {
            if (ignoreKeyword.isNotBlank() && sms.contains(ignoreKeyword, ignoreCase = true)) {
                return ParseResult("", "", "", false)
            }
        }

        // 使用字符串匹配查找地址
        for (pattern in customAddressPatterns) {
            if (sms.contains(pattern, ignoreCase = true)) {
                foundAddress = pattern
                break
            }
        }
        for (pattern in customCodePatterns) {
            val matcher = pattern.matcher(sms)
            if (matcher.find()) {
                foundCode = matcher.group(1)?.toString() ?: ""
                break
            }
        }
        for (keyword in customCodeKeywords) {
            val keywordMatcher = Pattern.compile(
                """(?i)${Pattern.quote(keyword)}\s*[：:]?\s*[A-Za-z0-9\s\-*—]{2,}(?:[，,、][A-Za-z0-9\s\-*—]{2,})*"""
            ).matcher(sms)
            if (keywordMatcher.find()) {
                val match = keywordMatcher.group(0)
                val codes = match?.split(Regex("[，,、]"))
                foundCode = codes?.joinToString(", ") { it.trim() }?.replace(Regex("[^A-Za-z0-9-*—, ]"), "")?.trim() ?: ""
                break
            }
        }

        // 如果自定义规则没有找到，优先匹配柜号地址，其次默认规则
        if (foundAddress.isEmpty()) {
            if (preferLockerAddress) {
                val lockerMatcher: Matcher = lockerPattern.matcher(sms)
                foundAddress = if (lockerMatcher.find()) lockerMatcher.group().toString() ?: "" else ""
            }

            if (foundAddress.isEmpty()) {
                val addressMatcher: Matcher = addressPattern.matcher(sms)
                var longestAddress = ""
                while (addressMatcher.find()) {
                    val currentAddress = addressMatcher.group(2)?.toString() ?: ""
                    if (currentAddress.length > longestAddress.length) {
                        longestAddress = currentAddress
                    }
                }
                foundAddress = longestAddress
            }
        }

        // 始终提取柜号数字
        val lockerMatcher: Matcher = lockerPattern.matcher(sms)
        val lockerNumber = if (lockerMatcher.find()) lockerMatcher.group(1) ?: "" else ""

        // 始终提取格口号：优先匹配"格口"字样（前后两种写法），其次匹配柜号后紧跟的字符
        val compartmentNumber = run {
            val m1 = compartmentPattern.matcher(sms)
            if (m1.find()) return@run m1.group(1) ?: ""
            val m1b = compartmentPrefixPattern.matcher(sms)
            if (m1b.find()) return@run m1b.group(1) ?: ""
            // 兜底：柜号后紧跟的字符；校验其后非单位词，排除 "7号柜36小时免费" 误识别
            val m2 = compartmentAfterLockerPattern.matcher(sms)
            if (m2.find()) {
                val candidate = m2.group(1) ?: ""
                val after = sms.substring(m2.end())
                if (candidate.isNotEmpty() && !unitAfterNumberPattern.matcher(after).find()) candidate else ""
            } else ""
        }

        if (foundCode.isEmpty()) {
            val codeMatcher: Matcher = codePattern.matcher(sms)

            while (codeMatcher.find()) {
                val match = codeMatcher.group(0)
                // 进一步将匹配到的内容按分隔符拆分成单个取件码
                val codes = match?.split(Regex("[，,、]"))
                foundCode = codes?.joinToString(", ") { it.trim() }?:""
                foundCode = foundCode.replace(Regex("[^A-Za-z0-9-*—, ]"), "").trim()
            }

        }
        foundAddress = foundAddress.replace(Regex("[,，。“”\"'」』]"), "")  // 移除所有标点和符号（含引号类终止符）
        foundAddress = foundAddress.replace("取件", "")  // 移除"取件"
        return ParseResult(
            foundAddress,
            foundCode,
            lockerNumber,
            foundAddress.isNotEmpty() && foundCode.isNotEmpty(),
            compartmentNumber
        )
    }

    // 添加自定义解析规则

    fun addCustomAddressPattern(pattern: String) {
        customAddressPatterns.add(pattern)
    }

    fun addCustomCodePattern(pattern: String) {
        customCodePatterns.add(Pattern.compile(pattern))
    }

    fun addCustomCodeKeyword(keyword: String) {
        if (keyword.isNotBlank()) {
            customCodeKeywords.add(keyword)
        }
    }

    fun clearAllCustomPatterns() {
        customAddressPatterns.clear()
        customCodePatterns.clear()
        customCodeKeywords.clear()
        ignoreKeywords.clear()
    }

    fun addIgnoreKeyword(keyword: String) {
        if (keyword.isNotBlank() && !ignoreKeywords.contains(keyword)) {
            ignoreKeywords.add(keyword)
        }
    }

    fun removeIgnoreKeyword(keyword: String) {
        ignoreKeywords.remove(keyword)
    }

    fun getIgnoreKeywords(): List<String> = ignoreKeywords.toList()

    fun clearIgnoreKeywords() {
        ignoreKeywords.clear()
    }
}