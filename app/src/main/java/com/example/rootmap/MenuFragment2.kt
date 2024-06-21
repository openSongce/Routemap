package com.example.rootmap

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.rootmap.databinding.FragmentMenu2Binding
import com.example.rootmap.databinding.PopupFilterBinding

private const val ARG_PARAM1 = "param1_board"
private const val ARG_PARAM2 = "param2_board"
private const val PREFS_NAME = "FilterPrefs"

class MenuFragment2 : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentMenu2Binding

    private val selectedLocations = mutableListOf<String>()
    private val selectedDurations = mutableListOf<String>()
    private val selectedThemes = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenu2Binding.inflate(inflater, container, false)

        // 필터 버튼 클릭 이벤트
        binding.filterButton.setOnClickListener {
            showFilterPopup()
        }

        return binding.root
    }

    private fun showFilterPopup() {
        val popupBinding = PopupFilterBinding.inflate(LayoutInflater.from(context))

        // 여행지, 여행일, 테마 체크박스 동적 생성
        addCheckBoxes(R.array.locations_array, popupBinding.locationsContainer, "locations")
        addCheckBoxes(R.array.durations_array, popupBinding.durationsContainer, "durations")
        addCheckBoxes(R.array.themes_array, popupBinding.themesContainer, "themes")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(popupBinding.root)
            .setPositiveButton("확인") { _, _ ->
                applyFilters(popupBinding)
            }
            .setNegativeButton("취소", null)
            .setNeutralButton("초기화") { _, _ ->
                resetFilters(popupBinding)
                applyFilters(popupBinding)
            }
            .create()
        dialog.show()
    }

    private fun applyFilters(popupBinding: PopupFilterBinding) {
        selectedLocations.clear()
        selectedDurations.clear()
        selectedThemes.clear()

        // 여행지 선택 확인
        checkAndAddAll(popupBinding.locationsContainer, selectedLocations, "locations")
        checkAndAddAll(popupBinding.durationsContainer, selectedDurations, "durations")
        checkAndAddAll(popupBinding.themesContainer, selectedThemes, "themes")

        updateSelectedOptions()
    }

    private fun resetFilters(popupBinding: PopupFilterBinding) {
        // 모든 체크박스 초기화
        clearAllCheckBoxes(popupBinding.locationsContainer, "locations")
        clearAllCheckBoxes(popupBinding.durationsContainer, "durations")
        clearAllCheckBoxes(popupBinding.themesContainer, "themes")
    }

    private fun clearAllCheckBoxes(container: ViewGroup, keyPrefix: String) {
        for (i in 0 until container.childCount) {
            val checkBox = container.getChildAt(i) as CheckBox
            checkBox.isChecked = false
            saveCheckboxState("$keyPrefix$i", false)
        }
    }

    private fun checkAndAddAll(container: ViewGroup, list: MutableList<String>, keyPrefix: String) {
        for (i in 0 until container.childCount) {
            val checkBox = container.getChildAt(i) as CheckBox
            checkAndAdd(checkBox, list)
            saveCheckboxState("$keyPrefix$i", checkBox.isChecked)
        }
    }

    private fun checkAndAdd(checkBox: CheckBox, list: MutableList<String>) {
        if (checkBox.isChecked) {
            list.add(checkBox.text.toString())
        }
    }

    private fun updateSelectedOptions() {
        val selectedOptions = "여행지: ${selectedLocations.joinToString(", ")}\n여행일: ${selectedDurations.joinToString(", ")}\n테마: ${selectedThemes.joinToString(", ")}"
        binding.selectedOptionsTextView.text = selectedOptions
    }

    private fun addCheckBoxes(arrayResId: Int, container: ViewGroup, keyPrefix: String) {
        val items = resources.getStringArray(arrayResId)
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        for (i in items.indices) {
            val checkBox = CheckBox(context).apply {
                text = items[i]
                isChecked = sharedPrefs.getBoolean("$keyPrefix$i", false)
            }
            container.addView(checkBox)
        }
    }

    private fun saveCheckboxState(key: String, state: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putBoolean(key, state)
            apply()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
