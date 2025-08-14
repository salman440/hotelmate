package com.systemnoxltd.hotelmatenox.ui.components

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.datepicker.MaterialDatePicker
import com.systemnoxltd.hotelmatenox.utils.formatMillisToDate

@Composable
fun DateRangeChipWithReset(
    startDate: Long,
    endDate: Long,
    onDateRangeSelected: (Long, Long) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp, 0.dp)
    ) {
        // Date range chip (opens picker on click)
        AssistChip(
            onClick = {
                val picker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select Date Range")
                    .setSelection(androidx.core.util.Pair(startDate, endDate))
                    .build()

                picker.addOnPositiveButtonClickListener { selection ->
                    val startMillis: Long? = selection.first
                    val endMillis: Long? = selection.second
                    if (startMillis != null && endMillis != null) {
                        onDateRangeSelected(startMillis, endMillis)
                    }
                }

                picker.show((context as AppCompatActivity).supportFragmentManager, picker.toString())
            },
            label = {
                Text(
                    "${formatMillisToDate(startDate)} - ${formatMillisToDate(endDate)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar Icon"
                )
            }
        )

        Spacer(
            modifier = Modifier
                .width(16.dp)
                .weight(1f)
        )

        // Reset Filter text
        Text(
            text = "Reset Filter",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onReset() },
            fontSize = 14.sp,
        )
    }
}
