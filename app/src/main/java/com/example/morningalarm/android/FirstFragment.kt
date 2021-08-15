package com.example.morningalarm.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.morningalarm.android.databinding.FragmentFirstBinding
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MorningAlarmManager.firstFragment = this

        setAdapter()
    }

    fun setAdapter(alarmList: JSONObject=MorningAlarmManager.get().getJSONObject("data")) {
        binding.alarmsRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        val keys = mutableListOf<String>()
        for (key in alarmList.keys()) {
            keys.add(key)
        }
        binding.alarmsRecyclerView.adapter = AlarmsAdapter(keys.toList(), alarmList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}