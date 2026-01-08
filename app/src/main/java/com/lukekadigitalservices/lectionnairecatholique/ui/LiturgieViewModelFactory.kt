package com.lukekadigitalservices.lectionnairecatholique.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lukekadigitalservices.lectionnairecatholique.data.LiturgieRepository

class LiturgieViewModelFactory(
    private val repository: LiturgieRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiturgieViewModel::class.java)) {
            return LiturgieViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}