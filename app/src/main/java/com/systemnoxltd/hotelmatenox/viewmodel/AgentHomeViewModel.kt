package com.systemnoxltd.hotelmatenox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemnoxltd.hotelmatenox.model.Customer
import com.systemnoxltd.hotelmatenox.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AgentHomeViewModel : ViewModel() {
    private val repository = CustomerRepository()
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers

    fun loadCustomers(agentId: String) {
        viewModelScope.launch {
            repository.getCustomersByAgent(agentId) {
                _customers.value = it
            }
        }
    }

    fun deleteCustomer(id: String) {
        repository.deleteCustomer(id) {
            _customers.value = _customers.value.filterNot { it.id == id }
        }
    }
}
