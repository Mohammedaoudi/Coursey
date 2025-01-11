package com.example.firstaidfront.cprDL

data class CPRResult(
    val depth: Float,
    val release: String,
    val handPosition: String,
    val compressionRate: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class CompressionCycle(
    val startTime: Long,
    val criticalFrames: List<Frame>
)

data class Frame(
    val timestamp: Long,
    val isCompression: Boolean,
    val verticalMovement: Float
)