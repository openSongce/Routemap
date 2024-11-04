package com.example.rootmap

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.rootmap.databinding.DialogLayoutBinding

class ConfirmDialog(
    confirmDialogInterface: ConfirmDialogInterface,
    text: String, id: String
) : DialogFragment() {

    // 뷰 바인딩 정의
    private var _binding: DialogLayoutBinding? = null
    private val binding get() = _binding!!

    private var confirmDialogInterface: ConfirmDialogInterface?

    private var text: String? = null
    private var id: String? = null

    init {
        this.text = text
        this.id = id
        this.confirmDialogInterface = confirmDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLayoutBinding.inflate(inflater, container, false)
        val view = binding.root
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.content.text = text

        // 취소 버튼 클릭
        binding.wButton.setOnClickListener {
            dismiss()
        }

        // 확인 버튼 클릭
        binding.bButton.setOnClickListener {
            this.confirmDialogInterface?.onYesButtonClick(id!!)
            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
interface ConfirmDialogInterface {
    fun onYesButtonClick(id:String)
}