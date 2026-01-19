package com.example.fitlife.progress

import androidx.compose.runtime.Composable

@Composable
fun ProgressTabScreen() {
    ProgressPage(viewModel = ProgressViewModel(), onBack = {})
}
