package br.com.fiap.wtcconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme // <-- ADD THIS IMPORT
import androidx.compose.material3.Surface     // <-- ADD THIS IMPORT
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.fiap.wtcconnect.navigation.AppNavigation
import br.com.fiap.wtcconnect.ui.theme.WtcCrmTheme // <-- Use the correct theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(applicationContext)
        setContent {
            WtcCrmTheme { // This was already correct
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
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