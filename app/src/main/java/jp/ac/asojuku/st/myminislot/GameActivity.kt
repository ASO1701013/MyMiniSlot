package jp.ac.asojuku.st.myminislot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.concurrent.thread

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity


class GameActivity : AppCompatActivity() {

    //どのリールか判定用
    val left = 0
    val center = 1
    val right = 2
    val all = 3

    var myCoins = -1
    var bets = -1

    var freeCnt = 0

    internal var mHandler = Handler()


    //柄一覧と画像のアドレス
    val marks: List<Map<String, Any>> = listOf(
            mapOf("mark" to "Cherry", "mul" to 10, "image" to R.drawable.cherry),
            mapOf("mark" to "Banana", "mul" to 15, "image" to R.drawable.banana),
            mapOf("mark" to "WaterMelon", "mul" to 20, "image" to R.drawable.watermelon),
            mapOf("mark" to "Bar", "mul" to 100, "image" to R.drawable.bar, "free" to 10),
            mapOf("mark" to "BigWin", "mul" to 150, "image" to R.drawable.bigwin, "free" to 20),
            mapOf("mark" to "Seven", "mul" to 777, "image" to R.drawable.seven, "free" to 50)
    )
    val markSize = marks.size //ランダム用
    var reel = arrayOf(-1, -1, -1, -1) //0以上でボタンを押した状態
    var stopCnt = 0 //いくつ止めたか判定用

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        btn_stop_left.setOnClickListener { onStopButtonTapped(left) }
        btn_stop_center.setOnClickListener { onStopButtonTapped(center) }
        btn_stop_right.setOnClickListener { onStopButtonTapped(right) }
        btn_allStop.setOnClickListener { onStopButtonTapped(all) }
        btn_game_finish.setOnClickListener { onGameFinishBottonTapped() }
    }

    override fun onResume() {
        super.onResume()
        bets = intent.getIntExtra("BETS", 0)
        myCoins = intent.getIntExtra("COINS", 0)

        str_myCoins.text = myCoins.toString()
        str_myBets.text = bets.toString()
        //nextGameで画面の初期化
        nextGame()

    }


    fun onStopButtonTapped(reelNo: Int) {
        if(!checkCoin()){//コインが足りない
            str_comment.text = "You haven't coin.."
            return
        }
        //allStopかどうか
        if (reelNo == 3) {
            //NEXT GAMEのときリセットする
            if (btn_allStop.text == "NEXT GAME") {
                nextGame()
            } else {
                mHandler.postDelayed({
                    onStopButtonTapped(left)
                }, 120)
                mHandler.postDelayed({
                    onStopButtonTapped(center)
                }, 240)
                mHandler.postDelayed({
                    onStopButtonTapped(right)
                }, 360)
            }
        } else {

            var selectMark = (Math.random() * markSize).toInt()

            //        img_slot_left.setImageResource(R.drawable.banana)
            val imageResources = Integer.parseInt((marks[selectMark]["image"].toString()))

            //すでに押されていないか判定
            if (reel[reelNo] >= 0) {
                //すでに押されている場合（特に何もしない）
            } else {

                when (reelNo) { //止めたヤツの画像の絵柄を上書き
                    0 -> {
                        img_slot_left.setImageResource(imageResources)
                        btn_stop_left.text = "STOPPED"
                    }
                    1 -> {
                        img_slot_center.setImageResource(imageResources)
                        btn_stop_center.text = "STOPPED"
                    }
                    2 -> {
                        img_slot_right.setImageResource(imageResources)
                        btn_stop_right.text = "STOPPED"
                    }
                }

                reel[reelNo] = selectMark //止めた絵柄を保持
                stopCnt++ //止めた数を数える

                if (stopCnt >= 3) {
                    judge()
                    btn_allStop.text = "NEXT GAME"
                }
            }
        }
    }

    //柄が揃っているかの判定
    fun judge() {


        var returnCoin: Int = -bets //負けていた場合用に先に反転させておく

        if (reel[0] == reel[1] && reel[1] == reel[2]) {
            //揃っていた場合
            var multi = marks[reel[0]]["mul"] //倍率の取り出し
            returnCoin *= -Integer.parseInt(multi.toString()) //獲得コインを計算
            str_comment.text = "YOU WIN!! " + marks[reel[0]]["mark"] + "x" + multi.toString() + "   GET " + returnCoin + " COINS!!" //コメントを更新
            str_returnCoins.text = returnCoin.toString()
            if (marks[reel[0]]["free"] != null) { //フリーボーナス付きの役の場合
                freeCnt += Integer.parseInt(marks[reel[0]]["free"].toString())
                str_free.text = "GET FREE " + freeCnt + " GAME!!"
            }

        } else if (freeCnt > 0) { //フリーゲームがある場合は消費
            freeCnt--
            returnCoin = 0
            if (freeCnt > 0) {
                str_comment.text = "Let's Next FREE GAME!"
            } else {
                str_comment.text = "END FREE GAME"
            }
        } else { //負けていた場合
            str_comment.text = "YOU LOSE...   " + returnCoin + " COINS"
            str_returnCoins.text = returnCoin.toString()
        }

        //所持コインの変動
        myCoins += returnCoin
        str_myCoins.text = myCoins.toString()

        return
    }

    fun nextGame() {
        //変数のリセット
        stopCnt = 0
        reel = arrayOf(-1, -1, -1, -1)

        //画像のリセット
        val image = R.drawable.question
        img_slot_left.setImageResource(image)
        img_slot_center.setImageResource(image)
        img_slot_right.setImageResource(image)
        str_returnCoins.text = "0"

        //表示テキストのリセット
        if (freeCnt > 0) { //フリーゲームの場合は表示を変更
            str_comment.text = "Let's FREE Try!"
            btn_allStop.text = "FREE GAME"
            str_free.text = "FREE " + freeCnt + " GAME"
            btn_stop_left.text = "FREE!"
            btn_stop_center.text = "FREE!"
            btn_stop_right.text = "FREE!"
        } else {
            str_comment.text = "Let's Try!"
            btn_allStop.text = "ALL STOP"
            str_free.text = ""
            btn_stop_left.text = "STOP!"
            btn_stop_center.text = "STOP!"
            btn_stop_right.text = "STOP!"
        }
    }

    fun onGameFinishBottonTapped() {
        saveData()
        startActivity<MainActivity>()
    }


    private fun saveData() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        //共有プリファレンスの更新用にをエディタを用意
        val editor = pref.edit()
        //エディタのメソッドを呼び出して共有プリファレンスを更新
        editor.putInt("MY_COINS", myCoins)
                .apply() //applyで更新を適用
    }

    //コインが足りるかどうかの判定
    fun checkCoin():Boolean{
        var rt = true
        if(myCoins < bets){
            rt = false
        }
        return rt
    }


}
