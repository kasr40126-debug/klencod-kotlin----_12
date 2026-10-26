
package com.kaser.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private val green = Color.rgb(0, 150, 100)
    private val dark = Color.rgb(18, 27, 34)
    private val prefs by lazy {
        getSharedPreferences("kaser_chat", MODE_PRIVATE)
    }

    private lateinit var root: LinearLayout
    private lateinit var chatArea: LinearLayout
    private lateinit var input: EditText
    private lateinit var scroll: ScrollView
    private var currentChat = "محادثتي"

    private val jokes = arrayOf(
        "😂 مرحباً بك في الكاسر آب!",
        "🤣 واحد نام متأخر فاته الحلم!",
        "😂 بخيل دخل مطعم قرأ القائمة وشبع!",
        "😄 ابتسم، أنت في الكاسر آب!"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showWelcome()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) showHome()
        }, 3500)
    }

    private fun setup(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(dark)
        }

        val header = TextView(this).apply {
            text = title
            textSize = 24f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.WHITE)
            setPadding(12, 18, 12, 18)
            setBackgroundColor(green)
        }

        root.addView(header)
        setContentView(root)
    }

    private fun addButton(
        parent: LinearLayout,
        text: String,
        action: () -> Unit
    ) {
        val b = Button(this).apply {
            this.text = text
            isAllCaps = false
            setOnClickListener { action() }
        }
        parent.addView(
            b,
            LinearLayout.LayoutParams(-1, -2)
        )
    }

    private fun showWelcome() {
        setup("KASER APP")
        val welcome = TextView(this).apply {
            text = jokes.random()
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        root.addView(
            welcome,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )
    }

    private fun showHome() {
        setup("💬 الكاسر آب")

        val subtitle = TextView(this).apply {
            text = "المحادثات     |     جهات الاتصال"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(8, 18, 8, 18)
        }
        root.addView(subtitle)

        val chats = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val names = listOf("محادثتي", "الأصدقاء", "العائلة")

        for (name in names) {
            addButton(chats, "👤  $name") {
                currentChat = name
                showChat()
            }
        }

        root.addView(
            chats,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        addButton(root, "➕ بدء محادثة") {
            showNewChat()
        }

        addButton(root, "⚙️ الإعدادات") {
            showSettings()
        }
    }

    private fun showNewChat() {
        setup("محادثة جديدة")

        val nameInput = EditText(this).apply {
            hint = "اكتب اسم المحادثة"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
        }
        root.addView(nameInput)

        addButton(root, "إنشاء المحادثة") {
            val name = nameInput.text.toString().trim()
            if (name.isNotEmpty()) {
                currentChat = name
                showChat()
            } else {
                Toast.makeText(
                    this, "اكتب اسم المحادثة",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        addButton(root, "رجوع") { showHome() }
    }

    private fun showChat() {
        setup("💬 $currentChat")

        scroll = ScrollView(this)
        chatArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 8, 4, 8)
        }
        scroll.addView(chatArea)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        input = EditText(this).apply {
            hint = "اكتب رسالة..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
        }
        root.addView(input)

        addButton(root, "إرسال ➤") {
            val message = input.text.toString().trim()
            if (message.isNotEmpty()) {
                val key = "messages_$currentChat"
                val old = prefs.getString(key, "") ?: ""
                val updated = if (old.isEmpty()) message
                    else "$old\n$message"
                prefs.edit().putString(key, updated).apply()
                addMessage(message)
                input.setText("")
                scroll.post {
                    scroll.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        addButton(root, "⬅ المحادثات") { showHome() }

        val saved = prefs.getString(
            "messages_$currentChat", ""
        ) ?: ""

        if (saved.isNotEmpty()) {
            saved.split("\n").forEach { addMessage(it) }
        }
    }

    private fun addMessage(message: String) {
        val bubble = TextView(this).apply {
            text = message
            textSize = 17f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(35, 75, 58))
            setPadding(14, 12, 14, 12)
        }

        chatArea.addView(
            bubble,
            LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 5, 0, 5)
            }
        )
    }

    private fun showSettings() {
        setup("الإعدادات")

        addButton(root, "مسح جميع الرسائل") {
            prefs.edit().clear().apply()
            Toast.makeText(
                this, "تم مسح الرسائل",
                Toast.LENGTH_SHORT
            ).show()
        }

        addButton(root, "مشاركة التطبيق") {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "جرّب تطبيق الكاسر آب KASER APP"
                )
            }
            startActivity(Intent.createChooser(intent, "مشاركة"))
        }

        addButton(root, "رجوع") { showHome() }
    }
}
