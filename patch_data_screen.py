import re

with open("app/src/main/java/lumia/tracker/ui/screens/settings/DataManagementScreen.kt", "r") as f:
    content = f.read()

# Replace the single Backup & Erasure Hub with two if activeProfile.isDefault

old_hub = """            SettingsGroupCard(title = "Backup & Erasure Hub", icon = Icons.Rounded.Storage) {
                SettingsActionItemInCard(
                    title = "Export Secure Data Profile",
                    subtitle = "Back up all settings, customisations, courses and assignments into a portable file",
                    icon = Icons.Rounded.Upload,
                    onClick = { showExportDialog = true }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Import Secure Data Profile",
                    subtitle = "Restore binary backup. Warning: Completely overwrites current active assets",
                    icon = Icons.Rounded.Download,
                    isDestructive = true,
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = if (activeProfile.isDefault) "Full Environment Factory Erase (All Accounts)" else "Erase Data & Delete Account",
                    subtitle = if (activeProfile.isDefault) "Permanently delete all data for all accounts on this device" else "Permanently delete your profile and all your data",
                    icon = Icons.Rounded.DeleteForever,
                    isDestructive = true,
                    onClick = { showResetDialog = true }
                )
            }"""

new_hub = """            SettingsGroupCard(title = "My Data Management", icon = Icons.Rounded.Person) {
                SettingsActionItemInCard(
                    title = "Export My Data",
                    subtitle = "Back up your own profile's settings, tasks, and data",
                    icon = Icons.Rounded.Upload,
                    onClick = { 
                        exportAllMode = false
                        showExportDialog = true 
                    }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Import Data",
                    subtitle = "Restore your profile's backup (Overwrites current profile)",
                    icon = Icons.Rounded.Download,
                    isDestructive = true,
                    onClick = { openDocumentLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                
                SettingsActionItemInCard(
                    title = "Erase My Data & Delete Account",
                    subtitle = "Permanently delete your profile and all your data",
                    icon = Icons.Rounded.DeleteForever,
                    isDestructive = true,
                    onClick = {
                        if (activeProfile.isDefault) {
                            showSuccessorDialog = true
                        } else {
                            showResetDialog = true
                            resetTarget = "self"
                        }
                    }
                )
            }

            if (activeProfile.isDefault) {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsGroupCard(title = "Collective Data Management", icon = Icons.Rounded.AdminPanelSettings) {
                    SettingsActionItemInCard(
                        title = "Export All Accounts Data",
                        subtitle = "Back up data for all users in the application",
                        icon = Icons.Rounded.CloudUpload,
                        onClick = { 
                            exportAllMode = true
                            showExportDialog = true 
                        }
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    
                    SettingsActionItemInCard(
                        title = "Factory Erase (All Accounts)",
                        subtitle = "Permanently delete all data for all accounts",
                        icon = Icons.Rounded.DeleteForever,
                        isDestructive = true,
                        onClick = { 
                            showResetDialog = true
                            resetTarget = "all"
                        }
                    )
                }
            }"""

content = content.replace(old_hub, new_hub)

# We need to add exportAllMode, showSuccessorDialog, resetTarget states at the top
states = """    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }"""
    
new_states = """    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportAllMode by remember { mutableStateOf(false) }
    var showSuccessorDialog by remember { mutableStateOf(false) }
    var resetTarget by remember { mutableStateOf("self") }"""

content = content.replace(states, new_states)

# Fix export document launcher to use exportAllMode
old_export_launcher = """        if (uri != null) {
            viewModel.exportData(uri)
        }"""
        
new_export_launcher = """        if (uri != null) {
            viewModel.exportData(uri, exportAll = exportAllMode)
        }"""
        
content = content.replace(old_export_launcher, new_export_launcher)

# Update reset dialog
old_reset_dialog = """    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (activeProfile.isDefault) "Erase All App Data?" else "Erase Data & Delete Account?") },
            text = { Text(if (activeProfile.isDefault) "This action cannot be undone. ALL user accounts (except Main) and their data will be permanently removed." else "This action cannot be undone. Your profile and all your data will be permanently removed.", color = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Data", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }"""

new_reset_dialog = """    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (resetTarget == "all") "Erase All App Data?" else "Erase Data & Delete Account?") },
            text = { Text(if (resetTarget == "all") "This action cannot be undone. ALL user accounts and their data will be permanently removed." else "This action cannot be undone. Your profile and all your data will be permanently removed.", color = MaterialTheme.colorScheme.error) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (resetTarget == "all") {
                            viewModel.clearAllData()
                        } else {
                            viewModel.eraseMyDataAndAccount()
                        }
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Data", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessorDialog) {
        val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
        var createNew by remember { mutableStateOf(false) }
        var selectedSuccessorId by remember { mutableStateOf("") }
        var newName by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSuccessorDialog = false },
            title = { Text("Main Account Required", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Since you are the main account, you must select a successor to become the new main account before you can delete yourself.")
                    Spacer(Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { createNew = false }) {
                        androidx.compose.material3.RadioButton(
                            selected = !createNew,
                            onClick = { createNew = false }
                        )
                        Text("Select existing user")
                    }
                    if (!createNew) {
                        val otherProfs = allProfiles.filter { it.id != activeProfile.id }
                        if (otherProfs.isEmpty()) {
                            Text("No other users found.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 32.dp))
                        } else {
                            if (selectedSuccessorId.isEmpty() && otherProfs.isNotEmpty()) selectedSuccessorId = otherProfs.first().id
                            Column(modifier = Modifier.padding(start = 32.dp)) {
                                otherProfs.forEach { prof ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedSuccessorId = prof.id }) {
                                        androidx.compose.material3.RadioButton(
                                            selected = selectedSuccessorId == prof.id,
                                            onClick = { selectedSuccessorId = prof.id }
                                        )
                                        Text(prof.name)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { createNew = true }) {
                        androidx.compose.material3.RadioButton(
                            selected = createNew,
                            onClick = { createNew = true }
                        )
                        Text("Create new account")
                    }
                    if (createNew) {
                        androidx.compose.material3.OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("New Account Name") },
                            modifier = Modifier.padding(start = 32.dp).fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                val canSubmit = if (createNew) newName.isNotBlank() else (selectedSuccessorId.isNotEmpty() && allProfiles.any { it.id != activeProfile.id })
                TextButton(
                    onClick = {
                        viewModel.switchMainAccountAndDeleteCurrent(
                            successorId = selectedSuccessorId,
                            createNew = createNew,
                            newName = newName,
                            newAvatar = "😊"
                        )
                        showSuccessorDialog = false
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete My Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }"""

content = content.replace(old_reset_dialog, new_reset_dialog)

# update export dialog text
old_export_dialog_text = """text = { Text(if (activeProfile.isDefault) "Export a backup of ALL user accounts?" else "Export a backup of YOUR data?") }"""
new_export_dialog_text = """text = { Text(if (exportAllMode) "Export a backup of ALL user accounts?" else "Export a backup of YOUR data?") }"""
content = content.replace(old_export_dialog_text, new_export_dialog_text)

with open("app/src/main/java/lumia/tracker/ui/screens/settings/DataManagementScreen.kt", "w") as f:
    f.write(content)

