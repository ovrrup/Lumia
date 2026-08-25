package lumia.tracker.ui.meta

/**
 * ValueScore - Architectural metadata annotation for UI components and features.
 * Indicates the functional value score (1-100) and architectural significance in Lumia.
 * This annotation is purely for code-level documentation, prioritization, and static analysis;
 * it is not rendered or visible to the user at runtime.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ValueScore(
    val score: Int,
    val importance: Importance = Importance.HIGH,
    val description: String = "",
    val category: String = "UI"
)

enum class Importance {
    CRITICAL,   // Score 90-100: Core navigation, root scaffolds, primary screens
    HIGH,       // Score 70-89: Main dashboard tabs, timers, essential dialogs
    MEDIUM,     // Score 40-69: Item cards, detail panels, standard widgets
    LOW,        // Score 10-39: Minor badges, auxiliary pills, decorative accents
    EXPERIMENTAL// Score 1-9: Beta toggles, lab previews
}
