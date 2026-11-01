package nesupe

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

/**
 * 静的ファイル（index.html と pages/）の配信と、学習データ（解答・履歴など）の保存を行う。
 * 保存するデータはブラウザが作ったJSONをそのままファイルに書くだけで、中身は解釈しない。
 */

private const val API_PATH = "/api/state"
private const val MAX_STATE_BYTES = 32L * 1024 * 1024

private val port = (System.getenv("PORT") ?: "8080").toInt()
private val wwwDir = File(System.getenv("WWW_DIR") ?: "www").canonicalFile
private val dataDir = File(System.getenv("DATA_DIR") ?: "data").canonicalFile
private val stateFile = File(dataDir, "state.json")
private val stateLock = Any()

private val mimeTypes = mapOf(
    "html" to "text/html; charset=utf-8",
    "css" to "text/css; charset=utf-8",
    "js" to "text/javascript; charset=utf-8",
    "json" to "application/json; charset=utf-8",
    "png" to "image/png",
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",
    "svg" to "image/svg+xml",
    "ico" to "image/x-icon",
    "txt" to "text/plain; charset=utf-8",
)

fun main() {
    dataDir.mkdirs()
    val server = HttpServer.create(InetSocketAddress(port), 0)
    server.executor = Executors.newFixedThreadPool(8)
    server.createContext("/") { ex ->
        try {
            handle(ex)
        } catch (e: Exception) {
            System.err.println("error: ${ex.requestMethod} ${ex.requestURI}: $e")
            runCatching { reply(ex, 500, "text/plain; charset=utf-8", "Internal Server Error".toByteArray()) }
        } finally {
            ex.close()
        }
    }
    server.start()
    println("nesupe-server: http://0.0.0.0:$port/  (www=$wwwDir, data=$dataDir)")
}

private fun handle(ex: HttpExchange) {
    val path = ex.requestURI.path
    if (path == API_PATH) {
        handleState(ex)
    } else {
        handleStatic(ex, path)
    }
}

// ---- 学習データ API ----

private fun handleState(ex: HttpExchange) {
    when (ex.requestMethod) {
        "GET" -> {
            val state = synchronized(stateLock) {
                if (stateFile.isFile) stateFile.readText(Charsets.UTF_8).trim() else "null"
            }
            val body = """{"app":"nesupe-server","version":1,"state":$state}"""
            reply(ex, 200, "application/json; charset=utf-8", body.toByteArray(Charsets.UTF_8), noStore = true)
        }
        "PUT" -> {
            if (!sameOrigin(ex)) {
                reply(ex, 403, "text/plain; charset=utf-8", "forbidden".toByteArray())
                return
            }
            val declared = ex.requestHeaders.getFirst("Content-Length")?.toLongOrNull()
            if (declared != null && declared > MAX_STATE_BYTES) {
                reply(ex, 413, "text/plain; charset=utf-8", "too large".toByteArray())
                return
            }
            val bytes = ex.requestBody.readNBytes((MAX_STATE_BYTES + 1).toInt())
            if (bytes.size > MAX_STATE_BYTES) {
                reply(ex, 413, "text/plain; charset=utf-8", "too large".toByteArray())
                return
            }
            val text = String(bytes, Charsets.UTF_8).trim()
            if (!(text.startsWith("{") && text.endsWith("}"))) {
                reply(ex, 400, "text/plain; charset=utf-8", "invalid body".toByteArray())
                return
            }
            synchronized(stateLock) {
                val tmp = File(dataDir, "state.json.tmp")
                tmp.writeText(text, Charsets.UTF_8)
                Files.move(tmp.toPath(), stateFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            }
            reply(ex, 200, "application/json; charset=utf-8", """{"ok":true}""".toByteArray(), noStore = true)
        }
        else -> {
            ex.responseHeaders.add("Allow", "GET, PUT")
            reply(ex, 405, "text/plain; charset=utf-8", "method not allowed".toByteArray())
        }
    }
}

/** 別サイトのページからの書き込みを防ぐ（Origin があれば Host と一致すること）。 */
private fun sameOrigin(ex: HttpExchange): Boolean {
    val origin = ex.requestHeaders.getFirst("Origin") ?: return true
    val host = ex.requestHeaders.getFirst("Host") ?: return false
    return try {
        val u = URI(origin)
        val originHost = if (u.port >= 0) "${u.host}:${u.port}" else u.host
        originHost.equals(host, ignoreCase = true)
    } catch (e: Exception) {
        false
    }
}

// ---- 静的ファイル ----

private fun handleStatic(ex: HttpExchange, rawPath: String) {
    if (ex.requestMethod != "GET" && ex.requestMethod != "HEAD") {
        ex.responseHeaders.add("Allow", "GET, HEAD")
        reply(ex, 405, "text/plain; charset=utf-8", "method not allowed".toByteArray())
        return
    }
    var rel = rawPath // URI.getPath() はデコード済み
    if (rel.endsWith("/")) rel += "index.html"
    val file = File(wwwDir, rel.trimStart('/')).canonicalFile
    // www の外側（..、シンボリックリンク経由など）は返さない
    if (!file.path.startsWith(wwwDir.path + File.separator) || !file.isFile) {
        reply(ex, 404, "text/plain; charset=utf-8", "not found".toByteArray())
        return
    }

    val lastModified = file.lastModified() / 1000 * 1000
    val since = ex.requestHeaders.getFirst("If-Modified-Since")?.let {
        runCatching { DateTimeFormatter.RFC_1123_DATE_TIME.parse(it, Instant::from).toEpochMilli() }.getOrNull()
    }
    val headers = ex.responseHeaders
    headers.add("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(lastModified).atOffset(ZoneOffset.UTC)))
    headers.add("Cache-Control", "no-cache")
    if (since != null && since >= lastModified) {
        ex.sendResponseHeaders(304, -1)
        return
    }
    headers.add("Content-Type", mimeTypes[file.extension.lowercase()] ?: "application/octet-stream")
    if (ex.requestMethod == "HEAD") {
        headers.add("Content-Length", file.length().toString())
        ex.sendResponseHeaders(200, -1)
        return
    }
    ex.sendResponseHeaders(200, file.length())
    file.inputStream().use { it.copyTo(ex.responseBody) }
}

private fun reply(ex: HttpExchange, status: Int, contentType: String, body: ByteArray, noStore: Boolean = false) {
    ex.responseHeaders.add("Content-Type", contentType)
    if (noStore) ex.responseHeaders.add("Cache-Control", "no-store")
    ex.sendResponseHeaders(status, body.size.toLong())
    ex.responseBody.use { it.write(body) }
}
