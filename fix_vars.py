with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "r") as f:
    content = f.read()

content = content.replace('_betaUsePaletteForText.value = false', '')
content = content.replace('_aodTapToWake.value = true', '')
content = content.replace('_aodSensitivity.value = 5', '_aodSensitivity.value = "highest"')
content = content.replace('_aodLockTimeout.value = 15L', '_aodLockTimeout.value = 30')

with open("app/src/main/java/lumia/tracker/viewmodel/ScholarViewModel.kt", "w") as f:
    f.write(content)
