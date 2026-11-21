package org.gameyfin.plugins.download.torrent

@Suppress("EnumEntryName")
enum class TorrentDiskIoMode {
    POSIX,
    `Memory-mapped`,
    pread
}
