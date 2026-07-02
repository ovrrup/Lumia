with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "r") as f:
    content = f.read()

content = content.replace("lumia.tracker.util.bouncyClick", "lumia.tracker.ui.theme.bouncyClick")
content = content.replace("lumia.tracker.ui.theme.LocalBetaGlassUi", "lumia.tracker.ui.theme.LocalGlassMode")
content = content.replace("val isGlass = LocalBetaGlassUi.current", "val isGlass = LocalGlassMode.current")
content = content.replace("import androidx.compose.ui.draw.drawWithContent", "import androidx.compose.ui.draw.drawWithContent\nimport androidx.compose.ui.graphics.drawscope.clipRect\nimport androidx.compose.ui.graphics.drawscope.clipPath")

with open("app/src/main/java/lumia/tracker/ui/components/StreakWidget.kt", "w") as f:
    f.write(content)
