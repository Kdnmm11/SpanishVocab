package com.example.spanishvocab

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog

object TtsAvailabilityHelper {

    private const val PREFS = "prefs"
    private const val KEY_INFO_SHOWN = "tts_info_shown_once"

    /**
     * 앱 설치 후 최초 실행에 한 번만 보여주는 안내 팝업.
     * TTS 설치 여부는 **감지하지 않습니다.**
     */
    fun showFirstRunInfoIfNeeded(activity: Activity) {
        val sp = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val shown = sp.getBoolean(KEY_INFO_SHOWN, false)
        if (shown) return

        // 액티비티가 이미 종료/종료 중이면 팝업 띄우지 않기
        if (activity.isFinishing || activity.isDestroyed) return

        AlertDialog.Builder(activity)
            .setTitle("스페인어 듣기 안내")
            .setMessage(
                "이 앱의 듣기 기능은 기기 TTS(음성 합성)의 스페인어 언어 데이터를 사용합니다.\n\n" +
                        "스페인어 언어팩이 없으면 듣기가 동작하지 않을 수 있으니, 필요 시 설정에서 설치해 주세요."
            )
            .setPositiveButton("설정 열기") { _, _ ->
                // 유저가 원할 때만 TTS/언어 관련 설정으로 이동
                try {
                    // 글자 읽어주기(접근성) 화면
                    val intent = Intent("com.android.settings.TTS_SETTINGS")
                    activity.startActivity(intent)
                } catch (_: Exception) {
                    // 일부 기종에서 액션이 없을 수 있음 → 일반 설정으로라도 진입
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                        activity.startActivity(intent)
                    } catch (_: Exception) { /* 무시 */ }
                }
            }
            .setNegativeButton("닫기", null)
            .show()

        sp.edit().putBoolean(KEY_INFO_SHOWN, true).apply()
    }
}