package org.linphone

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class Shop : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        val cardShop1 = findViewById<RelativeLayout>(R.id.cardShop1)
        val cardShop2 = findViewById<RelativeLayout>(R.id.cardShop2)
        val cardShop3 = findViewById<RelativeLayout>(R.id.cardShop3)

        val iv_click_me = findViewById(R.id.back_pressed) as ImageView

        val txtPuntos = findViewById<TextView>(R.id.total_coins_reward)

        val txtTitle1 = findViewById<TextView>(R.id.title1)
        val txtTitle2 = findViewById<TextView>(R.id.title2)
        val txtTitle3 = findViewById<TextView>(R.id.title3)

        val txtDescription1 = findViewById<TextView>(R.id.description1)
        val txtDescription2 = findViewById<TextView>(R.id.description2)
        val txtDescription3 = findViewById<TextView>(R.id.description3)

        // Write a message to the database
        val database = Firebase.database
        val myRef = database.getReference().child("users").child("bryanvelez007").child("puntos")
        val myRefShop = database.getReference().child("tienda")
        // val myRef = database.getReference("urlRegister")

        myRefShop.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val titulo1 = snapshot.child("titulo1").getValue<String>()
                val titulo2 = snapshot.child("titulo2").getValue<String>()
                val titulo3 = snapshot.child("titulo3").getValue<String>()

                val description1 = snapshot.child("description1").getValue<String>()
                val description2 = snapshot.child("description2").getValue<String>()
                val description3 = snapshot.child("description3").getValue<String>()

                val price1 = snapshot.child("valor1").getValue<Long>()

                txtTitle1.text = titulo1.toString()
                txtTitle2.text = titulo2.toString()
                txtTitle3.text = titulo3.toString()

                txtDescription1.text = description1.toString()
                txtDescription2.text = description2.toString()
                txtDescription3.text = description3.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                val value = error
                Toast.makeText(
                    LinphoneApplication.coreContext.context,
                    "" + value,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<Long>()
                if (value != null) {
                    Toast.makeText(
                        LinphoneApplication.coreContext.context,
                        "" + snapshot.getValue<Long>(),
                        Toast.LENGTH_LONG
                    ).show()
                    txtPuntos.text = value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val value = error
                Toast.makeText(
                    LinphoneApplication.coreContext.context,
                    "" + value,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        iv_click_me.setOnClickListener {
            // your code to perform when the user clicks on the ImageView
            super.onBackPressed()
        }

        cardShop1.setOnClickListener {

            val alert = AlertView(
                "Canjear Puntos",
                "¿Deseas canjear tus puntos por minutos?",
                AlertStyle.DIALOG
            )

            alert.addAction(
                AlertAction("Confirmar Canje", AlertActionStyle.POSITIVE) { action ->
                    Toast.makeText(this, action.title, Toast.LENGTH_SHORT).show()

                    lifecycleScope.launch {
                        myRefShop.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val price1 = snapshot.child("valor1").getValue<Long>()

                                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val value = snapshot.getValue<Long>()
                                        if (value != null) {
                                            if (value >= price1!!) {
                                                val totalFinal = value - price1!!

                                                val postUrl = "http://voip.llamadasvenezuela.com/saldo/recarga/index.php"
                                                val requestQueue: RequestQueue =
                                                    Volley.newRequestQueue(LinphoneApplication.coreContext.context)

                                                val postData = JSONObject()
                                                try {
                                                    postData.put("password", "16WdwAvB3F05uJ29")
                                                    postData.put("usuario", "102030")
                                                    postData.put("recarga", 0.5)
                                                } catch (e: JSONException) {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "Error conectando con el API" + e,
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }

                                                val jsonObjectRequest = JsonObjectRequest(
                                                    Request.Method.POST, postUrl, postData,
                                                    { response ->
                                                        val StatusResponse = response.getString("status")

                                                        if (StatusResponse == "true") {
                                                            Toast.makeText(
                                                                LinphoneApplication.coreContext.context,
                                                                "Felicidades tu recarga fue exitosa.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                LinphoneApplication.coreContext.context,
                                                                "Lo sentimos hubo un error en: " + StatusResponse,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                ) { error -> Log.d("Lo sentimos hubo un error en la peticion", ":" + error) }

                                                requestQueue.add(jsonObjectRequest)

                                                myRef.setValue(totalFinal)
                                            } else {
                                                Toast.makeText(
                                                    LinphoneApplication.coreContext.context,
                                                    "No tienes los puntos sufientes para esta compra",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        val value = error
                                        Toast.makeText(
                                            LinphoneApplication.coreContext.context,
                                            "" + value,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            )
            alert.addAction(
                AlertAction("Cancelar", AlertActionStyle.DEFAULT) { action ->
                    Toast.makeText(this, action.title, Toast.LENGTH_SHORT).show()
                }
            )

            alert.show(this)
        }

        cardShop2.setOnClickListener {

            val alert = AlertView(
                "Canjear Puntos",
                "¿Deseas canjear tus puntos por minutos?",
                AlertStyle.DIALOG
            )

            alert.addAction(
                AlertAction("Confirmar Canje", AlertActionStyle.POSITIVE) { action ->
                    Toast.makeText(this, action.title, Toast.LENGTH_SHORT).show()

                    myRefShop.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val price2 = snapshot.child("valor2").getValue<Long>()

                            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val value = snapshot.getValue<Long>()
                                    if (value != null) {
                                        if (value >= price2!!) {
                                            val totalFinal = value - price2!!

                                            val postUrl = "http://voip.llamadasvenezuela.com/saldo/recarga/index.php"
                                            val requestQueue: RequestQueue =
                                                Volley.newRequestQueue(LinphoneApplication.coreContext.context)

                                            val postData = JSONObject()
                                            try {
                                                postData.put("password", "16WdwAvB3F05uJ29")
                                                postData.put("usuario", "102030")
                                                postData.put("recarga", 0.1)
                                            } catch (e: JSONException) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Error conectando con el API" + e,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }

                                            val jsonObjectRequest = JsonObjectRequest(
                                                Request.Method.POST, postUrl, postData,
                                                { response ->
                                                    val StatusResponse = response.getString("status")

                                                    if (StatusResponse == "true") {
                                                        Toast.makeText(
                                                            LinphoneApplication.coreContext.context,
                                                            "Felicidades tu recarga fue exitosa.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            LinphoneApplication.coreContext.context,
                                                            "Lo sentimos hubo un error en: " + StatusResponse,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            ) { error -> Log.d("Lo sentimos hubo un error en la peticion", ":" + error) }

                                            requestQueue.add(jsonObjectRequest)

                                            myRef.setValue(totalFinal)
                                        } else {
                                            Toast.makeText(
                                                LinphoneApplication.coreContext.context,
                                                "No tienes los puntos sufientes para esta compra",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    val value = error
                                    Toast.makeText(
                                        LinphoneApplication.coreContext.context,
                                        "" + value,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            )
            alert.addAction(
                AlertAction("Cancelar", AlertActionStyle.DEFAULT) { action ->
                    Toast.makeText(this, action.title, Toast.LENGTH_SHORT).show()
                }
            )

            alert.show(this)
        }

        cardShop3.setOnClickListener {

            val alert = AlertView(
                "Canjear Puntos",
                "¿Deseas canjear tus puntos por minutos?",
                AlertStyle.DIALOG
            )

            alert.addAction(
                AlertAction("Confirmar Canje", AlertActionStyle.POSITIVE) { action ->
                    Toast.makeText(this, action.title, Toast.LENGTH_SHORT).show()

                    myRefShop.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val price3 = snapshot.child("valor3").getValue<Long>()

                            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val value = snapshot.getValue<Long>()
                                    if (value != null) {
                                        if (value >= price3!!) {
                                            val totalFinal = value - price3!!

                                            val postUrl = "http://voip.llamadasvenezuela.com/saldo/recarga/index.php"
                                            val requestQueue: RequestQueue =
                                                Volley.newRequestQueue(LinphoneApplication.coreContext.context)

                                            val postData = JSONObject()
                                            try {
                                                postData.put("password", "16WdwAvB3F05uJ29")
                                                postData.put("usuario", "102030")
                                                postData.put("recarga", 0.1)
                                            } catch (e: JSONException) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Error conectando con el API" + e,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }

                                            val jsonObjectRequest = JsonObjectRequest(
                                                Request.Method.POST, postUrl, postData,
                                                { response ->
                                                    val StatusResponse = response.getString("status")

                                                    if (StatusResponse == "true") {
                                                        Toast.makeText(
                                                            LinphoneApplication.coreContext.context,
                                                            "Felicidades tu recarga fue exitosa.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            LinphoneApplication.coreContext.context,
                                                            "Lo sentimos hubo un error en: " + StatusResponse,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            ) { error -> Log.d("Lo sentimos hubo un error en la peticion", ":" + error) }

                                            requestQueue.add(jsonObjectRequest)

                                            myRef.setValue(totalFinal)
                                        } else {
                                            Toast.makeText(
                                                LinphoneApplication.coreContext.context,
                                                "No tienes los puntos sufientes para esta compra",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    val value = error
                                    Toast.makeText(
                                        LinphoneApplication.coreContext.context,
                                        "" + value,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            )
            alert.addAction(
                AlertAction("Cancelar", AlertActionStyle.DEFAULT) { action ->
                    Toast.makeText(this, action.title, Toast.LENGTH_SHORT).show()
                }
            )

            alert.show(this)
        }
    }
}
