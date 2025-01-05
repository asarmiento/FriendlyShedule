fun CustomerEntity.toDomain(): Customer {
    return Customer(
        id = id,
        card = card,
        name = name,
        phone = phone,
        email = email,
        birthday = birthday,
        gender = gender,
        address = null
    )
}

fun Customer.toEntity(): CustomerEntity {
    return CustomerEntity(
        id = id,
        card = card,
        name = name,
        phone = phone,
        email = email,
        birthday = birthday,
        gender = gender
    )
} 