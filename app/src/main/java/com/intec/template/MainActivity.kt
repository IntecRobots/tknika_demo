package com.intec.template

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ainirobot.coreservice.client.actionbean.Pose
import com.intec.t2o.network.SocketClient
import com.intec.template.navigation.AppNavigation
import com.intec.template.robot.RobotConnectionService
import com.intec.template.robot.RobotManager
import com.intec.template.robot.SkillApiService
import com.intec.template.robot.listeners.NavigationListener
import com.intec.template.ui.theme.MyApplicationTheme
import com.intec.template.ui.viewmodels.RobotViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var robotManager: RobotManager

    @Inject
    lateinit var robotConnectionService: RobotConnectionService

    @Inject
    lateinit var skillApiService: SkillApiService

    private val robotViewModel : RobotViewModel by viewModels()

    @Inject
    lateinit var socketClient: SocketClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[android.Manifest.permission.CAMERA] == true -> {
                // El permiso de la cámara ha sido concedido, puedes continuar con la operación que requiere este permiso
            }
            permissions[android.Manifest.permission.RECORD_AUDIO] == true -> {
                // El permiso de grabación de audio ha sido concedido
            }
            else -> {
                // Permiso denegado. Puedes informar al usuario que no podrás continuar sin los permisos
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            )
        )

        setContent {
            MyApplicationTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(robotViewModel = robotViewModel)
                }
            }
        }

        // Asegúrate de que el listener del WebSocket se configure correctamente

        socketClient.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketClient.disconnect()
    }
}


