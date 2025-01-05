data class CustomerValidationResponse(
    val nombre: String,
    val tipoIdentificacion: String,
    val situacion: SituacionTributaria
)

data class SituacionTributaria(
    val estado: String,
    val mensaje: String
) 