import re

with open("app/src/main/java/lumia/tracker/ui/screens/study/SubjectsTab.kt", "r") as f:
    content = f.read()

edit_subject_match = re.search(r"(\s*if \(subjectToEdit != null\) \{.*?\n\s*\}\n)\}", content, re.DOTALL)

if edit_subject_match:
    content = content.replace(edit_subject_match.group(1), "\n    if (subjectToEdit != null) {\n        lumia.tracker.ui.screens.study.EditSubjectDialog(\n            subject = subjectToEdit!!,\n            viewModel = viewModel,\n            onDismiss = { subjectToEdit = null }\n        )\n    }\n")
    with open("app/src/main/java/lumia/tracker/ui/screens/study/SubjectsTab.kt", "w") as f:
        f.write(content)

print("Modified SubjectsTab.kt")
