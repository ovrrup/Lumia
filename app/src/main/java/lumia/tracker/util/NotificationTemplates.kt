package lumia.tracker.util

object NotificationTemplates {
    val formalClassStartTitles = listOf(
        "Class Starting Soon: %s", "Upcoming Lecture: %s", "Schedule Reminder: %s",
        "Get Ready for %s", "Session Commencing: %s", "%s is about to begin",
        "Academic Schedule: %s", "Next Class: %s"
    )
    val informalClassStartTitles = listOf(
        "%s is starting soon!", "Don't forget %s!", "Time for %s!",
        "%s starts shortly", "Up next: %s", "Heads up: %s starts soon",
        "Class time: %s", "Ready for %s? It's starting soon"
    )

    val formalClassEndTitles = listOf(
        "Class Ended: %s", "Session Concluded: %s", "Lecture Over: %s",
        "%s has finished", "Wrap up: %s", "Post-Class Reminder: %s",
        "Schedule Update: %s Ended", "End of Session for %s"
    )
    val informalClassEndTitles = listOf(
        "Class ended: %s. Log attendance", "%s is done! Remember to log attendance",
        "%s has ended. Don't forget your attendance", "%s is complete! Take a breather",
        "%s is concluded", "%s is a wrap! Mark your attendance",
        "Take a break: %s is done", "Class dismissed: %s"
    )

    val formalTaskTitles = listOf(
        "Deadline Reminder: %s", "Upcoming Deadline: %s", "Due Soon: %s",
        "Action Required: %s", "Approaching Deadline: %s", "Pending Item: %s",
        "Schedule Alert: %s", "Time-Sensitive: %s"
    )
    val informalTaskTitles = listOf(
        "Reminder: %s is due soon", "Don't forget: %s", "Upcoming deadline: %s",
        "Time to focus on %s", "Heads up: %s is due soon", "Focus time: %s",
        "Deadline approaching for %s", "Stay on track with %s"
    )
}
