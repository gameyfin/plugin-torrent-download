package org.gameyfin.plugins.download.torrent

@Suppress("EnumEntryName")
enum class TorrentDiskIoMode {
    POSIX,
    `Memory-mapped`,
    Pread;

    internal fun forOperatingSystem(os: String): TorrentDiskIoMode? = when {
        os.startsWith("Windows", ignoreCase = true) -> Pread
        listOf("nix", "nux", "mac", "darwin").any { os.contains(it, ignoreCase = true) } -> this
        else -> null
    }
}
