package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.CrmAppLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CrmViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CrmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge drawing under system status & navigation bars
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                CrmAppLayout(viewModel = viewModel)
            }
        }
    }
}
