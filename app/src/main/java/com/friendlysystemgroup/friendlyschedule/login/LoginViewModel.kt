@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                repository.login(email, password).fold(
                    onSuccess = { 
                        _loginState.value = LoginState.Success 
                    },
                    onFailure = { 
                        _loginState.value = LoginState.Error(it.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
} 