sed -i 's/fun getProfilePrefs(): SharedPreferences {/fun getProfilePrefs(id: String = getActiveProfileId()): SharedPreferences {/g' app/src/main/java/lumia/tracker/data/ProfileManager.kt
sed -i 's/val id = getActiveProfileId()//g' app/src/main/java/lumia/tracker/data/ProfileManager.kt
