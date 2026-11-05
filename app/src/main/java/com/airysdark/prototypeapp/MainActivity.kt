package com.airysdark.prototypeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.airysdark.prototypeapp.data.LocalProfileStore
import com.airysdark.prototypeapp.identity.LocalIdentity
import com.airysdark.prototypeapp.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileStore = LocalProfileStore(applicationContext)
        val identity = LocalIdentity(applicationContext)
        setContent {
            PrototypeApp {
                HomeScreen(
                    initialProfile = profileStore.load(),
                    pseudonymousId = identity.getOrCreateId(),
                    onSaveProfile = profileStore::save
                )
            }
        }
    }
}
