package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HotspotArea(
    val ward: String,
    val count: Int,
    val topCategory: String,
    val highestPriority: String,
    val sampleLocation: String
)

class ComplaintRepository(private val complaintDao: ComplaintDao) {

    val allComplaints: Flow<List<Complaint>> = complaintDao.getAllComplaints()
    val allDepartments: Flow<List<DepartmentConfig>> = complaintDao.getAllDepartments()
    val allCategoryMappings: Flow<List<CategoryMapping>> = complaintDao.getAllCategoryMappings()
    val allSlaConfigs: Flow<List<SlaConfig>> = complaintDao.getAllSlaConfigs()
    val allNotifications: Flow<List<AppNotification>> = complaintDao.getAllNotifications()
    val allSettings: Flow<List<SystemSetting>> = complaintDao.getAllSettings()

    fun getComplaintsByCitizen(citizenId: String): Flow<List<Complaint>> {
        return complaintDao.getComplaintsByCitizen(citizenId)
    }

    fun getComplaintsByOfficer(officerId: String): Flow<List<Complaint>> {
        return complaintDao.getComplaintsByOfficer(officerId)
    }

    suspend fun getComplaintById(id: String): Complaint? {
        return complaintDao.getComplaintById(id)
    }

    suspend fun insertComplaint(complaint: Complaint) {
        complaintDao.insertComplaint(complaint)
        // Add notification
        val notif = AppNotification(
            id = "notif-${System.currentTimeMillis()}",
            targetRole = "CITIZEN",
            targetUserId = complaint.citizenId,
            title = "Complaint Logged (${complaint.id})",
            message = "Your complaint for '${complaint.category}' in ${complaint.ward} is registered and routed to ${complaint.department}.",
            complaintId = complaint.id
        )
        complaintDao.insertNotification(notif)
    }

    suspend fun updateStatus(id: String, status: String) {
        complaintDao.updateStatus(id, status)
        val notif = AppNotification(
            id = "notif-${System.currentTimeMillis()}",
            targetRole = "CITIZEN",
            title = "Status Updated: $status",
            message = "Complaint $id status is now $status.",
            complaintId = id
        )
        complaintDao.insertNotification(notif)
    }

    suspend fun assignOfficer(id: String, officerId: String, officerName: String) {
        complaintDao.assignOfficer(id, officerId, officerName)
        val notif = AppNotification(
            id = "notif-${System.currentTimeMillis()}",
            targetRole = "OFFICER",
            targetUserId = officerId,
            title = "New Task Assigned ($id)",
            message = "Complaint $id has been assigned to you for inspection and resolution.",
            complaintId = id
        )
        complaintDao.insertNotification(notif)
    }

    suspend fun updateDepartment(id: String, department: String) {
        complaintDao.updateDepartment(id, department)
    }

    suspend fun updatePriority(id: String, priority: String) {
        complaintDao.updatePriority(id, priority)
    }

    suspend fun updateRemarks(id: String, remarks: String) {
        complaintDao.updateRemarks(id, remarks)
    }

    suspend fun updateTriageResult(
        id: String,
        category: String,
        priority: String,
        department: String,
        summary: String,
        reason: String,
        confidence: String,
        status: String
    ) {
        complaintDao.updateTriageResult(id, category, priority, department, summary, reason, confidence, status)
    }

    suspend fun resolveComplaint(id: String, remarks: String, imageUri: String?) {
        complaintDao.resolveComplaint(id, remarks, imageUri)
        val notif = AppNotification(
            id = "notif-${System.currentTimeMillis()}",
            targetRole = "CITIZEN",
            title = "Complaint Resolved! ($id)",
            message = "The municipal team has resolved your issue. Please review and provide feedback.",
            complaintId = id
        )
        complaintDao.insertNotification(notif)
    }

    suspend fun submitFeedback(id: String, rating: String, comment: String?, score: Int = 5) {
        complaintDao.submitFeedback(id, rating, comment, score)
    }

    suspend fun deleteComplaint(id: String) {
        complaintDao.deleteComplaint(id)
    }

    suspend fun deleteDemoComplaints() {
        complaintDao.deleteDemoComplaints()
    }

    suspend fun loadDemoComplaints() {
        complaintDao.insertComplaints(getDemoComplaints())
    }

    suspend fun getCount(): Int {
        return complaintDao.getCount()
    }

    // --- Users & Authentication ---
    suspend fun authenticateUser(contactOrUsername: String, pass: String): User? {
        val user = complaintDao.getUserByContactOrUsername(contactOrUsername)
        return if (user != null && user.password == pass) user else null
    }

    suspend fun verifyOtpAndLogin(phone: String, otp: String): User {
        // In Demo OTP Mode, OTP 123456 or any 6-digit match succeeds
        val cleanPhone = phone.trim().replace("+91", "").replace(" ", "")
        val existing = complaintDao.getUserByContactOrUsername(cleanPhone)
        if (existing != null) return existing

        val newUser = User(
            id = "usr-${System.currentTimeMillis()}",
            name = "Citizen ($cleanPhone)",
            contact = cleanPhone,
            username = "citizen_$cleanPhone",
            password = "password",
            role = "CITIZEN"
        )
        complaintDao.insertUser(newUser)
        return newUser
    }

    suspend fun registerUser(name: String, contact: String, username: String?, pass: String, role: String): User {
        val cleanContact = contact.trim().replace(" ", "")
        val newUser = User(
            id = "usr-${System.currentTimeMillis()}",
            name = name,
            contact = cleanContact,
            username = username?.ifBlank { "user_${System.currentTimeMillis() % 10000}" },
            password = pass,
            role = role
        )
        complaintDao.insertUser(newUser)
        return newUser
    }

    suspend fun updateUserProfile(user: User) {
        complaintDao.insertUser(user)
    }

    suspend fun getOfficers(): List<User> {
        return complaintDao.getOfficers()
    }

    // --- Configurations & Settings ---
    suspend fun updateDepartmentStatus(id: String, isActive: Boolean) {
        complaintDao.updateDepartmentStatus(id, isActive)
    }

    suspend fun insertCategoryMapping(mapping: CategoryMapping) {
        complaintDao.insertCategoryMapping(mapping)
    }

    suspend fun updateSlaHours(priority: String, hours: Int) {
        complaintDao.updateSlaHours(priority, hours)
    }

    suspend fun getSetting(key: String): String? {
        return complaintDao.getSetting(key)?.value
    }

    suspend fun saveSetting(key: String, value: String) {
        complaintDao.saveSetting(SystemSetting(key, value))
    }

    // --- Duplicate Detection ---
    suspend fun findSimilarComplaints(category: String, location: String): List<Complaint> {
        val list = complaintDao.getComplaintsList()
        val locKeywords = location.split(" ", ",", "-", "/").filter { it.length > 3 }
        return list.filter { existing ->
            existing.status != "RESOLVED" && existing.status != "CLOSED" &&
                    existing.category.equals(category, ignoreCase = true) &&
                    (locKeywords.any { kw -> existing.location.contains(kw, ignoreCase = true) } ||
                            existing.location.equals(location, ignoreCase = true))
        }
    }

    // --- Hotspots Detection ---
    fun detectHotspots(complaints: List<Complaint>): List<HotspotArea> {
        val active = complaints.filter { it.status != "RESOLVED" && it.status != "CLOSED" }
        val byWard = active.groupBy { it.ward }
        return byWard.mapNotNull { (ward, wardComplaints) ->
            if (wardComplaints.size >= 2) {
                val topCat = wardComplaints.groupBy { it.category }.maxByOrNull { it.value.size }?.key ?: "Civic"
                val hasCritical = wardComplaints.any { it.priority == "CRITICAL" }
                val priority = if (hasCritical) "CRITICAL" else "HIGH"
                HotspotArea(
                    ward = ward,
                    count = wardComplaints.size,
                    topCategory = topCat,
                    highestPriority = priority,
                    sampleLocation = wardComplaints.firstOrNull()?.location ?: ward
                )
            } else null
        }.sortedByDescending { it.count }
    }

    // --- CSV Export ---
    fun generateCsvExport(complaints: List<Complaint>): String {
        val sb = StringBuilder()
        sb.append("Complaint ID,Category,Priority,Department,Ward,Location,Status,Created Date,Resolved Date,Assigned Officer,Citizen Contact\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        for (c in complaints) {
            val created = sdf.format(Date(c.timestamp))
            val resolved = if (c.resolvedTimestamp != null) sdf.format(Date(c.resolvedTimestamp)) else "N/A"
            val safeLoc = c.location.replace(",", " ")
            val officer = c.assignedOfficerName ?: "Unassigned"
            sb.append("${c.id},${c.category},${c.priority},${c.department},${c.ward},\"$safeLoc\",${c.status},$created,$resolved,\"$officer\",${c.citizenContact}\n")
        }
        return sb.toString()
    }

    // --- Seed Data ---
    suspend fun seedDemoDataIfEmpty() {
        if (complaintDao.getCount() == 0) {
            complaintDao.insertComplaints(getDemoComplaints())
            complaintDao.insertUsers(getDemoUsers())
            complaintDao.insertDepartments(getDemoDepartments())
            complaintDao.insertCategoryMappings(getDemoCategoryMappings())
            complaintDao.insertSlaConfigs(getDemoSlaConfigs())
            complaintDao.saveSetting(SystemSetting("demo_mode", "true"))
            complaintDao.saveSetting(SystemSetting("sms_auth_enabled", "true"))
            complaintDao.saveSetting(SystemSetting("email_enabled", "true"))
            complaintDao.saveSetting(SystemSetting("email_destination", "triage-demo@mcgm.gov.in"))
        }
    }

    suspend fun resetToDemoData() {
        complaintDao.clearAll()
        complaintDao.insertComplaints(getDemoComplaints())
        complaintDao.insertUsers(getDemoUsers())
        complaintDao.insertDepartments(getDemoDepartments())
        complaintDao.insertCategoryMappings(getDemoCategoryMappings())
        complaintDao.insertSlaConfigs(getDemoSlaConfigs())
    }

    companion object {
        fun getDemoUsers(): List<User> {
            return listOf(
                User(
                    id = "usr-citizen-1",
                    name = "Ramesh Sharma",
                    contact = "9820098200",
                    username = "demo_citizen",
                    password = "password",
                    role = "CITIZEN"
                ),
                User(
                    id = "usr-citizen-2",
                    name = "Sunita Jadhav",
                    contact = "9830098300",
                    username = "sunita_j",
                    password = "password",
                    role = "CITIZEN"
                ),
                User(
                    id = "usr-off-1",
                    name = "K. Patil (Roads)",
                    contact = "patil.roads@mcgm.gov.in",
                    username = "demo_officer",
                    password = "password",
                    role = "OFFICER",
                    department = "Roads Department"
                ),
                User(
                    id = "usr-off-2",
                    name = "S. Deshmukh (SWM)",
                    contact = "deshmukh.swm@mcgm.gov.in",
                    username = "deshmukh_swm",
                    password = "password",
                    role = "OFFICER",
                    department = "Solid Waste Management"
                ),
                User(
                    id = "usr-admin-1",
                    name = "A. Mehta (Chief Triage Officer)",
                    contact = "admin.triage@mcgm.gov.in",
                    username = "demo_admin",
                    password = "password",
                    role = "ADMIN"
                )
            )
        }

        fun getDemoDepartments(): List<DepartmentConfig> {
            return listOf(
                DepartmentConfig("DEPT-01", "Roads Department", "ROADS", true),
                DepartmentConfig("DEPT-02", "Storm Water Drainage", "DRAIN", true),
                DepartmentConfig("DEPT-03", "Solid Waste Management", "SWM", true),
                DepartmentConfig("DEPT-04", "Water Supply Department", "WATER", true),
                DepartmentConfig("DEPT-05", "Electrical Department", "ELEC", true),
                DepartmentConfig("DEPT-06", "Sewerage Department", "SEWER", true),
                DepartmentConfig("DEPT-07", "Traffic Department", "TRAFFIC", true),
                DepartmentConfig("DEPT-08", "Building / Planning", "BLDG", true)
            )
        }

        fun getDemoCategoryMappings(): List<CategoryMapping> {
            return listOf(
                CategoryMapping("Pothole / Road Damage", "Roads Department", "HIGH"),
                CategoryMapping("Drainage / Waterlogging", "Storm Water Drainage", "CRITICAL"),
                CategoryMapping("Waste / Garbage", "Solid Waste Management", "MEDIUM"),
                CategoryMapping("Water Supply", "Water Supply Department", "HIGH"),
                CategoryMapping("Streetlight / Electrical", "Electrical Department", "MEDIUM"),
                CategoryMapping("Sewage", "Sewerage Department", "CRITICAL"),
                CategoryMapping("Traffic / Road Obstruction", "Traffic Department", "HIGH"),
                CategoryMapping("Public Infrastructure", "Roads Department", "MEDIUM"),
                CategoryMapping("Encroachment / Construction", "Building / Planning", "MEDIUM"),
                CategoryMapping("Other", "Roads Department", "LOW")
            )
        }

        fun getDemoSlaConfigs(): List<SlaConfig> {
            return listOf(
                SlaConfig("CRITICAL", 12),
                SlaConfig("HIGH", 24),
                SlaConfig("MEDIUM", 72),
                SlaConfig("LOW", 168)
            )
        }

        fun getDemoComplaints(): List<Complaint> {
            val now = System.currentTimeMillis()
            return listOf(
                Complaint(
                    id = "CIV-2026-000001",
                    citizenId = "usr-citizen-1",
                    citizenName = "Ramesh Sharma",
                    citizenContact = "9820098200",
                    description = "Large pothole near the main college gate causing problems for bikes and skidding.",
                    imageUri = null,
                    location = "SV Road near Andheri Railway Station, Mumbai",
                    latitude = 19.1197,
                    longitude = 72.8464,
                    ward = "Ward K-West",
                    category = "Pothole / Road Damage",
                    priority = "HIGH",
                    department = "Roads Department",
                    summary = "Large deep pothole on arterial road causing safety risk and skidding hazard for two-wheelers.",
                    priorityReason = "High priority because the reported pothole is large and located in high-traffic transit zone with accident risk.",
                    confidence = "97%",
                    status = "NEW",
                    isDemo = true,
                    timestamp = now - (3600_000L * 2)
                ),
                Complaint(
                    id = "CIV-2026-000002",
                    citizenId = "usr-citizen-2",
                    citizenName = "Sunita Jadhav",
                    citizenContact = "9830098300",
                    description = "Garbage has not been collected for three days near the residential building and foul smell is spreading.",
                    imageUri = null,
                    location = "Senapati Bapat Marg, Dadar West, Mumbai",
                    latitude = 19.0178,
                    longitude = 72.8478,
                    ward = "Ward G-North",
                    category = "Waste / Garbage",
                    priority = "MEDIUM",
                    department = "Solid Waste Management",
                    summary = "Accumulated uncollected domestic garbage pile overflowing onto pedestrian walkway.",
                    priorityReason = "Medium priority affecting neighborhood cleanliness, local health, and pedestrian walkway access.",
                    confidence = "98%",
                    status = "ASSIGNED",
                    assignedOfficerId = "usr-off-2",
                    assignedOfficerName = "S. Deshmukh (SWM)",
                    remarks = "Refuse truck scheduled for afternoon pickup.",
                    isDemo = true,
                    timestamp = now - (3600_000L * 15)
                ),
                Complaint(
                    id = "CIV-2026-000003",
                    citizenId = "usr-citizen-1",
                    citizenName = "Ramesh Sharma",
                    citizenContact = "9820098200",
                    description = "Drain is overflowing after rainfall and dirty water is entering highway subway.",
                    imageUri = null,
                    location = "Gokhale Road Subway, Thane West",
                    latitude = 19.1860,
                    longitude = 72.9754,
                    ward = "Ward T",
                    category = "Drainage / Waterlogging",
                    priority = "CRITICAL",
                    department = "Storm Water Drainage",
                    summary = "Blocked stormwater drainage system producing severe waterlogging and submerging pedestrian subway.",
                    priorityReason = "Immediate public safety hazard with subway submerged and imminent risk of electrocution and vehicle stalling.",
                    confidence = "95%",
                    status = "NEW",
                    isDemo = true,
                    timestamp = now - (3600_000L * 28) // Overdue since Critical SLA is 12h!
                ),
                Complaint(
                    id = "CIV-2026-000004",
                    citizenId = "usr-citizen-2",
                    citizenName = "Sunita Jadhav",
                    citizenContact = "9830098300",
                    description = "Street light has been broken for a week and cross junction is dark.",
                    imageUri = null,
                    location = "BKC Avenue 3 Junction, Bandra East, Mumbai",
                    latitude = 19.0657,
                    longitude = 72.8687,
                    ward = "Ward H-East",
                    category = "Streetlight / Electrical",
                    priority = "MEDIUM",
                    department = "Electrical Department",
                    summary = "Non-functional civic streetlights at cross junction resulting in pitch darkness.",
                    priorityReason = "Moderate risk to night pedestrian security and intersection visibility.",
                    confidence = "94%",
                    status = "IN PROGRESS",
                    assignedOfficerId = "usr-off-1",
                    assignedOfficerName = "K. Patil (Roads)",
                    remarks = "Fixture replacement ordered from central depot.",
                    isDemo = true,
                    timestamp = now - (3600_000L * 18)
                ),
                Complaint(
                    id = "CIV-2026-000005",
                    citizenId = "usr-citizen-1",
                    citizenName = "Ramesh Sharma",
                    citizenContact = "9820098200",
                    description = "Water leakage near the roadside gushing clean potable water onto street.",
                    imageUri = null,
                    location = "Pipe Road, Kurla West, Mumbai",
                    latitude = 19.0728,
                    longitude = 72.8797,
                    ward = "Ward L",
                    category = "Water Supply",
                    priority = "HIGH",
                    department = "Water Supply Department",
                    summary = "Ruptured high-pressure municipal water pipe causing substantial potable water loss.",
                    priorityReason = "Major potable resource wastage and supply disruption affecting hundreds of households.",
                    confidence = "96%",
                    status = "NEW",
                    isDemo = true,
                    timestamp = now - (3600_000L * 8)
                ),
                Complaint(
                    id = "CIV-2026-000006",
                    citizenId = "usr-citizen-2",
                    citizenName = "Sunita Jadhav",
                    citizenContact = "9830098300",
                    description = "Underground sewage line choked with toxic black sludge overflowing into lanes.",
                    imageUri = null,
                    location = "90 Feet Road, Dharavi, Mumbai",
                    latitude = 19.0434,
                    longitude = 72.8567,
                    ward = "Ward G-North",
                    category = "Sewage",
                    priority = "CRITICAL",
                    department = "Sewerage Department",
                    summary = "Blocked sewer main backflowing raw untreated sewage into populated residential pathway.",
                    priorityReason = "Severe public health emergency with infectious disease outbreak risk near educational facilities.",
                    confidence = "99%",
                    status = "ASSIGNED",
                    assignedOfficerId = "usr-off-2",
                    assignedOfficerName = "S. Deshmukh (SWM)",
                    remarks = "Desilting machine team assigned.",
                    isDemo = true,
                    timestamp = now - (3600_000L * 4)
                ),
                Complaint(
                    id = "CIV-2026-000007",
                    citizenId = "usr-citizen-1",
                    citizenName = "Ramesh Sharma",
                    citizenContact = "9820098200",
                    description = "Footpath is damaged and concrete paving slabs broken with exposed rebar.",
                    imageUri = null,
                    location = "Near Shivaji Park, Dadar West, Mumbai",
                    latitude = 19.0270,
                    longitude = 72.8385,
                    ward = "Ward G-North",
                    category = "Public Infrastructure",
                    priority = "MEDIUM",
                    department = "Roads Department",
                    summary = "Damaged footpath paving slab with exposed structural iron rebar along busy promenade.",
                    priorityReason = "Tripping and laceration hazard for morning joggers and elderly pedestrians.",
                    confidence = "93%",
                    status = "RESOLVED",
                    assignedOfficerId = "usr-off-1",
                    assignedOfficerName = "K. Patil (Roads)",
                    remarks = "Paving stones replaced and leveled.",
                    resolutionRemarks = "Concrete recasting completed. Footpath safe for pedestrians.",
                    feedbackRating = "YES",
                    feedbackComment = "Very prompt repair by ward engineers!",
                    feedbackScore = 5,
                    isDemo = true,
                    timestamp = now - (3600_000L * 48),
                    resolvedTimestamp = now - (3600_000L * 30)
                ),
                Complaint(
                    id = "CIV-2026-000008",
                    citizenId = "usr-citizen-1",
                    citizenName = "Ramesh Sharma",
                    citizenContact = "9820098200",
                    description = "Deep crater on the express flyover lane causing sudden vehicle swerves.",
                    imageUri = null,
                    location = "Western Express Highway Flyover, Goregaon East, Mumbai",
                    latitude = 19.1663,
                    longitude = 72.8526,
                    ward = "Ward P-South",
                    category = "Pothole / Road Damage",
                    priority = "CRITICAL",
                    department = "Roads Department",
                    summary = "Hazardous deep pothole on high-speed flyover carriageway causing sudden lane swerving.",
                    priorityReason = "High-speed crash risk on expressway flyover requiring emergency asphalt patching.",
                    confidence = "97%",
                    status = "IN PROGRESS",
                    assignedOfficerId = "usr-off-1",
                    assignedOfficerName = "K. Patil (Roads)",
                    remarks = "Asphalt mastic crew dispatched.",
                    isDemo = true,
                    timestamp = now - (3600_000L * 16) // Overdue since Critical SLA is 12h!
                ),
                Complaint(
                    id = "CIV-2026-000009",
                    citizenId = "usr-citizen-2",
                    citizenName = "Sunita Jadhav",
                    citizenContact = "9830098300",
                    description = "Another large pothole on the same carriageway near metro pillar 150.",
                    imageUri = null,
                    location = "SV Road near Metro Pillar 150, Andheri West, Mumbai",
                    latitude = 19.1205,
                    longitude = 72.8470,
                    ward = "Ward K-West",
                    category = "Pothole / Road Damage",
                    priority = "HIGH",
                    department = "Roads Department",
                    summary = "Secondary pothole reported on SV Road corridor.",
                    priorityReason = "Traffic bottleneck and motorcycle hazard.",
                    confidence = "95%",
                    status = "NEW",
                    isDemo = true,
                    timestamp = now - (3600_000L * 1)
                ),
                Complaint(
                    id = "CIV-2026-000010",
                    citizenId = "usr-citizen-1",
                    citizenName = "Ramesh Sharma",
                    citizenContact = "9820098200",
                    description = "Fallen tree branch blocking one carriage lane after heavy rain.",
                    imageUri = null,
                    location = "Linking Road near Turner Road, Bandra West, Mumbai",
                    latitude = 19.0596,
                    longitude = 72.8295,
                    ward = "Ward H-West",
                    category = "Traffic / Road Obstruction",
                    priority = "HIGH",
                    department = "Traffic Department",
                    summary = "Branch obstruction cleared on secondary arterial route.",
                    priorityReason = "Restores normal two-lane traffic flow.",
                    confidence = "94%",
                    status = "RESOLVED",
                    assignedOfficerId = "usr-off-1",
                    assignedOfficerName = "K. Patil (Roads)",
                    remarks = "Debris cleared by tree cutting crew.",
                    resolutionRemarks = "Branch trimmed and roadside cleared.",
                    feedbackRating = "YES",
                    feedbackComment = "Good work by municipal team.",
                    feedbackScore = 4,
                    isDemo = true,
                    timestamp = now - (3600_000L * 52),
                    resolvedTimestamp = now - (3600_000L * 40)
                )
            )
        }
    }
}
