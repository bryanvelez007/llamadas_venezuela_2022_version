/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.assistant.viewmodels

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.LinphoneApplication.Companion.corePreferences
import org.linphone.core.*
import org.linphone.core.tools.Log
import org.linphone.utils.Event

class GenericLoginViewModelFactory(private val accountCreator: AccountCreator) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GenericLoginViewModel(accountCreator) as T
    }
}

class GenericLoginViewModel(private val accountCreator: AccountCreator) : ViewModel() {
    val username = MutableLiveData<String>()
    val fakeTitle = MutableLiveData<String>()

    val password = MutableLiveData<String>()

    val domain: MutableLiveData<String> = MutableLiveData("voip.llamadasvenezuela.com:5077")

    val displayName = MutableLiveData<String>()

    val transport = MutableLiveData<TransportType>()

    val loginEnabled: MediatorLiveData<Boolean> = MediatorLiveData()

    val waitForServerAnswer = MutableLiveData<Boolean>()

    val leaveAssistantEvent = MutableLiveData<Event<Boolean>>()

    val isSuccessfull = MutableLiveData<Boolean>()

    val invalidCredentialsEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }

    val onErrorEvent: MutableLiveData<Event<String>> by lazy {
        MutableLiveData<Event<String>>()
    }

    private var proxyConfigToCheck: ProxyConfig? = null

    private val coreListener = object : CoreListenerStub() {
        override fun onRegistrationStateChanged(
            core: Core,
            cfg: ProxyConfig,
            state: RegistrationState,
            message: String
        ) {
            if (cfg == proxyConfigToCheck) {
                Log.i("[Assistant] [Generic Login] Registration state is $state: $message")
                if (state == RegistrationState.Ok) {
                    waitForServerAnswer.value = false
                    isSuccessfull.value = true
                    leaveAssistantEvent.value = Event(true)
                    core.removeListener(this)
                } else if (state == RegistrationState.Failed) {
                    isSuccessfull.value = false
                    waitForServerAnswer.value = false
                    invalidCredentialsEvent.value = Event(true)
                    core.removeListener(this)
                }
            }
        }
    }

    init {
        getProxyFirebase()

        transport.value = TransportType.Udp

        loginEnabled.value = false

        loginEnabled.addSource(username) {
            loginEnabled.value = isLoginButtonEnabled()
        }
        loginEnabled.addSource(password) {
            loginEnabled.value = isLoginButtonEnabled()
        }
        loginEnabled.addSource(domain) {
            loginEnabled.value = isLoginButtonEnabled()
        }
    }

    fun setTransport(transportType: TransportType) {
        transport.value = transportType
    }

    fun removeInvalidProxyConfig() {
        val cfg = proxyConfigToCheck
        cfg ?: return
        val authInfo = cfg.findAuthInfo()
        if (authInfo != null) coreContext.core.removeAuthInfo(authInfo)
        coreContext.core.removeProxyConfig(cfg)
        proxyConfigToCheck = null
    }

    fun continueEvenIfInvalidCredentials() {
        leaveAssistantEvent.value = Event(true)
    }

    fun getProxyFirebase() {
        // Write a message to the database
        val database = Firebase.database
        val myRef = database.getReference("proxy")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                domain.value = value
                // Toast.makeText(coreContext.context, "NUEVO PROXY: " + domain.value, Toast.LENGTH_LONG).show()
            }

            override fun onCancelled(error: DatabaseError) {
                val value = error
            }
        })
    }

    fun createProxyConfig() {

        waitForServerAnswer.value = true
        coreContext.core.addListener(coreListener)

        accountCreator.username = username.value
        accountCreator.password = password.value
        accountCreator.domain = domain.value
        accountCreator.displayName = displayName.value
        accountCreator.transport = transport.value

        val proxyConfig: ProxyConfig? = accountCreator.createProxyConfig()
        proxyConfigToCheck = proxyConfig

        if (proxyConfig == null) {
            Log.e("[Assistant] [Generic Login] Account creator couldn't create proxy config")
            coreContext.core.removeListener(coreListener)
            onErrorEvent.value = Event("Error: Failed to create account object")
            waitForServerAnswer.value = false
            return
        }

        Log.i("[Assistant] [Generic Login] Proxy config created")
        // The following is required to keep the app alive
        // and be able to receive calls while in background
        if (domain.value.orEmpty() != corePreferences.defaultDomain) {
            Log.i("[Assistant] [Generic Login] Background mode with foreground service automatically enabled")
            //  corePreferences.keepServiceAlive = true
            // coreContext.notificationsManager.startForeground()
        }

        val mPrefs = PreferenceManager.getDefaultSharedPreferences(coreContext.context)
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        prefsEditor.putString("updatedProxy", "no")
        prefsEditor.commit()
    }

    private fun isLoginButtonEnabled(): Boolean {
        return username.value.orEmpty().isNotEmpty() && password.value.orEmpty().isNotEmpty()
    }
}
