package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MeasurementLog::class,
        CompletedWorkout::class,
        WaterLog::class,
        CustomExercise::class,
        MealPhoto::class,
        AchievementBadge::class,
        FriendChallenge::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "body_blueprint_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default badges on creation
                            val scope = CoroutineScope(Dispatchers.IO)
                            scope.launch {
                                val dao = getDatabase(context).appDao()
                                dao.insertBadges(getDefaultBadges())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDefaultBadges(): List<AchievementBadge> {
            return listOf(
                AchievementBadge("first_workout", "First Sweat", "Logged your very first workout!", false),
                AchievementBadge("streak_7", "7-Day Streak", "Maintained a 7-day workout streak!", false),
                AchievementBadge("streak_30", "Iron Will", "Maintained a 30-day workout streak!", false),
                AchievementBadge("lost_2_waist", "Shedding Inches", "Lost 2 inches (5cm) or more from your waist!", false),
                AchievementBadge("hydration_king", "Hydro King", "Reached your hydration target for the day!", false),
                AchievementBadge("one_rep_max", "Strength Scholar", "Calculated your 1-Rep Max for the first time!", false),
                AchievementBadge("posture_check", "Aligned Mind", "Completed your first camera posture analysis!", false),
                AchievementBadge("meal_log", "Gourmet Health", "Logged your first meal photo!", false)
            )
        }
    }
}
