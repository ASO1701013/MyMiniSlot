package jp.ac.asojuku.st.myminislot

import org.jetbrains.anko.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var myCoins = 0;
    var bets = 0;


    override fun onCreate(savedInstanceState: Bundle?) {
        readData() //myCoinsの読み込み

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateScreen()

        btn_reset.setOnClickListener { onResetButtonTapped() }
        btn_bet_10up.setOnClickListener { onBetButtonTapped(10) }
        btn_bet_10down.setOnClickListener { onBetButtonTapped(-10) }
        btn_start.setOnClickListener { onStartButtonTapped(it) }
    }


    fun onResetButtonTapped() {
        myCoins = 1000;
        bets = 10;
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        //共有プリファレンスの更新用にをエディタを用意
        val editor = pref.edit()
        //エディタのメソッドを呼び出して共有プリファレンスを更新
        editor.putInt("MY_COINS", myCoins)
                .apply() //applyで更新を適用
        updateScreen()

    }

    fun onBetButtonTapped(bet:Int) {
        bets += bet
        if(bets < 10){
            bets = 10
        }
        updateScreen()
    }



    fun onStartButtonTapped(view: View?) {
        //BETSとCOINSを渡す
        startActivity<GameActivity>(
                "BETS" to bets,
                "COINS" to myCoins)
    }

    fun updateScreen() {
        //myCoinsとBetsの更新
        str_myCoins.text = myCoins.toString()
        str_myBets.text = bets.toString()
    }

    private fun readData() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        myCoins = pref.getInt("MY_COINS", 1000)
    }

}
