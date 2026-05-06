package br.com.fiap.wtcconnect.data.repository

import br.com.fiap.wtcconnect.network.CreateSegmentRequest
import br.com.fiap.wtcconnect.network.SegmentApi
import br.com.fiap.wtcconnect.network.SegmentDto
import br.com.fiap.wtcconnect.network.UpdateSegmentRequest

class SegmentRepository(private val api: SegmentApi) {

    suspend fun getSegments(): Result<List<SegmentDto>> = runCatching {
        api.getSegments()
    }

    suspend fun createSegment(name: String, description: String = ""): Result<SegmentDto> = runCatching {
        api.createSegment(CreateSegmentRequest(name, description))
    }

    suspend fun getSegmentById(id: String): Result<SegmentDto> = runCatching {
        api.getSegmentById(id)
    }

    suspend fun getSegmentByName(name: String): Result<SegmentDto> = runCatching {
        api.getSegmentByName(name)
    }

    suspend fun updateSegment(
        id: String,
        name: String? = null,
        description: String? = null
    ): Result<SegmentDto> = runCatching {
        api.updateSegment(UpdateSegmentRequest(id, name, description))
    }

    suspend fun deleteSegment(id: String): Result<SegmentDto> = runCatching {
        api.deleteSegment(id)
    }
}
