package com.xxxx.parcel.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

val timeFilterOptions = listOf(
    "全部",
    "今天",
    "近2天",
    "近3天",
    "近4天",
    "近5天",
    "近6天",
    "近7天",
    "近8天",
    "近9天",
    "近10天",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterSheet(
    isSeniorMode: Boolean,
    onOptionSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            timeFilterOptions.forEachIndexed { index, option ->
                Text(
                    text = option,
                    textAlign = TextAlign.Center,
                    style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            onOptionSelected(index)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onDismiss()
                                }
                            }
                        }
                )
            }
        }
    }
}
