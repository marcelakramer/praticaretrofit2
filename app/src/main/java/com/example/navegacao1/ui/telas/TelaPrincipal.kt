package com.example.navegacao1.ui.telas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navegacao1.model.dados.Endereco
import com.example.navegacao1.model.dados.RetrofitClient
import com.example.navegacao1.model.dados.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TelaPrincipal(modifier: Modifier = Modifier, onLogoffClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var endereco by remember { mutableStateOf(Endereco()) }
    var nome by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var idParaBuscarOuRemover by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            usuarios = getUsuarios()
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Tela Principal", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Adicionar Usuário", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                scope.launch {
                    val novoUsuario = Usuario(
                        id = getNextId(usuarios),
                        nome = nome,
                        senha = senha
                    )
                    inserirUsuario(novoUsuario)
                    usuarios = getUsuarios()
                    nome = ""
                    senha = ""

                    focusManager.clearFocus()
                }
            },
            enabled = nome.isNotEmpty() && senha.isNotEmpty()
        ) {
            Text("Adicionar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Buscar ou Remover Usuário", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = idParaBuscarOuRemover,
            onValueChange = { idParaBuscarOuRemover = it },
            label = { Text("ID do Usuário") },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    scope.launch {
                        if (idParaBuscarOuRemover.isNotEmpty()) {
                            try {
                                val usuario = buscarUsuarioPorId(idParaBuscarOuRemover)
                                usuarios = listOf(usuario)
                            } catch (e: Exception) {
                                mensagemErro = "Usuário não encontrado!"
                            }
                            idParaBuscarOuRemover = ""
                        }
                    }
                },
                enabled = idParaBuscarOuRemover.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Buscar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (idParaBuscarOuRemover.isNotEmpty()) {
                            removerUsuario(idParaBuscarOuRemover)
                            usuarios = getUsuarios()
                            idParaBuscarOuRemover = ""
                        }
                    }
                },
                enabled = idParaBuscarOuRemover.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Remover")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Lista de Usuários",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                scope.launch {
                    usuarios = getUsuarios()
                }
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Recarregar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(usuarios) { usuario ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "ID: ${usuario.id}")
                        Text(text = "Nome: ${usuario.nome}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

//        Button(onClick = { onLogoffClick() }) {
//            Text("Sair")
//        }

        mensagemErro?.let { erro ->
            LaunchedEffect(erro) {
                scope.launch {
                    delay(3000)
                    mensagemErro = null
                }
            }
            Snackbar(modifier = Modifier.padding(8.dp)) {
                Text(text = erro)
            }
        }
    }
}

suspend fun getUsuarios(): List<Usuario> {
    return withContext(Dispatchers.IO) {
        RetrofitClient.usuarioService.listar()
    }
}

suspend fun inserirUsuario(usuario: Usuario) {
    withContext(Dispatchers.IO) {
        RetrofitClient.usuarioService.inserir(usuario)
    }
}

suspend fun removerUsuario(id: String) {
    withContext(Dispatchers.IO) {
        RetrofitClient.usuarioService.remover(id)
    }
}

suspend fun buscarUsuarioPorId(id: String): Usuario {
    return withContext(Dispatchers.IO) {
        RetrofitClient.usuarioService.buscarPorId(id)
    }
}

fun getNextId(usuarios: List<Usuario>): String {
    return (usuarios.maxOfOrNull { it.id.toIntOrNull() ?: 0 }?.plus(1) ?: 1).toString()
}