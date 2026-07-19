package com.ashwathai.tradelab.shared

class WasmPlatform : Platform {
    override val name: String = "WebAssembly (Browser)"
}

actual fun getPlatform(): Platform = WasmPlatform()
