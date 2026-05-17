package br.com.fiap.wtcconnect

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.fiap.wtcconnect.navigation.AppNavigation
import br.com.fiap.wtcconnect.navigation.DeepLinkManager
import br.com.fiap.wtcconnect.ui.theme.WtcCrmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (throwable.cause?.message?.contains("ACTION_HOVER_EXIT") == true ||
                throwable.message?.contains("ACTION_HOVER_EXIT") == true) {
                android.util.Log.w("HoverBugWorkaround", "Ignored ACTION_HOVER_EXIT crash", throwable)
                return@setDefaultUncaughtExceptionHandler
            }
            originalHandler?.uncaughtException(thread, throwable)
        }

        val mainHandler = Handler(Looper.getMainLooper())
        val exceptionHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
            if (throwable.cause?.message?.contains("ACTION_HOVER_EXIT") == true ||
                throwable.message?.contains("ACTION_HOVER_EXIT") == true) {
                android.util.Log.w("HoverBugWorkaround", "Ignored ACTION_HOVER_EXIT crash", throwable)
                return@UncaughtExceptionHandler
            }
            originalHandler?.uncaughtException(thread, throwable)
        }
        mainHandler.post {
            Thread.currentThread().uncaughtExceptionHandler = exceptionHandler
        }

        AppContainer.init(applicationContext)
        DeepLinkManager.update(intent?.data)
        setContent {
            WtcCrmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DeepLinkManager.update(intent.data)
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    // FIX: Change WTCConnectTheme to WtcCrmTheme to match the rest of the file
    WtcCrmTheme {
        Greeting("Android")
    }
}