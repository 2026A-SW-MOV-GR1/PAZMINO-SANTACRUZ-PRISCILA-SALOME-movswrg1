package com.example.securerestmanager

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

val Context.secretDataStore by preferencesDataStore(name = "secret_datastore")

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

interface ApiService {
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): Post

    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Body post: Post
    ): Post
}

object ApiClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class RestRepository(
    private val apiService: ApiService
) {
    suspend fun getPost(id: Int): Post {
        return apiService.getPostById(id)
    }

    suspend fun updatePost(post: Post): Post {
        return apiService.updatePost(post.id, post)
    }
}

enum class StorageType {
    SHARED_PREFERENCES,
    DATASTORE,
    ENCRYPTED_SHARED_PREFERENCES
}

class SecretStorageManager(
    private val context: Context
) {
    private val normalPreferences =
        context.getSharedPreferences("normal_secrets", Context.MODE_PRIVATE)

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "encrypted_secrets",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveSecret(
        key: String,
        value: String,
        storageType: StorageType
    ) {
        when (storageType) {
            StorageType.SHARED_PREFERENCES -> {
                normalPreferences.edit()
                    .putString(key, value)
                    .apply()
            }

            StorageType.DATASTORE -> {
                val preferenceKey = stringPreferencesKey(key)
                context.secretDataStore.edit { preferences ->
                    preferences[preferenceKey] = value
                }
            }

            StorageType.ENCRYPTED_SHARED_PREFERENCES -> {
                encryptedPreferences.edit()
                    .putString(key, value)
                    .apply()
            }
        }
    }

    suspend fun getSecret(
        key: String,
        storageType: StorageType
    ): String? {
        return when (storageType) {
            StorageType.SHARED_PREFERENCES -> {
                normalPreferences.getString(key, null)
            }

            StorageType.DATASTORE -> {
                val preferenceKey = stringPreferencesKey(key)
                val preferences = context.secretDataStore.data.first()
                preferences[preferenceKey]
            }

            StorageType.ENCRYPTED_SHARED_PREFERENCES -> {
                encryptedPreferences.getString(key, null)
            }
        }
    }
}

data class RestUiState(
    val postId: String = "1",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val message: String = "",
    val restMethod: String = "",
    val restResource: String = "",
    val restServer: String = "",
    val restStatus: String = ""
)

class RestViewModel(
    private val repository: RestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestUiState())
    val uiState: StateFlow<RestUiState> = _uiState

    fun onPostIdChange(value: String) {
        _uiState.value = _uiState.value.copy(postId = value)
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onBodyChange(value: String) {
        _uiState.value = _uiState.value.copy(body = value)
    }

    fun getPost() {
        val id = _uiState.value.postId.toIntOrNull()

        if (id == null || id <= 0) {
            _uiState.value = _uiState.value.copy(
                message = "Ingrese un ID válido.",
                restMethod = "",
                restResource = "",
                restServer = "",
                restStatus = ""
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    message = "Consultando información...",
                    restMethod = "GET",
                    restResource = "/posts/$id",
                    restServer = "JSONPlaceholder",
                    restStatus = "En proceso"
                )

                val post = repository.getPost(id)

                _uiState.value = _uiState.value.copy(
                    userId = post.userId.toString(),
                    title = post.title,
                    body = post.body,
                    isLoading = false,
                    message = "Consulta GET realizada correctamente.",
                    restMethod = "GET",
                    restResource = "/posts/$id",
                    restServer = "JSONPlaceholder",
                    restStatus = "Correcto"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error al consultar: ${e.message}",
                    restMethod = "GET",
                    restResource = "/posts/$id",
                    restServer = "JSONPlaceholder",
                    restStatus = "Error"
                )
            }
        }
    }

    fun updatePost() {
        val id = _uiState.value.postId.toIntOrNull()

        if (id == null || id <= 0) {
            _uiState.value = _uiState.value.copy(
                message = "Ingrese un ID válido.",
                restMethod = "",
                restResource = "",
                restServer = "",
                restStatus = ""
            )
            return
        }

        viewModelScope.launch {
            try {
                val post = Post(
                    userId = _uiState.value.userId.toIntOrNull() ?: 1,
                    id = id,
                    title = _uiState.value.title,
                    body = _uiState.value.body
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    message = "Actualizando información...",
                    restMethod = "PUT",
                    restResource = "/posts/$id",
                    restServer = "JSONPlaceholder",
                    restStatus = "En proceso"
                )

                val updatedPost = repository.updatePost(post)

                _uiState.value = _uiState.value.copy(
                    userId = updatedPost.userId.toString(),
                    title = updatedPost.title,
                    body = updatedPost.body,
                    isLoading = false,
                    message = "Actualización PUT simulada correctamente.",
                    restMethod = "PUT",
                    restResource = "/posts/$id",
                    restServer = "JSONPlaceholder",
                    restStatus = "Correcto"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error al actualizar: ${e.message}",
                    restMethod = "PUT",
                    restResource = "/posts/$id",
                    restServer = "JSONPlaceholder",
                    restStatus = "Error"
                )
            }
        }
    }
}

class RestViewModelFactory(
    private val repository: RestRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RestViewModel(repository) as T
    }
}

data class SecretUiState(
    val key: String = "",
    val value: String = "",
    val result: String = "",
    val selectedStorage: StorageType = StorageType.SHARED_PREFERENCES,
    val isLoading: Boolean = false,
    val message: String = ""
)

class SecretViewModel(
    private val storageManager: SecretStorageManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecretUiState())
    val uiState: StateFlow<SecretUiState> = _uiState

    fun onKeyChange(value: String) {
        _uiState.value = _uiState.value.copy(key = value)
    }

    fun onValueChange(value: String) {
        _uiState.value = _uiState.value.copy(value = value)
    }

    fun onStorageChange(storageType: StorageType) {
        _uiState.value = _uiState.value.copy(selectedStorage = storageType)
    }

    fun saveSecret() {
        val key = _uiState.value.key.trim()
        val value = _uiState.value.value.trim()

        if (key.isBlank() || value.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Ingrese una clave y un valor."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    message = "Guardando secreto..."
                )

                storageManager.saveSecret(
                    key = key,
                    value = value,
                    storageType = _uiState.value.selectedStorage
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Secreto guardado correctamente."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    fun recoverSecret() {
        val key = _uiState.value.key.trim()

        if (key.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Ingrese la clave."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    message = "Recuperando secreto..."
                )

                val result = storageManager.getSecret(
                    key = key,
                    storageType = _uiState.value.selectedStorage
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    result = result ?: "No existe un secreto con esa clave.",
                    message = "Búsqueda finalizada."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Error al recuperar: ${e.message}"
                )
            }
        }
    }
}

class SecretViewModelFactory(
    private val storageManager: SecretStorageManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SecretViewModel(storageManager) as T
    }
}

class MainActivity : ComponentActivity() {

    private val restViewModel: RestViewModel by viewModels {
        RestViewModelFactory(
            RestRepository(ApiClient.apiService)
        )
    }

    private val secretViewModel: SecretViewModel by viewModels {
        SecretViewModelFactory(
            SecretStorageManager(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SecureRestApp(
                restViewModel = restViewModel,
                secretViewModel = secretViewModel
            )
        }
    }
}

@Composable
fun SecureRestApp(
    restViewModel: RestViewModel,
    secretViewModel: SecretViewModel
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            var selectedScreen by remember { mutableStateOf("REST") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Proyecto: Red y Seguridad",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Kotlin + REST + Persistencia Segura",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedScreen = "REST" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("REST API")
                    }

                    Button(
                        onClick = { selectedScreen = "SECRETS" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Secretos")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedScreen) {
                    "REST" -> RestScreen(restViewModel)
                    "SECRETS" -> SecretScreen(secretViewModel)
                }
            }
        }
    }
}

@Composable
fun RestScreen(viewModel: RestViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Módulo 1: Conectividad REST",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = state.postId,
                    onValueChange = viewModel::onPostIdChange,
                    label = { Text("ID del post") },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = { viewModel.getPost() },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Consultar GET")
                    }

                    ElevatedButton(
                        onClick = { viewModel.updatePost() },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Actualizar PUT")
                    }
                }

                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Título") },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.body,
                    onValueChange = viewModel::onBodyChange,
                    label = { Text("Contenido") },
                    enabled = !state.isLoading,
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "User ID: ${state.userId.ifBlank { "Sin datos" }}"
                )

                if (state.message.isNotBlank()) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (state.restMethod.isNotBlank()) {
                    RestTable(
                        metodo = state.restMethod,
                        servidor = state.restServer,
                        recurso = state.restResource,
                        estado = state.restStatus,
                        postId = state.postId,
                        userId = state.userId,
                        title = state.title,
                        body = state.body,
                        message = state.message
                    )
                }
            }
        }
    }
}

@Composable
fun RestTable(
    metodo: String,
    servidor: String,
    recurso: String,
    estado: String,
    postId: String,
    userId: String,
    title: String,
    body: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Detalle de la consulta REST",
                style = MaterialTheme.typography.titleMedium
            )

            RestTableRow("Método HTTP", metodo)
            RestTableRow("Servidor", servidor)
            RestTableRow("Recurso", recurso)
            RestTableRow("ID consultado", postId)
            RestTableRow("Estado", estado)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Datos obtenidos",
                style = MaterialTheme.typography.titleMedium
            )

            RestTableRow("User ID", userId.ifBlank { "Sin datos" })
            RestTableRow("Post ID", postId)
            RestTableRow("Título", title.ifBlank { "Sin datos" })
            RestTableRow("Contenido", body.ifBlank { "Sin datos" })

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun RestTableRow(
    campo: String,
    valor: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = campo,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = valor,
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SecretScreen(viewModel: SecretViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Módulo 3: Almacenamiento Seguro",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = state.key,
                    onValueChange = viewModel::onKeyChange,
                    label = { Text("Clave") },
                    placeholder = { Text("Ejemplo: api_key") },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.value,
                    onValueChange = viewModel::onValueChange,
                    label = { Text("Valor secreto") },
                    placeholder = { Text("Ejemplo: 12345ABC") },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                StorageSelector(
                    selectedStorage = state.selectedStorage,
                    onStorageSelected = viewModel::onStorageChange,
                    enabled = !state.isLoading
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = { viewModel.saveSecret() },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }

                    ElevatedButton(
                        onClick = { viewModel.recoverSecret() },
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Recuperar")
                    }
                }

                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (state.result.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Resultado recuperado:",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(text = state.result)
                        }
                    }
                }

                if (state.message.isNotBlank()) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Nota: SharedPreferences y DataStore guardan datos sin cifrado. EncryptedSharedPreferences cifra la clave y el valor antes de guardarlos.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun StorageSelector(
    selectedStorage: StorageType,
    onStorageSelected: (StorageType) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Tipo de almacenamiento:",
            style = MaterialTheme.typography.titleSmall
        )

        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when (selectedStorage) {
                    StorageType.SHARED_PREFERENCES -> "SharedPreferences"
                    StorageType.DATASTORE -> "DataStore"
                    StorageType.ENCRYPTED_SHARED_PREFERENCES -> "EncryptedSharedPreferences"
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("SharedPreferences") },
                onClick = {
                    onStorageSelected(StorageType.SHARED_PREFERENCES)
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("DataStore") },
                onClick = {
                    onStorageSelected(StorageType.DATASTORE)
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = { Text("EncryptedSharedPreferences") },
                onClick = {
                    onStorageSelected(StorageType.ENCRYPTED_SHARED_PREFERENCES)
                    expanded = false
                }
            )
        }
    }
}