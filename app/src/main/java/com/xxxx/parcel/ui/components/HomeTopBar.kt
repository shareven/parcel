package com.xxxx.parcel.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.xxxx.parcel.util.openPddIdentityEntry
import com.xxxx.parcel.util.openTaobaoIdentityEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    context: Context,
    navController: NavController,
    isSeniorMode: Boolean,
    isTimeSort: Boolean,
    preferLockerAddress: Boolean,
    isHorizontalLayout: Boolean,
    showCompleted: Boolean,
    showCodeTime: Boolean,
    showCompartment: Boolean,
    currentFilterLabel: String,
    successCount: Int,
    failedCount: Int,
    onFilterClick: () -> Unit,
    onSuccessCountClick: () -> Unit,
    onFailedCountClick: () -> Unit,
    onToggleTimeSort: () -> Unit,
    onTogglePreferLockerAddress: () -> Unit,
    onToggleHorizontalLayout: () -> Unit,
    onToggleShowCompleted: () -> Unit,
    onToggleShowCodeTime: () -> Unit,
    onToggleShowCompartment: () -> Unit,
    onSeniorModeChanged: (Boolean) -> Unit,
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            TextButton(
                onClick = onFilterClick,

                ) {
                Text(
                    text = currentFilterLabel,
                    style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                )
            }
        },
        actions = {

            Button(
                contentPadding = PaddingValues(2.dp),
                colors = ButtonColors(
                    containerColor = Color(0xFF25AF22),
                    contentColor = Color.White,
                    disabledContentColor = Color.DarkGray,
                    disabledContainerColor = Color.LightGray
                ),
                onClick = onSuccessCountClick,
            ) {
                Text(
                    text = successCount.toString(),
                    fontWeight = FontWeight.Bold,
                    style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.width(16.dp))
            Button(
                contentPadding = PaddingValues(2.dp),
                colors = ButtonColors(
                    containerColor = Color(0xFFAB1A65),
                    contentColor = Color.White,
                    disabledContentColor = Color.DarkGray,
                    disabledContainerColor = Color.LightGray
                ),
                onClick = onFailedCountClick,
            ) {
                Text(
                    text = failedCount.toString(),
                    color = Color.White,
                    style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.width(8.dp))
            // 顶栏菜单：规则列表 / 监听第三方app通知 / 关于
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "菜单",
                        modifier = Modifier.size(if (isSeniorMode) 48.dp else 24.dp)
                    )
                }
                DropdownMenu(
                    modifier = if (isSeniorMode) Modifier.fillMaxWidth() else Modifier,
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }) {

                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isTimeSort) "切换为默认排序" else "切换为时间排序",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleTimeSort()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (preferLockerAddress) "不优先显示几号柜" else "优先显示几号柜",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            onTogglePreferLockerAddress()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isHorizontalLayout) "切换为纵向地址" else "切换为横向地址",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleHorizontalLayout()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (showCompleted) "隐藏已取件的码" else "显示已取件的码",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleShowCompleted()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (showCompartment) "隐藏格口" else "显示格口",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleShowCompartment()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (showCodeTime) "隐藏时间" else "显示时间",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            onToggleShowCodeTime()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "添加自定义取件短信",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            navController.navigate("add_custom_sms/ ")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "地址归类",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            navController.navigate("address_group")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "规则列表",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            navController.navigate("rules")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "查看日志",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            navController.navigate("logs")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "监听第三方app通知",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            navController.navigate("use_notification")
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "淘宝身份码",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            openTaobaoIdentityEntry(context)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "拼多多身份码",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            openPddIdentityEntry(context)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isSeniorMode) "关闭老人模式" else "开启老人模式",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            onSeniorModeChanged(!isSeniorMode)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "关于",
                                style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showMenu = false
                            navController.navigate("about")
                        }
                    )
                }
            }

        }
    )
}
