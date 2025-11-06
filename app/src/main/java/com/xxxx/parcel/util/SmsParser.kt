package com.xxxx.parcel.util

import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

class SmsParser {
    // 改进的地址匹配模式：更精确的匹配
    private val addressPatterns = listOf(
        // 模式1：明确的"取件地址"前缀
        Pattern.compile("""(?i)取件地址[:\s]*([^，。！？\s]+[\s\S]*?)(?=取件码|$|请|尽快|及时)"""),
        
        // 模式2：快递品牌+地址（精确匹配地址部分）
        Pattern.compile("""(?i)(?:近邻宝|丰巢|菜鸟|中通|顺丰|韵达|圆通|申通|京东)[^，。！？]*?[|｜]?\s*([^，。！？\s]+?(?:驿站|快递柜|快递点|柜|室|号|栋|楼|单元|小区|学校|食堂|医院)[^，。！？]*)"""),
        
        // 模式3：带"到"前缀的地址（精确提取地址部分）
        Pattern.compile("""(?i)(?:到|位于|放至|送达|放入)[\s]*([^，。！？\s]+?(?:驿站|快递柜|快递点|柜|室|号|栋|楼|单元|小区|学校|食堂|医院)[^，。！？]*)"""),
        
        // 模式4：纯地址模式（包含常见地点关键词）
        Pattern.compile("""([\u4e00-\u9fa5a-zA-Z0-9\s-]+?(?:驿站|快递柜|快递点|快递室|快递站|门牌|柜|室|号|栋|楼|单元|小区|花园|苑|广场|大厦|超市|便利店|学校|食堂|医院|银行|校内|校外|路|街|巷)[^，。！？]*)""")
    )

    // 增强的取件码模式：支持「」【】等多种括号
    private val codePattern: Pattern = Pattern.compile(
        """(?i)(取件码为?|提货号为?|取货码为?|提货码为?|取件码[『「【\(\[ ]|提货号[『「【\(\[ ]|取货码[『「【\(\[ ]|提货码[『「【\(\[ ]|取件码|提货号|取货码|提货码|凭|快递|京东|天猫|中通|顺丰|韵达|德邦|菜鸟|拼多多|EMS|闪送|美团|饿了么|盒马|叮咚买菜|UU跑腿|签收码|签收编号|操作码|提货编码|收货编码|签收编码|取件編號|提貨號碼|運單碼|快遞碼|快件碼|包裹碼|貨品碼)[:\s]*[『「【\(\[ ]*([A-Za-z0-9\s-]{4,})[」』】\)\]]*"""
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
        
        // 第一步：提取取件码（先提取取件码，便于后续清理）
        if (foundCode.isEmpty()) {
            val codeMatcher: Matcher = codePattern.matcher(sms)
            val allCodes = mutableListOf<String>()

            while (codeMatcher.find()) {
                // 尝试从不同的group中提取取件码
                var code = ""
                for (i in 1..codeMatcher.groupCount()) {
                    val group = codeMatcher.group(i) ?: ""
                    if (group.isNotBlank() && isValidCode(group)) {
                        code = group.trim()
                        break
                    }
                }
                
                if (code.isNotEmpty() && !allCodes.contains(code)) {
                    allCodes.add(code)
                    Log.d("SmsParser", "取件码匹配: $code")
                }
            }
            
            // 如果上面的方法没找到，尝试备选方法
            if (allCodes.isEmpty()) {
                val backupCodePattern = Pattern.compile("""[凭|码|码为|码是][\s:：]*[『「【\(\[ ]*([A-Za-z0-9-]{4,})[」』】\)\]]*""")
                val backupMatcher = backupCodePattern.matcher(sms)
                if (backupMatcher.find()) {
                    val code = backupMatcher.group(1) ?: ""
                    if (isValidCode(code)) {
                        allCodes.add(code)
                        Log.d("SmsParser", "备选方法匹配到取件码: $code")
                    }
                }
            }
            
            foundCode = allCodes.joinToString(", ")
        }

        // 第二步：使用自定义地址规则
        for (pattern in customAddressPatterns) {
            if (sms.contains(pattern, ignoreCase = true)) {
                foundAddress = pattern
                Log.d("SmsParser", "自定义地址规则匹配: $foundAddress")
                break
            }
        }
        
        // 第三步：如果自定义规则没有找到，尝试使用默认地址规则
        if (foundAddress.isEmpty()) {
            for ((index, pattern) in addressPatterns.withIndex()) {
                val matcher = pattern.matcher(sms)
                if (matcher.find()) {
                    // 根据不同的模式提取地址
                    var extractedAddress = when (index) {
                        0 -> matcher.group(1) ?: ""  // 模式1：取件地址 xxx
                        1 -> matcher.group(1) ?: ""  // 模式2：近邻宝｜xxx
                        2 -> matcher.group(1) ?: ""  // 模式3：到xxx
                        3 -> matcher.group(1) ?: ""  // 模式4：纯地址
                        else -> matcher.group(0) ?: ""
                    }.trim()
                    
                    // 清理提取的地址
                    extractedAddress = cleanAddressText(extractedAddress, sms, foundCode)
                    
                    if (extractedAddress.isNotEmpty()) {
                        foundAddress = extractedAddress
                        Log.d("SmsParser", "模式${index + 1}匹配到地址: '$foundAddress'")
                        break
                    }
                }
            }
        }

        // 第四步：最终清理
        foundAddress = finalCleanAddress(foundAddress, sms)
        foundCode = cleanCodeText(foundCode)
        
        Log.d("SmsParser", "最终地址: '$foundAddress'")
        Log.d("SmsParser", "最终取件码: '$foundCode'")
        
        val success = foundAddress.isNotEmpty() && foundCode.isNotEmpty()
        Log.d("SmsParser", "解析结果: 成功=$success")
        
        return ParseResult(foundAddress, foundCode, success)
    }

    /**
     * 清理地址文本（智能清理）
     */
    private fun cleanAddressText(address: String, originalSms: String, code: String): String {
        if (address.isEmpty()) return ""
        
        var cleaned = address
        
        // 1. 移除取件码部分（如果地址中包含了取件码）
        if (code.isNotEmpty()) {
            cleaned = cleaned.replace(code, "").trim()
        }
        
        // 2. 移除常见的干扰词
        val noiseWords = listOf("凭", "到", "位于", "放至", "送达", "放入", "取件", "请", "尽快", "及时")
        noiseWords.forEach { word ->
            val wordStr = word.toString() // 确保字符串类型
            cleaned = cleaned.replace(wordStr, "").trim()
        }
        
        // 3. 移除各种括号和标点
        cleaned = cleaned.replace(Regex("""[『「【\(\[」』】\)\],，。！？!?|｜]"""), "").trim()
        
        // 4. 移除开头的品牌名称 - 修复 startsWith 调用
        val expressBrands = listOf("近邻宝", "丰巢", "菜鸟", "中通", "顺丰", "韵达", "圆通", "申通", "京东")
        expressBrands.forEach { brand ->
            val brandStr = brand.toString() // 明确的字符串转换
            if (cleaned.isNotEmpty() && cleaned.startsWith(brandStr)) {
                cleaned = cleaned.substring(brandStr.length).trim()
            }
        }
        
        // 5. 移除多余的空格
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()
        
        return cleaned
    }

    /**
     * 最终地址清理
     */
    private fun finalCleanAddress(address: String, originalSms: String): String {
        if (address.isEmpty()) return ""
        
        var cleaned = address
        
        // 修复 startsWith 调用，使用明确的字符串模式检查
        if (cleaned.isNotEmpty()) {
            val firstChar = cleaned.substring(0, 1)
            val invalidStartPattern = Regex("""[^\u4e00-\u9fa5a-zA-Z0-9]""")
            if (invalidStartPattern.matches(firstChar)) {
                // 尝试从原短信中提取更准确的地址
                val betterPattern = Pattern.compile("""([\u4e00-\u9fa5a-zA-Z0-9\s-]+?(?:驿站|快递柜|快递点|柜|室|号|栋|楼|单元|小区|学校|食堂|医院)[^，。！？]*)""")
                val matcher = betterPattern.matcher(originalSms)
                if (matcher.find()) {
                    val betterAddress = matcher.group(1) ?: ""
                    if (betterAddress.isNotEmpty()) {
                        cleaned = betterAddress.trim()
                        Log.d("SmsParser", "重新提取到更准确的地址: '$cleaned'")
                    }
                }
            }
        }
        
        return cleaned.replace(Regex("""^[^\\w\u4e00-\u9fa5]+"""), "").trim()
    }

    /**
     * 验证是否为有效的取件码
     */
    private fun isValidCode(code: String): Boolean {
        if (code.length < 4) return false
        
        // 排除纯数字且长度超过8位的（可能是电话号码或日期）
        if (code.matches(Regex("""^\\d{9,}$"""))) return false
        
        // 排除纯数字且长度小于4位的
        if (code.matches(Regex("""^\\d{1,3}$"""))) return false
        
        // 排除常见的日期格式
        if (code.matches(Regex("""^\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}$"""))) return false
        
        // 排除明显不是取件码的内容
        if (code.contains(Regex("""[年月日时分秒]"""))) return false
        
        return true
    }

    /**
     * 清理取件码文本
     */
    private fun cleanCodeText(code: String): String {
        if (code.isEmpty()) return ""
        
        return code
            .replace(Regex("""[『「【\(\[」』】\)\]]"""), "")  // 移除所有括号
            .replace(Regex("""\\s+"""), "")  // 移除所有空格
            .trim()
    }

    // 添加自定义解析规则
    fun addCustomAddressPattern(pattern: String) {
        customAddressPatterns.add(pattern)
        Log.d("SmsParser", "添加自定义地址规则: $pattern")
    }

    fun addCustomCodePattern(pattern: String) {
        try {
            customCodePatterns.add(Pattern.compile(pattern))
            Log.d("SmsParser", "添加自定义取件码规则: $pattern")
        } catch (e: Exception) {
            Log.e("SmsParser", "无效的正则表达式: $pattern", e)
        }
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
