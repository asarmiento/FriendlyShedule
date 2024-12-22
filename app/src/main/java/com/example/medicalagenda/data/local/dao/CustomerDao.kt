@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: Int): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE card = :identification")
    suspend fun getCustomerByIdentification(identification: String): CustomerEntity?
} 