package com.geeksmediapicker.koin

import com.geeksmediapicker.ui.PickerActivityVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val androidViewModel = module {
    viewModel { PickerActivityVM(get()) }
}