package com.example.firstaidfront.ui.profile

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.firstaidfront.R
import com.example.firstaidfront.databinding.FragmentProfileBinding
import com.example.firstaidfront.models.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupEditButtons()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                user?.let { updateUI(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEditing.collect { editingField ->
                updateEditingState(editingField)
            }
        }
    }

    private fun setupEditButtons() {
        with(binding) {
            editFirstName.setOnClickListener { startEditing("firstName", firstNameValue.text.toString()) }
            editLastName.setOnClickListener { startEditing("lastName", lastNameValue.text.toString()) }
            editUsername.setOnClickListener { startEditing("username", usernameValue.text.toString()) }
            editEmail.setOnClickListener { startEditing("email", emailValue.text.toString()) }
            editPhone.setOnClickListener { startEditing("phone", phoneValue.text.toString()) }
            editAddress.setOnClickListener { startEditing("address", addressValue.text.toString()) }

            saveButton.setOnClickListener {
                val editingField = viewModel.isEditing.value ?: return@setOnClickListener
                val editText = when (editingField) {
                    "firstName" -> firstNameInput
                    "lastName" -> lastNameInput
                    "username" -> usernameInput
                    "email" -> emailInput
                    "phone" -> phoneInput
                    "address" -> addressInput
                    else -> null
                }
                editText?.text?.toString()?.let { value ->
                    viewModel.updateField(editingField, value)
                }
            }

            cancelButton.setOnClickListener {
                viewModel.stopEditing()
            }
        }
    }

    private fun startEditing(field: String, currentValue: String) {
        viewModel.startEditing(field)
        with(binding) {
            when (field) {
                "firstName" -> firstNameInput.setText(currentValue)
                "lastName" -> lastNameInput.setText(currentValue)
                "username" -> usernameInput.setText(currentValue)
                "email" -> emailInput.setText(currentValue)
                "phone" -> phoneInput.setText(currentValue)
                "address" -> addressInput.setText(currentValue)
            }
        }
    }

    private fun updateEditingState(editingField: String?) {
        with(binding) {
            // Hide all edit layouts
            firstNameEditLayout.isVisible = false
            lastNameEditLayout.isVisible = false
            usernameEditLayout.isVisible = false
            emailEditLayout.isVisible = false
            phoneEditLayout.isVisible = false
            addressEditLayout.isVisible = false

            // Show the active edit layout
            when (editingField) {
                "firstName" -> firstNameEditLayout.isVisible = true
                "lastName" -> lastNameEditLayout.isVisible = true
                "username" -> usernameEditLayout.isVisible = true
                "email" -> emailEditLayout.isVisible = true
                "phone" -> phoneEditLayout.isVisible = true
                "address" -> addressEditLayout.isVisible = true
            }

            // Show/hide action buttons
            editActionsLayout.isVisible = editingField != null
        }
    }

    private fun updateUI(user: User) {
        with(binding) {
            firstNameValue.text = user.firstName
            lastNameValue.text = user.lastName
            usernameValue.text = user.username
            emailValue.text = user.email
            phoneValue.text = user.phone
            addressValue.text = user.address

            // Set profile image
            user.profileImage?.let { url ->
                Glide.with(this@ProfileFragment)
                    .load(url)
//                    .placeholder(R.drawable.profile_placeholder)
//                    .error(R.drawable.profile_placeholder)
                    .circleCrop()
                    .into(profileImage)
            }

            // Set joined date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            joinDateText.text = "Joined ${dateFormat.format(Date(user.dateJoined))}"
        }
    }
}
