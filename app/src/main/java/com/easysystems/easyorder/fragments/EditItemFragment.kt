package com.easysystems.easyorder.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.easysystems.easyorder.MainActivity
import com.easysystems.easyorder.R
import com.easysystems.easyorder.data.ItemObservable
import com.easysystems.easyorder.databinding.FragmentEditItemBinding
import com.easysystems.easyorder.helpclasses.StringResourcesProvider
import com.easysystems.easyorder.viewModels.ItemListViewModel
import org.koin.java.KoinJavaComponent
import java.text.DecimalFormat
import java.text.NumberFormat

class EditItemFragment : Fragment() {

    private val stringResourcesProvider: StringResourcesProvider by KoinJavaComponent.inject(
        StringResourcesProvider::class.java
    )

    private lateinit var binding: FragmentEditItemBinding
    private lateinit var viewModel: ItemListViewModel
    private lateinit var itemObservable: ItemObservable

    private val valuta: String = stringResourcesProvider.getString(R.string.default_valuta)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ItemListViewModel::class.java]
        itemObservable = arguments?.getSerializable("itemToEdit") as ItemObservable

        binding.viewModel = viewModel
        binding.itemObservable = itemObservable

        binding.btnSave.setOnClickListener {
            saveItem(itemObservable)
        }

        binding.editDescription.movementMethod= ScrollingMovementMethod()
    }

    private fun saveItem(itemObservable: ItemObservable) {

        val newItemObservable = itemObservable.copy().apply {
            this.name = binding.editName.text.toString()
            this.image = binding.itemImage.drawable
            this.category = binding.editCategory.text.toString()
            this.price = binding.editPrice.text.toString()
            this.description = binding.editDescription.text.toString()
        }

        viewModel.itemObservable.value = newItemObservable
        requireActivity().supportFragmentManager.popBackStack()
    }
}