package org.linphone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pay_in_app.*
import kotlinx.android.synthetic.main.activity_reward.*
import org.linphone.activities.main.MainActivity

class Reward : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

        goToReward.setOnClickListener {
            val intent = Intent(this@Reward, PayInApp::class.java)
            startActivity(intent)
            finish()
        }

        goToHome.setOnClickListener {
            val intent = Intent(this@Reward, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
