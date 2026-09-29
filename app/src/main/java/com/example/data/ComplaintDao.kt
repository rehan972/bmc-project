package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {

    // --- Complaints ---
    @Query("SELECT * FROM complaints ORDER BY timestamp DESC")
    fun getAllComplaints(): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints ORDER BY timestamp DESC")
    suspend fun getComplaintsList(): List<Complaint>

    @Query("SELECT * FROM complaints WHERE citizenId = :citizenId ORDER BY timestamp DESC")
    fun getComplaintsByCitizen(citizenId: String): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints WHERE assignedOfficerId = :officerId ORDER BY timestamp DESC")
    fun getComplaintsByOfficer(officerId: String): Flow<List<Complaint>>

    @Query("SELECT * FROM complaints WHERE id = :id")
    suspend fun getComplaintById(id: String): Complaint?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: Complaint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<Complaint>)

    @Update
    suspend fun updateComplaint(complaint: Complaint)

    @Query("UPDATE complaints SET status = :status, updatedTimestamp = :updatedTime WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedTime: Long = System.currentTimeMillis())

    @Query("UPDATE complaints SET assignedOfficerId = :officerId, assignedOfficerName = :officerName, status = 'ASSIGNED', updatedTimestamp = :updatedTime WHERE id = :id")
    suspend fun assignOfficer(id: String, officerId: String, officerName: String, updatedTime: Long = System.currentTimeMillis())

    @Query("UPDATE complaints SET department = :department, updatedTimestamp = :updatedTime WHERE id = :id")
    suspend fun updateDepartment(id: String, department: String, updatedTime: Long = System.currentTimeMillis())

    @Query("UPDATE complaints SET priority = :priority, updatedTimestamp = :updatedTime WHERE id = :id")
    suspend fun updatePriority(id: String, priority: String, updatedTime: Long = System.currentTimeMillis())

    @Query("UPDATE complaints SET remarks = :remarks, updatedTimestamp = :updatedTime WHERE id = :id")
    suspend fun updateRemarks(id: String, remarks: String, updatedTime: Long = System.currentTimeMillis())

    @Query("UPDATE complaints SET category = :category, priority = :priority, department = :department, summary = :summary, priorityReason = :reason, confidence = :confidence, status = :status, updatedTimestamp = :updatedTime WHERE id = :id")
    suspend fun updateTriageResult(id: String, category: String, priority: String, department: String, summary: String, reason: String, confidence: String, status: String, updatedTime: Long = System.currentTimeMillis())

    @Query("UPDATE complaints SET status = 'RESOLVED', resolutionRemarks = :resolutionRemarks, resolutionImageUri = :resolutionImageUri, resolvedTimestamp = :resolvedTime, updatedTimestamp = :resolvedTime WHERE id = :id")
    suspend fun resolveComplaint(id: String, resolutionRemarks: String, resolutionImageUri: String?, resolvedTime: Long = System.currentTimeMillis())

    @Query("UPDATE complaints SET feedbackRating = :rating, feedbackComment = :comment, feedbackScore = :score WHERE id = :id")
    suspend fun submitFeedback(id: String, rating: String, comment: String?, score: Int = 5)

    @Query("DELETE FROM complaints WHERE id = :id")
    suspend fun deleteComplaint(id: String)

    @Query("DELETE FROM complaints WHERE isDemo = 1")
    suspend fun deleteDemoComplaints()

    @Query("DELETE FROM complaints")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM complaints")
    suspend fun getCount(): Int

    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE contact = :query OR username = :query LIMIT 1")
    suspend fun getUserByContactOrUsername(query: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE role = 'OFFICER'")
    suspend fun getOfficers(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    // --- Departments ---
    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<DepartmentConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<DepartmentConfig>)

    @Query("UPDATE departments SET isActive = :isActive WHERE id = :id")
    suspend fun updateDepartmentStatus(id: String, isActive: Boolean)

    // --- Category Mappings ---
    @Query("SELECT * FROM category_mappings")
    fun getAllCategoryMappings(): Flow<List<CategoryMapping>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryMapping(mapping: CategoryMapping)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryMappings(mappings: List<CategoryMapping>)

    // --- SLA Configurations ---
    @Query("SELECT * FROM sla_configs")
    fun getAllSlaConfigs(): Flow<List<SlaConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlaConfigs(configs: List<SlaConfig>)

    @Query("UPDATE sla_configs SET maxHours = :maxHours WHERE priority = :priority")
    suspend fun updateSlaHours(priority: String, maxHours: Int)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    // --- System Settings ---
    @Query("SELECT * FROM system_settings")
    fun getAllSettings(): Flow<List<SystemSetting>>

    @Query("SELECT * FROM system_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): SystemSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: SystemSetting)
}
