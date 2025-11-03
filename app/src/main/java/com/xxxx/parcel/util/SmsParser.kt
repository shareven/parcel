package com.xxxx.parcel.util

import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

class SmsParser {
    // 改进的地址匹配模式：支持多种格式
    private val addressPatterns = listOf(
        // 模式1：明确的"取件地址"前缀
        Pattern.compile("""(?i)取件地址[:\s]*([^，。！？\s]+[\s\S]*?)(?=取件码|$|请|尽快|及时)"""),
        
        // 模式2：快递品牌+地址（如：近邻宝快递柜｜xxx）
        Pattern.compile("""(?i)(近邻宝|丰巢|菜鸟|中通|顺丰|韵达|圆通|申通|京东)[^，。！？]*?[|｜]?\s*([^，。！？\s]+[\s\S]*?)(?=取件码|$|请|尽快|及时)"""),
        
        // 模式3：纯地址模式（包含常见地点关键词）
        Pattern.compile("""([\u4e00-\u9fa5a-zA-Z0-9\s-]+?(?:驿站|快递柜|快递点|快递室|快递站|门牌|柜|室|号|栋|楼|单元|小区|花园|苑|广场|大厦|超市|便利店|学校|食堂|医院|银行|校内|校外|路|街|巷)[^，。！？]*)"""),
        
        // 模式4：带"到"前缀的地址（原来的逻辑保留）
        Pattern.compile("""(?i)(?:到|位于|放至|送达|放入)[\s]*([^，。！？\s]+[\s\S]*?)(?=取件|$|请|尽快|及时)""")
    )

    // 取件码模式（保持不变）
    private val codePattern: Pattern = Pattern.compile(
        """(?i)(取件码为|提货号为|取货码为|提货码为|取件码『|提货号『|取货码『|提货码『|取件码【|提货号【|取货码【|提货码【|取件码\(|提货号\(|取货码\(|提货码\(|取件码\[|提货号\[|取货码\[|提货码\[|取件码|提货号|取货码|提货码|凭|快递|京东|天猫|中通|顺丰|韵达|德邦|菜鸟|拼多多|EMS|闪送|美团|饿了么|盒马|叮咚买菜|UU跑腿|签收码|签收编号|操作码|提货编码|收货编码|签收编码|取件編號|提貨號碼|運單碼|快遞碼|快件碼|包裹碼|貨品碼)\s*[A-Za-z0-9\s-]{2,}(?:[，,、][A-Za-z0-9\s-]{2,})*"""
    )

    // 动态规则存储
    private val customAddressPatterns = mutableListOf<String>()
    private val customCodePatterns = mutableListOf<Pattern>()
    private val ignoreKeywords = mutableListOf<String>()

    data class ParseResult(val address: String, val code: String, val success: Boolean)

    fun parseSms(sms: String): ParseResult {
        var foundAddress = ""
        var foundCode = ""
        
        Log.d("SmsParser", "开始解析短信: $sms")
        
        // 检查是否包含忽略关键词
        for (ignoreKeyword in ignoreKeywords) {
            if (ignoreKeyword.isNotBlank() && sms.contains(ignoreKeyword, ignoreCase = true)) {
                Log.d("SmsParser", "包含忽略关键词: $ignoreKeyword，跳过解析")
                return ParseResult("", "", false)
            }
        }
        
        // 第一步：使用自定义地址规则
        for (pattern in customAddressPatterns) {
            if (sms.contains(pattern, ignoreCase = true)) {
                foundAddress = pattern
                Log.d("SmsParser", "自定义地址规则匹配: $foundAddress")
                break
            }
        }
        
        // 第二步：使用自定义取件码规则
        for (pattern in customCodePatterns) {
            val matcher = pattern.matcher(sms)
            if (matcher.find()) {
                foundCode = matcher.group(1)?.toString() ?: ""
                Log.d("SmsParser", "自定义取件码规则匹配: $foundCode")
                break
            }
        }

        // 第三步：如果自定义规则没有找到，尝试使用默认地址规则
        if (foundAddress.isEmpty()) {
            for ((index, pattern) in addressPatterns.withIndex()) {
                val matcher = pattern.matcher(sms)
                if (matcher.find()) {
                    // 根据不同的模式提取地址
                    foundAddress = when (index) {
                        0 -> matcher.group(1) ?: ""  // 模式1：取件地址 xxx
                        1 -> matcher.group(2) ?: ""  // 模式2：近邻宝｜xxx
                        2 -> matcher.group(1) ?: ""  // 模式3：纯地址
                        3 -> matcher.group(1) ?: ""  // 模式4：到xxx
                        else -> matcher.group(0) ?: ""
                    }
                    Log.d("SmsParser", "模式${index + 1}匹配到地址: '$foundAddress'")
                    break
                }
            }
        }

        // 第四步：提取取件码
        if (foundCode.isEmpty()) {
            val codeMatcher: Matcher = codePattern.matcher(sms)
            val allCodes = mutableListOf<String>()

            while (codeMatcher.find()) {
                val match = codeMatcher.group(0) ?: ""
                Log.d("SmsParser", "取件码原始匹配: $match")
                
                // 提取纯数字和字母组合（取件码）
                val codePattern = Pattern.compile("""[A-Za-z0-9-]{4,}""")
                val codeMatcherInner = codePattern.matcher(match)
                
                while (codeMatcherInner.find()) {
                    val code = codeMatcherInner.group()
                    // 过滤掉明显不是取件码的内容（如日期、电话号码等）
                    if (isValidCode(code) && !allCodes.contains(code)) {
                        allCodes.add(code)
                    }
                }
            }
            
            foundCode = allCodes.joinToString(", ")
            Log.d("SmsParser", "最终取件码: '$foundCode'")
        }

        // 清理地址文本
        foundAddress = cleanAddressText(foundAddress)
        Log.d("SmsParser", "清理后地址: '$foundAddress'")
        
        val success = foundAddress.isNotEmpty() && foundCode.isNotEmpty()
        Log.d("SmsParser", "解析结果: 成功=$success, 地址='$foundAddress', 取件码='$foundCode'")
        
        return ParseResult(foundAddress, foundCode, success)
    }

    /**
     * 验证是否为有效的取件码
     */
    private fun isValidCode(code: String): Boolean {
        // 排除纯数字且长度超过8位的（可能是电话号码或日期）
        if (code.matches(Regex("""^\d{9,}$"""))) return false
        
        // 排除纯数字且长度小于4位的
        if (code.matches(Regex("""^\d{1,3}$"""))) return false
        
        // 排除常见的日期格式
        if (code.matches(Regex("""^\d{4}[-/]\d{1,2}[-/]\d{1,2}$"""))) return false
        
        return true
    }

    /**
     * 清理地址文本
     */
    private fun cleanAddressText(address: String): String {
        var cleaned = address
            .replace(Regex("[,，。！？!?|｜]"), "")  // 移除标点符号
            .replace(Regex("取件$"), "")  // 移除结尾的"取件"
            .replace(Regex("^[|｜\s]+"), "")  // 移除开头的|和空格
            .replace(Regex("[\s]+$"), "")  // 移除结尾的空格
            .trim()
        
        // 如果地址以快递品牌开头，移除品牌名
        val expressBrands = listOf("近邻宝", "丰巢", "菜鸟", "中通", "顺丰", "韵达", "圆通", "申通", "京东")
        for (brand in expressBrands) {
            if (cleaned.startsWith(brand)) {
                cleaned = cleaned.substring(brand.length).trim()
                break
            }
        }
        
        return cleaned
    }

    // 添加自定义解析规则
    fun addCustomAddressPattern(pattern: String) {
        customAddressPatterns.add(pattern)
        Log.d("SmsParser", "添加自定义地址规则: $pattern")
    }

    fun addCustomCodePattern(pattern: String) {
        customCodePatterns.add(Pattern.compile(pattern))
        Log.d("SmsParser", "添加自定义取件码规则: $pattern")
    }

    fun clearAllCustomPatterns() {
        customAddressPatterns.clear()
        customCodePatterns.clear()
        ignoreKeywords.clear()
        Log.d("SmsParser", "清空所有自定义规则")
    }

    fun addIgnoreKeyword(keyword: String) {
        if (keyword.isNotBlank() && !ignoreKeywords.contains(keyword)) {
            ignoreKeywords.add(keyword)
            Log.d("SmsParser", "添加忽略关键词: $keyword")
        }
    }

    fun removeIgnoreKeyword(keyword: String) {
        ignoreKeywords.remove(keyword)
        Log.d("SmsParser", "移除忽略关键词: $keyword")
    }

    fun getIgnoreKeywords(): List<String> = ignoreKeywords.toList()

    fun clearIgnoreKeywords() {
        ignoreKeywords.clear()
        Log.d("SmsParser", "清空忽略关键词")
    }
}
