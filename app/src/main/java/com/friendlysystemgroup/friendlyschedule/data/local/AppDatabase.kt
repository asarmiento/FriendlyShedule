@Database(
    entities = [AppointmentEntity::class, CustomerEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun customerDao(): CustomerDao
}

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: Int,
    val customerId: Int,
    val initDate: String,
    val initTime: String,
    val description: String?,
    val attendance: Boolean,
    val absent: Boolean
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: Int,
    val card: String,
    val name: String,
    val phone: String,
    val email: String?,
    val birthday: String?,
    val gender: String
) 