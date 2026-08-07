package lumia.tracker.viewmodel

import lumia.tracker.model.ActionLog

object ScholarStreakCalculator {
    fun calculateNetDoneCount(
        actionLogs: List<ActionLog>,
        eligibleTitles: Set<String>,
        todayStart: Long,
        todayEnd: Long,
        completedPrefix: String,
        unmarkedPrefix: String
    ): Int {
        val completedLogs = actionLogs.filter { it.timestampMillis in todayStart..<todayEnd && it.actionText.startsWith(completedPrefix) }
        val unmarkedLogs = actionLogs.filter { it.timestampMillis in todayStart..<todayEnd && it.actionText.startsWith(unmarkedPrefix) }

        val cCounts = completedLogs.map { it.actionText.removePrefix(completedPrefix).trim() }.groupingBy { it }.eachCount()
        val uCounts = unmarkedLogs.map { it.actionText.removePrefix(unmarkedPrefix).trim() }.groupingBy { it }.eachCount()

        var netDone = 0
        for ((title, count) in cCounts) {
            if (title in eligibleTitles) {
                val uCount = uCounts[title] ?: 0
                netDone += maxOf(0, count - uCount)
            }
        }
        return netDone
    }
}
