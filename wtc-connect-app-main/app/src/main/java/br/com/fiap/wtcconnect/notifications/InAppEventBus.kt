package br.com.fiap.wtcconnect.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object InAppEventBus {
    private val _events = MutableSharedFlow<InAppEvent>(extraBufferCapacity = 10)
    val events = _events.asSharedFlow()

    fun emit(event: InAppEvent) {
        CoroutineScope(Dispatchers.Main).launch {
            _events.emit(event)
        }
    }
}

