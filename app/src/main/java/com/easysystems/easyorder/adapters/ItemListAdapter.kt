package com.easysystems.easyorder.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.easysystems.easyorder.R
import com.easysystems.easyorder.data.ItemObservable
import com.easysystems.easyorder.databinding.CardDesignBinding
import com.easysystems.easyorder.fragments.EditItemFragment
import com.easysystems.easyorder.fragments.FocusItemFragment
import com.easysystems.easyorder.fragments.ItemListFragment
import com.easysystems.easyorder.viewModels.ItemListViewModel

class ItemListAdapter(private val viewModel: ItemListViewModel) :
    RecyclerView.Adapter<ItemListAdapter.ItemHolder>() {

    var itemList = ArrayList<ItemObservable>()

    inner class ItemHolder(private val binding: CardDesignBinding, parent: ViewGroup) :
        RecyclerView.ViewHolder(binding.root) {

        private val holderParent = parent

        fun bind(itemObservable: ItemObservable?) {

            if (itemObservable != null) {

                binding.viewModel = viewModel
                binding.itemObservable = itemObservable

                if (viewModel.isEditMode) {

                    binding.btnAdd.isVisible = false
                    binding.btnEdit.isVisible = true

                    binding.btnEdit.setOnClickListener {
                        callEditItemFragment(itemObservable, holderParent)
                    }

                    binding.cardView.setOnClickListener {
                        callEditItemFragment(itemObservable, holderParent)
                    }
                } else {

                    binding.btnAdd.isVisible = true
                    binding.btnEdit.isVisible = false

                    binding.btnAdd.setOnClickListener {
                        viewModel.addItem(itemObservable)
                    }

                    binding.cardView.setOnClickListener {
                        callFocusItemFragment(itemObservable, holderParent)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = CardDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding, parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    fun callEditItemFragment(
        itemToEdit: ItemObservable,
        holderParent: ViewGroup
    ) {

        val fragmentManager: FragmentManager =
            holderParent.findFragment<ItemListFragment>().parentFragmentManager
        val editItemFragment = EditItemFragment()
        val bundle = Bundle()

        bundle.putSerializable("itemToEdit", itemToEdit)
        editItemFragment.arguments = bundle

        fragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.slide_in,
                R.anim.fade_out
            )
            replace(R.id.frame, editItemFragment)
            addToBackStack(null)
        }
    }

    fun callFocusItemFragment(
        itemToFocus: ItemObservable,
        holderParent: ViewGroup
    ) {

        val fragmentManager: FragmentManager =
            holderParent.findFragment<ItemListFragment>().parentFragmentManager
        val focusItemFragment = FocusItemFragment(holderParent)
        val bundle = Bundle()

        bundle.putSerializable("itemToFocus", itemToFocus)
        focusItemFragment.arguments = bundle

        fragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.slide_in,
                R.anim.fade_out
            )
            replace(R.id.frame, focusItemFragment)
            addToBackStack(null)
        }
    }
}