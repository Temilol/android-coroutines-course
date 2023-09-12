package com.techyourchance.coroutines.exercises.exercise3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.techyourchance.coroutines.R
import com.techyourchance.coroutines.common.BaseFragment
import com.techyourchance.coroutines.common.ThreadInfoLogger
import com.techyourchance.coroutines.exercises.exercise1.GetReputationEndpoint
import com.techyourchance.coroutines.home.ScreenReachableFromHome
import kotlinx.coroutines.*

class Exercise3Fragment : BaseFragment() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    override val screenTitle get() = ScreenReachableFromHome.EXERCISE_3.description

    private lateinit var edtUserId: EditText
    private lateinit var btnGetReputation: Button
    private lateinit var txtElapsedTime: TextView


    private lateinit var getReputationEndpoint: GetReputationEndpoint

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getReputationEndpoint = compositionRoot.getReputationEndpoint
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_exercise_3, container, false)

        txtElapsedTime = view.findViewById(R.id.txt_elapsed_time)

        edtUserId = view.findViewById(R.id.edt_user_id)
        edtUserId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnGetReputation.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnGetReputation = view.findViewById(R.id.btn_get_reputation)
        btnGetReputation.setOnClickListener {
            logThreadInfo("button callback")

            var benchmarkDurationSeconds = 0

            job = coroutineScope.launch {
                while (true) {
                    logThreadInfo("updateRemainingTime: $benchmarkDurationSeconds seconds")
                    benchmarkDurationSeconds += 1
                    delay(1000)
                }
            }
            coroutineScope.launch {
                btnGetReputation.isEnabled = false
                val reputation = getReputationForUser(edtUserId.text.toString())
                logThreadInfo("benchmark Duration Seconds: $benchmarkDurationSeconds")
                job?.cancel()
                Toast.makeText(requireContext(), "reputation: $reputation", Toast.LENGTH_SHORT).show()
                btnGetReputation.isEnabled = true
                txtElapsedTime.text = "benchmark Duration Seconds: $benchmarkDurationSeconds"
            }
        }

        return view
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.coroutineContext.cancelChildren()
        job?.apply {
            btnGetReputation.isEnabled = true
        }
    }

    private suspend fun getReputationForUser(userId: String): Int {
        return withContext(Dispatchers.Default) {
            logThreadInfo("getReputationForUser()")
            getReputationEndpoint.getReputation(userId)
        }
    }

    private fun logThreadInfo(message: String) {
        ThreadInfoLogger.logThreadInfo(message)
    }

    companion object {
        fun newInstance(): Fragment {
            return Exercise3Fragment()
        }
    }
}