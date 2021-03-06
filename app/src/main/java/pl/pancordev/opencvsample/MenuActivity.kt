package pl.pancordev.opencvsample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.act_menu.*

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_menu)

        findBlueBtn.setOnClickListener {
            val intent = Intent(this, FindBlueActivity::class.java)
            startActivity(intent)
        }

        loadImgBtn.setOnClickListener {
            val intent = Intent(this, LoadImageActivity::class.java)
            startActivity(intent)
        }

        loadVideoBtn.setOnClickListener {
            val intent = Intent(this, LoadVideoActivity::class.java)
            startActivity(intent)
        }

        findBallsBtn.setOnClickListener {
            val intent = Intent(this, FindBallsActivity::class.java)
            startActivity(intent)
        }

        findTableBtn.setOnClickListener {
            val intent = Intent(this, FindTableActivity::class.java)
            startActivity(intent)
        }
    }
}
