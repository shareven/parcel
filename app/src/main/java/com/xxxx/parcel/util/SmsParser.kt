package com.xxxx.parcel.util

import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

class SmsParser {
    // 使用正则表达式来匹配地址和取件码（1个或多个取件码）
    private val addressPattern: Pattern =
        Pattern.compile("""(?i)(地址|收货地址|送货地址|位于|放至|已到达|到达|已到|送达|到|已放入|已存放至|已存放|放入)[\s\S]*?([\w\s-]+?(?:门牌|驿站|,|，|。|$)\d*)""")
    private val codePattern: Pattern = Pattern.compile(
        """(?i)(取件码为|提货号为|取货码为|提货码为|取件码（|提货号（|取货码（|提货码（|取件码『|提货号『|取货码『|提货码『|取件码【|提货号【|取货码【|提货码【|取件码\(|提货号\(|取货码\(|提货码\(|取件码\[|提货号\[|取货码\[|提货码\[|取件码|提货号|取货码|提货码|凭|快递|京东|天猫|中通|顺丰|韵达|德邦|菜鸟|拼多多|EMS|闪送|美团|饿了么|盒马|叮咚买菜|UU跑腿|签收码|签收编号|操作码|提货编码|收货编码|签收编码|取件編號|提貨號碼|運單碼|快遞碼|快件碼|包裹碼|貨品碼)\s*[A-Za-z0-9\s-]{2,}(?:[，,、][A-Za-z0-9\s-]{2,})*"""
    )

    // 动态规则存储
    private val customAddressPatterns = mutableListOf<String>()
    private val customCodePatterns = mutableListOf<Pattern>()
    private val ignoreKeywords = mutableListOf<String>()


    data class ParseResult(val address: String, val code: String, val success: Boolean)

    fun parseSms(sms: String): ParseResult {
        var foundAddress = ""
        var foundCode = ""
        
        // 检查是否包含忽略关键词
        for (ignoreKeyword in ignoreKeywords) {
            if (ignoreKeyword.isNotBlank() && sms.contains(ignoreKeyword, ignoreCase = true)) {
                return ParseResult("", "", false)
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

        // 如果自定义规则没有找到，尝试使用默认规则
        if (foundAddress.isEmpty()) {
            val addressMatcher: Matcher = addressPattern.matcher(sms)
            foundAddress =
                if (addressMatcher.find()) addressMatcher.group(2)?.toString() ?: "" else ""
        }

        if (foundCode.isEmpty()) {
            val codeMatcher: Matcher = codePattern.matcher(sms)

            while (codeMatcher.find()) {
                val match = codeMatcher.group(0)
                // 进一步将匹配到的内容按分隔符拆分成单个取件码
                val codes = match?.split(Regex("[，,、]"))
                foundCode = codes?.joinToString(", ") { it.trim() }?:""
                foundCode = foundCode.replace(Regex("[^A-Za-z0-9-, ]"), "")
            }

        }
        foundAddress = foundAddress.replace(Regex("[,，。]"), "")  // 移除所有标点和符号
        foundAddress = foundAddress.replace("取件", "")  // 移除"取件"
        return ParseResult(
            foundAddress,
            foundCode,
            foundAddress.isNotEmpty() && foundCode.isNotEmpty()
        )
    }

    // 添加自定义解析规则

    fun addCustomAddressPattern(pattern: String) {
        customAddressPatterns.add(pattern)
    }

    fun addCustomCodePattern(pattern: String) {
        customCodePatterns.add(Pattern.compile(pattern))
    }

    fun clearAllCustomPatterns() {
        customAddressPatterns.clear()
        customCodePatterns.clear()
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