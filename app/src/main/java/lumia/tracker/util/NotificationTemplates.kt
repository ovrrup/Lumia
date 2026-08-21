package lumia.tracker.util

object NotificationTemplates {
    val formalClassStartTitles = listOf(
        "Class Starting Soon: %s", "Upcoming Lecture: %s", "Schedule Reminder: %s",
        "Get Ready for %s", "Session Commencing: %s", "%s is about to begin",
        "Academic Schedule: %s", "Next Class: %s"
    )
    val informalClassStartTitles = listOf(
        "Hurry! %s is starting!", "Don't be late for %s!", "Time to go: %s!",
        "Sprint to %s now!", "You're up! %s is next.", "Wake up! %s is about to start.",
        "Class time! Don't miss %s.", "Your presence is required at %s!"
    )

    val formalClassEndTitles = listOf(
        "Class Ended: %s", "Session Concluded: %s", "Lecture Over: %s",
        "%s has finished", "Wrap up: %s", "Post-Class Reminder: %s",
        "Schedule Update: %s Ended", "End of Session for %s"
    )
    val informalClassEndTitles = listOf(
        "Class over! Log your attendance for %s!", "Freedom! %s is done.",
        "Finally, %s is over.", "You survived %s!", "Time's up for %s.",
        "%s is a wrap!", "Go take a break, %s is done.", "Class dismissed: %s."
    )

    val formalTaskTitles = listOf(
        "Deadline Reminder: %s", "Upcoming %s: %s", "%s Due Soon: %s",
        "Action Required: %s", "Approaching Deadline: %s", "Pending %s: %s",
        "Schedule Alert: %s", "Time-Sensitive: %s"
    )
    val informalTaskTitles = listOf(
        "URGENT: %s is DUE!", "Don't forget: %s!", "You're running out of time for %s!",
        "Tick tock! %s is waiting.", "Alert! %s needs you.", "Are you slacking? %s is due!",
        "Focus time! Finish %s.", "Danger zone: %s deadline approaching!"
    )
}
