package com.example.rootmap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.rootmap.databinding.FragmentMenu2Binding

private const val ARG_PARAM1 = "param1_board"
private const val ARG_PARAM2 = "param2_board"

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

        // DrawerLayout 열기
        binding.filterButton.setOnClickListener {
            if (!binding.drawerLayout.isDrawerOpen(binding.scrollView)) {
                binding.drawerLayout.openDrawer(binding.scrollView)
            }
        }

        // DrawerLayout 닫기 및 확인 버튼 클릭 이벤트
        binding.confirmButton.setOnClickListener {
            applyFilters()
        }

        // 초기화 버튼 클릭 이벤트
        binding.resetButton.setOnClickListener {
            resetFilters()
            applyFilters()
        }

        // DrawerListener 설정
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Do nothing
            }

            override fun onDrawerOpened(drawerView: View) {
                // Do nothing
            }

            override fun onDrawerClosed(drawerView: View) {
                // Do nothing
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Handle drawer state changes if necessary
            }
        })

        // 여행지, 여행일, 테마 체크박스 동적 생성
        addCheckBoxes(
            R.array.locations_array,
            binding.sideMenu.findViewById(R.id.locations_container)
        )
        addCheckBoxes(
            R.array.durations_array,
            binding.sideMenu.findViewById(R.id.durations_container)
        )
        addCheckBoxes(
            R.array.themes_array,
            binding.sideMenu.findViewById(R.id.themes_container)
        )

        return binding.root
    }

    private fun applyFilters() {
        selectedLocations.clear()
        selectedDurations.clear()
        selectedThemes.clear()

        // 여행지 선택 확인
        checkAndAddAll(binding.sideMenu.findViewById(R.id.locations_container), selectedLocations)
        checkAndAddAll(binding.sideMenu.findViewById(R.id.durations_container), selectedDurations)
        checkAndAddAll(binding.sideMenu.findViewById(R.id.themes_container), selectedThemes)

        binding.drawerLayout.closeDrawer(binding.scrollView)
        updateSelectedOptions()
    }

    private fun resetFilters() {
        // 모든 체크박스 초기화
        clearAllCheckBoxes(binding.sideMenu.findViewById(R.id.locations_container))
        clearAllCheckBoxes(binding.sideMenu.findViewById(R.id.durations_container))
        clearAllCheckBoxes(binding.sideMenu.findViewById(R.id.themes_container))
    }

    private fun clearAllCheckBoxes(container: ViewGroup) {
        for (i in 0 until container.childCount) {
            val checkBox = container.getChildAt(i) as CheckBox
            checkBox.isChecked = false
        }
    }

    private fun checkAndAddAll(container: ViewGroup, list: MutableList<String>) {
        for (i in 0 until container.childCount) {
            val checkBox = container.getChildAt(i) as CheckBox
            checkAndAdd(checkBox, list)
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

    private fun addCheckBoxes(arrayResId: Int, container: ViewGroup) {
        val items = resources.getStringArray(arrayResId)
        for (item in items) {
            val checkBox = CheckBox(context).apply {
                text = item
            }
            container.addView(checkBox)
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

