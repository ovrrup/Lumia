sed -i 's/fun getDatabase(context: Context): AppDatabase {/fun getDatabase(context: Context, forceProfileId: String? = null): AppDatabase {/g' app/src/main/java/lumia/tracker/data/AppDatabase.kt
sed -i 's/val profileId = profMgr.getActiveProfileId()/val profileId = forceProfileId ?: profMgr.getActiveProfileId()/g' app/src/main/java/lumia/tracker/data/AppDatabase.kt
