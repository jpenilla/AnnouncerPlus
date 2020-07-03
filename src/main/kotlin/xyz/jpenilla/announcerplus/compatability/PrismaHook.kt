package xyz.jpenilla.announcerplus.compatability

import us.eunoians.prisma.PrismaColor

class PrismaHook {
    fun randomColor(): String {
        return PrismaColor.values().random().hex
    }
}